package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EnergySwirlLayer<S extends EntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    public EnergySwirlLayer(RenderLayerParent<S, M> p_116967_) {
        super(p_116967_);
    }

    @Override
    public void render(PoseStack p_116970_, MultiBufferSource p_116971_, int p_116972_, S p_367433_, float p_116974_, float p_116975_) {
        if (this.isPowered(p_367433_)) {
            float f = p_367433_.ageInTicks;
            M m = this.model();
            VertexConsumer vertexconsumer = p_116971_.getBuffer(RenderType.energySwirl(this.getTextureLocation(), this.xOffset(f) % 1.0F, f * 0.01F % 1.0F));
            m.setupAnim(p_367433_);
            m.renderToBuffer(p_116970_, vertexconsumer, p_116972_, OverlayTexture.NO_OVERLAY, -8355712);
        }
    }

    protected abstract boolean isPowered(S p_367450_);

    protected abstract float xOffset(float p_116968_);

    protected abstract ResourceLocation getTextureLocation();

    protected abstract M model();
}