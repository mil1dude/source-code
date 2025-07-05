package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArrowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ArrowRenderer<T extends AbstractArrow, S extends ArrowRenderState> extends EntityRenderer<T, S> {
    private final ArrowModel model;

    public ArrowRenderer(EntityRendererProvider.Context p_173917_) {
        super(p_173917_);
        this.model = new ArrowModel(p_173917_.bakeLayer(ModelLayers.ARROW));
    }

    public void render(S p_364846_, PoseStack p_113822_, MultiBufferSource p_113823_, int p_113824_) {
        p_113822_.pushPose();
        p_113822_.mulPose(Axis.YP.rotationDegrees(p_364846_.yRot - 90.0F));
        p_113822_.mulPose(Axis.ZP.rotationDegrees(p_364846_.xRot));
        VertexConsumer vertexconsumer = p_113823_.getBuffer(RenderType.entityCutout(this.getTextureLocation(p_364846_)));
        this.model.setupAnim(p_364846_);
        this.model.renderToBuffer(p_113822_, vertexconsumer, p_113824_, OverlayTexture.NO_OVERLAY);
        p_113822_.popPose();
        super.render(p_364846_, p_113822_, p_113823_, p_113824_);
    }

    protected abstract ResourceLocation getTextureLocation(S p_364393_);

    public void extractRenderState(T p_363488_, S p_367796_, float p_365866_) {
        super.extractRenderState(p_363488_, p_367796_, p_365866_);
        p_367796_.xRot = p_363488_.getXRot(p_365866_);
        p_367796_.yRot = p_363488_.getYRot(p_365866_);
        p_367796_.shake = (float)p_363488_.shakeTime - p_365866_;
    }
}