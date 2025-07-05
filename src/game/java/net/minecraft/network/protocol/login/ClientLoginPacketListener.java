package net.minecraft.network.protocol.login;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.cookie.ClientCookiePacketListener;

public interface ClientLoginPacketListener extends ClientCookiePacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.LOGIN;
    }

    void handleHello(ClientboundHelloPacket p_134742_);

    void handleLoginFinished(ClientboundLoginFinishedPacket p_369992_);

    void handleDisconnect(ClientboundLoginDisconnectPacket p_134744_);

    void handleCompression(ClientboundLoginCompressionPacket p_134743_);

    void handleCustomQuery(ClientboundCustomQueryPacket p_134740_);
}