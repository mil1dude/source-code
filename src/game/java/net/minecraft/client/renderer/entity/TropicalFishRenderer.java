package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishRenderer extends MobRenderer<TropicalFish, TropicalFishRenderState, EntityModel<TropicalFishRenderState>> {
    private final EntityModel<TropicalFishRenderState> modelA = this.getModel();
    private final EntityModel<TropicalFishRenderState> modelB;
    private static final ResourceLocation MODEL_A_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a.png");
    private static final ResourceLocation MODEL_B_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_b.png");

    public TropicalFishRenderer(EntityRendererProvider.Context p_174428_) {
        super(p_174428_, new TropicalFishModelA(p_174428_.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15F);
        this.modelB = new TropicalFishModelB(p_174428_.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
        this.addLayer(new TropicalFishPatternLayer(this, p_174428_.getModelSet()));
    }

    public ResourceLocation getTextureLocation(TropicalFishRenderState p_363843_) {
        return switch (p_363843_.variant.base()) {
            case SMALL -> MODEL_A_TEXTURE;
            case LARGE -> MODEL_B_TEXTURE;
        };
    }

    public TropicalFishRenderState createRenderState() {
        return new TropicalFishRenderState();
    }

    public void extractRenderState(TropicalFish p_367300_, TropicalFishRenderState p_361016_, float p_366837_) {
        super.extractRenderState(p_367300_, p_361016_, p_366837_);
        p_361016_.variant = p_367300_.getVariant();
        p_361016_.baseColor = p_367300_.getBaseColor().getTextureDiffuseColor();
        p_361016_.patternColor = p_367300_.getPatternColor().getTextureDiffuseColor();
    }

    public void render(TropicalFishRenderState p_365050_, PoseStack p_116193_, MultiBufferSource p_116194_, int p_116195_) {
        this.model = switch (p_365050_.variant.base()) {
            case SMALL -> this.modelA;
            case LARGE -> this.modelB;
        };
        super.render(p_365050_, p_116193_, p_116194_, p_116195_);
    }

    protected int getModelTint(TropicalFishRenderState p_363762_) {
        return p_363762_.baseColor;
    }

    protected void setupRotations(TropicalFishRenderState p_364918_, PoseStack p_116205_, float p_116206_, float p_116207_) {
        super.setupRotations(p_364918_, p_116205_, p_116206_, p_116207_);
        float f = 4.3F * Mth.sin(0.6F * p_364918_.ageInTicks);
        p_116205_.mulPose(Axis.YP.rotationDegrees(f));
        if (!p_364918_.isInWater) {
            p_116205_.translate(0.2F, 0.1F, 0.0F);
            p_116205_.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }
    }
}