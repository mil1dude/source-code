package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DragonFireballRenderer extends EntityRenderer<DragonFireball, EntityRenderState> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_fireball.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);

    public DragonFireballRenderer(EntityRendererProvider.Context p_173962_) {
        super(p_173962_);
    }

    protected int getBlockLightLevel(DragonFireball p_114087_, BlockPos p_114088_) {
        return 15;
    }

    @Override
    public void render(EntityRenderState p_365804_, PoseStack p_114083_, MultiBufferSource p_114084_, int p_114085_) {
        p_114083_.pushPose();
        p_114083_.scale(2.0F, 2.0F, 2.0F);
        p_114083_.mulPose(this.entityRenderDispatcher.cameraOrientation());
        PoseStack.Pose posestack$pose = p_114083_.last();
        VertexConsumer vertexconsumer = p_114084_.getBuffer(RENDER_TYPE);
        vertex(vertexconsumer, posestack$pose, p_114085_, 0.0F, 0, 0, 1);
        vertex(vertexconsumer, posestack$pose, p_114085_, 1.0F, 0, 1, 1);
        vertex(vertexconsumer, posestack$pose, p_114085_, 1.0F, 1, 1, 0);
        vertex(vertexconsumer, posestack$pose, p_114085_, 0.0F, 1, 0, 0);
        p_114083_.popPose();
        super.render(p_365804_, p_114083_, p_114084_, p_114085_);
    }

    private static void vertex(
        VertexConsumer p_254095_, PoseStack.Pose p_336223_, int p_253829_, float p_253995_, int p_254031_, int p_253641_, int p_254243_
    ) {
        p_254095_.addVertex(p_336223_, p_253995_ - 0.5F, (float)p_254031_ - 0.25F, 0.0F)
            .setColor(-1)
            .setUv((float)p_253641_, (float)p_254243_)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(p_253829_)
            .setNormal(p_336223_, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}