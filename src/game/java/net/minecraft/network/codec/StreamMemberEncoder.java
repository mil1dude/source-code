package net.minecraft.network.codec;

@FunctionalInterface
public interface StreamMemberEncoder<O, T> {
    void encode(T p_331320_, O p_329137_);
}