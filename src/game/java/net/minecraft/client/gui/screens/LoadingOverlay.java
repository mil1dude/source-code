package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoadingOverlay extends Overlay {
    public static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/title/mojangstudios.png");
    private static final int LOGO_BACKGROUND_COLOR = ARGB.color(255, 239, 50, 61);
    private static final int LOGO_BACKGROUND_COLOR_DARK = ARGB.color(255, 0, 0, 0);
    private static final IntSupplier BRAND_BACKGROUND = () -> Minecraft.getInstance().options.darkMojangStudiosBackground().get() ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
    private static final int LOGO_SCALE = 240;
    private static final float LOGO_QUARTER_FLOAT = 60.0F;
    private static final int LOGO_QUARTER = 60;
    private static final int LOGO_HALF = 120;
    private static final float LOGO_OVERLAP = 0.0625F;
    private static final float SMOOTHING = 0.95F;
    public static final long FADE_OUT_TIME = 1000L;
    public static final long FADE_IN_TIME = 500L;
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final boolean fadeIn;
    private float currentProgress;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;

    public LoadingOverlay(Minecraft p_96172_, ReloadInstance p_96173_, Consumer<Optional<Throwable>> p_96174_, boolean p_96175_) {
        this.minecraft = p_96172_;
        this.reload = p_96173_;
        this.onFinish = p_96174_;
        this.fadeIn = p_96175_;
    }

    public static void registerTextures(TextureManager p_377842_) {
        p_377842_.registerAndLoad(MOJANG_STUDIOS_LOGO_LOCATION, new LoadingOverlay.LogoTexture());
    }

    private static int replaceAlpha(int p_169325_, int p_169326_) {
        return p_169325_ & 16777215 | p_169326_ << 24;
    }

    @Override
    public void render(GuiGraphics p_281839_, int p_282704_, int p_283650_, float p_283394_) {
        int i = p_281839_.guiWidth();
        int j = p_281839_.guiHeight();
        long k = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = k;
        }

        float f = this.fadeOutStart > -1L ? (float)(k - this.fadeOutStart) / 1000.0F : -1.0F;
        float f1 = this.fadeInStart > -1L ? (float)(k - this.fadeInStart) / 500.0F : -1.0F;
        float f2;
        if (f >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(p_281839_, 0, 0, p_283394_);
            }

            int l = Mth.ceil((1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            p_281839_.fill(RenderType.guiOverlay(), 0, 0, i, j, replaceAlpha(BRAND_BACKGROUND.getAsInt(), l));
            f2 = 1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && f1 < 1.0F) {
                this.minecraft.screen.render(p_281839_, p_282704_, p_283650_, p_283394_);
            }

            int i2 = Mth.ceil(Mth.clamp((double)f1, 0.15, 1.0) * 255.0);
            p_281839_.fill(RenderType.guiOverlay(), 0, 0, i, j, replaceAlpha(BRAND_BACKGROUND.getAsInt(), i2));
            f2 = Mth.clamp(f1, 0.0F, 1.0F);
        } else {
            int j2 = BRAND_BACKGROUND.getAsInt();
            float f3 = (float)(j2 >> 16 & 0xFF) / 255.0F;
            float f4 = (float)(j2 >> 8 & 0xFF) / 255.0F;
            float f5 = (float)(j2 & 0xFF) / 255.0F;
            GlStateManager._clearColor(f3, f4, f5, 1.0F);
            GlStateManager._clear(16384);
            f2 = 1.0F;
        }

        int k2 = (int)((double)p_281839_.guiWidth() * 0.5);
        int l2 = (int)((double)p_281839_.guiHeight() * 0.5);
        double d1 = Math.min((double)p_281839_.guiWidth() * 0.75, (double)p_281839_.guiHeight()) * 0.25;
        int i1 = (int)(d1 * 0.5);
        double d0 = d1 * 4.0;
        int j1 = (int)(d0 * 0.5);
        int k1 = ARGB.white(f2);
        p_281839_.blit(p_357672_ -> RenderType.mojangLogo(), MOJANG_STUDIOS_LOGO_LOCATION, k2 - j1, l2 - i1, -0.0625F, 0.0F, j1, (int)d1, 120, 60, 120, 120, k1);
        p_281839_.blit(p_357673_ -> RenderType.mojangLogo(), MOJANG_STUDIOS_LOGO_LOCATION, k2, l2 - i1, 0.0625F, 60.0F, j1, (int)d1, 120, 60, 120, 120, k1);
        int l1 = (int)((double)p_281839_.guiHeight() * 0.8325);
        float f6 = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + f6 * 0.050000012F, 0.0F, 1.0F);
        if (f < 1.0F) {
            this.drawProgressBar(p_281839_, i / 2 - j1, l1 - 5, i / 2 + j1, l1 + 5, 1.0F - Mth.clamp(f, 0.0F, 1.0F));
        }

        if (f >= 2.0F) {
            this.minecraft.setOverlay(null);
        }

        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || f1 >= 2.0F)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }

            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, p_281839_.guiWidth(), p_281839_.guiHeight());
            }
        }
    }

    private void drawProgressBar(GuiGraphics p_283125_, int p_96184_, int p_96185_, int p_96186_, int p_96187_, float p_96188_) {
        int i = Mth.ceil((float)(p_96186_ - p_96184_ - 2) * this.currentProgress);
        int j = Math.round(p_96188_ * 255.0F);
        int k = ARGB.color(j, 255, 255, 255);
        p_283125_.fill(p_96184_ + 2, p_96185_ + 2, p_96184_ + i, p_96187_ - 2, k);
        p_283125_.fill(p_96184_ + 1, p_96185_, p_96186_ - 1, p_96185_ + 1, k);
        p_283125_.fill(p_96184_ + 1, p_96187_, p_96186_ - 1, p_96187_ - 1, k);
        p_283125_.fill(p_96184_, p_96185_, p_96184_ + 1, p_96187_, k);
        p_283125_.fill(p_96186_, p_96185_, p_96186_ - 1, p_96187_, k);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    static class LogoTexture extends ReloadableTexture {
        public LogoTexture() {
            super(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
        }

        @Override
        public TextureContents loadContents(ResourceManager p_376459_) throws IOException {
            ResourceProvider resourceprovider = Minecraft.getInstance().getVanillaPackResources().asProvider();

            TextureContents texturecontents;
            try (InputStream inputstream = resourceprovider.open(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION)) {
                texturecontents = new TextureContents(NativeImage.read(inputstream), new TextureMetadataSection(true, true));
            }

            return texturecontents;
        }
    }
}