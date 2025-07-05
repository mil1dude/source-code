package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CrossedArmsItemLayer<S extends HoldingEntityRenderState, M extends EntityModel<S> & VillagerLikeModel> extends RenderLayer<S, M> {
    public CrossedArmsItemLayer(RenderLayerParent<S, M> p_234818_) {
        super(p_234818_);
    }

    public void render(PoseStack p_116688_, MultiBufferSource p_116689_, int p_116690_, S p_377228_, float p_116692_, float p_116693_) {
        ItemStackRenderState itemstackrenderstate = p_377228_.heldItem;
        if (!itemstackrenderstate.isEmpty()) {
            p_116688_.pushPose();
            this.applyTranslation(p_377228_, p_116688_);
            itemstackrenderstate.render(p_116688_, p_116689_, p_116690_, OverlayTexture.NO_OVERLAY);
            p_116688_.popPose();
        }
    }

    protected void applyTranslation(S p_378379_, PoseStack p_378611_) {
        this.getParentModel().translateToArms(p_378611_);
        p_378611_.mulPose(Axis.XP.rotation(0.75F));
        p_378611_.scale(1.07F, 1.07F, 1.07F);
        p_378611_.translate(0.0F, 0.13F, -0.34F);
        p_378611_.mulPose(Axis.XP.rotation((float) Math.PI));
    }
}