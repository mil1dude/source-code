package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.ExperienceOrbRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExperienceOrbRenderer extends EntityRenderer<ExperienceOrb, ExperienceOrbRenderState> {
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

    public ExperienceOrbRenderer(EntityRendererProvider.Context p_174110_) {
        super(p_174110_);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    protected int getBlockLightLevel(ExperienceOrb p_114606_, BlockPos p_114607_) {
        return Mth.clamp(super.getBlockLightLevel(p_114606_, p_114607_) + 7, 0, 15);
    }

    public void render(ExperienceOrbRenderState p_366365_, PoseStack p_114602_, MultiBufferSource p_114603_, int p_114604_) {
        p_114602_.pushPose();
        int i = p_366365_.icon;
        float f = (float)(i % 4 * 16 + 0) / 64.0F;
        float f1 = (float)(i % 4 * 16 + 16) / 64.0F;
        float f2 = (float)(i / 4 * 16 + 0) / 64.0F;
        float f3 = (float)(i / 4 * 16 + 16) / 64.0F;
        float f4 = 1.0F;
        float f5 = 0.5F;
        float f6 = 0.25F;
        float f7 = 255.0F;
        float f8 = p_366365_.ageInTicks / 2.0F;
        int j = (int)((Mth.sin(f8 + 0.0F) + 1.0F) * 0.5F * 255.0F);
        int k = 255;
        int l = (int)((Mth.sin(f8 + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * 255.0F);
        p_114602_.translate(0.0F, 0.1F, 0.0F);
        p_114602_.mulPose(this.entityRenderDispatcher.cameraOrientation());
        float f9 = 0.3F;
        p_114602_.scale(0.3F, 0.3F, 0.3F);
        VertexConsumer vertexconsumer = p_114603_.getBuffer(RENDER_TYPE);
        PoseStack.Pose posestack$pose = p_114602_.last();
        vertex(vertexconsumer, posestack$pose, -0.5F, -0.25F, j, 255, l, f, f3, p_114604_);
        vertex(vertexconsumer, posestack$pose, 0.5F, -0.25F, j, 255, l, f1, f3, p_114604_);
        vertex(vertexconsumer, posestack$pose, 0.5F, 0.75F, j, 255, l, f1, f2, p_114604_);
        vertex(vertexconsumer, posestack$pose, -0.5F, 0.75F, j, 255, l, f, f2, p_114604_);
        p_114602_.popPose();
        super.render(p_366365_, p_114602_, p_114603_, p_114604_);
    }

    private static void vertex(
        VertexConsumer p_254515_,
        PoseStack.Pose p_333175_,
        float p_253952_,
        float p_254066_,
        int p_254283_,
        int p_254566_,
        int p_253882_,
        float p_254434_,
        float p_254223_,
        int p_254372_
    ) {
        p_254515_.addVertex(p_333175_, p_253952_, p_254066_, 0.0F)
            .setColor(p_254283_, p_254566_, p_253882_, 128)
            .setUv(p_254434_, p_254223_)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(p_254372_)
            .setNormal(p_333175_, 0.0F, 1.0F, 0.0F);
    }

    public ExperienceOrbRenderState createRenderState() {
        return new ExperienceOrbRenderState();
    }

    public void extractRenderState(ExperienceOrb p_363328_, ExperienceOrbRenderState p_365917_, float p_368286_) {
        super.extractRenderState(p_363328_, p_365917_, p_368286_);
        p_365917_.icon = p_363328_.getIcon();
    }
}