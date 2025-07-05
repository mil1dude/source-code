package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.BreezeEyesLayer;
import net.minecraft.client.renderer.entity.layers.BreezeWindLayer;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreezeRenderer extends MobRenderer<Breeze, BreezeRenderState, BreezeModel> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/breeze/breeze.png");

    public BreezeRenderer(EntityRendererProvider.Context p_311628_) {
        super(p_311628_, new BreezeModel(p_311628_.bakeLayer(ModelLayers.BREEZE)), 0.5F);
        this.addLayer(new BreezeWindLayer(p_311628_, this));
        this.addLayer(new BreezeEyesLayer(this));
    }

    public void render(BreezeRenderState p_368574_, PoseStack p_336051_, MultiBufferSource p_331735_, int p_333503_) {
        BreezeModel breezemodel = this.getModel();
        enable(breezemodel, breezemodel.head(), breezemodel.rods());
        super.render(p_368574_, p_336051_, p_331735_, p_333503_);
    }

    public ResourceLocation getTextureLocation(BreezeRenderState p_363766_) {
        return TEXTURE_LOCATION;
    }

    public BreezeRenderState createRenderState() {
        return new BreezeRenderState();
    }

    public void extractRenderState(Breeze p_362838_, BreezeRenderState p_366825_, float p_367068_) {
        super.extractRenderState(p_362838_, p_366825_, p_367068_);
        p_366825_.idle.copyFrom(p_362838_.idle);
        p_366825_.shoot.copyFrom(p_362838_.shoot);
        p_366825_.slide.copyFrom(p_362838_.slide);
        p_366825_.slideBack.copyFrom(p_362838_.slideBack);
        p_366825_.inhale.copyFrom(p_362838_.inhale);
        p_366825_.longJump.copyFrom(p_362838_.longJump);
    }

    public static BreezeModel enable(BreezeModel p_328756_, ModelPart... p_332502_) {
        p_328756_.head().visible = false;
        p_328756_.eyes().visible = false;
        p_328756_.rods().visible = false;
        p_328756_.wind().visible = false;

        for (ModelPart modelpart : p_332502_) {
            modelpart.visible = true;
        }

        return p_328756_;
    }
}