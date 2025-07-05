package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SkinTextureDownloader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SKIN_WIDTH = 64;
    private static final int SKIN_HEIGHT = 64;
    private static final int LEGACY_SKIN_HEIGHT = 32;

    public static CompletableFuture<ResourceLocation> downloadAndRegisterSkin(ResourceLocation p_375945_, Path p_376675_, String p_377957_, boolean p_377468_) {
        return CompletableFuture.<NativeImage>supplyAsync(() -> {
            NativeImage nativeimage;
            try {
                nativeimage = downloadSkin(p_376675_, p_377957_);
            } catch (IOException ioexception) {
                throw new UncheckedIOException(ioexception);
            }

            return p_377468_ ? processLegacySkin(nativeimage, p_377957_) : nativeimage;
        }, Util.nonCriticalIoPool().forName("downloadTexture")).thenCompose(p_378652_ -> registerTextureInManager(p_375945_, p_378652_));
    }

    private static NativeImage downloadSkin(Path p_376608_, String p_377291_) throws IOException {
        if (Files.isRegularFile(p_376608_)) {
            LOGGER.debug("Loading HTTP texture from local cache ({})", p_376608_);

            NativeImage nativeimage1;
            try (InputStream inputstream = Files.newInputStream(p_376608_)) {
                nativeimage1 = NativeImage.read(inputstream);
            }

            return nativeimage1;
        } else {
            HttpURLConnection httpurlconnection = null;
            LOGGER.debug("Downloading HTTP texture from {} to {}", p_377291_, p_376608_);
            URI uri = URI.create(p_377291_);

            NativeImage $$7;
            try {
                httpurlconnection = (HttpURLConnection)uri.toURL().openConnection(Minecraft.getInstance().getProxy());
                httpurlconnection.setDoInput(true);
                httpurlconnection.setDoOutput(false);
                httpurlconnection.connect();
                int i = httpurlconnection.getResponseCode();
                if (i / 100 != 2) {
                    throw new IOException("Failed to open " + uri + ", HTTP error code: " + i);
                }

                byte[] abyte = httpurlconnection.getInputStream().readAllBytes();

                try {
                    FileUtil.createDirectoriesSafe(p_376608_.getParent());
                    Files.write(p_376608_, abyte);
                } catch (IOException ioexception) {
                    LOGGER.warn("Failed to cache texture {} in {}", p_377291_, p_376608_);
                }

                $$7 = NativeImage.read(abyte);
            } finally {
                if (httpurlconnection != null) {
                    httpurlconnection.disconnect();
                }
            }

            return $$7;
        }
    }

    private static CompletableFuture<ResourceLocation> registerTextureInManager(ResourceLocation p_376941_, NativeImage p_375665_) {
        Minecraft minecraft = Minecraft.getInstance();
        return CompletableFuture.supplyAsync(() -> {
            minecraft.getTextureManager().register(p_376941_, new DynamicTexture(p_375665_));
            return p_376941_;
        }, minecraft);
    }

    private static NativeImage processLegacySkin(NativeImage p_378771_, String p_376069_) {
        int i = p_378771_.getHeight();
        int j = p_378771_.getWidth();
        if (j == 64 && (i == 32 || i == 64)) {
            boolean flag = i == 32;
            if (flag) {
                NativeImage nativeimage = new NativeImage(64, 64, true);
                nativeimage.copyFrom(p_378771_);
                p_378771_.close();
                p_378771_ = nativeimage;
                nativeimage.fillRect(0, 32, 64, 32, 0);
                nativeimage.copyRect(4, 16, 16, 32, 4, 4, true, false);
                nativeimage.copyRect(8, 16, 16, 32, 4, 4, true, false);
                nativeimage.copyRect(0, 20, 24, 32, 4, 12, true, false);
                nativeimage.copyRect(4, 20, 16, 32, 4, 12, true, false);
                nativeimage.copyRect(8, 20, 8, 32, 4, 12, true, false);
                nativeimage.copyRect(12, 20, 16, 32, 4, 12, true, false);
                nativeimage.copyRect(44, 16, -8, 32, 4, 4, true, false);
                nativeimage.copyRect(48, 16, -8, 32, 4, 4, true, false);
                nativeimage.copyRect(40, 20, 0, 32, 4, 12, true, false);
                nativeimage.copyRect(44, 20, -8, 32, 4, 12, true, false);
                nativeimage.copyRect(48, 20, -16, 32, 4, 12, true, false);
                nativeimage.copyRect(52, 20, -8, 32, 4, 12, true, false);
            }

            setNoAlpha(p_378771_, 0, 0, 32, 16);
            if (flag) {
                doNotchTransparencyHack(p_378771_, 32, 0, 64, 32);
            }

            setNoAlpha(p_378771_, 0, 16, 64, 32);
            setNoAlpha(p_378771_, 16, 48, 48, 64);
            return p_378771_;
        } else {
            p_378771_.close();
            throw new IllegalStateException("Discarding incorrectly sized (" + j + "x" + i + ") skin texture from " + p_376069_);
        }
    }

    private static void doNotchTransparencyHack(NativeImage p_377150_, int p_376728_, int p_375728_, int p_375419_, int p_376007_) {
        for (int i = p_376728_; i < p_375419_; i++) {
            for (int j = p_375728_; j < p_376007_; j++) {
                int k = p_377150_.getPixel(i, j);
                if (ARGB.alpha(k) < 128) {
                    return;
                }
            }
        }

        for (int l = p_376728_; l < p_375419_; l++) {
            for (int i1 = p_375728_; i1 < p_376007_; i1++) {
                p_377150_.setPixel(l, i1, p_377150_.getPixel(l, i1) & 16777215);
            }
        }
    }

    private static void setNoAlpha(NativeImage p_378167_, int p_376154_, int p_377364_, int p_378176_, int p_376328_) {
        for (int i = p_376154_; i < p_378176_; i++) {
            for (int j = p_377364_; j < p_376328_; j++) {
                p_378167_.setPixel(i, j, ARGB.opaque(p_378167_.getPixel(i, j)));
            }
        }
    }
}