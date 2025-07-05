package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.BeeStingerModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeStingerLayer<M extends PlayerModel> extends StuckInBodyLayer<M> {
    private static final ResourceLocation BEE_STINGER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_stinger.png");

    public BeeStingerLayer(LivingEntityRenderer<?, PlayerRenderState, M> p_116580_, EntityRendererProvider.Context p_367387_) {
        super(p_116580_, new BeeStingerModel(p_367387_.bakeLayer(ModelLayers.BEE_STINGER)), BEE_STINGER_LOCATION, StuckInBodyLayer.PlacementStyle.ON_SURFACE);
    }

    @Override
    protected int numStuck(PlayerRenderState p_362326_) {
        return p_362326_.stingerCount;
    }
}