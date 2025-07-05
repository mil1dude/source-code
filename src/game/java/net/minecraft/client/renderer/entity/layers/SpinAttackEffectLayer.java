package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.SpinAttackEffectModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpinAttackEffectLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
    public static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/trident_riptide.png");
    private final SpinAttackEffectModel model;

    public SpinAttackEffectLayer(RenderLayerParent<PlayerRenderState, PlayerModel> p_174540_, EntityModelSet p_174541_) {
        super(p_174540_);
        this.model = new SpinAttackEffectModel(p_174541_.bakeLayer(ModelLayers.PLAYER_SPIN_ATTACK));
    }

    public void render(PoseStack p_117515_, MultiBufferSource p_117516_, int p_117517_, PlayerRenderState p_367130_, float p_117519_, float p_117520_) {
        if (p_367130_.isAutoSpinAttack) {
            VertexConsumer vertexconsumer = p_117516_.getBuffer(this.model.renderType(TEXTURE));
            this.model.setupAnim(p_367130_);
            this.model.renderToBuffer(p_117515_, vertexconsumer, p_117517_, OverlayTexture.NO_OVERLAY);
        }
    }
}