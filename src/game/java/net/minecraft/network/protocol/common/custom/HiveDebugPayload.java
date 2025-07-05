package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record HiveDebugPayload(HiveDebugPayload.HiveInfo hiveInfo) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, HiveDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        HiveDebugPayload::write, HiveDebugPayload::new
    );
    public static final CustomPacketPayload.Type<HiveDebugPayload> TYPE = CustomPacketPayload.createType("debug/hive");

    private HiveDebugPayload(FriendlyByteBuf p_299613_) {
        this(new HiveDebugPayload.HiveInfo(p_299613_));
    }

    private void write(FriendlyByteBuf p_297901_) {
        this.hiveInfo.write(p_297901_);
    }

    @Override
    public CustomPacketPayload.Type<HiveDebugPayload> type() {
        return TYPE;
    }

    public static record HiveInfo(BlockPos pos, String hiveType, int occupantCount, int honeyLevel, boolean sedated) {
        public HiveInfo(FriendlyByteBuf p_299719_) {
            this(p_299719_.readBlockPos(), p_299719_.readUtf(), p_299719_.readInt(), p_299719_.readInt(), p_299719_.readBoolean());
        }

        public void write(FriendlyByteBuf p_301145_) {
            p_301145_.writeBlockPos(this.pos);
            p_301145_.writeUtf(this.hiveType);
            p_301145_.writeInt(this.occupantCount);
            p_301145_.writeInt(this.honeyLevel);
            p_301145_.writeBoolean(this.sedated);
        }
    }
}