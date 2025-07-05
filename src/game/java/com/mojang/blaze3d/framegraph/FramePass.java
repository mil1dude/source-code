package com.mojang.blaze3d.framegraph;

import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface FramePass {
    <T> ResourceHandle<T> createsInternal(String p_361163_, ResourceDescriptor<T> p_364869_);

    <T> void reads(ResourceHandle<T> p_368457_);

    <T> ResourceHandle<T> readsAndWrites(ResourceHandle<T> p_363072_);

    void requires(FramePass p_362517_);

    void disableCulling();

    void executes(Runnable p_362191_);
}