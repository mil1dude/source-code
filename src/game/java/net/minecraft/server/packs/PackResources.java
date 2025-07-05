package net.minecraft.server.packs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;

public interface PackResources extends AutoCloseable {
    String METADATA_EXTENSION = ".mcmeta";
    String PACK_META = "pack.mcmeta";

    @Nullable
    IoSupplier<InputStream> getRootResource(String... p_252049_);

    @Nullable
    IoSupplier<InputStream> getResource(PackType p_215339_, ResourceLocation p_249034_);

    void listResources(PackType p_10289_, String p_251379_, String p_251932_, PackResources.ResourceOutput p_249347_);

    Set<String> getNamespaces(PackType p_10283_);

    @Nullable
    <T> T getMetadataSection(MetadataSectionType<T> p_375641_) throws IOException;

    PackLocationInfo location();

    default String packId() {
        return this.location().id();
    }

    default Optional<KnownPack> knownPackInfo() {
        return this.location().knownPackInfo();
    }

    @Override
    void close();

    @FunctionalInterface
    public interface ResourceOutput extends BiConsumer<ResourceLocation, IoSupplier<InputStream>> {
    }
}