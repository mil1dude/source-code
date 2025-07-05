package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PoiRemovedDebugPayload(BlockPos pos) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PoiRemovedDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        PoiRemovedDebugPayload::write, PoiRemovedDebugPayload::new
    );
    public static final CustomPacketPayload.Type<PoiRemovedDebugPayload> TYPE = CustomPacketPayload.createType("debug/poi_removed");

    private PoiRemovedDebugPayload(FriendlyByteBuf p_300036_) {
        this(p_300036_.readBlockPos());
    }

    private void write(FriendlyByteBuf p_300931_) {
        p_300931_.writeBlockPos(this.pos);
    }

    @Override
    public CustomPacketPayload.Type<PoiRemovedDebugPayload> type() {
        return TYPE;
    }
}