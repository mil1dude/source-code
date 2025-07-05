package net.minecraft.client.resources;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TextureAtlasHolder implements PreparableReloadListener, AutoCloseable {
    private final TextureAtlas textureAtlas;
    private final ResourceLocation atlasInfoLocation;
    private final Set<MetadataSectionType<?>> metadataSections;

    public TextureAtlasHolder(TextureManager p_262057_, ResourceLocation p_261554_, ResourceLocation p_262147_) {
        this(p_262057_, p_261554_, p_262147_, SpriteLoader.DEFAULT_METADATA_SECTIONS);
    }

    public TextureAtlasHolder(TextureManager p_299844_, ResourceLocation p_299366_, ResourceLocation p_297647_, Set<MetadataSectionType<?>> p_298542_) {
        this.atlasInfoLocation = p_297647_;
        this.textureAtlas = new TextureAtlas(p_299366_);
        p_299844_.register(this.textureAtlas.location(), this.textureAtlas);
        this.metadataSections = p_298542_;
    }

    protected TextureAtlasSprite getSprite(ResourceLocation p_118902_) {
        return this.textureAtlas.getSprite(p_118902_);
    }

    @Override
    public final CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier p_249641_, ResourceManager p_250036_, Executor p_249427_, Executor p_250510_
    ) {
        return SpriteLoader.create(this.textureAtlas)
            .loadAndStitch(p_250036_, this.atlasInfoLocation, 0, p_249427_, this.metadataSections)
            .thenCompose(SpriteLoader.Preparations::waitForUpload)
            .thenCompose(p_249641_::wait)
            .thenAcceptAsync(this::apply, p_250510_);
    }

    private void apply(SpriteLoader.Preparations p_252333_) {
        try (Zone zone = Profiler.get().zone("upload")) {
            this.textureAtlas.upload(p_252333_);
        }
    }

    @Override
    public void close() {
        this.textureAtlas.clearTextureData();
    }
}