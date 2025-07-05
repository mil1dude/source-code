package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.CamelRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CamelRenderer extends AgeableMobRenderer<Camel, CamelRenderState, CamelModel> {
    private static final ResourceLocation CAMEL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/camel/camel.png");

    public CamelRenderer(EntityRendererProvider.Context p_251790_) {
        super(p_251790_, new CamelModel(p_251790_.bakeLayer(ModelLayers.CAMEL)), new CamelModel(p_251790_.bakeLayer(ModelLayers.CAMEL_BABY)), 0.7F);
    }

    public ResourceLocation getTextureLocation(CamelRenderState p_368992_) {
        return CAMEL_LOCATION;
    }

    public CamelRenderState createRenderState() {
        return new CamelRenderState();
    }

    public void extractRenderState(Camel p_361457_, CamelRenderState p_363176_, float p_363399_) {
        super.extractRenderState(p_361457_, p_363176_, p_363399_);
        p_363176_.isSaddled = p_361457_.isSaddled();
        p_363176_.isRidden = p_361457_.isVehicle();
        p_363176_.jumpCooldown = Math.max((float)p_361457_.getJumpCooldown() - p_363399_, 0.0F);
        p_363176_.sitAnimationState.copyFrom(p_361457_.sitAnimationState);
        p_363176_.sitPoseAnimationState.copyFrom(p_361457_.sitPoseAnimationState);
        p_363176_.sitUpAnimationState.copyFrom(p_361457_.sitUpAnimationState);
        p_363176_.idleAnimationState.copyFrom(p_361457_.idleAnimationState);
        p_363176_.dashAnimationState.copyFrom(p_361457_.dashAnimationState);
    }
}