package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class StuckInBodyLayer<M extends PlayerModel> extends RenderLayer<PlayerRenderState, M> {
    private final Model model;
    private final ResourceLocation texture;
    private final StuckInBodyLayer.PlacementStyle placementStyle;

    public StuckInBodyLayer(
        LivingEntityRenderer<?, PlayerRenderState, M> p_117564_, Model p_360738_, ResourceLocation p_361896_, StuckInBodyLayer.PlacementStyle p_363745_
    ) {
        super(p_117564_);
        this.model = p_360738_;
        this.texture = p_361896_;
        this.placementStyle = p_363745_;
    }

    protected abstract int numStuck(PlayerRenderState p_368620_);

    private void renderStuckItem(PoseStack p_117566_, MultiBufferSource p_117567_, int p_117568_, float p_117570_, float p_117571_, float p_117572_) {
        float f = Mth.sqrt(p_117570_ * p_117570_ + p_117572_ * p_117572_);
        float f1 = (float)(Math.atan2((double)p_117570_, (double)p_117572_) * 180.0F / (float)Math.PI);
        float f2 = (float)(Math.atan2((double)p_117571_, (double)f) * 180.0F / (float)Math.PI);
        p_117566_.mulPose(Axis.YP.rotationDegrees(f1 - 90.0F));
        p_117566_.mulPose(Axis.ZP.rotationDegrees(f2));
        this.model.renderToBuffer(p_117566_, p_117567_.getBuffer(this.model.renderType(this.texture)), p_117568_, OverlayTexture.NO_OVERLAY);
    }

    public void render(PoseStack p_117575_, MultiBufferSource p_117576_, int p_117577_, PlayerRenderState p_367175_, float p_117579_, float p_117580_) {
        int i = this.numStuck(p_367175_);
        if (i > 0) {
            RandomSource randomsource = RandomSource.create((long)p_367175_.id);

            for (int j = 0; j < i; j++) {
                p_117575_.pushPose();
                ModelPart modelpart = this.getParentModel().getRandomBodyPart(randomsource);
                ModelPart.Cube modelpart$cube = modelpart.getRandomCube(randomsource);
                modelpart.translateAndRotate(p_117575_);
                float f = randomsource.nextFloat();
                float f1 = randomsource.nextFloat();
                float f2 = randomsource.nextFloat();
                if (this.placementStyle == StuckInBodyLayer.PlacementStyle.ON_SURFACE) {
                    int k = randomsource.nextInt(3);
                    switch (k) {
                        case 0:
                            f = snapToFace(f);
                            break;
                        case 1:
                            f1 = snapToFace(f1);
                            break;
                        default:
                            f2 = snapToFace(f2);
                    }
                }

                p_117575_.translate(
                    Mth.lerp(f, modelpart$cube.minX, modelpart$cube.maxX) / 16.0F,
                    Mth.lerp(f1, modelpart$cube.minY, modelpart$cube.maxY) / 16.0F,
                    Mth.lerp(f2, modelpart$cube.minZ, modelpart$cube.maxZ) / 16.0F
                );
                this.renderStuckItem(p_117575_, p_117576_, p_117577_, -(f * 2.0F - 1.0F), -(f1 * 2.0F - 1.0F), -(f2 * 2.0F - 1.0F));
                p_117575_.popPose();
            }
        }
    }

    private static float snapToFace(float p_362675_) {
        return p_362675_ > 0.5F ? 1.0F : 0.5F;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum PlacementStyle {
        IN_CUBE,
        ON_SURFACE;
    }
}