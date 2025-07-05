package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RenderLayer<S extends EntityRenderState, M extends EntityModel<? super S>> {
    private final RenderLayerParent<S, M> renderer;

    public RenderLayer(RenderLayerParent<S, M> p_117346_) {
        this.renderer = p_117346_;
    }

    protected static <S extends LivingEntityRenderState> void coloredCutoutModelCopyLayerRender(
        EntityModel<S> p_117360_, ResourceLocation p_117362_, PoseStack p_117363_, MultiBufferSource p_117364_, int p_117365_, S p_366295_, int p_345259_
    ) {
        if (!p_366295_.isInvisible) {
            p_117360_.setupAnim(p_366295_);
            renderColoredCutoutModel(p_117360_, p_117362_, p_117363_, p_117364_, p_117365_, p_366295_, p_345259_);
        }
    }

    protected static void renderColoredCutoutModel(
        EntityModel<?> p_117377_,
        ResourceLocation p_117378_,
        PoseStack p_117379_,
        MultiBufferSource p_117380_,
        int p_117381_,
        LivingEntityRenderState p_360714_,
        int p_343754_
    ) {
        VertexConsumer vertexconsumer = p_117380_.getBuffer(RenderType.entityCutoutNoCull(p_117378_));
        p_117377_.renderToBuffer(p_117379_, vertexconsumer, p_117381_, LivingEntityRenderer.getOverlayCoords(p_360714_, 0.0F), p_343754_);
    }

    public M getParentModel() {
        return this.renderer.getModel();
    }

    public abstract void render(PoseStack p_117349_, MultiBufferSource p_117350_, int p_117351_, S p_361637_, float p_117353_, float p_117354_);
}