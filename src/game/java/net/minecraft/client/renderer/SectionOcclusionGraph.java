package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SectionOcclusionGraph {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MINIMUM_ADVANCED_CULLING_DISTANCE = 60;
    private static final double CEILED_SECTION_DIAGONAL = Math.ceil(Math.sqrt(3.0) * 16.0);
    private boolean needsFullUpdate = true;
    @Nullable
    private Future<?> fullUpdateTask;
    @Nullable
    private ViewArea viewArea;
    private final AtomicReference<SectionOcclusionGraph.GraphState> currentGraph = new AtomicReference<>();
    private final AtomicReference<SectionOcclusionGraph.GraphEvents> nextGraphEvents = new AtomicReference<>();
    private final AtomicBoolean needsFrustumUpdate = new AtomicBoolean(false);

    public void waitAndReset(@Nullable ViewArea p_298923_) {
        if (this.fullUpdateTask != null) {
            try {
                this.fullUpdateTask.get();
                this.fullUpdateTask = null;
            } catch (Exception exception) {
                LOGGER.warn("Full update failed", (Throwable)exception);
            }
        }

        this.viewArea = p_298923_;
        if (p_298923_ != null) {
            this.currentGraph.set(new SectionOcclusionGraph.GraphState(p_298923_));
            this.invalidate();
        } else {
            this.currentGraph.set(null);
        }
    }

    public void invalidate() {
        this.needsFullUpdate = true;
    }

    public void addSectionsInFrustum(Frustum p_299761_, List<SectionRenderDispatcher.RenderSection> p_301346_, List<SectionRenderDispatcher.RenderSection> p_365911_) {
        this.currentGraph.get().storage().sectionTree.visitNodes((p_357918_, p_357919_, p_357920_, p_357921_) -> {
            SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = p_357918_.getSection();
            if (sectionrenderdispatcher$rendersection != null) {
                p_301346_.add(sectionrenderdispatcher$rendersection);
                if (p_357921_) {
                    p_365911_.add(sectionrenderdispatcher$rendersection);
                }
            }
        }, p_299761_, 32);
    }

    public boolean consumeFrustumUpdate() {
        return this.needsFrustumUpdate.compareAndSet(true, false);
    }

    public void onChunkReadyToRender(ChunkPos p_299612_) {
        SectionOcclusionGraph.GraphEvents sectionocclusiongraph$graphevents = this.nextGraphEvents.get();
        if (sectionocclusiongraph$graphevents != null) {
            this.addNeighbors(sectionocclusiongraph$graphevents, p_299612_);
        }

        SectionOcclusionGraph.GraphEvents sectionocclusiongraph$graphevents1 = this.currentGraph.get().events;
        if (sectionocclusiongraph$graphevents1 != sectionocclusiongraph$graphevents) {
            this.addNeighbors(sectionocclusiongraph$graphevents1, p_299612_);
        }
    }

    public void schedulePropagationFrom(SectionRenderDispatcher.RenderSection p_301377_) {
        SectionOcclusionGraph.GraphEvents sectionocclusiongraph$graphevents = this.nextGraphEvents.get();
        if (sectionocclusiongraph$graphevents != null) {
            sectionocclusiongraph$graphevents.sectionsToPropagateFrom.add(p_301377_);
        }

        SectionOcclusionGraph.GraphEvents sectionocclusiongraph$graphevents1 = this.currentGraph.get().events;
        if (sectionocclusiongraph$graphevents1 != sectionocclusiongraph$graphevents) {
            sectionocclusiongraph$graphevents1.sectionsToPropagateFrom.add(p_301377_);
        }
    }

    public void update(
        boolean p_301275_, Camera p_298972_, Frustum p_298939_, List<SectionRenderDispatcher.RenderSection> p_300432_, LongOpenHashSet p_365816_
    ) {
        Vec3 vec3 = p_298972_.getPosition();
        if (this.needsFullUpdate && (this.fullUpdateTask == null || this.fullUpdateTask.isDone())) {
            this.scheduleFullUpdate(p_301275_, p_298972_, vec3, p_365816_);
        }

        this.runPartialUpdate(p_301275_, p_298939_, p_300432_, vec3, p_365816_);
    }

    private void scheduleFullUpdate(boolean p_298569_, Camera p_299582_, Vec3 p_297830_, LongOpenHashSet p_370191_) {
        this.needsFullUpdate = false;
        LongOpenHashSet longopenhashset = p_370191_.clone();
        this.fullUpdateTask = CompletableFuture.runAsync(() -> {
            SectionOcclusionGraph.GraphState sectionocclusiongraph$graphstate = new SectionOcclusionGraph.GraphState(this.viewArea);
            this.nextGraphEvents.set(sectionocclusiongraph$graphstate.events);
            Queue<SectionOcclusionGraph.Node> queue = Queues.newArrayDeque();
            this.initializeQueueForFullUpdate(p_299582_, queue);
            queue.forEach(p_299757_ -> sectionocclusiongraph$graphstate.storage.sectionToNodeMap.put(p_299757_.section, p_299757_));
            this.runUpdates(sectionocclusiongraph$graphstate.storage, p_297830_, queue, p_298569_, p_299279_ -> {
            }, longopenhashset);
            this.currentGraph.set(sectionocclusiongraph$graphstate);
            this.nextGraphEvents.set(null);
            this.needsFrustumUpdate.set(true);
        }, Util.backgroundExecutor());
    }

    private void runPartialUpdate(
        boolean p_298388_, Frustum p_299940_, List<SectionRenderDispatcher.RenderSection> p_297967_, Vec3 p_299094_, LongOpenHashSet p_363554_
    ) {
        SectionOcclusionGraph.GraphState sectionocclusiongraph$graphstate = this.currentGraph.get();
        this.queueSectionsWithNewNeighbors(sectionocclusiongraph$graphstate);
        if (!sectionocclusiongraph$graphstate.events.sectionsToPropagateFrom.isEmpty()) {
            Queue<SectionOcclusionGraph.Node> queue = Queues.newArrayDeque();

            while (!sectionocclusiongraph$graphstate.events.sectionsToPropagateFrom.isEmpty()) {
                SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = sectionocclusiongraph$graphstate.events.sectionsToPropagateFrom.poll();
                SectionOcclusionGraph.Node sectionocclusiongraph$node = sectionocclusiongraph$graphstate.storage
                    .sectionToNodeMap
                    .get(sectionrenderdispatcher$rendersection);
                if (sectionocclusiongraph$node != null && sectionocclusiongraph$node.section == sectionrenderdispatcher$rendersection) {
                    queue.add(sectionocclusiongraph$node);
                }
            }

            Frustum frustum = LevelRenderer.offsetFrustum(p_299940_);
            Consumer<SectionRenderDispatcher.RenderSection> consumer = p_357923_ -> {
                if (frustum.isVisible(p_357923_.getBoundingBox())) {
                    this.needsFrustumUpdate.set(true);
                }
            };
            this.runUpdates(sectionocclusiongraph$graphstate.storage, p_299094_, queue, p_298388_, consumer, p_363554_);
        }
    }

    private void queueSectionsWithNewNeighbors(SectionOcclusionGraph.GraphState p_298801_) {
        LongIterator longiterator = p_298801_.events.chunksWhichReceivedNeighbors.iterator();

        while (longiterator.hasNext()) {
            long i = longiterator.nextLong();
            List<SectionRenderDispatcher.RenderSection> list = p_298801_.storage.chunksWaitingForNeighbors.get(i);
            if (list != null && list.get(0).hasAllNeighbors()) {
                p_298801_.events.sectionsToPropagateFrom.addAll(list);
                p_298801_.storage.chunksWaitingForNeighbors.remove(i);
            }
        }

        p_298801_.events.chunksWhichReceivedNeighbors.clear();
    }

    private void addNeighbors(SectionOcclusionGraph.GraphEvents p_300825_, ChunkPos p_297758_) {
        p_300825_.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(p_297758_.x - 1, p_297758_.z));
        p_300825_.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(p_297758_.x, p_297758_.z - 1));
        p_300825_.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(p_297758_.x + 1, p_297758_.z));
        p_300825_.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(p_297758_.x, p_297758_.z + 1));
        p_300825_.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(p_297758_.x - 1, p_297758_.z - 1));
        p_300825_.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(p_297758_.x - 1, p_297758_.z + 1));
        p_300825_.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(p_297758_.x + 1, p_297758_.z - 1));
        p_300825_.chunksWhichReceivedNeighbors.add(ChunkPos.asLong(p_297758_.x + 1, p_297758_.z + 1));
    }

    private void initializeQueueForFullUpdate(Camera p_298889_, Queue<SectionOcclusionGraph.Node> p_297605_) {
        BlockPos blockpos = p_298889_.getBlockPosition();
        long i = SectionPos.asLong(blockpos);
        int j = SectionPos.y(i);
        SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = this.viewArea.getRenderSection(i);
        if (sectionrenderdispatcher$rendersection == null) {
            LevelHeightAccessor levelheightaccessor = this.viewArea.getLevelHeightAccessor();
            boolean flag = j < levelheightaccessor.getMinSectionY();
            int k = flag ? levelheightaccessor.getMinSectionY() : levelheightaccessor.getMaxSectionY();
            int l = this.viewArea.getViewDistance();
            List<SectionOcclusionGraph.Node> list = Lists.newArrayList();
            int i1 = SectionPos.x(i);
            int j1 = SectionPos.z(i);

            for (int k1 = -l; k1 <= l; k1++) {
                for (int l1 = -l; l1 <= l; l1++) {
                    SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection1 = this.viewArea
                        .getRenderSection(SectionPos.asLong(k1 + i1, k, l1 + j1));
                    if (sectionrenderdispatcher$rendersection1 != null && this.isInViewDistance(i, sectionrenderdispatcher$rendersection1.getSectionNode())) {
                        Direction direction = flag ? Direction.UP : Direction.DOWN;
                        SectionOcclusionGraph.Node sectionocclusiongraph$node = new SectionOcclusionGraph.Node(
                            sectionrenderdispatcher$rendersection1, direction, 0
                        );
                        sectionocclusiongraph$node.setDirections(sectionocclusiongraph$node.directions, direction);
                        if (k1 > 0) {
                            sectionocclusiongraph$node.setDirections(sectionocclusiongraph$node.directions, Direction.EAST);
                        } else if (k1 < 0) {
                            sectionocclusiongraph$node.setDirections(sectionocclusiongraph$node.directions, Direction.WEST);
                        }

                        if (l1 > 0) {
                            sectionocclusiongraph$node.setDirections(sectionocclusiongraph$node.directions, Direction.SOUTH);
                        } else if (l1 < 0) {
                            sectionocclusiongraph$node.setDirections(sectionocclusiongraph$node.directions, Direction.NORTH);
                        }

                        list.add(sectionocclusiongraph$node);
                    }
                }
            }

            list.sort(Comparator.comparingDouble(p_299847_ -> blockpos.distSqr(p_299847_.section.getOrigin().offset(8, 8, 8))));
            p_297605_.addAll(list);
        } else {
            p_297605_.add(new SectionOcclusionGraph.Node(sectionrenderdispatcher$rendersection, null, 0));
        }
    }

    private void runUpdates(
        SectionOcclusionGraph.GraphStorage p_299200_,
        Vec3 p_300018_,
        Queue<SectionOcclusionGraph.Node> p_300570_,
        boolean p_300892_,
        Consumer<SectionRenderDispatcher.RenderSection> p_298647_,
        LongOpenHashSet p_362895_
    ) {
        int i = 16;
        BlockPos blockpos = new BlockPos(
            Mth.floor(p_300018_.x / 16.0) * 16, Mth.floor(p_300018_.y / 16.0) * 16, Mth.floor(p_300018_.z / 16.0) * 16
        );
        long j = SectionPos.asLong(blockpos);
        BlockPos blockpos1 = blockpos.offset(8, 8, 8);

        while (!p_300570_.isEmpty()) {
            SectionOcclusionGraph.Node sectionocclusiongraph$node = p_300570_.poll();
            SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection = sectionocclusiongraph$node.section;
            if (!p_362895_.contains(sectionocclusiongraph$node.section.getSectionNode())) {
                if (p_299200_.sectionTree.add(sectionocclusiongraph$node.section)) {
                    p_298647_.accept(sectionocclusiongraph$node.section);
                }
            } else {
                sectionocclusiongraph$node.section
                    .compiled
                    .compareAndSet(SectionRenderDispatcher.CompiledSection.UNCOMPILED, SectionRenderDispatcher.CompiledSection.EMPTY);
            }

            boolean flag = Math.abs(sectionrenderdispatcher$rendersection.getOrigin().getX() - blockpos.getX()) > 60
                || Math.abs(sectionrenderdispatcher$rendersection.getOrigin().getY() - blockpos.getY()) > 60
                || Math.abs(sectionrenderdispatcher$rendersection.getOrigin().getZ() - blockpos.getZ()) > 60;

            for (Direction direction : DIRECTIONS) {
                SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection1 = this.getRelativeFrom(
                    j, sectionrenderdispatcher$rendersection, direction
                );
                if (sectionrenderdispatcher$rendersection1 != null && (!p_300892_ || !sectionocclusiongraph$node.hasDirection(direction.getOpposite()))) {
                    if (p_300892_ && sectionocclusiongraph$node.hasSourceDirections()) {
                        SectionRenderDispatcher.CompiledSection sectionrenderdispatcher$compiledsection = sectionrenderdispatcher$rendersection.getCompiled();
                        boolean flag1 = false;

                        for (int k = 0; k < DIRECTIONS.length; k++) {
                            if (sectionocclusiongraph$node.hasSourceDirection(k)
                                && sectionrenderdispatcher$compiledsection.facesCanSeeEachother(DIRECTIONS[k].getOpposite(), direction)) {
                                flag1 = true;
                                break;
                            }
                        }

                        if (!flag1) {
                            continue;
                        }
                    }

                    if (p_300892_ && flag) {
                        BlockPos blockpos2 = sectionrenderdispatcher$rendersection1.getOrigin();
                        BlockPos blockpos3 = blockpos2.offset(
                            (
                                    direction.getAxis() == Direction.Axis.X
                                        ? blockpos1.getX() <= blockpos2.getX()
                                        : blockpos1.getX() >= blockpos2.getX()
                                )
                                ? 0
                                : 16,
                            (
                                    direction.getAxis() == Direction.Axis.Y
                                        ? blockpos1.getY() <= blockpos2.getY()
                                        : blockpos1.getY() >= blockpos2.getY()
                                )
                                ? 0
                                : 16,
                            (
                                    direction.getAxis() == Direction.Axis.Z
                                        ? blockpos1.getZ() <= blockpos2.getZ()
                                        : blockpos1.getZ() >= blockpos2.getZ()
                                )
                                ? 0
                                : 16
                        );
                        Vec3 vec31 = new Vec3((double)blockpos3.getX(), (double)blockpos3.getY(), (double)blockpos3.getZ());
                        Vec3 vec3 = p_300018_.subtract(vec31).normalize().scale(CEILED_SECTION_DIAGONAL);
                        boolean flag2 = true;

                        while (p_300018_.subtract(vec31).lengthSqr() > 3600.0) {
                            vec31 = vec31.add(vec3);
                            LevelHeightAccessor levelheightaccessor = this.viewArea.getLevelHeightAccessor();
                            if (vec31.y > (double)levelheightaccessor.getMaxY() || vec31.y < (double)levelheightaccessor.getMinY()) {
                                break;
                            }

                            SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection2 = this.viewArea
                                .getRenderSectionAt(BlockPos.containing(vec31.x, vec31.y, vec31.z));
                            if (sectionrenderdispatcher$rendersection2 == null || p_299200_.sectionToNodeMap.get(sectionrenderdispatcher$rendersection2) == null
                                )
                             {
                                flag2 = false;
                                break;
                            }
                        }

                        if (!flag2) {
                            continue;
                        }
                    }

                    SectionOcclusionGraph.Node sectionocclusiongraph$node1 = p_299200_.sectionToNodeMap.get(sectionrenderdispatcher$rendersection1);
                    if (sectionocclusiongraph$node1 != null) {
                        sectionocclusiongraph$node1.addSourceDirection(direction);
                    } else {
                        SectionOcclusionGraph.Node sectionocclusiongraph$node2 = new SectionOcclusionGraph.Node(
                            sectionrenderdispatcher$rendersection1, direction, sectionocclusiongraph$node.step + 1
                        );
                        sectionocclusiongraph$node2.setDirections(sectionocclusiongraph$node.directions, direction);
                        if (sectionrenderdispatcher$rendersection1.hasAllNeighbors()) {
                            p_300570_.add(sectionocclusiongraph$node2);
                            p_299200_.sectionToNodeMap.put(sectionrenderdispatcher$rendersection1, sectionocclusiongraph$node2);
                        } else if (this.isInViewDistance(j, sectionrenderdispatcher$rendersection1.getSectionNode())) {
                            p_299200_.sectionToNodeMap.put(sectionrenderdispatcher$rendersection1, sectionocclusiongraph$node2);
                            p_299200_.chunksWaitingForNeighbors
                                .computeIfAbsent(ChunkPos.asLong(sectionrenderdispatcher$rendersection1.getOrigin()), p_298371_ -> new ArrayList<>())
                                .add(sectionrenderdispatcher$rendersection1);
                        }
                    }
                }
            }
        }
    }

    private boolean isInViewDistance(long p_363726_, long p_370059_) {
        return ChunkTrackingView.isInViewDistance(
            SectionPos.x(p_363726_),
            SectionPos.z(p_363726_),
            this.viewArea.getViewDistance(),
            SectionPos.x(p_370059_),
            SectionPos.z(p_370059_)
        );
    }

    @Nullable
    private SectionRenderDispatcher.RenderSection getRelativeFrom(long p_369239_, SectionRenderDispatcher.RenderSection p_299737_, Direction p_301139_) {
        long i = p_299737_.getNeighborSectionNode(p_301139_);
        if (!this.isInViewDistance(p_369239_, i)) {
            return null;
        } else {
            return Mth.abs(SectionPos.y(p_369239_) - SectionPos.y(i)) > this.viewArea.getViewDistance() ? null : this.viewArea.getRenderSection(i);
        }
    }

    @Nullable
    @VisibleForDebug
    public SectionOcclusionGraph.Node getNode(SectionRenderDispatcher.RenderSection p_299335_) {
        return this.currentGraph.get().storage.sectionToNodeMap.get(p_299335_);
    }

    public Octree getOctree() {
        return this.currentGraph.get().storage.sectionTree;
    }

    @OnlyIn(Dist.CLIENT)
    static record GraphEvents(LongSet chunksWhichReceivedNeighbors, BlockingQueue<SectionRenderDispatcher.RenderSection> sectionsToPropagateFrom) {
        GraphEvents() {
            this(new LongOpenHashSet(), new LinkedBlockingQueue<>());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record GraphState(SectionOcclusionGraph.GraphStorage storage, SectionOcclusionGraph.GraphEvents events) {
        GraphState(ViewArea p_367222_) {
            this(new SectionOcclusionGraph.GraphStorage(p_367222_), new SectionOcclusionGraph.GraphEvents());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class GraphStorage {
        public final SectionOcclusionGraph.SectionToNodeMap sectionToNodeMap;
        public final Octree sectionTree;
        public final Long2ObjectMap<List<SectionRenderDispatcher.RenderSection>> chunksWaitingForNeighbors;

        public GraphStorage(ViewArea p_364979_) {
            this.sectionToNodeMap = new SectionOcclusionGraph.SectionToNodeMap(p_364979_.sections.length);
            this.sectionTree = new Octree(p_364979_.getCameraSectionPos(), p_364979_.getViewDistance(), p_364979_.sectionGridSizeY, p_364979_.level.getMinY());
            this.chunksWaitingForNeighbors = new Long2ObjectOpenHashMap<>();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @VisibleForDebug
    public static class Node {
        @VisibleForDebug
        protected final SectionRenderDispatcher.RenderSection section;
        private byte sourceDirections;
        byte directions;
        @VisibleForDebug
        public final int step;

        Node(SectionRenderDispatcher.RenderSection p_299649_, @Nullable Direction p_299325_, int p_298364_) {
            this.section = p_299649_;
            if (p_299325_ != null) {
                this.addSourceDirection(p_299325_);
            }

            this.step = p_298364_;
        }

        void setDirections(byte p_298984_, Direction p_300480_) {
            this.directions = (byte)(this.directions | p_298984_ | 1 << p_300480_.ordinal());
        }

        boolean hasDirection(Direction p_299145_) {
            return (this.directions & 1 << p_299145_.ordinal()) > 0;
        }

        void addSourceDirection(Direction p_299877_) {
            this.sourceDirections = (byte)(this.sourceDirections | this.sourceDirections | 1 << p_299877_.ordinal());
        }

        @VisibleForDebug
        public boolean hasSourceDirection(int p_301075_) {
            return (this.sourceDirections & 1 << p_301075_) > 0;
        }

        boolean hasSourceDirections() {
            return this.sourceDirections != 0;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(this.section.getSectionNode());
        }

        @Override
        public boolean equals(Object p_300561_) {
            return !(p_300561_ instanceof SectionOcclusionGraph.Node sectionocclusiongraph$node)
                ? false
                : this.section.getSectionNode() == sectionocclusiongraph$node.section.getSectionNode();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class SectionToNodeMap {
        private final SectionOcclusionGraph.Node[] nodes;

        SectionToNodeMap(int p_298573_) {
            this.nodes = new SectionOcclusionGraph.Node[p_298573_];
        }

        public void put(SectionRenderDispatcher.RenderSection p_297513_, SectionOcclusionGraph.Node p_298532_) {
            this.nodes[p_297513_.index] = p_298532_;
        }

        @Nullable
        public SectionOcclusionGraph.Node get(SectionRenderDispatcher.RenderSection p_297749_) {
            int i = p_297749_.index;
            return i >= 0 && i < this.nodes.length ? this.nodes[i] : null;
        }
    }
}