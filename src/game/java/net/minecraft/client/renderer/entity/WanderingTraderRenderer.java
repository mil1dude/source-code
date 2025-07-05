package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WanderingTraderRenderer extends MobRenderer<WanderingTrader, VillagerRenderState, VillagerModel> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = ResourceLocation.withDefaultNamespace("textures/entity/wandering_trader.png");

    public WanderingTraderRenderer(EntityRendererProvider.Context p_174441_) {
        super(p_174441_, new VillagerModel(p_174441_.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this, p_174441_.getModelSet()));
        this.addLayer(new CrossedArmsItemLayer<>(this));
    }

    public ResourceLocation getTextureLocation(VillagerRenderState p_361166_) {
        return VILLAGER_BASE_SKIN;
    }

    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    public void extractRenderState(WanderingTrader p_367658_, VillagerRenderState p_365735_, float p_362801_) {
        super.extractRenderState(p_367658_, p_365735_, p_362801_);
        HoldingEntityRenderState.extractHoldingEntityRenderState(p_367658_, p_365735_, this.itemModelResolver);
        p_365735_.isUnhappy = p_367658_.getUnhappyCounter() > 0;
    }
}