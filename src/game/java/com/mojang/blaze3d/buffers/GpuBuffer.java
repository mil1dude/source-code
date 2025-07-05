package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GpuBuffer implements AutoCloseable {
    private static final MemoryPool MEMORY_POOl = TracyClient.createMemoryPool("GPU Buffers");
    private final BufferType type;
    private final BufferUsage usage;
    private boolean closed;
    private boolean initialized = false;
    public final int handle;
    public int size;

    public GpuBuffer(BufferType p_367350_, BufferUsage p_363902_, int p_361832_) {
        this.type = p_367350_;
        this.size = p_361832_;
        this.usage = p_363902_;
        this.handle = GlStateManager._glGenBuffers();
    }

    public GpuBuffer(BufferType p_367048_, BufferUsage p_369969_, ByteBuffer p_364811_) {
        this(p_367048_, p_369969_, p_364811_.remaining());
        this.write(p_364811_, 0);
    }

    public void resize(int p_367249_) {
        if (this.closed) {
            throw new IllegalStateException("Buffer already closed");
        } else {
            if (this.initialized) {
                MEMORY_POOl.free((long)this.handle);
            }

            this.size = p_367249_;
            if (this.usage.writable) {
                this.initialized = false;
            } else {
                this.bind();
                GlStateManager._glBufferData(this.type.id, (long)p_367249_, this.usage.id);
                MEMORY_POOl.malloc((long)this.handle, p_367249_);
                this.initialized = true;
            }
        }
    }

    public void write(ByteBuffer p_363379_, int p_364980_) {
        if (this.closed) {
            throw new IllegalStateException("Buffer already closed");
        } else if (!this.usage.writable) {
            throw new IllegalStateException("Buffer is not writable");
        } else {
            int i = p_363379_.remaining();
            if (i + p_364980_ > this.size) {
                throw new IllegalArgumentException(
                    "Cannot write more data than this buffer can hold (attempting to write "
                        + i
                        + " bytes at offset "
                        + p_364980_
                        + " to "
                        + this.size
                        + " size buffer)"
                );
            } else {
                this.bind();
                if (this.initialized) {
                    GlStateManager._glBufferSubData(this.type.id, p_364980_, p_363379_);
                } else if (p_364980_ == 0 && i == this.size) {
                    GlStateManager._glBufferData(this.type.id, p_363379_, this.usage.id);
                    MEMORY_POOl.malloc((long)this.handle, this.size);
                    this.initialized = true;
                } else {
                    GlStateManager._glBufferData(this.type.id, (long)this.size, this.usage.id);
                    GlStateManager._glBufferSubData(this.type.id, p_364980_, p_363379_);
                    MEMORY_POOl.malloc((long)this.handle, this.size);
                    this.initialized = true;
                }
            }
        }
    }

    @Nullable
    public GpuBuffer.ReadView read() {
        return this.read(0, this.size);
    }

    @Nullable
    public GpuBuffer.ReadView read(int p_361869_, int p_362198_) {
        if (this.closed) {
            throw new IllegalStateException("Buffer already closed");
        } else if (!this.usage.readable) {
            throw new IllegalStateException("Buffer is not readable");
        } else if (p_361869_ + p_362198_ > this.size) {
            throw new IllegalArgumentException(
                "Cannot read more data than this buffer can hold (attempting to read "
                    + p_362198_
                    + " bytes at offset "
                    + p_361869_
                    + " from "
                    + this.size
                    + " size buffer)"
            );
        } else {
            this.bind();
            ByteBuffer bytebuffer = GlStateManager._glMapBufferRange(this.type.id, p_361869_, p_362198_, 1);
            return bytebuffer == null ? null : new GpuBuffer.ReadView(this.type.id, bytebuffer);
        }
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            GlStateManager._glDeleteBuffers(this.handle);
            if (this.initialized) {
                MEMORY_POOl.free((long)this.handle);
            }
        }
    }

    public void bind() {
        GlStateManager._glBindBuffer(this.type.id, this.handle);
    }

    @OnlyIn(Dist.CLIENT)
    public static class ReadView implements AutoCloseable {
        private final int target;
        private final ByteBuffer data;

        protected ReadView(int p_367113_, ByteBuffer p_362615_) {
            this.target = p_367113_;
            this.data = p_362615_;
        }

        public ByteBuffer data() {
            return this.data;
        }

        @Override
        public void close() {
            GlStateManager._glUnmapBuffer(this.target);
        }
    }
}