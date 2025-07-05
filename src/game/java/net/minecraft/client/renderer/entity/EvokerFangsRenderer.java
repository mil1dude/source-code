package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.EvokerFangsRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvokerFangsRenderer extends EntityRenderer<EvokerFangs, EvokerFangsRenderState> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/evoker_fangs.png");
    private final EvokerFangsModel model;

    public EvokerFangsRenderer(EntityRendererProvider.Context p_174100_) {
        super(p_174100_);
        this.model = new EvokerFangsModel(p_174100_.bakeLayer(ModelLayers.EVOKER_FANGS));
    }

    public void render(EvokerFangsRenderState p_368442_, PoseStack p_114522_, MultiBufferSource p_114523_, int p_114524_) {
        float f = p_368442_.biteProgress;
        if (f != 0.0F) {
            p_114522_.pushPose();
            p_114522_.mulPose(Axis.YP.rotationDegrees(90.0F - p_368442_.yRot));
            p_114522_.scale(-1.0F, -1.0F, 1.0F);
            p_114522_.translate(0.0F, -1.501F, 0.0F);
            this.model.setupAnim(p_368442_);
            VertexConsumer vertexconsumer = p_114523_.getBuffer(this.model.renderType(TEXTURE_LOCATION));
            this.model.renderToBuffer(p_114522_, vertexconsumer, p_114524_, OverlayTexture.NO_OVERLAY);
            p_114522_.popPose();
            super.render(p_368442_, p_114522_, p_114523_, p_114524_);
        }
    }

    public EvokerFangsRenderState createRenderState() {
        return new EvokerFangsRenderState();
    }

    public void extractRenderState(EvokerFangs p_369816_, EvokerFangsRenderState p_364298_, float p_361549_) {
        super.extractRenderState(p_369816_, p_364298_, p_361549_);
        p_364298_.yRot = p_369816_.getYRot();
        p_364298_.biteProgress = p_369816_.getAnimationProgress(p_361549_);
    }
}