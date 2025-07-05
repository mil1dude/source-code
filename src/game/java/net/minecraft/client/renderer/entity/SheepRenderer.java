package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SheepWoolLayer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepRenderer extends AgeableMobRenderer<Sheep, SheepRenderState, SheepModel> {
    private static final ResourceLocation SHEEP_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sheep/sheep.png");

    public SheepRenderer(EntityRendererProvider.Context p_174366_) {
        super(p_174366_, new SheepModel(p_174366_.bakeLayer(ModelLayers.SHEEP)), new SheepModel(p_174366_.bakeLayer(ModelLayers.SHEEP_BABY)), 0.7F);
        this.addLayer(new SheepWoolLayer(this, p_174366_.getModelSet()));
    }

    public ResourceLocation getTextureLocation(SheepRenderState p_364199_) {
        return SHEEP_LOCATION;
    }

    public SheepRenderState createRenderState() {
        return new SheepRenderState();
    }

    public void extractRenderState(Sheep p_367299_, SheepRenderState p_365680_, float p_363826_) {
        super.extractRenderState(p_367299_, p_365680_, p_363826_);
        p_365680_.headEatAngleScale = p_367299_.getHeadEatAngleScale(p_363826_);
        p_365680_.headEatPositionScale = p_367299_.getHeadEatPositionScale(p_363826_);
        p_365680_.isSheared = p_367299_.isSheared();
        p_365680_.woolColor = p_367299_.getColor();
        p_365680_.id = p_367299_.getId();
    }
}