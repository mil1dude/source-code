package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundHorseScreenOpenPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundHorseScreenOpenPacket> STREAM_CODEC = Packet.codec(
        ClientboundHorseScreenOpenPacket::write, ClientboundHorseScreenOpenPacket::new
    );
    private final int containerId;
    private final int inventoryColumns;
    private final int entityId;

    public ClientboundHorseScreenOpenPacket(int p_132195_, int p_132196_, int p_132197_) {
        this.containerId = p_132195_;
        this.inventoryColumns = p_132196_;
        this.entityId = p_132197_;
    }

    private ClientboundHorseScreenOpenPacket(FriendlyByteBuf p_178867_) {
        this.containerId = p_178867_.readContainerId();
        this.inventoryColumns = p_178867_.readVarInt();
        this.entityId = p_178867_.readInt();
    }

    private void write(FriendlyByteBuf p_132206_) {
        p_132206_.writeContainerId(this.containerId);
        p_132206_.writeVarInt(this.inventoryColumns);
        p_132206_.writeInt(this.entityId);
    }

    @Override
    public PacketType<ClientboundHorseScreenOpenPacket> type() {
        return GamePacketTypes.CLIENTBOUND_HORSE_SCREEN_OPEN;
    }

    public void handle(ClientGamePacketListener p_132203_) {
        p_132203_.handleHorseScreenOpen(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getInventoryColumns() {
        return this.inventoryColumns;
    }

    public int getEntityId() {
        return this.entityId;
    }
}