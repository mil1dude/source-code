package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SkinManager {
    static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftSessionService sessionService;
    private final LoadingCache<SkinManager.CacheKey, CompletableFuture<Optional<PlayerSkin>>> skinCache;
    private final SkinManager.TextureCache skinTextures;
    private final SkinManager.TextureCache capeTextures;
    private final SkinManager.TextureCache elytraTextures;

    public SkinManager(Path p_299617_, final MinecraftSessionService p_118814_, final Executor p_299732_) {
        this.sessionService = p_118814_;
        this.skinTextures = new SkinManager.TextureCache(p_299617_, Type.SKIN);
        this.capeTextures = new SkinManager.TextureCache(p_299617_, Type.CAPE);
        this.elytraTextures = new SkinManager.TextureCache(p_299617_, Type.ELYTRA);
        this.skinCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(15L))
            .build(
                new CacheLoader<SkinManager.CacheKey, CompletableFuture<Optional<PlayerSkin>>>() {
                    public CompletableFuture<Optional<PlayerSkin>> load(SkinManager.CacheKey p_298169_) {
                        return CompletableFuture.<MinecraftProfileTextures>supplyAsync(() -> {
                                Property property = p_298169_.packedTextures();
                                if (property == null) {
                                    return MinecraftProfileTextures.EMPTY;
                                } else {
                                    MinecraftProfileTextures minecraftprofiletextures = p_118814_.unpackTextures(property);
                                    if (minecraftprofiletextures.signatureState() == SignatureState.INVALID) {
                                        SkinManager.LOGGER
                                            .warn("Profile contained invalid signature for textures property (profile id: {})", p_298169_.profileId());
                                    }

                                    return minecraftprofiletextures;
                                }
                            }, Util.backgroundExecutor().forName("unpackSkinTextures"))
                            .thenComposeAsync(p_308313_ -> SkinManager.this.registerTextures(p_298169_.profileId(), p_308313_), p_299732_)
                            .handle((p_374684_, p_374685_) -> {
                                if (p_374685_ != null) {
                                    SkinManager.LOGGER.warn("Failed to load texture for profile {}", p_298169_.profileId, p_374685_);
                                }

                                return Optional.ofNullable(p_374684_);
                            });
                    }
                }
            );
    }

    public Supplier<PlayerSkin> lookupInsecure(GameProfile p_298295_) {
        CompletableFuture<Optional<PlayerSkin>> completablefuture = this.getOrLoad(p_298295_);
        PlayerSkin playerskin = DefaultPlayerSkin.get(p_298295_);
        return () -> completablefuture.getNow(Optional.empty()).orElse(playerskin);
    }

    public PlayerSkin getInsecureSkin(GameProfile p_298019_) {
        PlayerSkin playerskin = this.getOrLoad(p_298019_).getNow(Optional.empty()).orElse(null);
        return playerskin != null ? playerskin : DefaultPlayerSkin.get(p_298019_);
    }

    public CompletableFuture<Optional<PlayerSkin>> getOrLoad(GameProfile p_298661_) {
        Property property = this.sessionService.getPackedTextures(p_298661_);
        return this.skinCache.getUnchecked(new SkinManager.CacheKey(p_298661_.getId(), property));
    }

    CompletableFuture<PlayerSkin> registerTextures(UUID p_312099_, MinecraftProfileTextures p_313047_) {
        MinecraftProfileTexture minecraftprofiletexture = p_313047_.skin();
        CompletableFuture<ResourceLocation> completablefuture;
        PlayerSkin.Model playerskin$model;
        if (minecraftprofiletexture != null) {
            completablefuture = this.skinTextures.getOrLoad(minecraftprofiletexture);
            playerskin$model = PlayerSkin.Model.byName(minecraftprofiletexture.getMetadata("model"));
        } else {
            PlayerSkin playerskin = DefaultPlayerSkin.get(p_312099_);
            completablefuture = CompletableFuture.completedFuture(playerskin.texture());
            playerskin$model = playerskin.model();
        }

        String s = Optionull.map(minecraftprofiletexture, MinecraftProfileTexture::getUrl);
        MinecraftProfileTexture minecraftprofiletexture1 = p_313047_.cape();
        CompletableFuture<ResourceLocation> completablefuture1 = minecraftprofiletexture1 != null
            ? this.capeTextures.getOrLoad(minecraftprofiletexture1)
            : CompletableFuture.completedFuture(null);
        MinecraftProfileTexture minecraftprofiletexture2 = p_313047_.elytra();
        CompletableFuture<ResourceLocation> completablefuture2 = minecraftprofiletexture2 != null
            ? this.elytraTextures.getOrLoad(minecraftprofiletexture2)
            : CompletableFuture.completedFuture(null);
        return CompletableFuture.allOf(completablefuture, completablefuture1, completablefuture2)
            .thenApply(
                p_308309_ -> new PlayerSkin(
                        completablefuture.join(),
                        s,
                        completablefuture1.join(),
                        completablefuture2.join(),
                        playerskin$model,
                        p_313047_.signatureState() == SignatureState.SIGNED
                    )
            );
    }

    @OnlyIn(Dist.CLIENT)
    static record CacheKey(UUID profileId, @Nullable Property packedTextures) {
    }

    @OnlyIn(Dist.CLIENT)
    static class TextureCache {
        private final Path root;
        private final Type type;
        private final Map<String, CompletableFuture<ResourceLocation>> textures = new Object2ObjectOpenHashMap<>();

        TextureCache(Path p_297921_, Type p_298775_) {
            this.root = p_297921_;
            this.type = p_298775_;
        }

        public CompletableFuture<ResourceLocation> getOrLoad(MinecraftProfileTexture p_300959_) {
            String s = p_300959_.getHash();
            CompletableFuture<ResourceLocation> completablefuture = this.textures.get(s);
            if (completablefuture == null) {
                completablefuture = this.registerTexture(p_300959_);
                this.textures.put(s, completablefuture);
            }

            return completablefuture;
        }

        private CompletableFuture<ResourceLocation> registerTexture(MinecraftProfileTexture p_300607_) {
            String s = Hashing.sha1().hashUnencodedChars(p_300607_.getHash()).toString();
            ResourceLocation resourcelocation = this.getTextureLocation(s);
            Path path = this.root.resolve(s.length() > 2 ? s.substring(0, 2) : "xx").resolve(s);
            return SkinTextureDownloader.downloadAndRegisterSkin(resourcelocation, path, p_300607_.getUrl(), this.type == Type.SKIN);
        }

        private ResourceLocation getTextureLocation(String p_297392_) {
            String s = switch (this.type) {
                case SKIN -> "skins";
                case CAPE -> "capes";
                case ELYTRA -> "elytra";
            };
            return ResourceLocation.withDefaultNamespace(s + "/" + p_297392_);
        }
    }
}