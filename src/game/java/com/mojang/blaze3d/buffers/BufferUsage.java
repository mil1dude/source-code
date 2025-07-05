package com.mojang.blaze3d.buffers;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum BufferUsage {
    DYNAMIC_WRITE(35048, false, true),
    STATIC_WRITE(35044, false, true),
    STREAM_WRITE(35040, false, true),
    STATIC_READ(35045, true, false),
    DYNAMIC_READ(35049, true, false),
    STREAM_READ(35041, true, false),
    DYNAMIC_COPY(35050, false, false),
    STATIC_COPY(35046, false, false),
    STREAM_COPY(35042, false, false);

    final int id;
    final boolean readable;
    final boolean writable;

    private BufferUsage(final int p_363086_, final boolean p_364371_, final boolean p_369858_) {
        this.id = p_363086_;
        this.readable = p_364371_;
        this.writable = p_369858_;
    }
}