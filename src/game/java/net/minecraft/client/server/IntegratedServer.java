package net.minecraft.client.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.stats.Stats;
import net.minecraft.util.ModCheck;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class IntegratedServer extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_SIM_DISTANCE = 2;
    private final Minecraft minecraft;
    private boolean paused = true;
    private int publishedPort = -1;
    @Nullable
    private GameType publishedGameType;
    @Nullable
    private LanServerPinger lanPinger;
    @Nullable
    private UUID uuid;
    private int previousSimulationDistance = 0;

    public IntegratedServer(
        Thread p_235248_,
        Minecraft p_235249_,
        LevelStorageSource.LevelStorageAccess p_235250_,
        PackRepository p_235251_,
        WorldStem p_235252_,
        Services p_235253_,
        ChunkProgressListenerFactory p_235254_
    ) {
        super(p_235248_, p_235250_, p_235251_, p_235252_, p_235249_.getProxy(), p_235249_.getFixerUpper(), p_235253_, p_235254_);
        this.setSingleplayerProfile(p_235249_.getGameProfile());
        this.setDemo(p_235249_.isDemo());
        this.setPlayerList(new IntegratedPlayerList(this, this.registries(), this.playerDataStorage));
        this.minecraft = p_235249_;
    }

    @Override
    public boolean initServer() {
        LOGGER.info("Starting integrated minecraft server version {}", SharedConstants.getCurrentVersion().getName());
        this.setUsesAuthentication(true);
        this.setPvpAllowed(true);
        this.setFlightAllowed(true);
        this.initializeKeyPair();
        this.loadLevel();
        GameProfile gameprofile = this.getSingleplayerProfile();
        String s = this.getWorldData().getLevelName();
        this.setMotd(gameprofile != null ? gameprofile.getName() + " - " + s : s);
        return true;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void tickServer(BooleanSupplier p_120049_) {
        boolean flag = this.paused;
        this.paused = Minecraft.getInstance().isPaused();
        ProfilerFiller profilerfiller = Profiler.get();
        if (!flag && this.paused) {
            profilerfiller.push("autoSave");
            LOGGER.info("Saving and pausing game...");
            this.saveEverything(false, false, false);
            profilerfiller.pop();
        }

        boolean flag1 = Minecraft.getInstance().getConnection() != null;
        if (flag1 && this.paused) {
            this.tickPaused();
        } else {
            if (flag && !this.paused) {
                this.forceTimeSynchronization();
            }

            super.tickServer(p_120049_);
            int i = Math.max(2, this.minecraft.options.renderDistance().get());
            if (i != this.getPlayerList().getViewDistance()) {
                LOGGER.info("Changing view distance to {}, from {}", i, this.getPlayerList().getViewDistance());
                this.getPlayerList().setViewDistance(i);
            }

            int j = Math.max(2, this.minecraft.options.simulationDistance().get());
            if (j != this.previousSimulationDistance) {
                LOGGER.info("Changing simulation distance to {}, from {}", j, this.previousSimulationDistance);
                this.getPlayerList().setSimulationDistance(j);
                this.previousSimulationDistance = j;
            }
        }
    }

    protected LocalSampleLogger getTickTimeLogger() {
        return this.minecraft.getDebugOverlay().getTickTimeLogger();
    }

    @Override
    public boolean isTickTimeLoggingEnabled() {
        return true;
    }

    private void tickPaused() {
        for (ServerPlayer serverplayer : this.getPlayerList().getPlayers()) {
            serverplayer.awardStat(Stats.TOTAL_WORLD_TIME);
        }
    }

    @Override
    public boolean shouldRconBroadcast() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return true;
    }

    @Override
    public Path getServerDirectory() {
        return this.minecraft.gameDirectory.toPath();
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return 0;
    }

    @Override
    public boolean isEpollEnabled() {
        return false;
    }

    @Override
    public void onServerCrash(CrashReport p_120051_) {
        this.minecraft.delayCrashRaw(p_120051_);
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport p_174970_) {
        p_174970_.setDetail("Type", "Integrated Server (map_client.txt)");
        p_174970_.setDetail("Is Modded", () -> this.getModdedStatus().fullDescription());
        p_174970_.setDetail("Launched Version", this.minecraft::getLaunchedVersion);
        return p_174970_;
    }

    @Override
    public ModCheck getModdedStatus() {
        return Minecraft.checkModStatus().merge(super.getModdedStatus());
    }

    @Override
    public boolean publishServer(@Nullable GameType p_120041_, boolean p_120042_, int p_120043_) {
        try {
            this.minecraft.prepareForMultiplayer();
            this.minecraft.getConnection().prepareKeyPair();
            this.getConnection().startTcpServerListener(null, p_120043_);
            LOGGER.info("Started serving on {}", p_120043_);
            this.publishedPort = p_120043_;
            this.lanPinger = new LanServerPinger(this.getMotd(), p_120043_ + "");
            this.lanPinger.start();
            this.publishedGameType = p_120041_;
            this.getPlayerList().setAllowCommandsForAllPlayers(p_120042_);
            int i = this.getProfilePermissions(this.minecraft.player.getGameProfile());
            this.minecraft.player.setPermissionLevel(i);

            for (ServerPlayer serverplayer : this.getPlayerList().getPlayers()) {
                this.getCommands().sendCommands(serverplayer);
            }

            return true;
        } catch (IOException ioexception) {
            return false;
        }
    }

    @Override
    public void stopServer() {
        super.stopServer();
        if (this.lanPinger != null) {
            this.lanPinger.interrupt();
            this.lanPinger = null;
        }
    }

    @Override
    public void halt(boolean p_120053_) {
        this.executeBlocking(() -> {
            for (ServerPlayer serverplayer : Lists.newArrayList(this.getPlayerList().getPlayers())) {
                if (!serverplayer.getUUID().equals(this.uuid)) {
                    this.getPlayerList().remove(serverplayer);
                }
            }
        });
        super.halt(p_120053_);
        if (this.lanPinger != null) {
            this.lanPinger.interrupt();
            this.lanPinger = null;
        }
    }

    @Override
    public boolean isPublished() {
        return this.publishedPort > -1;
    }

    @Override
    public int getPort() {
        return this.publishedPort;
    }

    @Override
    public void setDefaultGameType(GameType p_120039_) {
        super.setDefaultGameType(p_120039_);
        this.publishedGameType = null;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return true;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return 2;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return 2;
    }

    public void setUUID(UUID p_120047_) {
        this.uuid = p_120047_;
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile p_120045_) {
        return this.getSingleplayerProfile() != null && p_120045_.getName().equalsIgnoreCase(this.getSingleplayerProfile().getName());
    }

    @Override
    public int getScaledTrackingDistance(int p_120056_) {
        return (int)(this.minecraft.options.entityDistanceScaling().get() * (double)p_120056_);
    }

    @Override
    public boolean forceSynchronousWrites() {
        return this.minecraft.options.syncWrites;
    }

    @Nullable
    @Override
    public GameType getForcedGameType() {
        return this.isPublished() && !this.isHardcore() ? MoreObjects.firstNonNull(this.publishedGameType, this.worldData.getGameType()) : null;
    }

    @Override
    public boolean saveEverything(boolean p_329604_, boolean p_328766_, boolean p_334434_) {
        boolean flag = super.saveEverything(p_329604_, p_328766_, p_334434_);
        this.warnOnLowDiskSpace();
        return flag;
    }

    private void warnOnLowDiskSpace() {
        if (this.storageSource.checkForLowDiskSpace()) {
            this.minecraft.execute(() -> SystemToast.onLowDiskSpace(this.minecraft));
        }
    }

    @Override
    public void reportChunkLoadFailure(Throwable p_344018_, RegionStorageInfo p_345415_, ChunkPos p_335057_) {
        super.reportChunkLoadFailure(p_344018_, p_345415_, p_335057_);
        this.warnOnLowDiskSpace();
        this.minecraft.execute(() -> SystemToast.onChunkLoadFailure(this.minecraft, p_335057_));
    }

    @Override
    public void reportChunkSaveFailure(Throwable p_345295_, RegionStorageInfo p_345019_, ChunkPos p_328809_) {
        super.reportChunkSaveFailure(p_345295_, p_345019_, p_328809_);
        this.warnOnLowDiskSpace();
        this.minecraft.execute(() -> SystemToast.onChunkSaveFailure(this.minecraft, p_328809_));
    }
}