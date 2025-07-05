package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundResetChatPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientConfigurationPacketListenerImpl extends ClientCommonPacketListenerImpl implements ClientConfigurationPacketListener, TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GameProfile localGameProfile;
    private FeatureFlagSet enabledFeatures;
    private final RegistryAccess.Frozen receivedRegistries;
    private final RegistryDataCollector registryDataCollector = new RegistryDataCollector();
    @Nullable
    private KnownPacksManager knownPacks;
    @Nullable
    protected ChatComponent.State chatState;

    public ClientConfigurationPacketListenerImpl(Minecraft p_301278_, Connection p_299257_, CommonListenerCookie p_300907_) {
        super(p_301278_, p_299257_, p_300907_);
        this.localGameProfile = p_300907_.localGameProfile();
        this.receivedRegistries = p_300907_.receivedRegistries();
        this.enabledFeatures = p_300907_.enabledFeatures();
        this.chatState = p_300907_.chatState();
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    @Override
    protected void handleCustomPayload(CustomPacketPayload p_301281_) {
        this.handleUnknownCustomPayload(p_301281_);
    }

    private void handleUnknownCustomPayload(CustomPacketPayload p_300719_) {
        LOGGER.warn("Unknown custom packet payload: {}", p_300719_.type().id());
    }

    @Override
    public void handleRegistryData(ClientboundRegistryDataPacket p_299218_) {
        PacketUtils.ensureRunningOnSameThread(p_299218_, this, this.minecraft);
        this.registryDataCollector.appendContents(p_299218_.registry(), p_299218_.entries());
    }

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket p_335168_) {
        PacketUtils.ensureRunningOnSameThread(p_335168_, this, this.minecraft);
        this.registryDataCollector.appendTags(p_335168_.getTags());
    }

    @Override
    public void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket p_301158_) {
        this.enabledFeatures = FeatureFlags.REGISTRY.fromNames(p_301158_.features());
    }

    @Override
    public void handleSelectKnownPacks(ClientboundSelectKnownPacks p_333075_) {
        PacketUtils.ensureRunningOnSameThread(p_333075_, this, this.minecraft);
        if (this.knownPacks == null) {
            this.knownPacks = new KnownPacksManager();
        }

        List<KnownPack> list = this.knownPacks.trySelectingPacks(p_333075_.knownPacks());
        this.send(new ServerboundSelectKnownPacks(list));
    }

    @Override
    public void handleResetChat(ClientboundResetChatPacket p_328730_) {
        this.chatState = null;
    }

    private <T> T runWithResources(Function<ResourceProvider, T> p_330303_) {
        if (this.knownPacks == null) {
            return p_330303_.apply(ResourceProvider.EMPTY);
        } else {
            Object object;
            try (CloseableResourceManager closeableresourcemanager = this.knownPacks.createResourceManager()) {
                object = p_330303_.apply(closeableresourcemanager);
            }

            return (T)object;
        }
    }

    @Override
    public void handleConfigurationFinished(ClientboundFinishConfigurationPacket p_299280_) {
        PacketUtils.ensureRunningOnSameThread(p_299280_, this, this.minecraft);
        RegistryAccess.Frozen registryaccess$frozen = this.runWithResources(
            p_357775_ -> this.registryDataCollector.collectGameRegistries(p_357775_, this.receivedRegistries, this.connection.isMemoryConnection())
        );
        this.connection
            .setupInboundProtocol(
                GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(registryaccess$frozen)),
                new ClientPacketListener(
                    this.minecraft,
                    this.connection,
                    new CommonListenerCookie(
                        this.localGameProfile,
                        this.telemetryManager,
                        registryaccess$frozen,
                        this.enabledFeatures,
                        this.serverBrand,
                        this.serverData,
                        this.postDisconnectScreen,
                        this.serverCookies,
                        this.chatState,
                        this.customReportDetails,
                        this.serverLinks
                    )
                )
            );
        this.connection.send(ServerboundFinishConfigurationPacket.INSTANCE);
        this.connection.setupOutboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(registryaccess$frozen)));
    }

    @Override
    public void tick() {
        this.sendDeferredPackets();
    }

    @Override
    public void onDisconnect(DisconnectionDetails p_343449_) {
        super.onDisconnect(p_343449_);
        this.minecraft.clearDownloadedResourcePacks();
    }
}