package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nullable;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundServerLinksPacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.config.JoinWorldTask;
import net.minecraft.server.network.config.ServerResourcePackConfigurationTask;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.flag.FeatureFlags;
import org.slf4j.Logger;

public class ServerConfigurationPacketListenerImpl extends ServerCommonPacketListenerImpl implements ServerConfigurationPacketListener, TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component DISCONNECT_REASON_INVALID_DATA = Component.translatable("multiplayer.disconnect.invalid_player_data");
    private final GameProfile gameProfile;
    private final Queue<ConfigurationTask> configurationTasks = new ConcurrentLinkedQueue<>();
    @Nullable
    private ConfigurationTask currentTask;
    private ClientInformation clientInformation;
    @Nullable
    private SynchronizeRegistriesTask synchronizeRegistriesTask;

    public ServerConfigurationPacketListenerImpl(MinecraftServer p_301415_, Connection p_298106_, CommonListenerCookie p_301309_) {
        super(p_301415_, p_298106_, p_301309_);
        this.gameProfile = p_301309_.gameProfile();
        this.clientInformation = p_301309_.clientInformation();
    }

    @Override
    protected GameProfile playerProfile() {
        return this.gameProfile;
    }

    @Override
    public void onDisconnect(DisconnectionDetails p_345446_) {
        LOGGER.info("{} lost connection: {}", this.gameProfile, p_345446_.reason().getString());
        super.onDisconnect(p_345446_);
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    public void startConfiguration() {
        this.send(new ClientboundCustomPayloadPacket(new BrandPayload(this.server.getServerModName())));
        ServerLinks serverlinks = this.server.serverLinks();
        if (!serverlinks.isEmpty()) {
            this.send(new ClientboundServerLinksPacket(serverlinks.untrust()));
        }

        LayeredRegistryAccess<RegistryLayer> layeredregistryaccess = this.server.registries();
        List<KnownPack> list = this.server.getResourceManager().listPacks().flatMap(p_326454_ -> p_326454_.location().knownPackInfo().stream()).toList();
        this.send(new ClientboundUpdateEnabledFeaturesPacket(FeatureFlags.REGISTRY.toNames(this.server.getWorldData().enabledFeatures())));
        this.synchronizeRegistriesTask = new SynchronizeRegistriesTask(list, layeredregistryaccess);
        this.configurationTasks.add(this.synchronizeRegistriesTask);
        this.addOptionalTasks();
        this.configurationTasks.add(new JoinWorldTask());
        this.startNextTask();
    }

    public void returnToWorld() {
        this.configurationTasks.add(new JoinWorldTask());
        this.startNextTask();
    }

    private void addOptionalTasks() {
        this.server.getServerResourcePack().ifPresent(p_300306_ -> this.configurationTasks.add(new ServerResourcePackConfigurationTask(p_300306_)));
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket p_297305_) {
        this.clientInformation = p_297305_.information();
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket p_300631_) {
        super.handleResourcePackResponse(p_300631_);
        if (p_300631_.action().isTerminal()) {
            this.finishCurrentTask(ServerResourcePackConfigurationTask.TYPE);
        }
    }

    @Override
    public void handleSelectKnownPacks(ServerboundSelectKnownPacks p_330488_) {
        PacketUtils.ensureRunningOnSameThread(p_330488_, this, this.server);
        if (this.synchronizeRegistriesTask == null) {
            throw new IllegalStateException("Unexpected response from client: received pack selection, but no negotiation ongoing");
        } else {
            this.synchronizeRegistriesTask.handleResponse(p_330488_.knownPacks(), this::send);
            this.finishCurrentTask(SynchronizeRegistriesTask.TYPE);
        }
    }

    @Override
    public void handleConfigurationFinished(ServerboundFinishConfigurationPacket p_297811_) {
        PacketUtils.ensureRunningOnSameThread(p_297811_, this, this.server);
        this.finishCurrentTask(JoinWorldTask.TYPE);
        this.connection.setupOutboundProtocol(GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(this.server.registryAccess())));

        try {
            PlayerList playerlist = this.server.getPlayerList();
            if (playerlist.getPlayer(this.gameProfile.getId()) != null) {
                this.disconnect(PlayerList.DUPLICATE_LOGIN_DISCONNECT_MESSAGE);
                return;
            }

            Component component = playerlist.canPlayerLogin(this.connection.getRemoteAddress(), this.gameProfile);
            if (component != null) {
                this.disconnect(component);
                return;
            }

            ServerPlayer serverplayer = playerlist.getPlayerForLogin(this.gameProfile, this.clientInformation);
            playerlist.placeNewPlayer(this.connection, serverplayer, this.createCookie(this.clientInformation));
        } catch (Exception exception) {
            LOGGER.error("Couldn't place player in world", (Throwable)exception);
            this.connection.send(new ClientboundDisconnectPacket(DISCONNECT_REASON_INVALID_DATA));
            this.connection.disconnect(DISCONNECT_REASON_INVALID_DATA);
        }
    }

    @Override
    public void tick() {
        this.keepConnectionAlive();
    }

    private void startNextTask() {
        if (this.currentTask != null) {
            throw new IllegalStateException("Task " + this.currentTask.type().id() + " has not finished yet");
        } else if (this.isAcceptingMessages()) {
            ConfigurationTask configurationtask = this.configurationTasks.poll();
            if (configurationtask != null) {
                this.currentTask = configurationtask;
                configurationtask.start(this::send);
            }
        }
    }

    private void finishCurrentTask(ConfigurationTask.Type p_297864_) {
        ConfigurationTask.Type configurationtask$type = this.currentTask != null ? this.currentTask.type() : null;
        if (!p_297864_.equals(configurationtask$type)) {
            throw new IllegalStateException("Unexpected request for task finish, current task: " + configurationtask$type + ", requested: " + p_297864_);
        } else {
            this.currentTask = null;
            this.startNextTask();
        }
    }
}