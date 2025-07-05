package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Objects;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.OldMinecartBehavior;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractMinecartRenderer<T extends AbstractMinecart, S extends MinecartRenderState> extends EntityRenderer<T, S> {
    private static final ResourceLocation MINECART_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/minecart.png");
    protected final MinecartModel model;
    private final BlockRenderDispatcher blockRenderer;

    public AbstractMinecartRenderer(EntityRendererProvider.Context p_369922_, ModelLayerLocation p_364230_) {
        super(p_369922_);
        this.shadowRadius = 0.7F;
        this.model = new MinecartModel(p_369922_.bakeLayer(p_364230_));
        this.blockRenderer = p_369922_.getBlockRenderDispatcher();
    }

    public void render(S p_361135_, PoseStack p_366647_, MultiBufferSource p_368030_, int p_370214_) {
        super.render(p_361135_, p_366647_, p_368030_, p_370214_);
        p_366647_.pushPose();
        long i = p_361135_.offsetSeed;
        float f = (((float)(i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float f1 = (((float)(i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float f2 = (((float)(i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        p_366647_.translate(f, f1, f2);
        if (p_361135_.isNewRender) {
            newRender(p_361135_, p_366647_);
        } else {
            oldRender(p_361135_, p_366647_);
        }

        float f3 = p_361135_.hurtTime;
        if (f3 > 0.0F) {
            p_366647_.mulPose(Axis.XP.rotationDegrees(Mth.sin(f3) * f3 * p_361135_.damageTime / 10.0F * (float)p_361135_.hurtDir));
        }

        BlockState blockstate = p_361135_.displayBlockState;
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            p_366647_.pushPose();
            float f4 = 0.75F;
            p_366647_.scale(0.75F, 0.75F, 0.75F);
            p_366647_.translate(-0.5F, (float)(p_361135_.displayOffset - 8) / 16.0F, 0.5F);
            p_366647_.mulPose(Axis.YP.rotationDegrees(90.0F));
            this.renderMinecartContents(p_361135_, blockstate, p_366647_, p_368030_, p_370214_);
            p_366647_.popPose();
        }

        p_366647_.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(p_361135_);
        VertexConsumer vertexconsumer = p_368030_.getBuffer(this.model.renderType(MINECART_LOCATION));
        this.model.renderToBuffer(p_366647_, vertexconsumer, p_370214_, OverlayTexture.NO_OVERLAY);
        p_366647_.popPose();
    }

    private static <S extends MinecartRenderState> void newRender(S p_369039_, PoseStack p_366808_) {
        p_366808_.mulPose(Axis.YP.rotationDegrees(p_369039_.yRot));
        p_366808_.mulPose(Axis.ZP.rotationDegrees(-p_369039_.xRot));
        p_366808_.translate(0.0F, 0.375F, 0.0F);
    }

    private static <S extends MinecartRenderState> void oldRender(S p_364306_, PoseStack p_367729_) {
        double d0 = p_364306_.x;
        double d1 = p_364306_.y;
        double d2 = p_364306_.z;
        float f = p_364306_.xRot;
        float f1 = p_364306_.yRot;
        if (p_364306_.posOnRail != null && p_364306_.frontPos != null && p_364306_.backPos != null) {
            Vec3 vec3 = p_364306_.frontPos;
            Vec3 vec31 = p_364306_.backPos;
            p_367729_.translate(p_364306_.posOnRail.x - d0, (vec3.y + vec31.y) / 2.0 - d1, p_364306_.posOnRail.z - d2);
            Vec3 vec32 = vec31.add(-vec3.x, -vec3.y, -vec3.z);
            if (vec32.length() != 0.0) {
                vec32 = vec32.normalize();
                f1 = (float)(Math.atan2(vec32.z, vec32.x) * 180.0 / Math.PI);
                f = (float)(Math.atan(vec32.y) * 73.0);
            }
        }

        p_367729_.translate(0.0F, 0.375F, 0.0F);
        p_367729_.mulPose(Axis.YP.rotationDegrees(180.0F - f1));
        p_367729_.mulPose(Axis.ZP.rotationDegrees(-f));
    }

    public void extractRenderState(T p_369176_, S p_364445_, float p_364174_) {
        super.extractRenderState(p_369176_, p_364445_, p_364174_);
        if (p_369176_.getBehavior() instanceof NewMinecartBehavior newminecartbehavior) {
            newExtractState(p_369176_, newminecartbehavior, p_364445_, p_364174_);
            p_364445_.isNewRender = true;
        } else if (p_369176_.getBehavior() instanceof OldMinecartBehavior oldminecartbehavior) {
            oldExtractState(p_369176_, oldminecartbehavior, p_364445_, p_364174_);
            p_364445_.isNewRender = false;
        }

        long i = (long)p_369176_.getId() * 493286711L;
        p_364445_.offsetSeed = i * i * 4392167121L + i * 98761L;
        p_364445_.hurtTime = (float)p_369176_.getHurtTime() - p_364174_;
        p_364445_.hurtDir = p_369176_.getHurtDir();
        p_364445_.damageTime = Math.max(p_369176_.getDamage() - p_364174_, 0.0F);
        p_364445_.displayOffset = p_369176_.getDisplayOffset();
        p_364445_.displayBlockState = p_369176_.getDisplayBlockState();
    }

    private static <T extends AbstractMinecart, S extends MinecartRenderState> void newExtractState(
        T p_366236_, NewMinecartBehavior p_366892_, S p_367623_, float p_365529_
    ) {
        if (p_366892_.cartHasPosRotLerp()) {
            p_367623_.renderPos = p_366892_.getCartLerpPosition(p_365529_);
            p_367623_.xRot = p_366892_.getCartLerpXRot(p_365529_);
            p_367623_.yRot = p_366892_.getCartLerpYRot(p_365529_);
        } else {
            p_367623_.renderPos = null;
            p_367623_.xRot = p_366236_.getXRot();
            p_367623_.yRot = p_366236_.getYRot();
        }
    }

    private static <T extends AbstractMinecart, S extends MinecartRenderState> void oldExtractState(
        T p_367481_, OldMinecartBehavior p_362885_, S p_368073_, float p_362159_
    ) {
        float f = 0.3F;
        p_368073_.xRot = p_367481_.getXRot(p_362159_);
        p_368073_.yRot = p_367481_.getYRot(p_362159_);
        double d0 = p_368073_.x;
        double d1 = p_368073_.y;
        double d2 = p_368073_.z;
        Vec3 vec3 = p_362885_.getPos(d0, d1, d2);
        if (vec3 != null) {
            p_368073_.posOnRail = vec3;
            Vec3 vec31 = p_362885_.getPosOffs(d0, d1, d2, 0.3F);
            Vec3 vec32 = p_362885_.getPosOffs(d0, d1, d2, -0.3F);
            p_368073_.frontPos = Objects.requireNonNullElse(vec31, vec3);
            p_368073_.backPos = Objects.requireNonNullElse(vec32, vec3);
        } else {
            p_368073_.posOnRail = null;
            p_368073_.frontPos = null;
            p_368073_.backPos = null;
        }
    }

    protected void renderMinecartContents(S p_361290_, BlockState p_362203_, PoseStack p_367440_, MultiBufferSource p_368595_, int p_366033_) {
        this.blockRenderer.renderSingleBlock(p_362203_, p_367440_, p_368595_, p_366033_, OverlayTexture.NO_OVERLAY);
    }

    protected AABB getBoundingBoxForCulling(T p_363708_) {
        AABB aabb = super.getBoundingBoxForCulling(p_363708_);
        return p_363708_.hasCustomDisplay() ? aabb.inflate((double)Math.abs(p_363708_.getDisplayOffset()) / 16.0) : aabb;
    }

    public Vec3 getRenderOffset(S p_367749_) {
        Vec3 vec3 = super.getRenderOffset(p_367749_);
        return p_367749_.isNewRender && p_367749_.renderPos != null
            ? vec3.add(
                p_367749_.renderPos.x - p_367749_.x,
                p_367749_.renderPos.y - p_367749_.y,
                p_367749_.renderPos.z - p_367749_.z
            )
            : vec3;
    }
}