package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RenderTarget {
    private static final int RED_CHANNEL = 0;
    private static final int GREEN_CHANNEL = 1;
    private static final int BLUE_CHANNEL = 2;
    private static final int ALPHA_CHANNEL = 3;
    public int width;
    public int height;
    public int viewWidth;
    public int viewHeight;
    public final boolean useDepth;
    public int frameBufferId;
    protected int colorTextureId;
    protected int depthBufferId;
    private final float[] clearChannels = Util.make(() -> new float[]{1.0F, 1.0F, 1.0F, 0.0F});
    public int filterMode;

    public RenderTarget(boolean p_166199_) {
        this.useDepth = p_166199_;
        this.frameBufferId = -1;
        this.colorTextureId = -1;
        this.depthBufferId = -1;
    }

    public void resize(int p_83942_, int p_83943_) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._enableDepthTest();
        if (this.frameBufferId >= 0) {
            this.destroyBuffers();
        }

        this.createBuffers(p_83942_, p_83943_);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    public void destroyBuffers() {
        RenderSystem.assertOnRenderThreadOrInit();
        this.unbindRead();
        this.unbindWrite();
        if (this.depthBufferId > -1) {
            TextureUtil.releaseTextureId(this.depthBufferId);
            this.depthBufferId = -1;
        }

        if (this.colorTextureId > -1) {
            TextureUtil.releaseTextureId(this.colorTextureId);
            this.colorTextureId = -1;
        }

        if (this.frameBufferId > -1) {
            GlStateManager._glBindFramebuffer(36160, 0);
            GlStateManager._glDeleteFramebuffers(this.frameBufferId);
            this.frameBufferId = -1;
        }
    }

    public void copyDepthFrom(RenderTarget p_83946_) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._glBindFramebuffer(36008, p_83946_.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, this.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, p_83946_.width, p_83946_.height, 0, 0, this.width, this.height, 256, 9728);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    public void createBuffers(int p_83951_, int p_83952_) {
        RenderSystem.assertOnRenderThreadOrInit();
        int i = RenderSystem.maxSupportedTextureSize();
        if (p_83951_ > 0 && p_83951_ <= i && p_83952_ > 0 && p_83952_ <= i) {
            this.viewWidth = p_83951_;
            this.viewHeight = p_83952_;
            this.width = p_83951_;
            this.height = p_83952_;
            this.frameBufferId = GlStateManager.glGenFramebuffers();
            this.colorTextureId = TextureUtil.generateTextureId();
            if (this.useDepth) {
                this.depthBufferId = TextureUtil.generateTextureId();
                GlStateManager._bindTexture(this.depthBufferId);
                GlStateManager._texParameter(3553, 10241, 9728);
                GlStateManager._texParameter(3553, 10240, 9728);
                GlStateManager._texParameter(3553, 34892, 0);
                GlStateManager._texParameter(3553, 10242, 33071);
                GlStateManager._texParameter(3553, 10243, 33071);
                GlStateManager._texImage2D(3553, 0, 6402, this.width, this.height, 0, 6402, 5126, null);
            }

            this.setFilterMode(9728, true);
            GlStateManager._bindTexture(this.colorTextureId);
            GlStateManager._texParameter(3553, 10242, 33071);
            GlStateManager._texParameter(3553, 10243, 33071);
            GlStateManager._texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, null);
            GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
            GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);
            if (this.useDepth) {
                GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
            }

            this.checkStatus();
            this.clear();
            this.unbindRead();
        } else {
            throw new IllegalArgumentException("Window " + p_83951_ + "x" + p_83952_ + " size out of bounds (max. size: " + i + ")");
        }
    }

    public void setFilterMode(int p_83937_) {
        this.setFilterMode(p_83937_, false);
    }

    private void setFilterMode(int p_334552_, boolean p_333030_) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (p_333030_ || p_334552_ != this.filterMode) {
            this.filterMode = p_334552_;
            GlStateManager._bindTexture(this.colorTextureId);
            GlStateManager._texParameter(3553, 10241, p_334552_);
            GlStateManager._texParameter(3553, 10240, p_334552_);
            GlStateManager._bindTexture(0);
        }
    }

    public void checkStatus() {
        RenderSystem.assertOnRenderThreadOrInit();
        int i = GlStateManager.glCheckFramebufferStatus(36160);
        if (i != 36053) {
            if (i == 36054) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            } else if (i == 36055) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            } else if (i == 36059) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
            } else if (i == 36060) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
            } else if (i == 36061) {
                throw new RuntimeException("GL_FRAMEBUFFER_UNSUPPORTED");
            } else if (i == 1285) {
                throw new RuntimeException("GL_OUT_OF_MEMORY");
            } else {
                throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
            }
        }
    }

    public void bindRead() {
        RenderSystem.assertOnRenderThread();
        GlStateManager._bindTexture(this.colorTextureId);
    }

    public void unbindRead() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._bindTexture(0);
    }

    public void bindWrite(boolean p_83948_) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
        if (p_83948_) {
            GlStateManager._viewport(0, 0, this.viewWidth, this.viewHeight);
        }
    }

    public void unbindWrite() {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    public void setClearColor(float p_83932_, float p_83933_, float p_83934_, float p_83935_) {
        this.clearChannels[0] = p_83932_;
        this.clearChannels[1] = p_83933_;
        this.clearChannels[2] = p_83934_;
        this.clearChannels[3] = p_83935_;
    }

    public void blitToScreen(int p_83939_, int p_83940_) {
        GlStateManager._glBindFramebuffer(36008, this.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, this.width, this.height, 0, 0, p_83939_, p_83940_, 16384, 9728);
        GlStateManager._glBindFramebuffer(36008, 0);
    }

    public void blitAndBlendToScreen(int p_364677_, int p_363939_) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._viewport(0, 0, p_364677_, p_363939_);
        CompiledShaderProgram compiledshaderprogram = Objects.requireNonNull(RenderSystem.setShader(CoreShaders.BLIT_SCREEN), "Blit shader not loaded");
        compiledshaderprogram.bindSampler("InSampler", this.colorTextureId);
        BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
    }

    public void clear() {
        RenderSystem.assertOnRenderThreadOrInit();
        this.bindWrite(true);
        GlStateManager._clearColor(this.clearChannels[0], this.clearChannels[1], this.clearChannels[2], this.clearChannels[3]);
        int i = 16384;
        if (this.useDepth) {
            GlStateManager._clearDepth(1.0);
            i |= 256;
        }

        GlStateManager._clear(i);
        this.unbindWrite();
    }

    public int getColorTextureId() {
        return this.colorTextureId;
    }

    public int getDepthTextureId() {
        return this.depthBufferId;
    }
}