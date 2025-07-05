package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BakedGlyph {
    public static final float Z_FIGHTER = 0.001F;
    private final GlyphRenderTypes renderTypes;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final float left;
    private final float right;
    private final float up;
    private final float down;

    public BakedGlyph(
        GlyphRenderTypes p_285527_,
        float p_285271_,
        float p_284970_,
        float p_285098_,
        float p_285023_,
        float p_285242_,
        float p_285043_,
        float p_285100_,
        float p_284948_
    ) {
        this.renderTypes = p_285527_;
        this.u0 = p_285271_;
        this.u1 = p_284970_;
        this.v0 = p_285098_;
        this.v1 = p_285023_;
        this.left = p_285242_;
        this.right = p_285043_;
        this.up = p_285100_;
        this.down = p_284948_;
    }

    public void renderChar(BakedGlyph.GlyphInstance p_368554_, Matrix4f p_365625_, VertexConsumer p_370130_, int p_369456_) {
        Style style = p_368554_.style();
        boolean flag = style.isItalic();
        float f = p_368554_.x();
        float f1 = p_368554_.y();
        int i = p_368554_.color();
        int j = p_368554_.shadowColor();
        boolean flag1 = style.isBold();
        if (p_368554_.hasShadow()) {
            this.render(flag, f + p_368554_.shadowOffset(), f1 + p_368554_.shadowOffset(), p_365625_, p_370130_, j, flag1, p_369456_);
            this.render(flag, f, f1, 0.03F, p_365625_, p_370130_, i, flag1, p_369456_);
        } else {
            this.render(flag, f, f1, p_365625_, p_370130_, i, flag1, p_369456_);
        }

        if (flag1) {
            if (p_368554_.hasShadow()) {
                this.render(
                    flag, f + p_368554_.boldOffset() + p_368554_.shadowOffset(), f1 + p_368554_.shadowOffset(), 0.001F, p_365625_, p_370130_, j, true, p_369456_
                );
                this.render(flag, f + p_368554_.boldOffset(), f1, 0.03F, p_365625_, p_370130_, i, true, p_369456_);
            } else {
                this.render(flag, f + p_368554_.boldOffset(), f1, p_365625_, p_370130_, i, true, p_369456_);
            }
        }
    }

    private void render(
        boolean p_95227_, float p_95228_, float p_95229_, Matrix4f p_253706_, VertexConsumer p_95231_, int p_95236_, boolean p_378824_, int p_365126_
    ) {
        this.render(p_95227_, p_95228_, p_95229_, 0.0F, p_253706_, p_95231_, p_95236_, p_378824_, p_365126_);
    }

    private void render(
        boolean p_378370_,
        float p_378368_,
        float p_377211_,
        float p_376193_,
        Matrix4f p_376064_,
        VertexConsumer p_377733_,
        int p_375579_,
        boolean p_376230_,
        int p_378811_
    ) {
        float f = p_378368_ + this.left;
        float f1 = p_378368_ + this.right;
        float f2 = p_377211_ + this.up;
        float f3 = p_377211_ + this.down;
        float f4 = p_378370_ ? 1.0F - 0.25F * this.up : 0.0F;
        float f5 = p_378370_ ? 1.0F - 0.25F * this.down : 0.0F;
        float f6 = p_376230_ ? 0.1F : 0.0F;
        p_377733_.addVertex(p_376064_, f + f4 - f6, f2 - f6, p_376193_).setColor(p_375579_).setUv(this.u0, this.v0).setLight(p_378811_);
        p_377733_.addVertex(p_376064_, f + f5 - f6, f3 + f6, p_376193_).setColor(p_375579_).setUv(this.u0, this.v1).setLight(p_378811_);
        p_377733_.addVertex(p_376064_, f1 + f5 + f6, f3 + f6, p_376193_).setColor(p_375579_).setUv(this.u1, this.v1).setLight(p_378811_);
        p_377733_.addVertex(p_376064_, f1 + f4 + f6, f2 - f6, p_376193_).setColor(p_375579_).setUv(this.u1, this.v0).setLight(p_378811_);
    }

    public void renderEffect(BakedGlyph.Effect p_95221_, Matrix4f p_254370_, VertexConsumer p_95223_, int p_95224_) {
        if (p_95221_.hasShadow()) {
            this.buildEffect(p_95221_, p_95221_.shadowOffset(), 0.0F, p_95221_.shadowColor(), p_95223_, p_95224_, p_254370_);
            this.buildEffect(p_95221_, 0.0F, 0.03F, p_95221_.color, p_95223_, p_95224_, p_254370_);
        } else {
            this.buildEffect(p_95221_, 0.0F, 0.0F, p_95221_.color, p_95223_, p_95224_, p_254370_);
        }
    }

    private void buildEffect(
        BakedGlyph.Effect p_376178_, float p_376440_, float p_376102_, int p_377377_, VertexConsumer p_377166_, int p_377325_, Matrix4f p_375465_
    ) {
        p_377166_.addVertex(p_375465_, p_376178_.x0 + p_376440_, p_376178_.y0 + p_376440_, p_376178_.depth + p_376102_)
            .setColor(p_377377_)
            .setUv(this.u0, this.v0)
            .setLight(p_377325_);
        p_377166_.addVertex(p_375465_, p_376178_.x1 + p_376440_, p_376178_.y0 + p_376440_, p_376178_.depth + p_376102_)
            .setColor(p_377377_)
            .setUv(this.u0, this.v1)
            .setLight(p_377325_);
        p_377166_.addVertex(p_375465_, p_376178_.x1 + p_376440_, p_376178_.y1 + p_376440_, p_376178_.depth + p_376102_)
            .setColor(p_377377_)
            .setUv(this.u1, this.v1)
            .setLight(p_377325_);
        p_377166_.addVertex(p_375465_, p_376178_.x0 + p_376440_, p_376178_.y1 + p_376440_, p_376178_.depth + p_376102_)
            .setColor(p_377377_)
            .setUv(this.u1, this.v0)
            .setLight(p_377325_);
    }

    public RenderType renderType(Font.DisplayMode p_181388_) {
        return this.renderTypes.select(p_181388_);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Effect(float x0, float y0, float x1, float y1, float depth, int color, int shadowColor, float shadowOffset) {
        public Effect(float p_95247_, float p_95248_, float p_95249_, float p_95250_, float p_95251_, int p_365759_) {
            this(p_95247_, p_95248_, p_95249_, p_95250_, p_95251_, p_365759_, 0, 0.0F);
        }

        boolean hasShadow() {
            return this.shadowColor() != 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record GlyphInstance(
        float x, float y, int color, int shadowColor, BakedGlyph glyph, Style style, float boldOffset, float shadowOffset
    ) {
        boolean hasShadow() {
            return this.shadowColor() != 0;
        }
    }
}