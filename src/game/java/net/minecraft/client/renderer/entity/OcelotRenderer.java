package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OcelotRenderer extends AgeableMobRenderer<Ocelot, FelineRenderState, OcelotModel> {
    private static final ResourceLocation CAT_OCELOT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/cat/ocelot.png");

    public OcelotRenderer(EntityRendererProvider.Context p_174330_) {
        super(p_174330_, new OcelotModel(p_174330_.bakeLayer(ModelLayers.OCELOT)), new OcelotModel(p_174330_.bakeLayer(ModelLayers.OCELOT_BABY)), 0.4F);
    }

    public ResourceLocation getTextureLocation(FelineRenderState p_368921_) {
        return CAT_OCELOT_LOCATION;
    }

    public FelineRenderState createRenderState() {
        return new FelineRenderState();
    }

    public void extractRenderState(Ocelot p_364123_, FelineRenderState p_367316_, float p_369664_) {
        super.extractRenderState(p_364123_, p_367316_, p_369664_);
        p_367316_.isCrouching = p_364123_.isCrouching();
        p_367316_.isSprinting = p_364123_.isSprinting();
    }
}