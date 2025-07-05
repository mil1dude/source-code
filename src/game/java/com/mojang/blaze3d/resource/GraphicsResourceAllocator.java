package com.mojang.blaze3d.resource;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GraphicsResourceAllocator {
    GraphicsResourceAllocator UNPOOLED = new GraphicsResourceAllocator() {
        @Override
        public <T> T acquire(ResourceDescriptor<T> p_369530_) {
            return p_369530_.allocate();
        }

        @Override
        public <T> void release(ResourceDescriptor<T> p_367442_, T p_361994_) {
            p_367442_.free(p_361994_);
        }
    };

    <T> T acquire(ResourceDescriptor<T> p_362950_);

    <T> void release(ResourceDescriptor<T> p_368482_, T p_369841_);
}