package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotRenderer extends MobRenderer<Parrot, ParrotRenderState, ParrotModel> {
    private static final ResourceLocation RED_BLUE = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_red_blue.png");
    private static final ResourceLocation BLUE = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_blue.png");
    private static final ResourceLocation GREEN = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_green.png");
    private static final ResourceLocation YELLOW_BLUE = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_yellow_blue.png");
    private static final ResourceLocation GREY = ResourceLocation.withDefaultNamespace("textures/entity/parrot/parrot_grey.png");

    public ParrotRenderer(EntityRendererProvider.Context p_174336_) {
        super(p_174336_, new ParrotModel(p_174336_.bakeLayer(ModelLayers.PARROT)), 0.3F);
    }

    public ResourceLocation getTextureLocation(ParrotRenderState p_366563_) {
        return getVariantTexture(p_366563_.variant);
    }

    public ParrotRenderState createRenderState() {
        return new ParrotRenderState();
    }

    public void extractRenderState(Parrot p_363999_, ParrotRenderState p_366002_, float p_362989_) {
        super.extractRenderState(p_363999_, p_366002_, p_362989_);
        p_366002_.variant = p_363999_.getVariant();
        float f = Mth.lerp(p_362989_, p_363999_.oFlap, p_363999_.flap);
        float f1 = Mth.lerp(p_362989_, p_363999_.oFlapSpeed, p_363999_.flapSpeed);
        p_366002_.flapAngle = (Mth.sin(f) + 1.0F) * f1;
        p_366002_.pose = ParrotModel.getPose(p_363999_);
    }

    public static ResourceLocation getVariantTexture(Parrot.Variant p_262577_) {
        return switch (p_262577_) {
            case RED_BLUE -> RED_BLUE;
            case BLUE -> BLUE;
            case GREEN -> GREEN;
            case YELLOW_BLUE -> YELLOW_BLUE;
            case GRAY -> GREY;
        };
    }
}