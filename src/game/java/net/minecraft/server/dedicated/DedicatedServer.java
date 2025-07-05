package net.minecraft.server.dedicated;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerTextFilter;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.DebugSampleSubscriptionTracker;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.util.debugchart.RemoteSampleLogger;
import net.minecraft.util.debugchart.SampleLogger;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.util.monitoring.jmx.MinecraftServerStatistics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

public class DedicatedServer extends MinecraftServer implements ServerInterface {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int CONVERSION_RETRY_DELAY_MS = 5000;
    private static final int CONVERSION_RETRIES = 2;
    private final List<ConsoleInput> consoleInput = Collections.synchronizedList(Lists.newArrayList());
    @Nullable
    private QueryThreadGs4 queryThreadGs4;
    private final RconConsoleSource rconConsoleSource;
    @Nullable
    private RconThread rconThread;
    private final DedicatedServerSettings settings;
    @Nullable
    private MinecraftServerGui gui;
    @Nullable
    private final ServerTextFilter serverTextFilter;
    @Nullable
    private RemoteSampleLogger tickTimeLogger;
    @Nullable
    private DebugSampleSubscriptionTracker debugSampleSubscriptionTracker;
    private final ServerLinks serverLinks;

    public DedicatedServer(
        Thread p_214789_,
        LevelStorageSource.LevelStorageAccess p_214790_,
        PackRepository p_214791_,
        WorldStem p_214792_,
        DedicatedServerSettings p_214793_,
        DataFixer p_214794_,
        Services p_214795_,
        ChunkProgressListenerFactory p_214796_
    ) {
        super(p_214789_, p_214790_, p_214791_, p_214792_, Proxy.NO_PROXY, p_214794_, p_214795_, p_214796_);
        this.settings = p_214793_;
        this.rconConsoleSource = new RconConsoleSource(this);
        this.serverTextFilter = ServerTextFilter.createFromConfig(p_214793_.getProperties());
        this.serverLinks = createServerLinks(p_214793_);
    }

    @Override
    public boolean initServer() throws IOException {
        Thread thread = new Thread("Server console handler") {
            @Override
            public void run() {
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

                String s1;
                try {
                    while (!DedicatedServer.this.isStopped() && DedicatedServer.this.isRunning() && (s1 = bufferedreader.readLine()) != null) {
                        DedicatedServer.this.handleConsoleInput(s1, DedicatedServer.this.createCommandSourceStack());
                    }
                } catch (IOException ioexception1) {
                    DedicatedServer.LOGGER.error("Exception handling console input", (Throwable)ioexception1);
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
        LOGGER.info("Starting minecraft server version {}", SharedConstants.getCurrentVersion().getName());
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
            LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        }

        LOGGER.info("Loading properties");
        DedicatedServerProperties dedicatedserverproperties = this.settings.getProperties();
        if (this.isSingleplayer()) {
            this.setLocalIp("127.0.0.1");
        } else {
            this.setUsesAuthentication(dedicatedserverproperties.onlineMode);
            this.setPreventProxyConnections(dedicatedserverproperties.preventProxyConnections);
            this.setLocalIp(dedicatedserverproperties.serverIp);
        }

        this.setPvpAllowed(dedicatedserverproperties.pvp);
        this.setFlightAllowed(dedicatedserverproperties.allowFlight);
        this.setMotd(dedicatedserverproperties.motd);
        super.setPlayerIdleTimeout(dedicatedserverproperties.playerIdleTimeout.get());
        this.setEnforceWhitelist(dedicatedserverproperties.enforceWhitelist);
        this.worldData.setGameType(dedicatedserverproperties.gamemode);
        LOGGER.info("Default game type: {}", dedicatedserverproperties.gamemode);
        InetAddress inetaddress = null;
        if (!this.getLocalIp().isEmpty()) {
            inetaddress = InetAddress.getByName(this.getLocalIp());
        }

        if (this.getPort() < 0) {
            this.setPort(dedicatedserverproperties.serverPort);
        }

        this.initializeKeyPair();
        LOGGER.info("Starting Minecraft server on {}:{}", this.getLocalIp().isEmpty() ? "*" : this.getLocalIp(), this.getPort());

        try {
            this.getConnection().startTcpServerListener(inetaddress, this.getPort());
        } catch (IOException ioexception) {
            LOGGER.warn("**** FAILED TO BIND TO PORT!");
            LOGGER.warn("The exception was: {}", ioexception.toString());
            LOGGER.warn("Perhaps a server is already running on that port?");
            return false;
        }

        if (!this.usesAuthentication()) {
            LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
            LOGGER.warn(
                "While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose."
            );
            LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
        }

        if (this.convertOldUsers()) {
            this.getProfileCache().save();
        }

        if (!OldUsersConverter.serverReadyAfterUserconversion(this)) {
            return false;
        } else {
            this.setPlayerList(new DedicatedPlayerList(this, this.registries(), this.playerDataStorage));
            this.debugSampleSubscriptionTracker = new DebugSampleSubscriptionTracker(this.getPlayerList());
            this.tickTimeLogger = new RemoteSampleLogger(TpsDebugDimensions.values().length, this.debugSampleSubscriptionTracker, RemoteDebugSampleType.TICK_TIME);
            long i = Util.getNanos();
            SkullBlockEntity.setup(this.services, this);
            GameProfileCache.setUsesAuthentication(this.usesAuthentication());
            LOGGER.info("Preparing level \"{}\"", this.getLevelIdName());
            this.loadLevel();
            long j = Util.getNanos() - i;
            String s = String.format(Locale.ROOT, "%.3fs", (double)j / 1.0E9);
            LOGGER.info("Done ({})! For help, type \"help\"", s);
            if (dedicatedserverproperties.announcePlayerAchievements != null) {
                this.getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(dedicatedserverproperties.announcePlayerAchievements, this);
            }

            if (dedicatedserverproperties.enableQuery) {
                LOGGER.info("Starting GS4 status listener");
                this.queryThreadGs4 = QueryThreadGs4.create(this);
            }

            if (dedicatedserverproperties.enableRcon) {
                LOGGER.info("Starting remote control listener");
                this.rconThread = RconThread.create(this);
            }

            if (this.getMaxTickLength() > 0L) {
                Thread thread1 = new Thread(new ServerWatchdog(this));
                thread1.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
                thread1.setName("Server Watchdog");
                thread1.setDaemon(true);
                thread1.start();
            }

            if (dedicatedserverproperties.enableJmxMonitoring) {
                MinecraftServerStatistics.registerJmxMonitoring(this);
                LOGGER.info("JMX monitoring enabled");
            }

            return true;
        }
    }

    @Override
    public boolean isSpawningMonsters() {
        return this.settings.getProperties().spawnMonsters && super.isSpawningMonsters();
    }

    @Override
    public DedicatedServerProperties getProperties() {
        return this.settings.getProperties();
    }

    @Override
    public void forceDifficulty() {
        this.setDifficulty(this.getProperties().difficulty, true);
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport p_142870_) {
        p_142870_.setDetail("Is Modded", () -> this.getModdedStatus().fullDescription());
        p_142870_.setDetail("Type", () -> "Dedicated Server (map_server.txt)");
        return p_142870_;
    }

    @Override
    public void dumpServerProperties(Path p_142872_) throws IOException {
        DedicatedServerProperties dedicatedserverproperties = this.getProperties();

        try (Writer writer = Files.newBufferedWriter(p_142872_)) {
            writer.write(String.format(Locale.ROOT, "sync-chunk-writes=%s%n", dedicatedserverproperties.syncChunkWrites));
            writer.write(String.format(Locale.ROOT, "gamemode=%s%n", dedicatedserverproperties.gamemode));
            writer.write(String.format(Locale.ROOT, "spawn-monsters=%s%n", dedicatedserverproperties.spawnMonsters));
            writer.write(String.format(Locale.ROOT, "entity-broadcast-range-percentage=%d%n", dedicatedserverproperties.entityBroadcastRangePercentage));
            writer.write(String.format(Locale.ROOT, "max-world-size=%d%n", dedicatedserverproperties.maxWorldSize));
            writer.write(String.format(Locale.ROOT, "view-distance=%d%n", dedicatedserverproperties.viewDistance));
            writer.write(String.format(Locale.ROOT, "simulation-distance=%d%n", dedicatedserverproperties.simulationDistance));
            writer.write(String.format(Locale.ROOT, "generate-structures=%s%n", dedicatedserverproperties.worldOptions.generateStructures()));
            writer.write(String.format(Locale.ROOT, "use-native=%s%n", dedicatedserverproperties.useNativeTransport));
            writer.write(String.format(Locale.ROOT, "rate-limit=%d%n", dedicatedserverproperties.rateLimitPacketsPerSecond));
        }
    }

    @Override
    public void onServerExit() {
        if (this.serverTextFilter != null) {
            this.serverTextFilter.close();
        }

        if (this.gui != null) {
            this.gui.close();
        }

        if (this.rconThread != null) {
            this.rconThread.stop();
        }

        if (this.queryThreadGs4 != null) {
            this.queryThreadGs4.stop();
        }
    }

    @Override
    public void tickConnection() {
        super.tickConnection();
        this.handleConsoleInputs();
    }

    @Override
    public boolean isLevelEnabled(Level p_344961_) {
        return p_344961_.dimension() == Level.NETHER ? this.getProperties().allowNether : true;
    }

    public void handleConsoleInput(String p_139646_, CommandSourceStack p_139647_) {
        this.consoleInput.add(new ConsoleInput(p_139646_, p_139647_));
    }

    public void handleConsoleInputs() {
        while (!this.consoleInput.isEmpty()) {
            ConsoleInput consoleinput = this.consoleInput.remove(0);
            this.getCommands().performPrefixedCommand(consoleinput.source, consoleinput.msg);
        }
    }

    @Override
    public boolean isDedicatedServer() {
        return true;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return this.getProperties().rateLimitPacketsPerSecond;
    }

    @Override
    public boolean isEpollEnabled() {
        return this.getProperties().useNativeTransport;
    }

    public DedicatedPlayerList getPlayerList() {
        return (DedicatedPlayerList)super.getPlayerList();
    }

    @Override
    public boolean isPublished() {
        return true;
    }

    @Override
    public String getServerIp() {
        return this.getLocalIp();
    }

    @Override
    public int getServerPort() {
        return this.getPort();
    }

    @Override
    public String getServerName() {
        return this.getMotd();
    }

    public void showGui() {
        if (this.gui == null) {
            this.gui = MinecraftServerGui.showFrameFor(this);
        }
    }

    @Override
    public boolean hasGui() {
        return this.gui != null;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return this.getProperties().enableCommandBlock;
    }

    @Override
    public int getSpawnProtectionRadius() {
        return this.getProperties().spawnProtection;
    }

    @Override
    public boolean isUnderSpawnProtection(ServerLevel p_139630_, BlockPos p_139631_, Player p_139632_) {
        if (p_139630_.dimension() != Level.OVERWORLD) {
            return false;
        } else if (this.getPlayerList().getOps().isEmpty()) {
            return false;
        } else if (this.getPlayerList().isOp(p_139632_.getGameProfile())) {
            return false;
        } else if (this.getSpawnProtectionRadius() <= 0) {
            return false;
        } else {
            BlockPos blockpos = p_139630_.getSharedSpawnPos();
            int i = Mth.abs(p_139631_.getX() - blockpos.getX());
            int j = Mth.abs(p_139631_.getZ() - blockpos.getZ());
            int k = Math.max(i, j);
            return k <= this.getSpawnProtectionRadius();
        }
    }

    @Override
    public boolean repliesToStatus() {
        return this.getProperties().enableStatus;
    }

    @Override
    public boolean hidesOnlinePlayers() {
        return this.getProperties().hideOnlinePlayers;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return this.getProperties().opPermissionLevel;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return this.getProperties().functionPermissionLevel;
    }

    @Override
    public void setPlayerIdleTimeout(int p_139676_) {
        super.setPlayerIdleTimeout(p_139676_);
        this.settings.update(p_358657_ -> p_358657_.playerIdleTimeout.update(this.registryAccess(), p_139676_));
    }

    @Override
    public boolean shouldRconBroadcast() {
        return this.getProperties().broadcastRconToOps;
    }

    @Override
    public boolean shouldInformAdmins() {
        return this.getProperties().broadcastConsoleToOps;
    }

    @Override
    public int getAbsoluteMaxWorldSize() {
        return this.getProperties().maxWorldSize;
    }

    @Override
    public int getCompressionThreshold() {
        return this.getProperties().networkCompressionThreshold;
    }

    @Override
    public boolean enforceSecureProfile() {
        DedicatedServerProperties dedicatedserverproperties = this.getProperties();
        return dedicatedserverproperties.enforceSecureProfile && dedicatedserverproperties.onlineMode && this.services.canValidateProfileKeys();
    }

    @Override
    public boolean logIPs() {
        return this.getProperties().logIPs;
    }

    protected boolean convertOldUsers() {
        boolean flag = false;

        for (int i = 0; !flag && i <= 2; i++) {
            if (i > 0) {
                LOGGER.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
                this.waitForRetry();
            }

            flag = OldUsersConverter.convertUserBanlist(this);
        }

        boolean flag1 = false;

        for (int j = 0; !flag1 && j <= 2; j++) {
            if (j > 0) {
                LOGGER.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
                this.waitForRetry();
            }

            flag1 = OldUsersConverter.convertIpBanlist(this);
        }

        boolean flag2 = false;

        for (int k = 0; !flag2 && k <= 2; k++) {
            if (k > 0) {
                LOGGER.warn("Encountered a problem while converting the op list, retrying in a few seconds");
                this.waitForRetry();
            }

            flag2 = OldUsersConverter.convertOpsList(this);
        }

        boolean flag3 = false;

        for (int l = 0; !flag3 && l <= 2; l++) {
            if (l > 0) {
                LOGGER.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
                this.waitForRetry();
            }

            flag3 = OldUsersConverter.convertWhiteList(this);
        }

        boolean flag4 = false;

        for (int i1 = 0; !flag4 && i1 <= 2; i1++) {
            if (i1 > 0) {
                LOGGER.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
                this.waitForRetry();
            }

            flag4 = OldUsersConverter.convertPlayers(this);
        }

        return flag || flag1 || flag2 || flag3 || flag4;
    }

    private void waitForRetry() {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException interruptedexception) {
        }
    }

    public long getMaxTickLength() {
        return this.getProperties().maxTickTime;
    }

    @Override
    public int getMaxChainedNeighborUpdates() {
        return this.getProperties().maxChainedNeighborUpdates;
    }

    @Override
    public String getPluginNames() {
        return "";
    }

    @Override
    public String runCommand(String p_139644_) {
        this.rconConsoleSource.prepareForCommand();
        this.executeBlocking(() -> this.getCommands().performPrefixedCommand(this.rconConsoleSource.createCommandSourceStack(), p_139644_));
        return this.rconConsoleSource.getCommandResponse();
    }

    public void storeUsingWhiteList(boolean p_139689_) {
        this.settings.update(p_358659_ -> p_358659_.whiteList.update(this.registryAccess(), p_139689_));
    }

    @Override
    public void stopServer() {
        super.stopServer();
        Util.shutdownExecutors();
        SkullBlockEntity.clear();
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile p_139642_) {
        return false;
    }

    @Override
    public int getScaledTrackingDistance(int p_139659_) {
        return this.getProperties().entityBroadcastRangePercentage * p_139659_ / 100;
    }

    @Override
    public String getLevelIdName() {
        return this.storageSource.getLevelId();
    }

    @Override
    public boolean forceSynchronousWrites() {
        return this.settings.getProperties().syncChunkWrites;
    }

    @Override
    public TextFilter createTextFilterForPlayer(ServerPlayer p_139634_) {
        return this.serverTextFilter != null ? this.serverTextFilter.createContext(p_139634_.getGameProfile()) : TextFilter.DUMMY;
    }

    @Nullable
    @Override
    public GameType getForcedGameType() {
        return this.settings.getProperties().forceGameMode ? this.worldData.getGameType() : null;
    }

    @Override
    public Optional<MinecraftServer.ServerResourcePackInfo> getServerResourcePack() {
        return this.settings.getProperties().serverResourcePackInfo;
    }

    @Override
    public void endMetricsRecordingTick() {
        super.endMetricsRecordingTick();
        this.debugSampleSubscriptionTracker.tick(this.getTickCount());
    }

    @Override
    public SampleLogger getTickTimeLogger() {
        return this.tickTimeLogger;
    }

    @Override
    public boolean isTickTimeLoggingEnabled() {
        return this.debugSampleSubscriptionTracker.shouldLogSamples(RemoteDebugSampleType.TICK_TIME);
    }

    @Override
    public void subscribeToDebugSample(ServerPlayer p_328957_, RemoteDebugSampleType p_333036_) {
        this.debugSampleSubscriptionTracker.subscribe(p_328957_, p_333036_);
    }

    @Override
    public boolean acceptsTransfers() {
        return this.settings.getProperties().acceptsTransfers;
    }

    @Override
    public ServerLinks serverLinks() {
        return this.serverLinks;
    }

    @Override
    public int pauseWhileEmptySeconds() {
        return this.settings.getProperties().pauseWhenEmptySeconds;
    }

    private static ServerLinks createServerLinks(DedicatedServerSettings p_343848_) {
        Optional<URI> optional = parseBugReportLink(p_343848_.getProperties());
        return optional.<ServerLinks>map(p_341204_ -> new ServerLinks(List.of(ServerLinks.KnownLinkType.BUG_REPORT.create(p_341204_))))
            .orElse(ServerLinks.EMPTY);
    }

    private static Optional<URI> parseBugReportLink(DedicatedServerProperties p_342981_) {
        String s = p_342981_.bugReportLink;
        if (s.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(Util.parseAndValidateUntrustedUri(s));
            } catch (Exception exception) {
                LOGGER.warn("Failed to parse bug link {}", s, exception);
                return Optional.empty();
            }
        }
    }
}