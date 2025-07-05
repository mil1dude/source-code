package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownTridentRenderer extends EntityRenderer<ThrownTrident, ThrownTridentRenderState> {
    public static final ResourceLocation TRIDENT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/trident.png");
    private final TridentModel model;

    public ThrownTridentRenderer(EntityRendererProvider.Context p_174420_) {
        super(p_174420_);
        this.model = new TridentModel(p_174420_.bakeLayer(ModelLayers.TRIDENT));
    }

    public void render(ThrownTridentRenderState p_365591_, PoseStack p_116114_, MultiBufferSource p_116115_, int p_116116_) {
        p_116114_.pushPose();
        p_116114_.mulPose(Axis.YP.rotationDegrees(p_365591_.yRot - 90.0F));
        p_116114_.mulPose(Axis.ZP.rotationDegrees(p_365591_.xRot + 90.0F));
        VertexConsumer vertexconsumer = ItemRenderer.getFoilBuffer(p_116115_, this.model.renderType(TRIDENT_LOCATION), false, p_365591_.isFoil);
        this.model.renderToBuffer(p_116114_, vertexconsumer, p_116116_, OverlayTexture.NO_OVERLAY);
        p_116114_.popPose();
        super.render(p_365591_, p_116114_, p_116115_, p_116116_);
    }

    public ThrownTridentRenderState createRenderState() {
        return new ThrownTridentRenderState();
    }

    public void extractRenderState(ThrownTrident p_370113_, ThrownTridentRenderState p_370121_, float p_366503_) {
        super.extractRenderState(p_370113_, p_370121_, p_366503_);
        p_370121_.yRot = p_370113_.getYRot(p_366503_);
        p_370121_.xRot = p_370113_.getXRot(p_366503_);
        p_370121_.isFoil = p_370113_.isFoil();
    }
}