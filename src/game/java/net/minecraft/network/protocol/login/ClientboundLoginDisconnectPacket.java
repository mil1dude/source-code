package net.minecraft.network.protocol.login;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundLoginDisconnectPacket implements Packet<ClientLoginPacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundLoginDisconnectPacket> STREAM_CODEC = Packet.codec(
        ClientboundLoginDisconnectPacket::write, ClientboundLoginDisconnectPacket::new
    );
    private final Component reason;

    public ClientboundLoginDisconnectPacket(Component p_134812_) {
        this.reason = p_134812_;
    }

    private ClientboundLoginDisconnectPacket(FriendlyByteBuf p_179820_) {
        this.reason = Component.Serializer.fromJsonLenient(p_179820_.readUtf(262144), RegistryAccess.EMPTY);
    }

    private void write(FriendlyByteBuf p_134821_) {
        p_134821_.writeUtf(Component.Serializer.toJson(this.reason, RegistryAccess.EMPTY));
    }

    @Override
    public PacketType<ClientboundLoginDisconnectPacket> type() {
        return LoginPacketTypes.CLIENTBOUND_LOGIN_DISCONNECT;
    }

    public void handle(ClientLoginPacketListener p_134818_) {
        p_134818_.handleDisconnect(this);
    }

    public Component getReason() {
        return this.reason;
    }
}