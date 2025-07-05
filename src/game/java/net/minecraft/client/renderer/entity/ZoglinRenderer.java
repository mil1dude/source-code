package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZoglinRenderer extends AbstractHoglinRenderer<Zoglin> {
    private static final ResourceLocation ZOGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/hoglin/zoglin.png");

    public ZoglinRenderer(EntityRendererProvider.Context p_174454_) {
        super(p_174454_, ModelLayers.ZOGLIN, ModelLayers.ZOGLIN_BABY, 0.7F);
    }

    public ResourceLocation getTextureLocation(HoglinRenderState p_361329_) {
        return ZOGLIN_LOCATION;
    }
}