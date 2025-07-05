package net.minecraft.network.codec;

@FunctionalInterface
public interface StreamEncoder<O, T> {
    void encode(O p_329563_, T p_334765_);
}