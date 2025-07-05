package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record StructuresDebugPayload(ResourceKey<Level> dimension, BoundingBox mainBB, List<StructuresDebugPayload.PieceInfo> pieces)
    implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, StructuresDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        StructuresDebugPayload::write, StructuresDebugPayload::new
    );
    public static final CustomPacketPayload.Type<StructuresDebugPayload> TYPE = CustomPacketPayload.createType("debug/structures");

    private StructuresDebugPayload(FriendlyByteBuf p_301247_) {
        this(p_301247_.readResourceKey(Registries.DIMENSION), readBoundingBox(p_301247_), p_301247_.readList(StructuresDebugPayload.PieceInfo::new));
    }

    private void write(FriendlyByteBuf p_300362_) {
        p_300362_.writeResourceKey(this.dimension);
        writeBoundingBox(p_300362_, this.mainBB);
        p_300362_.writeCollection(this.pieces, (p_300337_, p_299834_) -> p_299834_.write(p_300362_));
    }

    @Override
    public CustomPacketPayload.Type<StructuresDebugPayload> type() {
        return TYPE;
    }

    static BoundingBox readBoundingBox(FriendlyByteBuf p_297781_) {
        return new BoundingBox(p_297781_.readInt(), p_297781_.readInt(), p_297781_.readInt(), p_297781_.readInt(), p_297781_.readInt(), p_297781_.readInt());
    }

    static void writeBoundingBox(FriendlyByteBuf p_300963_, BoundingBox p_297295_) {
        p_300963_.writeInt(p_297295_.minX());
        p_300963_.writeInt(p_297295_.minY());
        p_300963_.writeInt(p_297295_.minZ());
        p_300963_.writeInt(p_297295_.maxX());
        p_300963_.writeInt(p_297295_.maxY());
        p_300963_.writeInt(p_297295_.maxZ());
    }

    public static record PieceInfo(BoundingBox boundingBox, boolean isStart) {
        public PieceInfo(FriendlyByteBuf p_297915_) {
            this(StructuresDebugPayload.readBoundingBox(p_297915_), p_297915_.readBoolean());
        }

        public void write(FriendlyByteBuf p_298576_) {
            StructuresDebugPayload.writeBoundingBox(p_298576_, this.boundingBox);
            p_298576_.writeBoolean(this.isStart);
        }
    }
}