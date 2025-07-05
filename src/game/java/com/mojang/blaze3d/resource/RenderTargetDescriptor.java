package com.mojang.blaze3d.resource;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record RenderTargetDescriptor(int width, int height, boolean useDepth) implements ResourceDescriptor<RenderTarget> {
    public RenderTarget allocate() {
        return new TextureTarget(this.width, this.height, this.useDepth);
    }

    public void free(RenderTarget p_362881_) {
        p_362881_.destroyBuffers();
    }
}