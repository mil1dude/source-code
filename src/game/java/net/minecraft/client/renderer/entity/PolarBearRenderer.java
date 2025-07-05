package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.PolarBearModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.PolarBearRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PolarBearRenderer extends AgeableMobRenderer<PolarBear, PolarBearRenderState, PolarBearModel> {
    private static final ResourceLocation BEAR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bear/polarbear.png");

    public PolarBearRenderer(EntityRendererProvider.Context p_174356_) {
        super(p_174356_, new PolarBearModel(p_174356_.bakeLayer(ModelLayers.POLAR_BEAR)), new PolarBearModel(p_174356_.bakeLayer(ModelLayers.POLAR_BEAR_BABY)), 0.9F);
    }

    public ResourceLocation getTextureLocation(PolarBearRenderState p_364571_) {
        return BEAR_LOCATION;
    }

    public PolarBearRenderState createRenderState() {
        return new PolarBearRenderState();
    }

    public void extractRenderState(PolarBear p_366655_, PolarBearRenderState p_361043_, float p_363014_) {
        super.extractRenderState(p_366655_, p_361043_, p_363014_);
        p_361043_.standScale = p_366655_.getStandingAnimationScale(p_363014_);
    }
}