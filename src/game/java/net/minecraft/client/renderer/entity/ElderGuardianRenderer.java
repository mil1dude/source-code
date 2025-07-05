package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ElderGuardianRenderer extends GuardianRenderer {
    public static final ResourceLocation GUARDIAN_ELDER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/guardian_elder.png");

    public ElderGuardianRenderer(EntityRendererProvider.Context p_173966_) {
        super(p_173966_, 1.2F, ModelLayers.ELDER_GUARDIAN);
    }

    @Override
    public ResourceLocation getTextureLocation(GuardianRenderState p_367408_) {
        return GUARDIAN_ELDER_LOCATION;
    }
}