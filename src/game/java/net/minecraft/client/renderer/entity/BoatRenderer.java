package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoatRenderer extends AbstractBoatRenderer {
    private final Model waterPatchModel;
    private final ResourceLocation texture;
    private final EntityModel<BoatRenderState> model;

    public BoatRenderer(EntityRendererProvider.Context p_234563_, ModelLayerLocation p_369070_) {
        super(p_234563_);
        this.texture = p_369070_.model().withPath(p_357968_ -> "textures/entity/" + p_357968_ + ".png");
        this.waterPatchModel = new Model.Simple(p_234563_.bakeLayer(ModelLayers.BOAT_WATER_PATCH), p_357969_ -> RenderType.waterMask());
        this.model = new BoatModel(p_234563_.bakeLayer(p_369070_));
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }

    @Override
    protected RenderType renderType() {
        return this.model.renderType(this.texture);
    }

    @Override
    protected void renderTypeAdditions(BoatRenderState p_361893_, PoseStack p_362428_, MultiBufferSource p_360939_, int p_366995_) {
        if (!p_361893_.isUnderWater) {
            this.waterPatchModel.renderToBuffer(p_362428_, p_360939_.getBuffer(this.waterPatchModel.renderType(this.texture)), p_366995_, OverlayTexture.NO_OVERLAY);
        }
    }
}