package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.TurtleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.TurtleRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TurtleRenderer extends AgeableMobRenderer<Turtle, TurtleRenderState, TurtleModel> {
    private static final ResourceLocation TURTLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/turtle/big_sea_turtle.png");

    public TurtleRenderer(EntityRendererProvider.Context p_174430_) {
        super(p_174430_, new TurtleModel(p_174430_.bakeLayer(ModelLayers.TURTLE)), new TurtleModel(p_174430_.bakeLayer(ModelLayers.TURTLE_BABY)), 0.7F);
    }

    protected float getShadowRadius(TurtleRenderState p_363081_) {
        float f = super.getShadowRadius(p_363081_);
        return p_363081_.isBaby ? f * 0.83F : f;
    }

    public TurtleRenderState createRenderState() {
        return new TurtleRenderState();
    }

    public void extractRenderState(Turtle p_369829_, TurtleRenderState p_365033_, float p_360902_) {
        super.extractRenderState(p_369829_, p_365033_, p_360902_);
        p_365033_.isOnLand = !p_369829_.isInWater() && p_369829_.onGround();
        p_365033_.isLayingEgg = p_369829_.isLayingEgg();
        p_365033_.hasEgg = !p_369829_.isBaby() && p_369829_.hasEgg();
    }

    public ResourceLocation getTextureLocation(TurtleRenderState p_368462_) {
        return TURTLE_LOCATION;
    }
}