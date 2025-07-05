package net.minecraft.network.codec;

@FunctionalInterface
public interface StreamDecoder<I, T> {
    T decode(I p_330042_);
}