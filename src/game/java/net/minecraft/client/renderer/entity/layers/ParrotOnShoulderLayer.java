package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotOnShoulderLayer extends RenderLayer<PlayerRenderState, PlayerModel> {
    private final ParrotModel model;
    private final ParrotRenderState parrotState = new ParrotRenderState();

    public ParrotOnShoulderLayer(RenderLayerParent<PlayerRenderState, PlayerModel> p_174511_, EntityModelSet p_174512_) {
        super(p_174511_);
        this.model = new ParrotModel(p_174512_.bakeLayer(ModelLayers.PARROT));
        this.parrotState.pose = ParrotModel.Pose.ON_SHOULDER;
    }

    public void render(PoseStack p_117318_, MultiBufferSource p_117319_, int p_117320_, PlayerRenderState p_365020_, float p_117322_, float p_117323_) {
        Parrot.Variant parrot$variant = p_365020_.parrotOnLeftShoulder;
        if (parrot$variant != null) {
            this.renderOnShoulder(p_117318_, p_117319_, p_117320_, p_365020_, parrot$variant, p_117322_, p_117323_, true);
        }

        Parrot.Variant parrot$variant1 = p_365020_.parrotOnRightShoulder;
        if (parrot$variant1 != null) {
            this.renderOnShoulder(p_117318_, p_117319_, p_117320_, p_365020_, parrot$variant1, p_117322_, p_117323_, false);
        }
    }

    private void renderOnShoulder(
        PoseStack p_366319_,
        MultiBufferSource p_366949_,
        int p_369936_,
        PlayerRenderState p_361438_,
        Parrot.Variant p_369073_,
        float p_363523_,
        float p_366258_,
        boolean p_367707_
    ) {
        p_366319_.pushPose();
        p_366319_.translate(p_367707_ ? 0.4F : -0.4F, p_361438_.isCrouching ? -1.3F : -1.5F, 0.0F);
        this.parrotState.ageInTicks = p_361438_.ageInTicks;
        this.parrotState.walkAnimationPos = p_361438_.walkAnimationPos;
        this.parrotState.walkAnimationSpeed = p_361438_.walkAnimationSpeed;
        this.parrotState.yRot = p_363523_;
        this.parrotState.xRot = p_366258_;
        this.model.setupAnim(this.parrotState);
        this.model
            .renderToBuffer(p_366319_, p_366949_.getBuffer(this.model.renderType(ParrotRenderer.getVariantTexture(p_369073_))), p_369936_, OverlayTexture.NO_OVERLAY);
        p_366319_.popPose();
    }
}