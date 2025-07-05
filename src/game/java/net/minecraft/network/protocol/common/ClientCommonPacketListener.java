package net.minecraft.network.protocol.common;

import net.minecraft.network.protocol.cookie.ClientCookiePacketListener;

public interface ClientCommonPacketListener extends ClientCookiePacketListener {
    void handleKeepAlive(ClientboundKeepAlivePacket p_299456_);

    void handlePing(ClientboundPingPacket p_297871_);

    void handleCustomPayload(ClientboundCustomPayloadPacket p_299137_);

    void handleDisconnect(ClientboundDisconnectPacket p_300983_);

    void handleResourcePackPush(ClientboundResourcePackPushPacket p_312935_);

    void handleResourcePackPop(ClientboundResourcePackPopPacket p_311379_);

    void handleUpdateTags(ClientboundUpdateTagsPacket p_297352_);

    void handleStoreCookie(ClientboundStoreCookiePacket p_331954_);

    void handleTransfer(ClientboundTransferPacket p_329215_);

    void handleCustomReportDetails(ClientboundCustomReportDetailsPacket p_343430_);

    void handleServerLinks(ClientboundServerLinksPacket p_343928_);
}