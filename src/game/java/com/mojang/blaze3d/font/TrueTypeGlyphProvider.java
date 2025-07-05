package com.mojang.blaze3d.font;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

@OnlyIn(Dist.CLIENT)
public class TrueTypeGlyphProvider implements GlyphProvider {
    @Nullable
    private ByteBuffer fontMemory;
    @Nullable
    private FT_Face face;
    final float oversample;
    private final CodepointMap<TrueTypeGlyphProvider.GlyphEntry> glyphs = new CodepointMap<>(
        TrueTypeGlyphProvider.GlyphEntry[]::new, TrueTypeGlyphProvider.GlyphEntry[][]::new
    );

    public TrueTypeGlyphProvider(ByteBuffer p_83846_, FT_Face p_330978_, float p_83848_, float p_83849_, float p_83850_, float p_83851_, String p_83852_) {
        this.fontMemory = p_83846_;
        this.face = p_330978_;
        this.oversample = p_83849_;
        IntSet intset = new IntArraySet();
        p_83852_.codePoints().forEach(intset::add);
        int i = Math.round(p_83848_ * p_83849_);
        FreeType.FT_Set_Pixel_Sizes(p_330978_, i, i);
        float f = p_83850_ * p_83849_;
        float f1 = -p_83851_ * p_83849_;

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            FT_Vector ft_vector = FreeTypeUtil.setVector(FT_Vector.malloc(memorystack), f, f1);
            FreeType.FT_Set_Transform(p_330978_, null, ft_vector);
            IntBuffer intbuffer = memorystack.mallocInt(1);
            int j = (int)FreeType.FT_Get_First_Char(p_330978_, intbuffer);

            while (true) {
                int k = intbuffer.get(0);
                if (k == 0) {
                    return;
                }

                if (!intset.contains(j)) {
                    this.glyphs.put(j, new TrueTypeGlyphProvider.GlyphEntry(k));
                }

                j = (int)FreeType.FT_Get_Next_Char(p_330978_, (long)j, intbuffer);
            }
        }
    }

    @Nullable
    @Override
    public GlyphInfo getGlyph(int p_231116_) {
        TrueTypeGlyphProvider.GlyphEntry truetypeglyphprovider$glyphentry = this.glyphs.get(p_231116_);
        return truetypeglyphprovider$glyphentry != null ? this.getOrLoadGlyphInfo(p_231116_, truetypeglyphprovider$glyphentry) : null;
    }

    private GlyphInfo getOrLoadGlyphInfo(int p_369372_, TrueTypeGlyphProvider.GlyphEntry p_364388_) {
        GlyphInfo glyphinfo = p_364388_.glyph;
        if (glyphinfo == null) {
            FT_Face ft_face = this.validateFontOpen();
            synchronized (ft_face) {
                glyphinfo = p_364388_.glyph;
                if (glyphinfo == null) {
                    glyphinfo = this.loadGlyph(p_369372_, ft_face, p_364388_.index);
                    p_364388_.glyph = glyphinfo;
                }
            }
        }

        return glyphinfo;
    }

    private GlyphInfo loadGlyph(int p_362561_, FT_Face p_369907_, int p_362974_) {
        int i = FreeType.FT_Load_Glyph(p_369907_, p_362974_, 4194312);
        if (i != 0) {
            FreeTypeUtil.assertError(i, String.format(Locale.ROOT, "Loading glyph U+%06X", p_362561_));
        }

        FT_GlyphSlot ft_glyphslot = p_369907_.glyph();
        if (ft_glyphslot == null) {
            throw new NullPointerException(String.format(Locale.ROOT, "Glyph U+%06X not initialized", p_362561_));
        } else {
            float f = FreeTypeUtil.x(ft_glyphslot.advance());
            FT_Bitmap ft_bitmap = ft_glyphslot.bitmap();
            int j = ft_glyphslot.bitmap_left();
            int k = ft_glyphslot.bitmap_top();
            int l = ft_bitmap.width();
            int i1 = ft_bitmap.rows();
            return (GlyphInfo)(l > 0 && i1 > 0 ? new TrueTypeGlyphProvider.Glyph((float)j, (float)k, l, i1, f, p_362974_) : (GlyphInfo.SpaceGlyphInfo)() -> f / this.oversample);
        }
    }

    FT_Face validateFontOpen() {
        if (this.fontMemory != null && this.face != null) {
            return this.face;
        } else {
            throw new IllegalStateException("Provider already closed");
        }
    }

    @Override
    public void close() {
        if (this.face != null) {
            synchronized (FreeTypeUtil.LIBRARY_LOCK) {
                FreeTypeUtil.checkError(FreeType.FT_Done_Face(this.face), "Deleting face");
            }

            this.face = null;
        }

        MemoryUtil.memFree(this.fontMemory);
        this.fontMemory = null;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return this.glyphs.keySet();
    }

    @OnlyIn(Dist.CLIENT)
    class Glyph implements GlyphInfo {
        final int width;
        final int height;
        final float bearingX;
        final float bearingY;
        private final float advance;
        final int index;

        Glyph(final float p_83886_, final float p_83887_, final int p_83882_, final int p_83883_, final float p_331469_, final int p_83884_) {
            this.width = p_83882_;
            this.height = p_83883_;
            this.advance = p_331469_ / TrueTypeGlyphProvider.this.oversample;
            this.bearingX = p_83886_ / TrueTypeGlyphProvider.this.oversample;
            this.bearingY = p_83887_ / TrueTypeGlyphProvider.this.oversample;
            this.index = p_83884_;
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> p_231120_) {
            return p_231120_.apply(new SheetGlyphInfo() {
                @Override
                public int getPixelWidth() {
                    return Glyph.this.width;
                }

                @Override
                public int getPixelHeight() {
                    return Glyph.this.height;
                }

                @Override
                public float getOversample() {
                    return TrueTypeGlyphProvider.this.oversample;
                }

                @Override
                public float getBearingLeft() {
                    return Glyph.this.bearingX;
                }

                @Override
                public float getBearingTop() {
                    return Glyph.this.bearingY;
                }

                @Override
                public void upload(int p_231126_, int p_231127_) {
                    FT_Face ft_face = TrueTypeGlyphProvider.this.validateFontOpen();
                    NativeImage nativeimage = new NativeImage(NativeImage.Format.LUMINANCE, Glyph.this.width, Glyph.this.height, false);
                    if (nativeimage.copyFromFont(ft_face, Glyph.this.index)) {
                        nativeimage.upload(0, p_231126_, p_231127_, 0, 0, Glyph.this.width, Glyph.this.height, true);
                    } else {
                        nativeimage.close();
                    }
                }

                @Override
                public boolean isColored() {
                    return false;
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class GlyphEntry {
        final int index;
        @Nullable
        volatile GlyphInfo glyph;

        GlyphEntry(int p_362332_) {
            this.index = p_362332_;
        }
    }
}
