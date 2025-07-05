package net.minecraft.client.gui.screens.inventory.tooltip;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TooltipRenderUtil {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/background");
    private static final ResourceLocation FRAME_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/frame");
    public static final int MOUSE_OFFSET = 12;
    private static final int PADDING = 3;
    public static final int PADDING_LEFT = 3;
    public static final int PADDING_RIGHT = 3;
    public static final int PADDING_TOP = 3;
    public static final int PADDING_BOTTOM = 3;
    private static final int MARGIN = 9;

    public static void renderTooltipBackground(
        GuiGraphics p_282666_, int p_281901_, int p_281846_, int p_281559_, int p_283336_, int p_283422_, @Nullable ResourceLocation p_367789_
    ) {
        int i = p_281901_ - 3 - 9;
        int j = p_281846_ - 3 - 9;
        int k = p_281559_ + 3 + 3 + 18;
        int l = p_283336_ + 3 + 3 + 18;
        p_282666_.pose().pushPose();
        p_282666_.pose().translate(0.0F, 0.0F, (float)p_283422_);
        p_282666_.blitSprite(RenderType::guiTextured, getBackgroundSprite(p_367789_), i, j, k, l);
        p_282666_.blitSprite(RenderType::guiTextured, getFrameSprite(p_367789_), i, j, k, l);
        p_282666_.pose().popPose();
    }

    private static ResourceLocation getBackgroundSprite(@Nullable ResourceLocation p_364502_) {
        return p_364502_ == null ? BACKGROUND_SPRITE : p_364502_.withPath(p_362641_ -> "tooltip/" + p_362641_ + "_background");
    }

    private static ResourceLocation getFrameSprite(@Nullable ResourceLocation p_362761_) {
        return p_362761_ == null ? FRAME_SPRITE : p_362761_.withPath(p_364578_ -> "tooltip/" + p_364578_ + "_frame");
    }
}