package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.pathfinder.Path;

public record PathfindingDebugPayload(int entityId, Path path, float maxNodeDistance) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, PathfindingDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        PathfindingDebugPayload::write, PathfindingDebugPayload::new
    );
    public static final CustomPacketPayload.Type<PathfindingDebugPayload> TYPE = CustomPacketPayload.createType("debug/path");

    private PathfindingDebugPayload(FriendlyByteBuf p_297728_) {
        this(p_297728_.readInt(), Path.createFromStream(p_297728_), p_297728_.readFloat());
    }

    private void write(FriendlyByteBuf p_298780_) {
        p_298780_.writeInt(this.entityId);
        this.path.writeToStream(p_298780_);
        p_298780_.writeFloat(this.maxNodeDistance);
    }

    @Override
    public CustomPacketPayload.Type<PathfindingDebugPayload> type() {
        return TYPE;
    }
}