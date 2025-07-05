package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChickenRenderer extends AgeableMobRenderer<Chicken, ChickenRenderState, ChickenModel> {
    private static final ResourceLocation CHICKEN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/chicken.png");

    public ChickenRenderer(EntityRendererProvider.Context p_173952_) {
        super(p_173952_, new ChickenModel(p_173952_.bakeLayer(ModelLayers.CHICKEN)), new ChickenModel(p_173952_.bakeLayer(ModelLayers.CHICKEN_BABY)), 0.3F);
    }

    public ResourceLocation getTextureLocation(ChickenRenderState p_368820_) {
        return CHICKEN_LOCATION;
    }

    public ChickenRenderState createRenderState() {
        return new ChickenRenderState();
    }

    public void extractRenderState(Chicken p_368951_, ChickenRenderState p_368780_, float p_370144_) {
        super.extractRenderState(p_368951_, p_368780_, p_370144_);
        p_368780_.flap = Mth.lerp(p_370144_, p_368951_.oFlap, p_368951_.flap);
        p_368780_.flapSpeed = Mth.lerp(p_370144_, p_368951_.oFlapSpeed, p_368951_.flapSpeed);
    }
}