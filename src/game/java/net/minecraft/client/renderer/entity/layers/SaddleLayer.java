package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SaddleableRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SaddleLayer<S extends LivingEntityRenderState & SaddleableRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private final ResourceLocation textureLocation;
    private final M adultModel;
    private final M babyModel;

    public SaddleLayer(RenderLayerParent<S, M> p_363699_, M p_364536_, M p_367998_, ResourceLocation p_369712_) {
        super(p_363699_);
        this.adultModel = p_364536_;
        this.babyModel = p_367998_;
        this.textureLocation = p_369712_;
    }

    public SaddleLayer(RenderLayerParent<S, M> p_117390_, M p_117391_, ResourceLocation p_117392_) {
        this(p_117390_, p_117391_, p_117391_, p_117392_);
    }

    public void render(PoseStack p_117394_, MultiBufferSource p_117395_, int p_117396_, S p_367847_, float p_117398_, float p_117399_) {
        if (p_367847_.isSaddled()) {
            M m = p_367847_.isBaby ? this.babyModel : this.adultModel;
            m.setupAnim(p_367847_);
            VertexConsumer vertexconsumer = p_117395_.getBuffer(RenderType.entityCutoutNoCull(this.textureLocation));
            m.renderToBuffer(p_117394_, vertexconsumer, p_117396_, OverlayTexture.NO_OVERLAY);
        }
    }
}