package com.mojang.blaze3d;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.jtracy.TracyClient;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TracyFrameCapture implements AutoCloseable {
    private static final int MAX_WIDTH = 320;
    private static final int MAX_HEIGHT = 180;
    private static final int BYTES_PER_PIXEL = 4;
    private int targetWidth;
    private int targetHeight;
    private int width;
    private int height;
    private final RenderTarget frameBuffer = new TextureTarget(320, 180, false);
    private final GpuBuffer pixelbuffer = new GpuBuffer(BufferType.PIXEL_PACK, BufferUsage.STREAM_READ, 0);
    @Nullable
    private GpuFence fence;
    private int lastCaptureDelay;
    private boolean capturedThisFrame;

    private void resize(int p_361808_, int p_365044_) {
        float f = (float)p_361808_ / (float)p_365044_;
        if (p_361808_ > 320) {
            p_361808_ = 320;
            p_365044_ = (int)(320.0F / f);
        }

        if (p_365044_ > 180) {
            p_361808_ = (int)(180.0F * f);
            p_365044_ = 180;
        }

        p_361808_ = p_361808_ / 4 * 4;
        p_365044_ = p_365044_ / 4 * 4;
        if (this.width != p_361808_ || this.height != p_365044_) {
            this.width = p_361808_;
            this.height = p_365044_;
            this.frameBuffer.resize(p_361808_, p_365044_);
            this.pixelbuffer.resize(p_361808_ * p_365044_ * 4);
            if (this.fence != null) {
                this.fence.close();
                this.fence = null;
            }
        }
    }

    public void capture(RenderTarget p_367460_) {
        if (this.fence == null && !this.capturedThisFrame) {
            this.capturedThisFrame = true;
            if (p_367460_.width != this.targetWidth || p_367460_.height != this.targetHeight) {
                this.targetWidth = p_367460_.width;
                this.targetHeight = p_367460_.height;
                this.resize(this.targetWidth, this.targetHeight);
            }

            GlStateManager._glBindFramebuffer(36009, this.frameBuffer.frameBufferId);
            GlStateManager._glBindFramebuffer(36008, p_367460_.frameBufferId);
            GlStateManager._glBlitFrameBuffer(0, 0, p_367460_.width, p_367460_.height, 0, 0, this.width, this.height, 16384, 9729);
            GlStateManager._glBindFramebuffer(36008, 0);
            GlStateManager._glBindFramebuffer(36009, 0);
            this.pixelbuffer.bind();
            GlStateManager._glBindFramebuffer(36008, this.frameBuffer.frameBufferId);
            GlStateManager._readPixels(0, 0, this.width, this.height, 6408, 5121, 0L);
            GlStateManager._glBindFramebuffer(36008, 0);
            this.fence = new GpuFence();
            this.lastCaptureDelay = 0;
        }
    }

    public void upload() {
        if (this.fence != null) {
            if (this.fence.awaitCompletion(0L)) {
                this.fence = null;

                try (GpuBuffer.ReadView gpubuffer$readview = this.pixelbuffer.read()) {
                    if (gpubuffer$readview != null) {
                        TracyClient.frameImage(gpubuffer$readview.data(), this.width, this.height, this.lastCaptureDelay, true);
                    }
                }
            }
        }
    }

    public void endFrame() {
        this.lastCaptureDelay++;
        this.capturedThisFrame = false;
        TracyClient.markFrame();
    }

    @Override
    public void close() {
        if (this.fence != null) {
            this.fence.close();
            this.fence = null;
        }

        this.pixelbuffer.close();
        this.frameBuffer.destroyBuffers();
    }
}