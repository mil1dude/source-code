package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreezeEyesLayer extends RenderLayer<BreezeRenderState, BreezeModel> {
    private static final RenderType BREEZE_EYES = RenderType.breezeEyes(ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze_eyes.png"));

    public BreezeEyesLayer(RenderLayerParent<BreezeRenderState, BreezeModel> p_310165_) {
        super(p_310165_);
    }

    public void render(PoseStack p_312911_, MultiBufferSource p_312666_, int p_311532_, BreezeRenderState p_368188_, float p_311193_, float p_309423_) {
        VertexConsumer vertexconsumer = p_312666_.getBuffer(BREEZE_EYES);
        BreezeModel breezemodel = this.getParentModel();
        BreezeRenderer.enable(breezemodel, breezemodel.head(), breezemodel.eyes())
            .renderToBuffer(p_312911_, vertexconsumer, p_311532_, OverlayTexture.NO_OVERLAY);
    }
}