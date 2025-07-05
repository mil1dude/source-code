package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.GsonHelper;

public interface ResourceMetadata {
    ResourceMetadata EMPTY = new ResourceMetadata() {
        @Override
        public <T> Optional<T> getSection(MetadataSectionType<T> p_376398_) {
            return Optional.empty();
        }
    };
    IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> EMPTY;

    static ResourceMetadata fromJsonStream(InputStream p_215581_) throws IOException {
        ResourceMetadata resourcemetadata;
        try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(p_215581_, StandardCharsets.UTF_8))) {
            final JsonObject jsonobject = GsonHelper.parse(bufferedreader);
            resourcemetadata = new ResourceMetadata() {
                @Override
                public <T> Optional<T> getSection(MetadataSectionType<T> p_377366_) {
                    String s = p_377366_.name();
                    if (jsonobject.has(s)) {
                        T t = p_377366_.codec().parse(JsonOps.INSTANCE, jsonobject.get(s)).getOrThrow(JsonParseException::new);
                        return Optional.of(t);
                    } else {
                        return Optional.empty();
                    }
                }
            };
        }

        return resourcemetadata;
    }

    <T> Optional<T> getSection(MetadataSectionType<T> p_376138_);

    default ResourceMetadata copySections(Collection<MetadataSectionType<?>> p_299820_) {
        ResourceMetadata.Builder resourcemetadata$builder = new ResourceMetadata.Builder();

        for (MetadataSectionType<?> metadatasectiontype : p_299820_) {
            this.copySection(resourcemetadata$builder, metadatasectiontype);
        }

        return resourcemetadata$builder.build();
    }

    private <T> void copySection(ResourceMetadata.Builder p_299159_, MetadataSectionType<T> p_375583_) {
        this.getSection(p_375583_).ifPresent(p_374889_ -> p_299159_.put(p_375583_, (T)p_374889_));
    }

    public static class Builder {
        private final ImmutableMap.Builder<MetadataSectionType<?>, Object> map = ImmutableMap.builder();

        public <T> ResourceMetadata.Builder put(MetadataSectionType<T> p_377511_, T p_298435_) {
            this.map.put(p_377511_, p_298435_);
            return this;
        }

        public ResourceMetadata build() {
            final ImmutableMap<MetadataSectionType<?>, Object> immutablemap = this.map.build();
            return immutablemap.isEmpty() ? ResourceMetadata.EMPTY : new ResourceMetadata() {
                @Override
                public <T> Optional<T> getSection(MetadataSectionType<T> p_376403_) {
                    return Optional.ofNullable((T)immutablemap.get(p_376403_));
                }
            };
        }
    }
}