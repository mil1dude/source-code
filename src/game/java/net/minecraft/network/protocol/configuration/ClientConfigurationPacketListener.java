package net.minecraft.network.protocol.configuration;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;

public interface ClientConfigurationPacketListener extends ClientCommonPacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.CONFIGURATION;
    }

    void handleConfigurationFinished(ClientboundFinishConfigurationPacket p_301141_);

    void handleRegistryData(ClientboundRegistryDataPacket p_298669_);

    void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket p_298844_);

    void handleSelectKnownPacks(ClientboundSelectKnownPacks p_333475_);

    void handleResetChat(ClientboundResetChatPacket p_333315_);
}