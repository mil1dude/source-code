package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PigRenderer extends AgeableMobRenderer<Pig, PigRenderState, PigModel> {
    private static final ResourceLocation PIG_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/pig/pig.png");

    public PigRenderer(EntityRendererProvider.Context p_174340_) {
        super(p_174340_, new PigModel(p_174340_.bakeLayer(ModelLayers.PIG)), new PigModel(p_174340_.bakeLayer(ModelLayers.PIG_BABY)), 0.7F);
        this.addLayer(
            new SaddleLayer<>(
                this,
                new PigModel(p_174340_.bakeLayer(ModelLayers.PIG_SADDLE)),
                new PigModel(p_174340_.bakeLayer(ModelLayers.PIG_BABY_SADDLE)),
                ResourceLocation.withDefaultNamespace("textures/entity/pig/pig_saddle.png")
            )
        );
    }

    public ResourceLocation getTextureLocation(PigRenderState p_363892_) {
        return PIG_LOCATION;
    }

    public PigRenderState createRenderState() {
        return new PigRenderState();
    }

    public void extractRenderState(Pig p_364955_, PigRenderState p_370177_, float p_367094_) {
        super.extractRenderState(p_364955_, p_370177_, p_367094_);
        p_370177_.isSaddled = p_364955_.isSaddled();
    }
}