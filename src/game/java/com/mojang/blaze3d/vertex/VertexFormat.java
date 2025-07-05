package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VertexFormat {
    public static final int UNKNOWN_ELEMENT = -1;
    private final List<VertexFormatElement> elements;
    private final List<String> names;
    private final int vertexSize;
    private final int elementsMask;
    private final int[] offsetsByElement = new int[32];
    @Nullable
    private VertexBuffer immediateDrawVertexBuffer;

    VertexFormat(List<VertexFormatElement> p_343616_, List<String> p_345241_, IntList p_345522_, int p_344162_) {
        this.elements = p_343616_;
        this.names = p_345241_;
        this.vertexSize = p_344162_;
        this.elementsMask = p_343616_.stream().mapToInt(VertexFormatElement::mask).reduce(0, (p_344142_, p_345074_) -> p_344142_ | p_345074_);

        for (int i = 0; i < this.offsetsByElement.length; i++) {
            VertexFormatElement vertexformatelement = VertexFormatElement.byId(i);
            int j = vertexformatelement != null ? p_343616_.indexOf(vertexformatelement) : -1;
            this.offsetsByElement[i] = j != -1 ? p_345522_.getInt(j) : -1;
        }
    }

    public static VertexFormat.Builder builder() {
        return new VertexFormat.Builder();
    }

    public void bindAttributes(int p_361973_) {
        int i = 0;

        for (String s : this.getElementAttributeNames()) {
            GlStateManager._glBindAttribLocation(p_361973_, i, s);
            i++;
        }
    }

    @Override
    public String toString() {
        return "VertexFormat" + this.names;
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public List<String> getElementAttributeNames() {
        return this.names;
    }

    public int[] getOffsetsByElement() {
        return this.offsetsByElement;
    }

    public int getOffset(VertexFormatElement p_342517_) {
        return this.offsetsByElement[p_342517_.id()];
    }

    public boolean contains(VertexFormatElement p_345196_) {
        return (this.elementsMask & p_345196_.mask()) != 0;
    }

    public int getElementsMask() {
        return this.elementsMask;
    }

    public String getElementName(VertexFormatElement p_345336_) {
        int i = this.elements.indexOf(p_345336_);
        if (i == -1) {
            throw new IllegalArgumentException(p_345336_ + " is not contained in format");
        } else {
            return this.names.get(i);
        }
    }

    @Override
    public boolean equals(Object p_86026_) {
        if (this == p_86026_) {
            return true;
        } else {
            if (p_86026_ instanceof VertexFormat vertexformat
                && this.elementsMask == vertexformat.elementsMask
                && this.vertexSize == vertexformat.vertexSize
                && this.names.equals(vertexformat.names)
                && Arrays.equals(this.offsetsByElement, vertexformat.offsetsByElement)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.elementsMask * 31 + Arrays.hashCode(this.offsetsByElement);
    }

    public void setupBufferState() {
        RenderSystem.assertOnRenderThread();
        int i = this.getVertexSize();

        for (int j = 0; j < this.elements.size(); j++) {
            GlStateManager._enableVertexAttribArray(j);
            VertexFormatElement vertexformatelement = this.elements.get(j);
            vertexformatelement.setupBufferState(j, (long)this.getOffset(vertexformatelement), i);
        }
    }

    public void clearBufferState() {
        RenderSystem.assertOnRenderThread();

        for (int i = 0; i < this.elements.size(); i++) {
            GlStateManager._disableVertexAttribArray(i);
        }
    }

    public VertexBuffer getImmediateDrawVertexBuffer() {
        VertexBuffer vertexbuffer = this.immediateDrawVertexBuffer;
        if (vertexbuffer == null) {
            this.immediateDrawVertexBuffer = vertexbuffer = new VertexBuffer(BufferUsage.DYNAMIC_WRITE);
        }

        return vertexbuffer;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();
        private final IntList offsets = new IntArrayList();
        private int offset;

        Builder() {
        }

        public VertexFormat.Builder add(String p_343401_, VertexFormatElement p_345244_) {
            this.elements.put(p_343401_, p_345244_);
            this.offsets.add(this.offset);
            this.offset = this.offset + p_345244_.byteSize();
            return this;
        }

        public VertexFormat.Builder padding(int p_345477_) {
            this.offset += p_345477_;
            return this;
        }

        public VertexFormat build() {
            ImmutableMap<String, VertexFormatElement> immutablemap = this.elements.buildOrThrow();
            ImmutableList<VertexFormatElement> immutablelist = immutablemap.values().asList();
            ImmutableList<String> immutablelist1 = immutablemap.keySet().asList();
            return new VertexFormat(immutablelist, immutablelist1, this.offsets, this.offset);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum IndexType {
        SHORT(5123, 2),
        INT(5125, 4);

        public final int asGLType;
        public final int bytes;

        private IndexType(final int p_166930_, final int p_166931_) {
            this.asGLType = p_166930_;
            this.bytes = p_166931_;
        }

        public static VertexFormat.IndexType least(int p_166934_) {
            return (p_166934_ & -65536) != 0 ? INT : SHORT;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Mode {
        LINES(4, 2, 2, false),
        LINE_STRIP(5, 2, 1, true),
        DEBUG_LINES(1, 2, 2, false),
        DEBUG_LINE_STRIP(3, 2, 1, true),
        TRIANGLES(4, 3, 3, false),
        TRIANGLE_STRIP(5, 3, 1, true),
        TRIANGLE_FAN(6, 3, 1, true),
        QUADS(4, 4, 4, false);

        public final int asGLMode;
        public final int primitiveLength;
        public final int primitiveStride;
        public final boolean connectedPrimitives;

        private Mode(final int p_231238_, final int p_231239_, final int p_231240_, final boolean p_231241_) {
            this.asGLMode = p_231238_;
            this.primitiveLength = p_231239_;
            this.primitiveStride = p_231240_;
            this.connectedPrimitives = p_231241_;
        }

        public int indexCount(int p_166959_) {
            return switch (this) {
                case LINES, QUADS -> p_166959_ / 4 * 6;
                case LINE_STRIP, DEBUG_LINES, DEBUG_LINE_STRIP, TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> p_166959_;
                default -> 0;
            };
        }
    }
}