package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class VertexBuffer implements AutoCloseable {
    private final BufferUsage usage;
    private final GpuBuffer vertexBuffer;
    @Nullable
    private GpuBuffer indexBuffer = null;
    private int arrayObjectId;
    @Nullable
    private VertexFormat format;
    @Nullable
    private RenderSystem.AutoStorageIndexBuffer sequentialIndices;
    private VertexFormat.IndexType indexType;
    private int indexCount;
    private VertexFormat.Mode mode;

    public VertexBuffer(BufferUsage p_368664_) {
        this.usage = p_368664_;
        RenderSystem.assertOnRenderThread();
        this.vertexBuffer = new GpuBuffer(BufferType.VERTICES, p_368664_, 0);
        this.arrayObjectId = GlStateManager._glGenVertexArrays();
    }

    public static VertexBuffer uploadStatic(VertexFormat.Mode p_376245_, VertexFormat p_376414_, Consumer<VertexConsumer> p_377755_) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(p_376245_, p_376414_);
        p_377755_.accept(bufferbuilder);
        VertexBuffer vertexbuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
        vertexbuffer.bind();
        vertexbuffer.upload(bufferbuilder.buildOrThrow());
        unbind();
        return vertexbuffer;
    }

    public void upload(MeshData p_345178_) {
        MeshData meshdata = p_345178_;

        label40: {
            try {
                if (this.isInvalid()) {
                    break label40;
                }

                RenderSystem.assertOnRenderThread();
                MeshData.DrawState meshdata$drawstate = p_345178_.drawState();
                this.format = this.uploadVertexBuffer(meshdata$drawstate, p_345178_.vertexBuffer());
                this.sequentialIndices = this.uploadIndexBuffer(meshdata$drawstate, p_345178_.indexBuffer());
                this.indexCount = meshdata$drawstate.indexCount();
                this.indexType = meshdata$drawstate.indexType();
                this.mode = meshdata$drawstate.mode();
            } catch (Throwable throwable1) {
                if (p_345178_ != null) {
                    try {
                        meshdata.close();
                    } catch (Throwable throwable) {
                        throwable1.addSuppressed(throwable);
                    }
                }

                throw throwable1;
            }

            if (p_345178_ != null) {
                p_345178_.close();
            }

            return;
        }

        if (p_345178_ != null) {
            p_345178_.close();
        }
    }

    public void uploadIndexBuffer(ByteBufferBuilder.Result p_343348_) {
        ByteBufferBuilder.Result bytebufferbuilder$result = p_343348_;

        label46: {
            try {
                if (this.isInvalid()) {
                    break label46;
                }

                RenderSystem.assertOnRenderThread();
                if (this.indexBuffer != null) {
                    this.indexBuffer.close();
                }

                this.indexBuffer = new GpuBuffer(BufferType.INDICES, this.usage, p_343348_.byteBuffer());
                this.sequentialIndices = null;
            } catch (Throwable throwable1) {
                if (p_343348_ != null) {
                    try {
                        bytebufferbuilder$result.close();
                    } catch (Throwable throwable) {
                        throwable1.addSuppressed(throwable);
                    }
                }

                throw throwable1;
            }

            if (p_343348_ != null) {
                p_343348_.close();
            }

            return;
        }

        if (p_343348_ != null) {
            p_343348_.close();
        }
    }

    private VertexFormat uploadVertexBuffer(MeshData.DrawState p_342212_, @Nullable ByteBuffer p_231220_) {
        boolean flag = false;
        if (!p_342212_.format().equals(this.format)) {
            if (this.format != null) {
                this.format.clearBufferState();
            }

            this.vertexBuffer.bind();
            p_342212_.format().setupBufferState();
            flag = true;
        }

        if (p_231220_ != null) {
            if (!flag) {
                this.vertexBuffer.bind();
            }

            this.vertexBuffer.resize(p_231220_.remaining());
            this.vertexBuffer.write(p_231220_, 0);
        }

        return p_342212_.format();
    }

    @Nullable
    private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(MeshData.DrawState p_345013_, @Nullable ByteBuffer p_231225_) {
        if (p_231225_ != null) {
            if (this.indexBuffer != null) {
                this.indexBuffer.close();
            }

            this.indexBuffer = new GpuBuffer(BufferType.INDICES, this.usage, p_231225_);
            return null;
        } else {
            RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(p_345013_.mode());
            if (rendersystem$autostorageindexbuffer != this.sequentialIndices || !rendersystem$autostorageindexbuffer.hasStorage(p_345013_.indexCount())) {
                rendersystem$autostorageindexbuffer.bind(p_345013_.indexCount());
            }

            return rendersystem$autostorageindexbuffer;
        }
    }

    public void bind() {
        BufferUploader.invalidate();
        GlStateManager._glBindVertexArray(this.arrayObjectId);
    }

    public static void unbind() {
        BufferUploader.invalidate();
        GlStateManager._glBindVertexArray(0);
    }

    public void draw() {
        RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType);
    }

    private VertexFormat.IndexType getIndexType() {
        RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = this.sequentialIndices;
        return rendersystem$autostorageindexbuffer != null ? rendersystem$autostorageindexbuffer.type() : this.indexType;
    }

    public void drawWithShader(Matrix4f p_254480_, Matrix4f p_254555_, @Nullable CompiledShaderProgram p_369565_) {
        if (p_369565_ != null) {
            RenderSystem.assertOnRenderThread();
            p_369565_.setDefaultUniforms(this.mode, p_254480_, p_254555_, Minecraft.getInstance().getWindow());
            p_369565_.apply();
            this.draw();
            p_369565_.clear();
        }
    }

    public void drawWithRenderType(RenderType p_376845_) {
        p_376845_.setupRenderState();
        this.bind();
        this.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        unbind();
        p_376845_.clearRenderState();
    }

    @Override
    public void close() {
        this.vertexBuffer.close();
        if (this.indexBuffer != null) {
            this.indexBuffer.close();
            this.indexBuffer = null;
        }

        if (this.arrayObjectId >= 0) {
            RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
            this.arrayObjectId = -1;
        }
    }

    public VertexFormat getFormat() {
        return this.format;
    }

    public boolean isInvalid() {
        return this.arrayObjectId == -1;
    }
}