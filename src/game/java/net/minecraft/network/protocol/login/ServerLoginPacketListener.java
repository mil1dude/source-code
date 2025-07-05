package net.minecraft.network.protocol.login;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.cookie.ServerCookiePacketListener;

public interface ServerLoginPacketListener extends ServerCookiePacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.LOGIN;
    }

    void handleHello(ServerboundHelloPacket p_134823_);

    void handleKey(ServerboundKeyPacket p_134824_);

    void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket p_298453_);

    void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket p_301180_);
}