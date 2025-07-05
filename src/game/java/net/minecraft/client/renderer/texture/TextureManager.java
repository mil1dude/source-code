package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.screens.AddRealmPopupScreen;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureManager implements PreparableReloadListener, Tickable, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = ResourceLocation.withDefaultNamespace("");
    private final Map<ResourceLocation, AbstractTexture> byPath = new HashMap<>();
    private final Set<Tickable> tickableTextures = new HashSet<>();
    private final ResourceManager resourceManager;

    public TextureManager(ResourceManager p_118474_) {
        this.resourceManager = p_118474_;
        NativeImage nativeimage = MissingTextureAtlasSprite.generateMissingImage();
        this.register(MissingTextureAtlasSprite.getLocation(), new DynamicTexture(nativeimage));
    }

    public void registerAndLoad(ResourceLocation p_377323_, ReloadableTexture p_376843_) {
        try {
            p_376843_.apply(this.loadContentsSafe(p_377323_, p_376843_));
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Uploading texture");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Uploaded texture");
            crashreportcategory.setDetail("Resource location", p_376843_.resourceId());
            crashreportcategory.setDetail("Texture id", p_377323_);
            throw new ReportedException(crashreport);
        }

        this.register(p_377323_, p_376843_);
    }

    private TextureContents loadContentsSafe(ResourceLocation p_378160_, ReloadableTexture p_378623_) {
        try {
            return loadContents(this.resourceManager, p_378160_, p_378623_);
        } catch (Exception exception) {
            LOGGER.error("Failed to load texture {} into slot {}", p_378623_.resourceId(), p_378160_, exception);
            return TextureContents.createMissing();
        }
    }

    public void registerForNextReload(ResourceLocation p_377796_) {
        this.register(p_377796_, new SimpleTexture(p_377796_));
    }

    public void register(ResourceLocation p_118496_, AbstractTexture p_118497_) {
        AbstractTexture abstracttexture = this.byPath.put(p_118496_, p_118497_);
        if (abstracttexture != p_118497_) {
            if (abstracttexture != null) {
                this.safeClose(p_118496_, abstracttexture);
            }

            if (p_118497_ instanceof Tickable tickable) {
                this.tickableTextures.add(tickable);
            }
        }
    }

    private void safeClose(ResourceLocation p_118509_, AbstractTexture p_118510_) {
        this.tickableTextures.remove(p_118510_);

        try {
            p_118510_.close();
        } catch (Exception exception) {
            LOGGER.warn("Failed to close texture {}", p_118509_, exception);
        }

        p_118510_.releaseId();
    }

    public AbstractTexture getTexture(ResourceLocation p_118507_) {
        AbstractTexture abstracttexture = this.byPath.get(p_118507_);
        if (abstracttexture != null) {
            return abstracttexture;
        } else {
            SimpleTexture simpletexture = new SimpleTexture(p_118507_);
            this.registerAndLoad(p_118507_, simpletexture);
            return simpletexture;
        }
    }

    @Override
    public void tick() {
        for (Tickable tickable : this.tickableTextures) {
            tickable.tick();
        }
    }

    public void release(ResourceLocation p_118514_) {
        AbstractTexture abstracttexture = this.byPath.remove(p_118514_);
        if (abstracttexture != null) {
            this.safeClose(p_118514_, abstracttexture);
        }
    }

    @Override
    public void close() {
        this.byPath.forEach(this::safeClose);
        this.byPath.clear();
        this.tickableTextures.clear();
    }

    @Override
    public CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier p_118476_, ResourceManager p_118477_, Executor p_118480_, Executor p_118481_
    ) {
        List<TextureManager.PendingReload> list = new ArrayList<>();
        this.byPath.forEach((p_374670_, p_374671_) -> {
            if (p_374671_ instanceof ReloadableTexture reloadabletexture) {
                list.add(scheduleLoad(p_118477_, p_374670_, reloadabletexture, p_118480_));
            }
        });
        return CompletableFuture.allOf(list.stream().map(TextureManager.PendingReload::newContents).toArray(CompletableFuture[]::new))
            .thenCompose(p_118476_::wait)
            .thenAcceptAsync(p_374677_ -> {
                AddRealmPopupScreen.updateCarouselImages(this.resourceManager);

                for (TextureManager.PendingReload texturemanager$pendingreload : list) {
                    texturemanager$pendingreload.texture.apply(texturemanager$pendingreload.newContents.join());
                }
            }, p_118481_);
    }

    public void dumpAllSheets(Path p_276129_) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this._dumpAllSheets(p_276129_));
        } else {
            this._dumpAllSheets(p_276129_);
        }
    }

    private void _dumpAllSheets(Path p_276128_) {
        try {
            Files.createDirectories(p_276128_);
        } catch (IOException ioexception) {
            LOGGER.error("Failed to create directory {}", p_276128_, ioexception);
            return;
        }

        this.byPath.forEach((p_276101_, p_276102_) -> {
            if (p_276102_ instanceof Dumpable dumpable) {
                try {
                    dumpable.dumpContents(p_276101_, p_276128_);
                } catch (IOException ioexception1) {
                    LOGGER.error("Failed to dump texture {}", p_276101_, ioexception1);
                }
            }
        });
    }

    private static TextureContents loadContents(ResourceManager p_375654_, ResourceLocation p_378136_, ReloadableTexture p_377917_) throws IOException {
        try {
            return p_377917_.loadContents(p_375654_);
        } catch (FileNotFoundException filenotfoundexception) {
            if (p_378136_ != INTENTIONAL_MISSING_TEXTURE) {
                LOGGER.warn("Missing resource {} referenced from {}", p_377917_.resourceId(), p_378136_);
            }

            return TextureContents.createMissing();
        }
    }

    private static TextureManager.PendingReload scheduleLoad(
        ResourceManager p_377119_, ResourceLocation p_377352_, ReloadableTexture p_377978_, Executor p_376135_
    ) {
        return new TextureManager.PendingReload(p_377978_, CompletableFuture.supplyAsync(() -> {
            try {
                return loadContents(p_377119_, p_377352_, p_377978_);
            } catch (IOException ioexception) {
                throw new UncheckedIOException(ioexception);
            }
        }, p_376135_));
    }

    @OnlyIn(Dist.CLIENT)
    static record PendingReload(ReloadableTexture texture, CompletableFuture<TextureContents> newContents) {
    }
}