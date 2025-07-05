package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class Font {
    private static final float EFFECT_DEPTH = 0.01F;
    public static final float SHADOW_DEPTH = 0.03F;
    public static final int NO_SHADOW = 0;
    public static final int ALPHA_CUTOFF = 8;
    public final int lineHeight = 9;
    public final RandomSource random = RandomSource.create();
    private final Function<ResourceLocation, FontSet> fonts;
    final boolean filterFishyGlyphs;
    private final StringSplitter splitter;

    public Font(Function<ResourceLocation, FontSet> p_243253_, boolean p_243245_) {
        this.fonts = p_243253_;
        this.filterFishyGlyphs = p_243245_;
        this.splitter = new StringSplitter(
            (p_92722_, p_92723_) -> this.getFontSet(p_92723_.getFont()).getGlyphInfo(p_92722_, this.filterFishyGlyphs).getAdvance(p_92723_.isBold())
        );
    }

    FontSet getFontSet(ResourceLocation p_92864_) {
        return this.fonts.apply(p_92864_);
    }

    public String bidirectionalShaping(String p_92802_) {
        try {
            Bidi bidi = new Bidi(new ArabicShaping(8).shape(p_92802_), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        } catch (ArabicShapingException arabicshapingexception) {
            return p_92802_;
        }
    }

    public int drawInBatch(
        String p_272751_,
        float p_272661_,
        float p_273129_,
        int p_273272_,
        boolean p_273209_,
        Matrix4f p_272940_,
        MultiBufferSource p_273017_,
        Font.DisplayMode p_272608_,
        int p_273365_,
        int p_272755_
    ) {
        if (this.isBidirectional()) {
            p_272751_ = this.bidirectionalShaping(p_272751_);
        }

        return this.drawInternal(p_272751_, p_272661_, p_273129_, p_273272_, p_273209_, p_272940_, p_273017_, p_272608_, p_273365_, p_272755_, true);
    }

    public int drawInBatch(
        Component p_273032_,
        float p_273249_,
        float p_273594_,
        int p_273714_,
        boolean p_273050_,
        Matrix4f p_272974_,
        MultiBufferSource p_273695_,
        Font.DisplayMode p_272782_,
        int p_272603_,
        int p_273632_
    ) {
        return this.drawInBatch(p_273032_, p_273249_, p_273594_, p_273714_, p_273050_, p_272974_, p_273695_, p_272782_, p_272603_, p_273632_, true);
    }

    public int drawInBatch(
        Component p_363863_,
        float p_272811_,
        float p_272610_,
        int p_273422_,
        boolean p_273016_,
        Matrix4f p_273443_,
        MultiBufferSource p_273387_,
        Font.DisplayMode p_273551_,
        int p_272706_,
        int p_273114_,
        boolean p_273022_
    ) {
        return this.drawInternal(p_363863_.getVisualOrderText(), p_272811_, p_272610_, p_273422_, p_273016_, p_273443_, p_273387_, p_273551_, p_272706_, p_273114_, p_273022_);
    }

    public int drawInBatch(
        FormattedCharSequence p_273262_,
        float p_273006_,
        float p_273254_,
        int p_273375_,
        boolean p_273674_,
        Matrix4f p_273525_,
        MultiBufferSource p_272624_,
        Font.DisplayMode p_273418_,
        int p_273330_,
        int p_272981_
    ) {
        return this.drawInternal(p_273262_, p_273006_, p_273254_, p_273375_, p_273674_, p_273525_, p_272624_, p_273418_, p_273330_, p_272981_, true);
    }

    public void drawInBatch8xOutline(
        FormattedCharSequence p_168646_,
        float p_168647_,
        float p_168648_,
        int p_168649_,
        int p_168650_,
        Matrix4f p_254170_,
        MultiBufferSource p_168652_,
        int p_168653_
    ) {
        int i = adjustColor(p_168650_);
        Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(
            p_168652_, 0.0F, 0.0F, i, false, p_254170_, Font.DisplayMode.NORMAL, p_168653_
        );

        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
                if (j != 0 || k != 0) {
                    float[] afloat = new float[]{p_168647_};
                    int l = j;
                    int i1 = k;
                    p_168646_.accept((p_168661_, p_168662_, p_168663_) -> {
                        boolean flag = p_168662_.isBold();
                        FontSet fontset = this.getFontSet(p_168662_.getFont());
                        GlyphInfo glyphinfo = fontset.getGlyphInfo(p_168663_, this.filterFishyGlyphs);
                        font$stringrenderoutput.x = afloat[0] + (float)l * glyphinfo.getShadowOffset();
                        font$stringrenderoutput.y = p_168648_ + (float)i1 * glyphinfo.getShadowOffset();
                        afloat[0] += glyphinfo.getAdvance(flag);
                        return font$stringrenderoutput.accept(p_168661_, p_168662_.withColor(i), p_168663_);
                    });
                }
            }
        }

        font$stringrenderoutput.renderCharacters();
        Font.StringRenderOutput font$stringrenderoutput1 = new Font.StringRenderOutput(
            p_168652_, p_168647_, p_168648_, adjustColor(p_168649_), false, p_254170_, Font.DisplayMode.POLYGON_OFFSET, p_168653_
        );
        p_168646_.accept(font$stringrenderoutput1);
        font$stringrenderoutput1.finish(p_168647_);
    }

    private static int adjustColor(int p_92720_) {
        return (p_92720_ & -67108864) == 0 ? ARGB.opaque(p_92720_) : p_92720_;
    }

    private int drawInternal(
        String p_273658_,
        float p_273086_,
        float p_272883_,
        int p_273547_,
        boolean p_272778_,
        Matrix4f p_272662_,
        MultiBufferSource p_273012_,
        Font.DisplayMode p_273381_,
        int p_272855_,
        int p_272745_,
        boolean p_272785_
    ) {
        p_273547_ = adjustColor(p_273547_);
        p_273086_ = this.renderText(p_273658_, p_273086_, p_272883_, p_273547_, p_272778_, p_272662_, p_273012_, p_273381_, p_272855_, p_272745_, p_272785_);
        return (int)p_273086_ + (p_272778_ ? 1 : 0);
    }

    private int drawInternal(
        FormattedCharSequence p_273025_,
        float p_273121_,
        float p_272717_,
        int p_273653_,
        boolean p_273531_,
        Matrix4f p_273265_,
        MultiBufferSource p_273560_,
        Font.DisplayMode p_273342_,
        int p_273373_,
        int p_273266_,
        boolean p_362833_
    ) {
        p_273653_ = adjustColor(p_273653_);
        p_273121_ = this.renderText(p_273025_, p_273121_, p_272717_, p_273653_, p_273531_, p_273265_, p_273560_, p_273342_, p_273373_, p_273266_, p_362833_);
        return (int)p_273121_ + (p_273531_ ? 1 : 0);
    }

    private float renderText(
        String p_273765_,
        float p_273532_,
        float p_272783_,
        int p_273217_,
        boolean p_273583_,
        Matrix4f p_272734_,
        MultiBufferSource p_272595_,
        Font.DisplayMode p_273610_,
        int p_273727_,
        int p_273199_,
        boolean p_369545_
    ) {
        Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(
            p_272595_, p_273532_, p_272783_, p_273217_, p_273727_, p_273583_, p_272734_, p_273610_, p_273199_, p_369545_
        );
        StringDecomposer.iterateFormatted(p_273765_, Style.EMPTY, font$stringrenderoutput);
        return font$stringrenderoutput.finish(p_273532_);
    }

    private float renderText(
        FormattedCharSequence p_273322_,
        float p_272632_,
        float p_273541_,
        int p_273200_,
        boolean p_273312_,
        Matrix4f p_273276_,
        MultiBufferSource p_273392_,
        Font.DisplayMode p_272625_,
        int p_273774_,
        int p_273371_,
        boolean p_365371_
    ) {
        Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(
            p_273392_, p_272632_, p_273541_, p_273200_, p_273774_, p_273312_, p_273276_, p_272625_, p_273371_, p_365371_
        );
        p_273322_.accept(font$stringrenderoutput);
        return font$stringrenderoutput.finish(p_272632_);
    }

    public int width(String p_92896_) {
        return Mth.ceil(this.splitter.stringWidth(p_92896_));
    }

    public int width(FormattedText p_92853_) {
        return Mth.ceil(this.splitter.stringWidth(p_92853_));
    }

    public int width(FormattedCharSequence p_92725_) {
        return Mth.ceil(this.splitter.stringWidth(p_92725_));
    }

    public String plainSubstrByWidth(String p_92838_, int p_92839_, boolean p_92840_) {
        return p_92840_ ? this.splitter.plainTailByWidth(p_92838_, p_92839_, Style.EMPTY) : this.splitter.plainHeadByWidth(p_92838_, p_92839_, Style.EMPTY);
    }

    public String plainSubstrByWidth(String p_92835_, int p_92836_) {
        return this.splitter.plainHeadByWidth(p_92835_, p_92836_, Style.EMPTY);
    }

    public FormattedText substrByWidth(FormattedText p_92855_, int p_92856_) {
        return this.splitter.headByWidth(p_92855_, p_92856_, Style.EMPTY);
    }

    public int wordWrapHeight(String p_92921_, int p_92922_) {
        return 9 * this.splitter.splitLines(p_92921_, p_92922_, Style.EMPTY).size();
    }

    public int wordWrapHeight(FormattedText p_239134_, int p_239135_) {
        return 9 * this.splitter.splitLines(p_239134_, p_239135_, Style.EMPTY).size();
    }

    public List<FormattedCharSequence> split(FormattedText p_92924_, int p_92925_) {
        return Language.getInstance().getVisualOrder(this.splitter.splitLines(p_92924_, p_92925_, Style.EMPTY));
    }

    public boolean isBidirectional() {
        return Language.getInstance().isDefaultRightToLeft();
    }

    public StringSplitter getSplitter() {
        return this.splitter;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum DisplayMode {
        NORMAL,
        SEE_THROUGH,
        POLYGON_OFFSET;
    }

    @OnlyIn(Dist.CLIENT)
    class StringRenderOutput implements FormattedCharSink {
        final MultiBufferSource bufferSource;
        private final boolean drawShadow;
        private final int color;
        private final int backgroundColor;
        private final Matrix4f pose;
        private final Font.DisplayMode mode;
        private final int packedLightCoords;
        private final boolean inverseDepth;
        float x;
        float y;
        private final List<BakedGlyph.GlyphInstance> glyphInstances;
        @Nullable
        private List<BakedGlyph.Effect> effects;

        private void addEffect(BakedGlyph.Effect p_92965_) {
            if (this.effects == null) {
                this.effects = Lists.newArrayList();
            }

            this.effects.add(p_92965_);
        }

        public StringRenderOutput(
            final MultiBufferSource p_181365_,
            final float p_181366_,
            final float p_181367_,
            final int p_181368_,
            final boolean p_181369_,
            final Matrix4f p_254510_,
            final Font.DisplayMode p_181371_,
            final int p_181372_
        ) {
            this(p_181365_, p_181366_, p_181367_, p_181368_, 0, p_181369_, p_254510_, p_181371_, p_181372_, true);
        }

        public StringRenderOutput(
            final MultiBufferSource p_369661_,
            final float p_364376_,
            final float p_367232_,
            final int p_363543_,
            final int p_366865_,
            final boolean p_369620_,
            final Matrix4f p_365215_,
            final Font.DisplayMode p_366135_,
            final int p_361543_,
            final boolean p_363799_
        ) {
            this.glyphInstances = new ArrayList<>();
            this.bufferSource = p_369661_;
            this.x = p_364376_;
            this.y = p_367232_;
            this.drawShadow = p_369620_;
            this.color = p_363543_;
            this.backgroundColor = p_366865_;
            this.pose = p_365215_;
            this.mode = p_366135_;
            this.packedLightCoords = p_361543_;
            this.inverseDepth = p_363799_;
        }

        @Override
        public boolean accept(int p_92967_, Style p_92968_, int p_92969_) {
            FontSet fontset = Font.this.getFontSet(p_92968_.getFont());
            GlyphInfo glyphinfo = fontset.getGlyphInfo(p_92969_, Font.this.filterFishyGlyphs);
            BakedGlyph bakedglyph = p_92968_.isObfuscated() && p_92969_ != 32 ? fontset.getRandomGlyph(glyphinfo) : fontset.getGlyph(p_92969_);
            boolean flag = p_92968_.isBold();
            TextColor textcolor = p_92968_.getColor();
            int i = this.getTextColor(textcolor);
            int j = this.getShadowColor(p_92968_, i);
            float f = glyphinfo.getAdvance(flag);
            float f1 = p_92967_ == 0 ? this.x - 1.0F : this.x;
            float f2 = glyphinfo.getShadowOffset();
            if (!(bakedglyph instanceof EmptyGlyph)) {
                float f3 = flag ? glyphinfo.getBoldOffset() : 0.0F;
                this.glyphInstances.add(new BakedGlyph.GlyphInstance(this.x, this.y, i, j, bakedglyph, p_92968_, f3, f2));
            }

            if (p_92968_.isStrikethrough()) {
                this.addEffect(new BakedGlyph.Effect(f1, this.y + 4.5F, this.x + f, this.y + 4.5F - 1.0F, this.getOverTextEffectDepth(), i, j, f2));
            }

            if (p_92968_.isUnderlined()) {
                this.addEffect(new BakedGlyph.Effect(f1, this.y + 9.0F, this.x + f, this.y + 9.0F - 1.0F, this.getOverTextEffectDepth(), i, j, f2));
            }

            this.x += f;
            return true;
        }

        float finish(float p_92963_) {
            BakedGlyph bakedglyph = null;
            if (this.backgroundColor != 0) {
                BakedGlyph.Effect bakedglyph$effect = new BakedGlyph.Effect(
                    p_92963_ - 1.0F, this.y + 9.0F, this.x, this.y - 1.0F, this.getUnderTextEffectDepth(), this.backgroundColor
                );
                bakedglyph = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
                VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));
                bakedglyph.renderEffect(bakedglyph$effect, this.pose, vertexconsumer, this.packedLightCoords);
            }

            this.renderCharacters();
            if (this.effects != null) {
                if (bakedglyph == null) {
                    bakedglyph = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
                }

                VertexConsumer vertexconsumer1 = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));

                for (BakedGlyph.Effect bakedglyph$effect1 : this.effects) {
                    bakedglyph.renderEffect(bakedglyph$effect1, this.pose, vertexconsumer1, this.packedLightCoords);
                }
            }

            return this.x;
        }

        private int getTextColor(@Nullable TextColor p_378404_) {
            if (p_378404_ != null) {
                int i = ARGB.alpha(this.color);
                int j = p_378404_.getValue();
                return ARGB.color(i, j);
            } else {
                return this.color;
            }
        }

        private int getShadowColor(Style p_378453_, int p_375981_) {
            Integer integer = p_378453_.getShadowColor();
            if (integer != null) {
                float f = ARGB.alphaFloat(p_375981_);
                float f1 = ARGB.alphaFloat(integer);
                return f != 1.0F ? ARGB.color(ARGB.as8BitChannel(f * f1), integer) : integer;
            } else {
                return this.drawShadow ? ARGB.scaleRGB(p_375981_, 0.25F) : 0;
            }
        }

        void renderCharacters() {
            for (BakedGlyph.GlyphInstance bakedglyph$glyphinstance : this.glyphInstances) {
                BakedGlyph bakedglyph = bakedglyph$glyphinstance.glyph();
                VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));
                bakedglyph.renderChar(bakedglyph$glyphinstance, this.pose, vertexconsumer, this.packedLightCoords);
            }
        }

        private float getOverTextEffectDepth() {
            return this.inverseDepth ? 0.01F : -0.01F;
        }

        private float getUnderTextEffectDepth() {
            return this.inverseDepth ? -0.01F : 0.01F;
        }
    }
}
