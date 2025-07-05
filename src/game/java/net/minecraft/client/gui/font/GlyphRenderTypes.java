package net.minecraft.client.gui.font;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record GlyphRenderTypes(RenderType normal, RenderType seeThrough, RenderType polygonOffset) {
    public static GlyphRenderTypes createForIntensityTexture(ResourceLocation p_285411_) {
        return new GlyphRenderTypes(RenderType.textIntensity(p_285411_), RenderType.textIntensitySeeThrough(p_285411_), RenderType.textIntensityPolygonOffset(p_285411_));
    }

    public static GlyphRenderTypes createForColorTexture(ResourceLocation p_285486_) {
        return new GlyphRenderTypes(RenderType.text(p_285486_), RenderType.textSeeThrough(p_285486_), RenderType.textPolygonOffset(p_285486_));
    }

    public RenderType select(Font.DisplayMode p_285259_) {
        return switch (p_285259_) {
            case NORMAL -> this.normal;
            case SEE_THROUGH -> this.seeThrough;
            case POLYGON_OFFSET -> this.polygonOffset;
        };
    }
}