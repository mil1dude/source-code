package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.CrossbowItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PiglinRenderer extends HumanoidMobRenderer<AbstractPiglin, PiglinRenderState, PiglinModel> {
    private static final ResourceLocation PIGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/piglin/piglin.png");
    private static final ResourceLocation PIGLIN_BRUTE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/piglin/piglin_brute.png");
    public static final CustomHeadLayer.Transforms PIGLIN_CUSTOM_HEAD_TRANSFORMS = new CustomHeadLayer.Transforms(0.0F, 0.0F, 1.0019531F);

    public PiglinRenderer(
        EntityRendererProvider.Context p_174344_,
        ModelLayerLocation p_174345_,
        ModelLayerLocation p_174346_,
        ModelLayerLocation p_174347_,
        ModelLayerLocation p_365005_,
        ModelLayerLocation p_360866_,
        ModelLayerLocation p_360847_
    ) {
        super(p_174344_, new PiglinModel(p_174344_.bakeLayer(p_174345_)), new PiglinModel(p_174344_.bakeLayer(p_174346_)), 0.5F, PIGLIN_CUSTOM_HEAD_TRANSFORMS);
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new HumanoidArmorModel(p_174344_.bakeLayer(p_174347_)),
                new HumanoidArmorModel(p_174344_.bakeLayer(p_365005_)),
                new HumanoidArmorModel(p_174344_.bakeLayer(p_360866_)),
                new HumanoidArmorModel(p_174344_.bakeLayer(p_360847_)),
                p_174344_.getEquipmentRenderer()
            )
        );
    }

    public ResourceLocation getTextureLocation(PiglinRenderState p_363461_) {
        return p_363461_.isBrute ? PIGLIN_BRUTE_LOCATION : PIGLIN_LOCATION;
    }

    public PiglinRenderState createRenderState() {
        return new PiglinRenderState();
    }

    public void extractRenderState(AbstractPiglin p_360925_, PiglinRenderState p_367741_, float p_364947_) {
        super.extractRenderState(p_360925_, p_367741_, p_364947_);
        p_367741_.isBrute = p_360925_.getType() == EntityType.PIGLIN_BRUTE;
        p_367741_.armPose = p_360925_.getArmPose();
        p_367741_.maxCrossbowChageDuration = (float)CrossbowItem.getChargeDuration(p_360925_.getUseItem(), p_360925_);
        p_367741_.isConverting = p_360925_.isConverting();
    }

    protected boolean isShaking(PiglinRenderState p_364796_) {
        return super.isShaking(p_364796_) || p_364796_.isConverting;
    }
}