package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSignRenderer implements BlockEntityRenderer<SignBlockEntity> {
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Font font;

    public AbstractSignRenderer(BlockEntityRendererProvider.Context p_377237_) {
        this.font = p_377237_.getFont();
    }

    protected abstract Model getSignModel(BlockState p_378255_, WoodType p_376054_);

    protected abstract Material getSignMaterial(WoodType p_376937_);

    protected abstract float getSignModelRenderScale();

    protected abstract float getSignTextRenderScale();

    protected abstract Vec3 getTextOffset();

    protected abstract void translateSign(PoseStack p_377787_, float p_378640_, BlockState p_376264_);

    public void render(SignBlockEntity p_375644_, float p_376234_, PoseStack p_377246_, MultiBufferSource p_378186_, int p_378621_, int p_376297_) {
        BlockState blockstate = p_375644_.getBlockState();
        SignBlock signblock = (SignBlock)blockstate.getBlock();
        Model model = this.getSignModel(blockstate, signblock.type());
        this.renderSignWithText(p_375644_, p_377246_, p_378186_, p_378621_, p_376297_, blockstate, signblock, signblock.type(), model);
    }

    private void renderSignWithText(
        SignBlockEntity p_375839_,
        PoseStack p_376878_,
        MultiBufferSource p_378051_,
        int p_375958_,
        int p_375909_,
        BlockState p_376590_,
        SignBlock p_376023_,
        WoodType p_375949_,
        Model p_377521_
    ) {
        p_376878_.pushPose();
        this.translateSign(p_376878_, -p_376023_.getYRotationDegrees(p_376590_), p_376590_);
        this.renderSign(p_376878_, p_378051_, p_375958_, p_375909_, p_375949_, p_377521_);
        this.renderSignText(p_375839_.getBlockPos(), p_375839_.getFrontText(), p_376878_, p_378051_, p_375958_, p_375839_.getTextLineHeight(), p_375839_.getMaxTextLineWidth(), true);
        this.renderSignText(p_375839_.getBlockPos(), p_375839_.getBackText(), p_376878_, p_378051_, p_375958_, p_375839_.getTextLineHeight(), p_375839_.getMaxTextLineWidth(), false);
        p_376878_.popPose();
    }

    protected void renderSign(PoseStack p_378035_, MultiBufferSource p_378542_, int p_377671_, int p_376670_, WoodType p_375855_, Model p_375450_) {
        p_378035_.pushPose();
        float f = this.getSignModelRenderScale();
        p_378035_.scale(f, -f, -f);
        Material material = this.getSignMaterial(p_375855_);
        VertexConsumer vertexconsumer = material.buffer(p_378542_, p_375450_::renderType);
        p_375450_.renderToBuffer(p_378035_, vertexconsumer, p_377671_, p_376670_);
        p_378035_.popPose();
    }

    private void renderSignText(
        BlockPos p_375961_,
        SignText p_378476_,
        PoseStack p_376276_,
        MultiBufferSource p_378832_,
        int p_378087_,
        int p_375648_,
        int p_378485_,
        boolean p_377200_
    ) {
        p_376276_.pushPose();
        this.translateSignText(p_376276_, p_377200_, this.getTextOffset());
        int i = getDarkColor(p_378476_);
        int j = 4 * p_375648_ / 2;
        FormattedCharSequence[] aformattedcharsequence = p_378476_.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), p_378315_ -> {
            List<FormattedCharSequence> list = this.font.split(p_378315_, p_378485_);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });
        int k;
        boolean flag;
        int l;
        if (p_378476_.hasGlowingText()) {
            k = p_378476_.getColor().getTextColor();
            flag = isOutlineVisible(p_375961_, k);
            l = 15728880;
        } else {
            k = i;
            flag = false;
            l = p_378087_;
        }

        for (int i1 = 0; i1 < 4; i1++) {
            FormattedCharSequence formattedcharsequence = aformattedcharsequence[i1];
            float f = (float)(-this.font.width(formattedcharsequence) / 2);
            if (flag) {
                this.font.drawInBatch8xOutline(formattedcharsequence, f, (float)(i1 * p_375648_ - j), k, i, p_376276_.last().pose(), p_378832_, l);
            } else {
                this.font
                    .drawInBatch(
                        formattedcharsequence,
                        f,
                        (float)(i1 * p_375648_ - j),
                        k,
                        false,
                        p_376276_.last().pose(),
                        p_378832_,
                        Font.DisplayMode.POLYGON_OFFSET,
                        0,
                        l
                    );
            }
        }

        p_376276_.popPose();
    }

    private void translateSignText(PoseStack p_377496_, boolean p_376226_, Vec3 p_377669_) {
        if (!p_376226_) {
            p_377496_.mulPose(Axis.YP.rotationDegrees(180.0F));
        }

        float f = 0.015625F * this.getSignTextRenderScale();
        p_377496_.translate(p_377669_);
        p_377496_.scale(f, -f, f);
    }

    private static boolean isOutlineVisible(BlockPos p_376971_, int p_378481_) {
        if (p_378481_ == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localplayer = minecraft.player;
            if (localplayer != null && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping()) {
                return true;
            } else {
                Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(p_376971_)) < (double)OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    public static int getDarkColor(SignText p_376682_) {
        int i = p_376682_.getColor().getTextColor();
        if (i == DyeColor.BLACK.getTextColor() && p_376682_.hasGlowingText()) {
            return -988212;
        } else {
            double d0 = 0.4;
            int j = (int)((double)ARGB.red(i) * 0.4);
            int k = (int)((double)ARGB.green(i) * 0.4);
            int l = (int)((double)ARGB.blue(i) * 0.4);
            return ARGB.color(0, j, k, l);
        }
    }
}