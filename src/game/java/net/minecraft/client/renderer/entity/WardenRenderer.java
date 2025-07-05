package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WardenRenderer extends MobRenderer<Warden, WardenRenderState, WardenModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden.png");
    private static final ResourceLocation BIOLUMINESCENT_LAYER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_bioluminescent_layer.png");
    private static final ResourceLocation HEART_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_heart.png");
    private static final ResourceLocation PULSATING_SPOTS_TEXTURE_1 = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_pulsating_spots_1.png");
    private static final ResourceLocation PULSATING_SPOTS_TEXTURE_2 = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_pulsating_spots_2.png");

    public WardenRenderer(EntityRendererProvider.Context p_234787_) {
        super(p_234787_, new WardenModel(p_234787_.bakeLayer(ModelLayers.WARDEN)), 0.9F);
        this.addLayer(new LivingEntityEmissiveLayer<>(this, BIOLUMINESCENT_LAYER_TEXTURE, (p_361019_, p_234810_) -> 1.0F, WardenModel::getBioluminescentLayerModelParts, RenderType::entityTranslucentEmissive, false));
        this.addLayer(
            new LivingEntityEmissiveLayer<>(
                this,
                PULSATING_SPOTS_TEXTURE_1,
                (p_365328_, p_234806_) -> Math.max(0.0F, Mth.cos(p_234806_ * 0.045F) * 0.25F),
                WardenModel::getPulsatingSpotsLayerModelParts,
                RenderType::entityTranslucentEmissive,
                false
            )
        );
        this.addLayer(
            new LivingEntityEmissiveLayer<>(
                this,
                PULSATING_SPOTS_TEXTURE_2,
                (p_362642_, p_234802_) -> Math.max(0.0F, Mth.cos(p_234802_ * 0.045F + (float) Math.PI) * 0.25F),
                WardenModel::getPulsatingSpotsLayerModelParts,
                RenderType::entityTranslucentEmissive,
                false
            )
        );
        this.addLayer(
            new LivingEntityEmissiveLayer<>(
                this, TEXTURE, (p_358002_, p_358003_) -> p_358002_.tendrilAnimation, WardenModel::getTendrilsLayerModelParts, RenderType::entityTranslucentEmissive, false
            )
        );
        this.addLayer(
            new LivingEntityEmissiveLayer<>(
                this, HEART_TEXTURE, (p_358004_, p_358005_) -> p_358004_.heartAnimation, WardenModel::getHeartLayerModelParts, RenderType::entityTranslucentEmissive, false
            )
        );
    }

    public ResourceLocation getTextureLocation(WardenRenderState p_361021_) {
        return TEXTURE;
    }

    public WardenRenderState createRenderState() {
        return new WardenRenderState();
    }

    public void extractRenderState(Warden p_363046_, WardenRenderState p_369482_, float p_368535_) {
        super.extractRenderState(p_363046_, p_369482_, p_368535_);
        p_369482_.tendrilAnimation = p_363046_.getTendrilAnimation(p_368535_);
        p_369482_.heartAnimation = p_363046_.getHeartAnimation(p_368535_);
        p_369482_.roarAnimationState.copyFrom(p_363046_.roarAnimationState);
        p_369482_.sniffAnimationState.copyFrom(p_363046_.sniffAnimationState);
        p_369482_.emergeAnimationState.copyFrom(p_363046_.emergeAnimationState);
        p_369482_.diggingAnimationState.copyFrom(p_363046_.diggingAnimationState);
        p_369482_.attackAnimationState.copyFrom(p_363046_.attackAnimationState);
        p_369482_.sonicBoomAnimationState.copyFrom(p_363046_.sonicBoomAnimationState);
    }
}