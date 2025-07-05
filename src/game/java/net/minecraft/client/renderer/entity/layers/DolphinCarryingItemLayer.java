package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.DolphinRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinCarryingItemLayer extends RenderLayer<DolphinRenderState, DolphinModel> {
    public DolphinCarryingItemLayer(RenderLayerParent<DolphinRenderState, DolphinModel> p_234834_) {
        super(p_234834_);
    }

    public void render(PoseStack p_116897_, MultiBufferSource p_116898_, int p_116899_, DolphinRenderState p_366153_, float p_116901_, float p_116902_) {
        ItemStackRenderState itemstackrenderstate = p_366153_.heldItem;
        if (!itemstackrenderstate.isEmpty()) {
            p_116897_.pushPose();
            float f = 1.0F;
            float f1 = -1.0F;
            float f2 = Mth.abs(p_366153_.xRot) / 60.0F;
            if (p_366153_.xRot < 0.0F) {
                p_116897_.translate(0.0F, 1.0F - f2 * 0.5F, -1.0F + f2 * 0.5F);
            } else {
                p_116897_.translate(0.0F, 1.0F + f2 * 0.8F, -1.0F + f2 * 0.2F);
            }

            itemstackrenderstate.render(p_116897_, p_116898_, p_116899_, OverlayTexture.NO_OVERLAY);
            p_116897_.popPose();
        }
    }
}