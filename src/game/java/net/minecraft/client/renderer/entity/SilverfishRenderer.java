package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SilverfishRenderer extends MobRenderer<Silverfish, LivingEntityRenderState, SilverfishModel> {
    private static final ResourceLocation SILVERFISH_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/silverfish.png");

    public SilverfishRenderer(EntityRendererProvider.Context p_174378_) {
        super(p_174378_, new SilverfishModel(p_174378_.bakeLayer(ModelLayers.SILVERFISH)), 0.3F);
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState p_367083_) {
        return SILVERFISH_LOCATION;
    }

    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}