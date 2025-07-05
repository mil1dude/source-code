package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GhastRenderer extends MobRenderer<Ghast, GhastRenderState, GhastModel> {
    private static final ResourceLocation GHAST_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/ghast/ghast.png");
    private static final ResourceLocation GHAST_SHOOTING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/ghast/ghast_shooting.png");

    public GhastRenderer(EntityRendererProvider.Context p_174129_) {
        super(p_174129_, new GhastModel(p_174129_.bakeLayer(ModelLayers.GHAST)), 1.5F);
    }

    public ResourceLocation getTextureLocation(GhastRenderState p_364698_) {
        return p_364698_.isCharging ? GHAST_SHOOTING_LOCATION : GHAST_LOCATION;
    }

    public GhastRenderState createRenderState() {
        return new GhastRenderState();
    }

    public void extractRenderState(Ghast p_369528_, GhastRenderState p_367209_, float p_362335_) {
        super.extractRenderState(p_369528_, p_367209_, p_362335_);
        p_367209_.isCharging = p_369528_.isCharging();
    }
}