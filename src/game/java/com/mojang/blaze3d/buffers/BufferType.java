package com.mojang.blaze3d.buffers;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum BufferType {
    VERTICES(34962),
    INDICES(34963),
    PIXEL_PACK(35051),
    COPY_READ(36662),
    COPY_WRITE(36663),
    PIXEL_UNPACK(35052),
    UNIFORM(35345);

    final int id;

    private BufferType(final int p_362116_) {
        this.id = p_362116_;
    }
}