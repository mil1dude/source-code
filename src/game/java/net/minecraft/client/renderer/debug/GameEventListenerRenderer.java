package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GameEventListenerRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int LISTENER_RENDER_DIST = 32;
    private static final float BOX_HEIGHT = 1.0F;
    private final List<GameEventListenerRenderer.TrackedGameEvent> trackedGameEvents = Lists.newArrayList();
    private final List<GameEventListenerRenderer.TrackedListener> trackedListeners = Lists.newArrayList();

    public GameEventListenerRenderer(Minecraft p_173822_) {
        this.minecraft = p_173822_;
    }

    @Override
    public void render(PoseStack p_173846_, MultiBufferSource p_173847_, double p_173848_, double p_173849_, double p_173850_) {
        Level level = this.minecraft.level;
        if (level == null) {
            this.trackedGameEvents.clear();
            this.trackedListeners.clear();
        } else {
            Vec3 vec3 = new Vec3(p_173848_, 0.0, p_173850_);
            this.trackedGameEvents.removeIf(GameEventListenerRenderer.TrackedGameEvent::isExpired);
            this.trackedListeners.removeIf(p_234512_ -> p_234512_.isExpired(level, vec3));
            VertexConsumer vertexconsumer = p_173847_.getBuffer(RenderType.lines());

            for (GameEventListenerRenderer.TrackedListener gameeventlistenerrenderer$trackedlistener : this.trackedListeners) {
                gameeventlistenerrenderer$trackedlistener.getPosition(level)
                    .ifPresent(
                        p_269731_ -> {
                            double d7 = p_269731_.x() - (double)gameeventlistenerrenderer$trackedlistener.getListenerRadius();
                            double d8 = p_269731_.y() - (double)gameeventlistenerrenderer$trackedlistener.getListenerRadius();
                            double d9 = p_269731_.z() - (double)gameeventlistenerrenderer$trackedlistener.getListenerRadius();
                            double d10 = p_269731_.x() + (double)gameeventlistenerrenderer$trackedlistener.getListenerRadius();
                            double d11 = p_269731_.y() + (double)gameeventlistenerrenderer$trackedlistener.getListenerRadius();
                            double d12 = p_269731_.z() + (double)gameeventlistenerrenderer$trackedlistener.getListenerRadius();
                            DebugRenderer.renderVoxelShape(
                                p_173846_,
                                vertexconsumer,
                                Shapes.create(new AABB(d7, d8, d9, d10, d11, d12)),
                                -p_173848_,
                                -p_173849_,
                                -p_173850_,
                                1.0F,
                                1.0F,
                                0.0F,
                                0.35F,
                                true
                            );
                        }
                    );
            }

            VertexConsumer vertexconsumer1 = p_173847_.getBuffer(RenderType.debugFilledBox());

            for (GameEventListenerRenderer.TrackedListener gameeventlistenerrenderer$trackedlistener1 : this.trackedListeners) {
                gameeventlistenerrenderer$trackedlistener1.getPosition(level)
                    .ifPresent(
                        p_269724_ -> ShapeRenderer.addChainedFilledBoxVertices(
                                p_173846_,
                                vertexconsumer1,
                                p_269724_.x() - 0.25 - p_173848_,
                                p_269724_.y() - p_173849_,
                                p_269724_.z() - 0.25 - p_173850_,
                                p_269724_.x() + 0.25 - p_173848_,
                                p_269724_.y() - p_173849_ + 1.0,
                                p_269724_.z() + 0.25 - p_173850_,
                                1.0F,
                                1.0F,
                                0.0F,
                                0.35F
                            )
                    );
            }

            for (GameEventListenerRenderer.TrackedListener gameeventlistenerrenderer$trackedlistener2 : this.trackedListeners) {
                gameeventlistenerrenderer$trackedlistener2.getPosition(level)
                    .ifPresent(
                        p_274713_ -> {
                            DebugRenderer.renderFloatingText(
                                p_173846_, p_173847_, "Listener Origin", p_274713_.x(), p_274713_.y() + 1.8F, p_274713_.z(), -1, 0.025F
                            );
                            DebugRenderer.renderFloatingText(
                                p_173846_,
                                p_173847_,
                                BlockPos.containing(p_274713_).toString(),
                                p_274713_.x(),
                                p_274713_.y() + 1.5,
                                p_274713_.z(),
                                -6959665,
                                0.025F
                            );
                        }
                    );
            }

            for (GameEventListenerRenderer.TrackedGameEvent gameeventlistenerrenderer$trackedgameevent : this.trackedGameEvents) {
                Vec3 vec31 = gameeventlistenerrenderer$trackedgameevent.position;
                double d0 = 0.2F;
                double d1 = vec31.x - 0.2F;
                double d2 = vec31.y - 0.2F;
                double d3 = vec31.z - 0.2F;
                double d4 = vec31.x + 0.2F;
                double d5 = vec31.y + 0.2F + 0.5;
                double d6 = vec31.z + 0.2F;
                renderFilledBox(p_173846_, p_173847_, new AABB(d1, d2, d3, d4, d5, d6), 1.0F, 1.0F, 1.0F, 0.2F);
                DebugRenderer.renderFloatingText(
                    p_173846_,
                    p_173847_,
                    gameeventlistenerrenderer$trackedgameevent.gameEvent.location().toString(),
                    vec31.x,
                    vec31.y + 0.85F,
                    vec31.z,
                    -7564911,
                    0.0075F
                );
            }
        }
    }

    private static void renderFilledBox(
        PoseStack p_270351_, MultiBufferSource p_270763_, AABB p_270205_, float p_270707_, float p_270538_, float p_270314_, float p_270966_
    ) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            Vec3 vec3 = camera.getPosition().reverse();
            DebugRenderer.renderFilledBox(p_270351_, p_270763_, p_270205_.move(vec3), p_270707_, p_270538_, p_270314_, p_270966_);
        }
    }

    public void trackGameEvent(ResourceKey<GameEvent> p_297445_, Vec3 p_234515_) {
        this.trackedGameEvents.add(new GameEventListenerRenderer.TrackedGameEvent(Util.getMillis(), p_297445_, p_234515_));
    }

    public void trackListener(PositionSource p_173831_, int p_173832_) {
        this.trackedListeners.add(new GameEventListenerRenderer.TrackedListener(p_173831_, p_173832_));
    }

    @OnlyIn(Dist.CLIENT)
    static record TrackedGameEvent(long timeStamp, ResourceKey<GameEvent> gameEvent, Vec3 position) {
        public boolean isExpired() {
            return Util.getMillis() - this.timeStamp > 3000L;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class TrackedListener implements GameEventListener {
        public final PositionSource listenerSource;
        public final int listenerRange;

        public TrackedListener(PositionSource p_173872_, int p_173873_) {
            this.listenerSource = p_173872_;
            this.listenerRange = p_173873_;
        }

        public boolean isExpired(Level p_234543_, Vec3 p_234544_) {
            return this.listenerSource.getPosition(p_234543_).filter(p_234547_ -> p_234547_.distanceToSqr(p_234544_) <= 1024.0).isPresent();
        }

        public Optional<Vec3> getPosition(Level p_173876_) {
            return this.listenerSource.getPosition(p_173876_);
        }

        @Override
        public PositionSource getListenerSource() {
            return this.listenerSource;
        }

        @Override
        public int getListenerRadius() {
            return this.listenerRange;
        }

        @Override
        public boolean handleGameEvent(ServerLevel p_234540_, Holder<GameEvent> p_333830_, GameEvent.Context p_250285_, Vec3 p_250758_) {
            return false;
        }
    }
}