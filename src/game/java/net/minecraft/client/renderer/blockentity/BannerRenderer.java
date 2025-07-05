package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.BannerFlagModel;
import net.minecraft.client.model.BannerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BannerRenderer implements BlockEntityRenderer<BannerBlockEntity> {
    private static final int MAX_PATTERNS = 16;
    private static final float SIZE = 0.6666667F;
    private final BannerModel standingModel;
    private final BannerModel wallModel;
    private final BannerFlagModel standingFlagModel;
    private final BannerFlagModel wallFlagModel;

    public BannerRenderer(BlockEntityRendererProvider.Context p_173521_) {
        this(p_173521_.getModelSet());
    }

    public BannerRenderer(EntityModelSet p_375660_) {
        this.standingModel = new BannerModel(p_375660_.bakeLayer(ModelLayers.STANDING_BANNER));
        this.wallModel = new BannerModel(p_375660_.bakeLayer(ModelLayers.WALL_BANNER));
        this.standingFlagModel = new BannerFlagModel(p_375660_.bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
        this.wallFlagModel = new BannerFlagModel(p_375660_.bakeLayer(ModelLayers.WALL_BANNER_FLAG));
    }

    public void render(BannerBlockEntity p_112052_, float p_112053_, PoseStack p_112054_, MultiBufferSource p_112055_, int p_112056_, int p_112057_) {
        BlockState blockstate = p_112052_.getBlockState();
        BannerModel bannermodel;
        BannerFlagModel bannerflagmodel;
        float f;
        if (blockstate.getBlock() instanceof BannerBlock) {
            f = -RotationSegment.convertToDegrees(blockstate.getValue(BannerBlock.ROTATION));
            bannermodel = this.standingModel;
            bannerflagmodel = this.standingFlagModel;
        } else {
            f = -blockstate.getValue(WallBannerBlock.FACING).toYRot();
            bannermodel = this.wallModel;
            bannerflagmodel = this.wallFlagModel;
        }

        long i = p_112052_.getLevel().getGameTime();
        BlockPos blockpos = p_112052_.getBlockPos();
        float f1 = ((float)Math.floorMod((long)(blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + i, 100L) + p_112053_)
            / 100.0F;
        renderBanner(p_112054_, p_112055_, p_112056_, p_112057_, f, bannermodel, bannerflagmodel, f1, p_112052_.getBaseColor(), p_112052_.getPatterns());
    }

    public void renderInHand(PoseStack p_378791_, MultiBufferSource p_377506_, int p_376325_, int p_376814_, DyeColor p_378689_, BannerPatternLayers p_376552_) {
        renderBanner(p_378791_, p_377506_, p_376325_, p_376814_, 0.0F, this.standingModel, this.standingFlagModel, 0.0F, p_378689_, p_376552_);
    }

    private static void renderBanner(
        PoseStack p_375890_,
        MultiBufferSource p_378479_,
        int p_378067_,
        int p_375649_,
        float p_376968_,
        BannerModel p_376125_,
        BannerFlagModel p_375614_,
        float p_376731_,
        DyeColor p_378557_,
        BannerPatternLayers p_377307_
    ) {
        p_375890_.pushPose();
        p_375890_.translate(0.5F, 0.0F, 0.5F);
        p_375890_.mulPose(Axis.YP.rotationDegrees(p_376968_));
        p_375890_.scale(0.6666667F, -0.6666667F, -0.6666667F);
        p_376125_.renderToBuffer(p_375890_, ModelBakery.BANNER_BASE.buffer(p_378479_, RenderType::entitySolid), p_378067_, p_375649_);
        p_375614_.setupAnim(p_376731_);
        renderPatterns(p_375890_, p_378479_, p_378067_, p_375649_, p_375614_.root(), ModelBakery.BANNER_BASE, true, p_378557_, p_377307_);
        p_375890_.popPose();
    }

    public static void renderPatterns(
        PoseStack p_112066_,
        MultiBufferSource p_112067_,
        int p_112068_,
        int p_112069_,
        ModelPart p_112070_,
        Material p_112071_,
        boolean p_112072_,
        DyeColor p_331835_,
        BannerPatternLayers p_327702_
    ) {
        renderPatterns(p_112066_, p_112067_, p_112068_, p_112069_, p_112070_, p_112071_, p_112072_, p_331835_, p_327702_, false, true);
    }

    public static void renderPatterns(
        PoseStack p_112075_,
        MultiBufferSource p_112076_,
        int p_112077_,
        int p_112078_,
        ModelPart p_112079_,
        Material p_112080_,
        boolean p_112081_,
        DyeColor p_336347_,
        BannerPatternLayers p_332113_,
        boolean p_112083_,
        boolean p_361895_
    ) {
        p_112079_.render(p_112075_, p_112080_.buffer(p_112076_, RenderType::entitySolid, p_361895_, p_112083_), p_112077_, p_112078_);
        renderPatternLayer(p_112075_, p_112076_, p_112077_, p_112078_, p_112079_, p_112081_ ? Sheets.BANNER_BASE : Sheets.SHIELD_BASE, p_336347_);

        for (int i = 0; i < 16 && i < p_332113_.layers().size(); i++) {
            BannerPatternLayers.Layer bannerpatternlayers$layer = p_332113_.layers().get(i);
            Material material = p_112081_ ? Sheets.getBannerMaterial(bannerpatternlayers$layer.pattern()) : Sheets.getShieldMaterial(bannerpatternlayers$layer.pattern());
            renderPatternLayer(p_112075_, p_112076_, p_112077_, p_112078_, p_112079_, material, bannerpatternlayers$layer.color());
        }
    }

    private static void renderPatternLayer(
        PoseStack p_332210_, MultiBufferSource p_336119_, int p_333952_, int p_335632_, ModelPart p_327937_, Material p_327979_, DyeColor p_331652_
    ) {
        int i = p_331652_.getTextureDiffuseColor();
        p_327937_.render(p_332210_, p_327979_.buffer(p_336119_, RenderType::entityNoOutline), p_333952_, p_335632_, i);
    }
}