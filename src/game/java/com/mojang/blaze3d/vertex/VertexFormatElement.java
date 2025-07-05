package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record VertexFormatElement(int id, int index, VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
    public static final int MAX_COUNT = 32;
    private static final VertexFormatElement[] BY_ID = new VertexFormatElement[32];
    private static final List<VertexFormatElement> ELEMENTS = new ArrayList<>(32);
    public static final VertexFormatElement POSITION = register(0, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
    public static final VertexFormatElement COLOR = register(1, 0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4);
    public static final VertexFormatElement UV0 = register(2, 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2);
    public static final VertexFormatElement UV = UV0;
    public static final VertexFormatElement UV1 = register(3, 1, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
    public static final VertexFormatElement UV2 = register(4, 2, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
    public static final VertexFormatElement NORMAL = register(5, 0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 3);

    public VertexFormatElement(int id, int index, VertexFormatElement.Type type, VertexFormatElement.Usage usage, int count) {
        if (id < 0 || id >= BY_ID.length) {
            throw new IllegalArgumentException("Element ID must be in range [0; " + BY_ID.length + ")");
        } else if (!this.supportsUsage(index, usage)) {
            throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
        } else {
            this.id = id;
            this.index = index;
            this.type = type;
            this.usage = usage;
            this.count = count;
        }
    }

    public static VertexFormatElement register(
        int p_343820_, int p_343175_, VertexFormatElement.Type p_342455_, VertexFormatElement.Usage p_344304_, int p_343812_
    ) {
        VertexFormatElement vertexformatelement = new VertexFormatElement(p_343820_, p_343175_, p_342455_, p_344304_, p_343812_);
        if (BY_ID[p_343820_] != null) {
            throw new IllegalArgumentException("Duplicate element registration for: " + p_343820_);
        } else {
            BY_ID[p_343820_] = vertexformatelement;
            ELEMENTS.add(vertexformatelement);
            return vertexformatelement;
        }
    }

    private boolean supportsUsage(int p_86043_, VertexFormatElement.Usage p_86044_) {
        return p_86043_ == 0 || p_86044_ == VertexFormatElement.Usage.UV;
    }

    @Override
    public String toString() {
        return this.count + "," + this.usage + "," + this.type + " (" + this.id + ")";
    }

    public int mask() {
        return 1 << this.id;
    }

    public int byteSize() {
        return this.type.size() * this.count;
    }

    public void setupBufferState(int p_166966_, long p_166967_, int p_166968_) {
        this.usage.setupState.setupBufferState(this.count, this.type.glType(), p_166968_, p_166967_, p_166966_);
    }

    @Nullable
    public static VertexFormatElement byId(int p_343405_) {
        return BY_ID[p_343405_];
    }

    public static Stream<VertexFormatElement> elementsFromMask(int p_344546_) {
        return ELEMENTS.stream().filter(p_345037_ -> p_345037_ != null && (p_344546_ & p_345037_.mask()) != 0);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        FLOAT(4, "Float", 5126),
        UBYTE(1, "Unsigned Byte", 5121),
        BYTE(1, "Byte", 5120),
        USHORT(2, "Unsigned Short", 5123),
        SHORT(2, "Short", 5122),
        UINT(4, "Unsigned Int", 5125),
        INT(4, "Int", 5124);

        private final int size;
        private final String name;
        private final int glType;

        private Type(final int p_86071_, final String p_86072_, final int p_86073_) {
            this.size = p_86071_;
            this.name = p_86072_;
            this.glType = p_86073_;
        }

        public int size() {
            return this.size;
        }

        public int glType() {
            return this.glType;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Usage {
        POSITION(
            "Position",
            (p_340646_, p_340647_, p_340648_, p_340649_, p_340650_) -> GlStateManager._vertexAttribPointer(
                    p_340650_, p_340646_, p_340647_, false, p_340648_, p_340649_
                )
        ),
        NORMAL(
            "Normal",
            (p_340656_, p_340657_, p_340658_, p_340659_, p_340660_) -> GlStateManager._vertexAttribPointer(
                    p_340660_, p_340656_, p_340657_, true, p_340658_, p_340659_
                )
        ),
        COLOR(
            "Vertex Color",
            (p_340661_, p_340662_, p_340663_, p_340664_, p_340665_) -> GlStateManager._vertexAttribPointer(
                    p_340665_, p_340661_, p_340662_, true, p_340663_, p_340664_
                )
        ),
        UV("UV", (p_340641_, p_340642_, p_340643_, p_340644_, p_340645_) -> {
            if (p_340642_ == 5126) {
                GlStateManager._vertexAttribPointer(p_340645_, p_340641_, p_340642_, false, p_340643_, p_340644_);
            } else {
                GlStateManager._vertexAttribIPointer(p_340645_, p_340641_, p_340642_, p_340643_, p_340644_);
            }
        }),
        GENERIC(
            "Generic",
            (p_340651_, p_340652_, p_340653_, p_340654_, p_340655_) -> GlStateManager._vertexAttribPointer(
                    p_340655_, p_340651_, p_340652_, false, p_340653_, p_340654_
                )
        );

        private final String name;
        final VertexFormatElement.Usage.SetupState setupState;

        private Usage(final String p_166975_, final VertexFormatElement.Usage.SetupState p_166976_) {
            this.name = p_166975_;
            this.setupState = p_166976_;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @FunctionalInterface
        @OnlyIn(Dist.CLIENT)
        interface SetupState {
            void setupBufferState(int p_167053_, int p_167054_, int p_167055_, long p_167056_, int p_167057_);
        }
    }
}