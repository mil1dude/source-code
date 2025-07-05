package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DolphinCarryingItemLayer;
import net.minecraft.client.renderer.entity.state.DolphinRenderState;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinRenderer extends AgeableMobRenderer<Dolphin, DolphinRenderState, DolphinModel> {
    private static final ResourceLocation DOLPHIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/dolphin.png");

    public DolphinRenderer(EntityRendererProvider.Context p_173960_) {
        super(p_173960_, new DolphinModel(p_173960_.bakeLayer(ModelLayers.DOLPHIN)), new DolphinModel(p_173960_.bakeLayer(ModelLayers.DOLPHIN_BABY)), 0.7F);
        this.addLayer(new DolphinCarryingItemLayer(this));
    }

    public ResourceLocation getTextureLocation(DolphinRenderState p_363936_) {
        return DOLPHIN_LOCATION;
    }

    public DolphinRenderState createRenderState() {
        return new DolphinRenderState();
    }

    public void extractRenderState(Dolphin p_367367_, DolphinRenderState p_370009_, float p_361573_) {
        super.extractRenderState(p_367367_, p_370009_, p_361573_);
        HoldingEntityRenderState.extractHoldingEntityRenderState(p_367367_, p_370009_, this.itemModelResolver);
        p_370009_.isMoving = p_367367_.getDeltaMovement().horizontalDistanceSqr() > 1.0E-7;
    }
}