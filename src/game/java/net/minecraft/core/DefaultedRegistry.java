package net.minecraft.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public interface DefaultedRegistry<T> extends Registry<T> {
    @Nonnull
    @Override
    ResourceLocation getKey(T p_122330_);

    @Nonnull
    @Override
    T getValue(@Nullable ResourceLocation p_122328_);

    @Nonnull
    @Override
    T byId(int p_122317_);

    ResourceLocation getDefaultKey();
}