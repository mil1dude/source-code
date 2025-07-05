package net.minecraft.client.renderer.texture;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SpriteLoader {
    public static final Set<MetadataSectionType<?>> DEFAULT_METADATA_SECTIONS = Set.of(AnimationMetadataSection.TYPE);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation location;
    private final int maxSupportedTextureSize;
    private final int minWidth;
    private final int minHeight;

    public SpriteLoader(ResourceLocation p_276126_, int p_276121_, int p_276110_, int p_276114_) {
        this.location = p_276126_;
        this.maxSupportedTextureSize = p_276121_;
        this.minWidth = p_276110_;
        this.minHeight = p_276114_;
    }

    public static SpriteLoader create(TextureAtlas p_249085_) {
        return new SpriteLoader(p_249085_.location(), p_249085_.maxSupportedTextureSize(), p_249085_.getWidth(), p_249085_.getHeight());
    }

    public SpriteLoader.Preparations stitch(List<SpriteContents> p_262029_, int p_261919_, Executor p_261665_) {
        int i = this.maxSupportedTextureSize;
        Stitcher<SpriteContents> stitcher = new Stitcher<>(i, i, p_261919_);
        int j = Integer.MAX_VALUE;
        int k = 1 << p_261919_;

        for (SpriteContents spritecontents : p_262029_) {
            j = Math.min(j, Math.min(spritecontents.width(), spritecontents.height()));
            int l = Math.min(Integer.lowestOneBit(spritecontents.width()), Integer.lowestOneBit(spritecontents.height()));
            if (l < k) {
                LOGGER.warn(
                    "Texture {} with size {}x{} limits mip level from {} to {}",
                    spritecontents.name(),
                    spritecontents.width(),
                    spritecontents.height(),
                    Mth.log2(k),
                    Mth.log2(l)
                );
                k = l;
            }

            stitcher.registerSprite(spritecontents);
        }

        int j1 = Math.min(j, k);
        int k1 = Mth.log2(j1);
        int l1;
        if (k1 < p_261919_) {
            LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, p_261919_, k1, j1);
            l1 = k1;
        } else {
            l1 = p_261919_;
        }

        try {
            stitcher.stitch();
        } catch (StitcherException stitcherexception) {
            CrashReport crashreport = CrashReport.forThrowable(stitcherexception, "Stitching");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Stitcher");
            crashreportcategory.setDetail(
                "Sprites",
                stitcherexception.getAllSprites()
                    .stream()
                    .map(p_249576_ -> String.format(Locale.ROOT, "%s[%dx%d]", p_249576_.name(), p_249576_.width(), p_249576_.height()))
                    .collect(Collectors.joining(","))
            );
            crashreportcategory.setDetail("Max Texture Size", i);
            throw new ReportedException(crashreport);
        }

        int i1 = Math.max(stitcher.getWidth(), this.minWidth);
        int i2 = Math.max(stitcher.getHeight(), this.minHeight);
        Map<ResourceLocation, TextureAtlasSprite> map = this.getStitchedSprites(stitcher, i1, i2);
        TextureAtlasSprite textureatlassprite = map.get(MissingTextureAtlasSprite.getLocation());
        CompletableFuture<Void> completablefuture;
        if (l1 > 0) {
            completablefuture = CompletableFuture.runAsync(() -> map.values().forEach(p_251202_ -> p_251202_.contents().increaseMipLevel(l1)), p_261665_);
        } else {
            completablefuture = CompletableFuture.completedFuture(null);
        }

        return new SpriteLoader.Preparations(i1, i2, l1, textureatlassprite, map, completablefuture);
    }

    public static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(
        SpriteResourceLoader p_297457_, List<Function<SpriteResourceLoader, SpriteContents>> p_261516_, Executor p_261791_
    ) {
        List<CompletableFuture<SpriteContents>> list = p_261516_.stream()
            .map(p_296294_ -> CompletableFuture.supplyAsync(() -> (SpriteContents)p_296294_.apply(p_297457_), p_261791_))
            .toList();
        return Util.sequence(list).thenApply(p_252234_ -> p_252234_.stream().filter(Objects::nonNull).toList());
    }

    public CompletableFuture<SpriteLoader.Preparations> loadAndStitch(ResourceManager p_262108_, ResourceLocation p_261754_, int p_262104_, Executor p_261687_) {
        return this.loadAndStitch(p_262108_, p_261754_, p_262104_, p_261687_, DEFAULT_METADATA_SECTIONS);
    }

    public CompletableFuture<SpriteLoader.Preparations> loadAndStitch(
        ResourceManager p_300124_, ResourceLocation p_301196_, int p_300787_, Executor p_300950_, Collection<MetadataSectionType<?>> p_298047_
    ) {
        SpriteResourceLoader spriteresourceloader = SpriteResourceLoader.create(p_298047_);
        return CompletableFuture.<List<Function<SpriteResourceLoader, SpriteContents>>>supplyAsync(
                () -> SpriteSourceList.load(p_300124_, p_301196_).list(p_300124_), p_300950_
            )
            .thenCompose(p_296297_ -> runSpriteSuppliers(spriteresourceloader, (List<Function<SpriteResourceLoader, SpriteContents>>)p_296297_, p_300950_))
            .thenApply(p_261393_ -> this.stitch((List<SpriteContents>)p_261393_, p_300787_, p_300950_));
    }

    private Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> p_276117_, int p_276111_, int p_276112_) {
        Map<ResourceLocation, TextureAtlasSprite> map = new HashMap<>();
        p_276117_.gatherSprites(
            (p_251421_, p_250533_, p_251913_) -> map.put(
                    p_251421_.name(), new TextureAtlasSprite(this.location, p_251421_, p_276111_, p_276112_, p_250533_, p_251913_)
                )
        );
        return map;
    }

    @OnlyIn(Dist.CLIENT)
    public static record Preparations(
        int width,
        int height,
        int mipLevel,
        TextureAtlasSprite missing,
        Map<ResourceLocation, TextureAtlasSprite> regions,
        CompletableFuture<Void> readyForUpload
    ) {
        public CompletableFuture<SpriteLoader.Preparations> waitForUpload() {
            return this.readyForUpload.thenApply(p_249056_ -> this);
        }
    }
}