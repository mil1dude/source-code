package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LlamaSpitRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit, LlamaSpitRenderState> {
    private static final ResourceLocation LLAMA_SPIT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/llama/spit.png");
    private final LlamaSpitModel model;

    public LlamaSpitRenderer(EntityRendererProvider.Context p_174296_) {
        super(p_174296_);
        this.model = new LlamaSpitModel(p_174296_.bakeLayer(ModelLayers.LLAMA_SPIT));
    }

    public void render(LlamaSpitRenderState p_363077_, PoseStack p_115376_, MultiBufferSource p_115377_, int p_115378_) {
        p_115376_.pushPose();
        p_115376_.translate(0.0F, 0.15F, 0.0F);
        p_115376_.mulPose(Axis.YP.rotationDegrees(p_363077_.yRot - 90.0F));
        p_115376_.mulPose(Axis.ZP.rotationDegrees(p_363077_.xRot));
        this.model.setupAnim(p_363077_);
        VertexConsumer vertexconsumer = p_115377_.getBuffer(this.model.renderType(LLAMA_SPIT_LOCATION));
        this.model.renderToBuffer(p_115376_, vertexconsumer, p_115378_, OverlayTexture.NO_OVERLAY);
        p_115376_.popPose();
        super.render(p_363077_, p_115376_, p_115377_, p_115378_);
    }

    public LlamaSpitRenderState createRenderState() {
        return new LlamaSpitRenderState();
    }

    public void extractRenderState(LlamaSpit p_362342_, LlamaSpitRenderState p_368891_, float p_369375_) {
        super.extractRenderState(p_362342_, p_368891_, p_369375_);
        p_368891_.xRot = p_362342_.getXRot(p_369375_);
        p_368891_.yRot = p_362342_.getYRot(p_369375_);
    }
}