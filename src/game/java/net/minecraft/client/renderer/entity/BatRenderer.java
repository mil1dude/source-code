package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BatRenderer extends MobRenderer<Bat, BatRenderState, BatModel> {
    private static final ResourceLocation BAT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bat.png");

    public BatRenderer(EntityRendererProvider.Context p_173929_) {
        super(p_173929_, new BatModel(p_173929_.bakeLayer(ModelLayers.BAT)), 0.25F);
    }

    public ResourceLocation getTextureLocation(BatRenderState p_365861_) {
        return BAT_LOCATION;
    }

    public BatRenderState createRenderState() {
        return new BatRenderState();
    }

    public void extractRenderState(Bat p_362534_, BatRenderState p_364476_, float p_368671_) {
        super.extractRenderState(p_362534_, p_364476_, p_368671_);
        p_364476_.isResting = p_362534_.isResting();
        p_364476_.flyAnimationState.copyFrom(p_362534_.flyAnimationState);
        p_364476_.restAnimationState.copyFrom(p_362534_.restAnimationState);
    }
}