package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ArrowModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArrowLayer<M extends PlayerModel> extends StuckInBodyLayer<M> {
    public ArrowLayer(LivingEntityRenderer<?, PlayerRenderState, M> p_174466_, EntityRendererProvider.Context p_174465_) {
        super(p_174466_, new ArrowModel(p_174465_.bakeLayer(ModelLayers.ARROW)), TippableArrowRenderer.NORMAL_ARROW_LOCATION, StuckInBodyLayer.PlacementStyle.IN_CUBE);
    }

    @Override
    protected int numStuck(PlayerRenderState p_369469_) {
        return p_369469_.arrowCount;
    }
}