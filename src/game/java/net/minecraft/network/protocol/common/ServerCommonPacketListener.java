package net.minecraft.network.protocol.common;

import net.minecraft.network.protocol.cookie.ServerCookiePacketListener;

public interface ServerCommonPacketListener extends ServerCookiePacketListener {
    void handleKeepAlive(ServerboundKeepAlivePacket p_300190_);

    void handlePong(ServerboundPongPacket p_297980_);

    void handleCustomPayload(ServerboundCustomPayloadPacket p_297952_);

    void handleResourcePackResponse(ServerboundResourcePackPacket p_300293_);

    void handleClientInformation(ServerboundClientInformationPacket p_301286_);
}