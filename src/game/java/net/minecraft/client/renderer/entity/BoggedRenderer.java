package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.BoggedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BoggedRenderer extends AbstractSkeletonRenderer<Bogged, BoggedRenderState> {
    private static final ResourceLocation BOGGED_SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/bogged.png");
    private static final ResourceLocation BOGGED_OUTER_LAYER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/bogged_overlay.png");

    public BoggedRenderer(EntityRendererProvider.Context p_329255_) {
        super(p_329255_, ModelLayers.BOGGED_INNER_ARMOR, ModelLayers.BOGGED_OUTER_ARMOR, new BoggedModel(p_329255_.bakeLayer(ModelLayers.BOGGED)));
        this.addLayer(new SkeletonClothingLayer<>(this, p_329255_.getModelSet(), ModelLayers.BOGGED_OUTER_LAYER, BOGGED_OUTER_LAYER_LOCATION));
    }

    public ResourceLocation getTextureLocation(BoggedRenderState p_362684_) {
        return BOGGED_SKELETON_LOCATION;
    }

    public BoggedRenderState createRenderState() {
        return new BoggedRenderState();
    }

    public void extractRenderState(Bogged p_360836_, BoggedRenderState p_363990_, float p_364913_) {
        super.extractRenderState(p_360836_, p_363990_, p_364913_);
        p_363990_.isSheared = p_360836_.isSheared();
    }
}