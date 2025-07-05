package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EndermiteModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndermiteRenderer extends MobRenderer<Endermite, LivingEntityRenderState, EndermiteModel> {
    private static final ResourceLocation ENDERMITE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/endermite.png");

    public EndermiteRenderer(EntityRendererProvider.Context p_173994_) {
        super(p_173994_, new EndermiteModel(p_173994_.bakeLayer(ModelLayers.ENDERMITE)), 0.3F);
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState p_364996_) {
        return ENDERMITE_LOCATION;
    }

    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}