package net.minecraft.server.packs.metadata;

import com.mojang.serialization.Codec;

public record MetadataSectionType<T>(String name, Codec<T> codec) {
}