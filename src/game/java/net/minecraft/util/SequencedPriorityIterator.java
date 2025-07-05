package net.minecraft.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.util.Deque;
import javax.annotation.Nullable;

public final class SequencedPriorityIterator<T> extends AbstractIterator<T> {
    private static final int MIN_PRIO = Integer.MIN_VALUE;
    @Nullable
    private Deque<T> highestPrioQueue = null;
    private int highestPrio = Integer.MIN_VALUE;
    private final Int2ObjectMap<Deque<T>> queuesByPriority = new Int2ObjectOpenHashMap<>();

    public void add(T p_312570_, int p_312199_) {
        if (p_312199_ == this.highestPrio && this.highestPrioQueue != null) {
            this.highestPrioQueue.addLast(p_312570_);
        } else {
            Deque<T> deque = this.queuesByPriority.computeIfAbsent(p_312199_, p_310516_ -> Queues.newArrayDeque());
            deque.addLast(p_312570_);
            if (p_312199_ >= this.highestPrio) {
                this.highestPrioQueue = deque;
                this.highestPrio = p_312199_;
            }
        }
    }

    @Nullable
    @Override
    protected T computeNext() {
        if (this.highestPrioQueue == null) {
            return this.endOfData();
        } else {
            T t = this.highestPrioQueue.removeFirst();
            if (t == null) {
                return this.endOfData();
            } else {
                if (this.highestPrioQueue.isEmpty()) {
                    this.switchCacheToNextHighestPrioQueue();
                }

                return t;
            }
        }
    }

    private void switchCacheToNextHighestPrioQueue() {
        int i = Integer.MIN_VALUE;
        Deque<T> deque = null;

        for (Entry<Deque<T>> entry : Int2ObjectMaps.fastIterable(this.queuesByPriority)) {
            Deque<T> deque1 = entry.getValue();
            int j = entry.getIntKey();
            if (j > i && !deque1.isEmpty()) {
                i = j;
                deque = deque1;
                if (j == this.highestPrio - 1) {
                    break;
                }
            }
        }

        this.highestPrio = i;
        this.highestPrioQueue = deque;
    }
}