package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.Difficulty;

public class ServerboundChangeDifficultyPacket implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundChangeDifficultyPacket> STREAM_CODEC = Packet.codec(
        ServerboundChangeDifficultyPacket::write, ServerboundChangeDifficultyPacket::new
    );
    private final Difficulty difficulty;

    public ServerboundChangeDifficultyPacket(Difficulty p_133817_) {
        this.difficulty = p_133817_;
    }

    private ServerboundChangeDifficultyPacket(FriendlyByteBuf p_179542_) {
        this.difficulty = Difficulty.byId(p_179542_.readUnsignedByte());
    }

    private void write(FriendlyByteBuf p_133826_) {
        p_133826_.writeByte(this.difficulty.getId());
    }

    @Override
    public PacketType<ServerboundChangeDifficultyPacket> type() {
        return GamePacketTypes.SERVERBOUND_CHANGE_DIFFICULTY;
    }

    public void handle(ServerGamePacketListener p_133823_) {
        p_133823_.handleChangeDifficulty(this);
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }
}