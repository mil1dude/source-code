package net.minecraft.core;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface WritableRegistry<T> extends Registry<T> {
    Holder.Reference<T> register(ResourceKey<T> p_256320_, T p_255978_, RegistrationInfo p_329112_);

    void bindTag(TagKey<T> p_364693_, List<Holder<T>> p_363533_);

    boolean isEmpty();

    HolderGetter<T> createRegistrationLookup();
}