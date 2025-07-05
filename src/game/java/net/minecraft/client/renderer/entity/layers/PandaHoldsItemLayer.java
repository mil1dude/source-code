package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaHoldsItemLayer extends RenderLayer<PandaRenderState, PandaModel> {
    public PandaHoldsItemLayer(RenderLayerParent<PandaRenderState, PandaModel> p_234862_) {
        super(p_234862_);
    }

    public void render(PoseStack p_117269_, MultiBufferSource p_117270_, int p_117271_, PandaRenderState p_367900_, float p_117273_, float p_117274_) {
        ItemStackRenderState itemstackrenderstate = p_367900_.heldItem;
        if (!itemstackrenderstate.isEmpty() && p_367900_.isSitting && !p_367900_.isScared) {
            float f = -0.6F;
            float f1 = 1.4F;
            if (p_367900_.isEating) {
                f -= 0.2F * Mth.sin(p_367900_.ageInTicks * 0.6F) + 0.2F;
                f1 -= 0.09F * Mth.sin(p_367900_.ageInTicks * 0.6F);
            }

            p_117269_.pushPose();
            p_117269_.translate(0.1F, f1, f);
            itemstackrenderstate.render(p_117269_, p_117270_, p_117271_, OverlayTexture.NO_OVERLAY);
            p_117269_.popPose();
        }
    }
}