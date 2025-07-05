package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record WorldGenAttemptDebugPayload(BlockPos pos, float scale, float red, float green, float blue, float alpha)
    implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, WorldGenAttemptDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        WorldGenAttemptDebugPayload::write, WorldGenAttemptDebugPayload::new
    );
    public static final CustomPacketPayload.Type<WorldGenAttemptDebugPayload> TYPE = CustomPacketPayload.createType("debug/worldgen_attempt");

    private WorldGenAttemptDebugPayload(FriendlyByteBuf p_298227_) {
        this(p_298227_.readBlockPos(), p_298227_.readFloat(), p_298227_.readFloat(), p_298227_.readFloat(), p_298227_.readFloat(), p_298227_.readFloat());
    }

    private void write(FriendlyByteBuf p_301372_) {
        p_301372_.writeBlockPos(this.pos);
        p_301372_.writeFloat(this.scale);
        p_301372_.writeFloat(this.red);
        p_301372_.writeFloat(this.green);
        p_301372_.writeFloat(this.blue);
        p_301372_.writeFloat(this.alpha);
    }

    @Override
    public CustomPacketPayload.Type<WorldGenAttemptDebugPayload> type() {
        return TYPE;
    }
}