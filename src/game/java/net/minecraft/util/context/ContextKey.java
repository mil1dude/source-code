package net.minecraft.util.context;

import net.minecraft.resources.ResourceLocation;

public class ContextKey<T> {
    private final ResourceLocation name;

    public ContextKey(ResourceLocation p_369113_) {
        this.name = p_369113_;
    }

    public static <T> ContextKey<T> vanilla(String p_369920_) {
        return new ContextKey<>(ResourceLocation.withDefaultNamespace(p_369920_));
    }

    public ResourceLocation name() {
        return this.name;
    }

    @Override
    public String toString() {
        return "<parameter " + this.name + ">";
    }
}