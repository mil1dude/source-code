package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetDefaultSpawnPositionPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetDefaultSpawnPositionPacket> STREAM_CODEC = Packet.codec(
        ClientboundSetDefaultSpawnPositionPacket::write, ClientboundSetDefaultSpawnPositionPacket::new
    );
    private final BlockPos pos;
    private final float angle;

    public ClientboundSetDefaultSpawnPositionPacket(BlockPos p_133115_, float p_133116_) {
        this.pos = p_133115_;
        this.angle = p_133116_;
    }

    private ClientboundSetDefaultSpawnPositionPacket(FriendlyByteBuf p_179286_) {
        this.pos = p_179286_.readBlockPos();
        this.angle = p_179286_.readFloat();
    }

    private void write(FriendlyByteBuf p_133125_) {
        p_133125_.writeBlockPos(this.pos);
        p_133125_.writeFloat(this.angle);
    }

    @Override
    public PacketType<ClientboundSetDefaultSpawnPositionPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_DEFAULT_SPAWN_POSITION;
    }

    public void handle(ClientGamePacketListener p_133122_) {
        p_133122_.handleSetSpawn(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public float getAngle() {
        return this.angle;
    }
}