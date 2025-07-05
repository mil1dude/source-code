package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxHeldItemLayer extends RenderLayer<FoxRenderState, FoxModel> {
    public FoxHeldItemLayer(RenderLayerParent<FoxRenderState, FoxModel> p_234838_) {
        super(p_234838_);
    }

    public void render(PoseStack p_116996_, MultiBufferSource p_116997_, int p_116998_, FoxRenderState p_360829_, float p_117000_, float p_117001_) {
        ItemStackRenderState itemstackrenderstate = p_360829_.heldItem;
        if (!itemstackrenderstate.isEmpty()) {
            boolean flag = p_360829_.isSleeping;
            boolean flag1 = p_360829_.isBaby;
            p_116996_.pushPose();
            p_116996_.translate(
                this.getParentModel().head.x / 16.0F, this.getParentModel().head.y / 16.0F, this.getParentModel().head.z / 16.0F
            );
            if (flag1) {
                float f = 0.75F;
                p_116996_.scale(0.75F, 0.75F, 0.75F);
            }

            p_116996_.mulPose(Axis.ZP.rotation(p_360829_.headRollAngle));
            p_116996_.mulPose(Axis.YP.rotationDegrees(p_117000_));
            p_116996_.mulPose(Axis.XP.rotationDegrees(p_117001_));
            if (p_360829_.isBaby) {
                if (flag) {
                    p_116996_.translate(0.4F, 0.26F, 0.15F);
                } else {
                    p_116996_.translate(0.06F, 0.26F, -0.5F);
                }
            } else if (flag) {
                p_116996_.translate(0.46F, 0.26F, 0.22F);
            } else {
                p_116996_.translate(0.06F, 0.27F, -0.5F);
            }

            p_116996_.mulPose(Axis.XP.rotationDegrees(90.0F));
            if (flag) {
                p_116996_.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }

            itemstackrenderstate.render(p_116996_, p_116997_, p_116998_, OverlayTexture.NO_OVERLAY);
            p_116996_.popPose();
        }
    }
}