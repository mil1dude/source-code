package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;

public class LevelChunkTicks<T> implements SerializableTickContainer<T>, TickContainerAccess<T> {
    private final Queue<ScheduledTick<T>> tickQueue = new PriorityQueue<>(ScheduledTick.DRAIN_ORDER);
    @Nullable
    private List<SavedTick<T>> pendingTicks;
    private final Set<ScheduledTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet<>(ScheduledTick.UNIQUE_TICK_HASH);
    @Nullable
    private BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> onTickAdded;

    public LevelChunkTicks() {
    }

    public LevelChunkTicks(List<SavedTick<T>> p_193169_) {
        this.pendingTicks = p_193169_;

        for (SavedTick<T> savedtick : p_193169_) {
            this.ticksPerPosition.add(ScheduledTick.probe(savedtick.type(), savedtick.pos()));
        }
    }

    public void setOnTickAdded(@Nullable BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> p_193182_) {
        this.onTickAdded = p_193182_;
    }

    @Nullable
    public ScheduledTick<T> peek() {
        return this.tickQueue.peek();
    }

    @Nullable
    public ScheduledTick<T> poll() {
        ScheduledTick<T> scheduledtick = this.tickQueue.poll();
        if (scheduledtick != null) {
            this.ticksPerPosition.remove(scheduledtick);
        }

        return scheduledtick;
    }

    @Override
    public void schedule(ScheduledTick<T> p_193177_) {
        if (this.ticksPerPosition.add(p_193177_)) {
            this.scheduleUnchecked(p_193177_);
        }
    }

    private void scheduleUnchecked(ScheduledTick<T> p_193194_) {
        this.tickQueue.add(p_193194_);
        if (this.onTickAdded != null) {
            this.onTickAdded.accept(this, p_193194_);
        }
    }

    @Override
    public boolean hasScheduledTick(BlockPos p_193179_, T p_193180_) {
        return this.ticksPerPosition.contains(ScheduledTick.probe(p_193180_, p_193179_));
    }

    public void removeIf(Predicate<ScheduledTick<T>> p_193184_) {
        Iterator<ScheduledTick<T>> iterator = this.tickQueue.iterator();

        while (iterator.hasNext()) {
            ScheduledTick<T> scheduledtick = iterator.next();
            if (p_193184_.test(scheduledtick)) {
                iterator.remove();
                this.ticksPerPosition.remove(scheduledtick);
            }
        }
    }

    public Stream<ScheduledTick<T>> getAll() {
        return this.tickQueue.stream();
    }

    @Override
    public int count() {
        return this.tickQueue.size() + (this.pendingTicks != null ? this.pendingTicks.size() : 0);
    }

    @Override
    public List<SavedTick<T>> pack(long p_360739_) {
        List<SavedTick<T>> list = new ArrayList<>(this.tickQueue.size());
        if (this.pendingTicks != null) {
            list.addAll(this.pendingTicks);
        }

        for (ScheduledTick<T> scheduledtick : this.tickQueue) {
            list.add(scheduledtick.toSavedTick(p_360739_));
        }

        return list;
    }

    public ListTag save(long p_193174_, Function<T, String> p_193175_) {
        ListTag listtag = new ListTag();

        for (SavedTick<T> savedtick : this.pack(p_193174_)) {
            listtag.add(savedtick.save(p_193175_));
        }

        return listtag;
    }

    public void unpack(long p_193172_) {
        if (this.pendingTicks != null) {
            int i = -this.pendingTicks.size();

            for (SavedTick<T> savedtick : this.pendingTicks) {
                this.scheduleUnchecked(savedtick.unpack(p_193172_, (long)(i++)));
            }
        }

        this.pendingTicks = null;
    }

    public static <T> LevelChunkTicks<T> load(ListTag p_193186_, Function<String, Optional<T>> p_193187_, ChunkPos p_193188_) {
        return new LevelChunkTicks<>(SavedTick.loadTickList(p_193186_, p_193187_, p_193188_));
    }
}