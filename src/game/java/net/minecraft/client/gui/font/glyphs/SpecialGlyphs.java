package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum SpecialGlyphs implements GlyphInfo {
    WHITE(() -> generate(5, 8, (p_232613_, p_232614_) -> -1)),
    MISSING(() -> {
        int i = 5;
        int j = 8;
        return generate(5, 8, (p_232606_, p_232607_) -> {
            boolean flag = p_232606_ == 0 || p_232606_ + 1 == 5 || p_232607_ == 0 || p_232607_ + 1 == 8;
            return flag ? -1 : 0;
        });
    });

    final NativeImage image;

    private static NativeImage generate(int p_232609_, int p_232610_, SpecialGlyphs.PixelProvider p_232611_) {
        NativeImage nativeimage = new NativeImage(NativeImage.Format.RGBA, p_232609_, p_232610_, false);

        for (int i = 0; i < p_232610_; i++) {
            for (int j = 0; j < p_232609_; j++) {
                nativeimage.setPixel(j, i, p_232611_.getColor(j, i));
            }
        }

        nativeimage.untrack();
        return nativeimage;
    }

    private SpecialGlyphs(final Supplier<NativeImage> p_232604_) {
        this.image = p_232604_.get();
    }

    @Override
    public float getAdvance() {
        return (float)(this.image.getWidth() + 1);
    }

    @Override
    public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> p_232616_) {
        return p_232616_.apply(new SheetGlyphInfo() {
            @Override
            public int getPixelWidth() {
                return SpecialGlyphs.this.image.getWidth();
            }

            @Override
            public int getPixelHeight() {
                return SpecialGlyphs.this.image.getHeight();
            }

            @Override
            public float getOversample() {
                return 1.0F;
            }

            @Override
            public void upload(int p_232629_, int p_232630_) {
                SpecialGlyphs.this.image.upload(0, p_232629_, p_232630_, false);
            }

            @Override
            public boolean isColored() {
                return true;
            }
        });
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface PixelProvider {
        int getColor(int p_232635_, int p_232636_);
    }
}