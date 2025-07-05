package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.SalmonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SalmonRenderer extends MobRenderer<Salmon, SalmonRenderState, SalmonModel> {
    private static final ResourceLocation SALMON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fish/salmon.png");
    private final SalmonModel smallSalmonModel;
    private final SalmonModel mediumSalmonModel;
    private final SalmonModel largeSalmonModel;

    public SalmonRenderer(EntityRendererProvider.Context p_174364_) {
        super(p_174364_, new SalmonModel(p_174364_.bakeLayer(ModelLayers.SALMON)), 0.4F);
        this.smallSalmonModel = new SalmonModel(p_174364_.bakeLayer(ModelLayers.SALMON_SMALL));
        this.mediumSalmonModel = new SalmonModel(p_174364_.bakeLayer(ModelLayers.SALMON));
        this.largeSalmonModel = new SalmonModel(p_174364_.bakeLayer(ModelLayers.SALMON_LARGE));
    }

    public void extractRenderState(Salmon p_368522_, SalmonRenderState p_369928_, float p_363641_) {
        super.extractRenderState(p_368522_, p_369928_, p_363641_);
        p_369928_.variant = p_368522_.getVariant();
    }

    public ResourceLocation getTextureLocation(SalmonRenderState p_363562_) {
        return SALMON_LOCATION;
    }

    public SalmonRenderState createRenderState() {
        return new SalmonRenderState();
    }

    protected void setupRotations(SalmonRenderState p_363147_, PoseStack p_115829_, float p_115830_, float p_115831_) {
        super.setupRotations(p_363147_, p_115829_, p_115830_, p_115831_);
        float f = 1.0F;
        float f1 = 1.0F;
        if (!p_363147_.isInWater) {
            f = 1.3F;
            f1 = 1.7F;
        }

        float f2 = f * 4.3F * Mth.sin(f1 * 0.6F * p_363147_.ageInTicks);
        p_115829_.mulPose(Axis.YP.rotationDegrees(f2));
        if (!p_363147_.isInWater) {
            p_115829_.translate(0.2F, 0.1F, 0.0F);
            p_115829_.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }
    }

    public void render(SalmonRenderState p_364512_, PoseStack p_368878_, MultiBufferSource p_363357_, int p_365924_) {
        if (p_364512_.variant == Salmon.Variant.SMALL) {
            this.model = this.smallSalmonModel;
        } else if (p_364512_.variant == Salmon.Variant.LARGE) {
            this.model = this.largeSalmonModel;
        } else {
            this.model = this.mediumSalmonModel;
        }

        super.render(p_364512_, p_368878_, p_363357_, p_365924_);
    }
}