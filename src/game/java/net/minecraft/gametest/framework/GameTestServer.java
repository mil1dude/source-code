package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.debugchart.SampleLogger;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.slf4j.Logger;

public class GameTestServer extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PROGRESS_REPORT_INTERVAL = 20;
    private static final int TEST_POSITION_RANGE = 14999992;
    private static final Services NO_SERVICES = new Services(null, ServicesKeySet.EMPTY, null, null);
    private static final FeatureFlagSet ENABLED_FEATURES = FeatureFlags.REGISTRY
        .allFlags()
        .subtract(FeatureFlagSet.of(FeatureFlags.REDSTONE_EXPERIMENTS, FeatureFlags.MINECART_IMPROVEMENTS));
    private final LocalSampleLogger sampleLogger = new LocalSampleLogger(4);
    private List<GameTestBatch> testBatches = new ArrayList<>();
    private final List<TestFunction> testFunctions;
    private final BlockPos spawnPos;
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private static final GameRules TEST_GAME_RULES = Util.make(new GameRules(ENABLED_FEATURES), p_341096_ -> {
        p_341096_.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
        p_341096_.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        p_341096_.getRule(GameRules.RULE_RANDOMTICKING).set(0, null);
        p_341096_.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
    });
    private static final WorldOptions WORLD_OPTIONS = new WorldOptions(0L, false, false);
    @Nullable
    private MultipleTestTracker testTracker;

    public static GameTestServer create(
        Thread p_206607_, LevelStorageSource.LevelStorageAccess p_206608_, PackRepository p_206609_, Collection<TestFunction> p_206610_, BlockPos p_206611_
    ) {
        if (p_206610_.isEmpty()) {
            throw new IllegalArgumentException("No test functions were given!");
        } else {
            p_206609_.reload();
            WorldDataConfiguration worlddataconfiguration = new WorldDataConfiguration(
                new DataPackConfig(new ArrayList<>(p_206609_.getAvailableIds()), List.of()), ENABLED_FEATURES
            );
            LevelSettings levelsettings = new LevelSettings("Test Level", GameType.CREATIVE, false, Difficulty.NORMAL, true, TEST_GAME_RULES, worlddataconfiguration);
            WorldLoader.PackConfig worldloader$packconfig = new WorldLoader.PackConfig(p_206609_, worlddataconfiguration, false, true);
            WorldLoader.InitConfig worldloader$initconfig = new WorldLoader.InitConfig(worldloader$packconfig, Commands.CommandSelection.DEDICATED, 4);

            try {
                LOGGER.debug("Starting resource loading");
                Stopwatch stopwatch = Stopwatch.createStarted();
                WorldStem worldstem = Util.<WorldStem>blockUntilDone(
                        p_358474_ -> WorldLoader.load(
                                worldloader$initconfig,
                                p_358476_ -> {
                                    Registry<LevelStem> registry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable()).freeze();
                                    WorldDimensions.Complete worlddimensions$complete = p_358476_.datapackWorldgen()
                                        .lookupOrThrow(Registries.WORLD_PRESET)
                                        .getOrThrow(WorldPresets.FLAT)
                                        .value()
                                        .createWorldDimensions()
                                        .bake(registry);
                                    return new WorldLoader.DataLoadOutput<>(
                                        new PrimaryLevelData(
                                            levelsettings, WORLD_OPTIONS, worlddimensions$complete.specialWorldProperty(), worlddimensions$complete.lifecycle()
                                        ),
                                        worlddimensions$complete.dimensionsRegistryAccess()
                                    );
                                },
                                WorldStem::new,
                                Util.backgroundExecutor(),
                                p_358474_
                            )
                    )
                    .get();
                stopwatch.stop();
                LOGGER.debug("Finished resource loading after {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                return new GameTestServer(p_206607_, p_206608_, p_206609_, worldstem, p_206610_, p_206611_);
            } catch (Exception exception) {
                LOGGER.warn("Failed to load vanilla datapack, bit oops", (Throwable)exception);
                System.exit(-1);
                throw new IllegalStateException();
            }
        }
    }

    private GameTestServer(
        Thread p_206597_,
        LevelStorageSource.LevelStorageAccess p_206598_,
        PackRepository p_206599_,
        WorldStem p_206600_,
        Collection<TestFunction> p_206601_,
        BlockPos p_206602_
    ) {
        super(p_206597_, p_206598_, p_206599_, p_206600_, Proxy.NO_PROXY, DataFixers.getDataFixer(), NO_SERVICES, LoggerChunkProgressListener::createFromGameruleRadius);
        this.testFunctions = Lists.newArrayList(p_206601_);
        this.spawnPos = p_206602_;
    }

    @Override
    public boolean initServer() {
        this.setPlayerList(new PlayerList(this, this.registries(), this.playerDataStorage, 1) {
        });
        this.loadLevel();
        ServerLevel serverlevel = this.overworld();
        this.testBatches = Lists.newArrayList(GameTestBatchFactory.fromTestFunction(this.testFunctions, serverlevel));
        serverlevel.setDefaultSpawnPos(this.spawnPos, 0.0F);
        int i = 20000000;
        serverlevel.setWeatherParameters(20000000, 20000000, false, false);
        LOGGER.info("Started game test server");
        return true;
    }

    @Override
    public void tickServer(BooleanSupplier p_177619_) {
        super.tickServer(p_177619_);
        ServerLevel serverlevel = this.overworld();
        if (!this.haveTestsStarted()) {
            this.startTests(serverlevel);
        }

        if (serverlevel.getGameTime() % 20L == 0L) {
            LOGGER.info(this.testTracker.getProgressBar());
        }

        if (this.testTracker.isDone()) {
            this.halt(false);
            LOGGER.info(this.testTracker.getProgressBar());
            GlobalTestReporter.finish();
            LOGGER.info("========= {} GAME TESTS COMPLETE IN {} ======================", this.testTracker.getTotalCount(), this.stopwatch.stop());
            if (this.testTracker.hasFailedRequired()) {
                LOGGER.info("{} required tests failed :(", this.testTracker.getFailedRequiredCount());
                this.testTracker.getFailedRequired().forEach(p_206615_ -> LOGGER.info("   - {}", p_206615_.getTestName()));
            } else {
                LOGGER.info("All {} required tests passed :)", this.testTracker.getTotalCount());
            }

            if (this.testTracker.hasFailedOptional()) {
                LOGGER.info("{} optional tests failed", this.testTracker.getFailedOptionalCount());
                this.testTracker.getFailedOptional().forEach(p_358471_ -> LOGGER.info("   - {} with rotation: {}", p_358471_.getTestName(), p_358471_.getRotation()));
            }

            LOGGER.info("====================================================");
        }
    }

    @Override
    public SampleLogger getTickTimeLogger() {
        return this.sampleLogger;
    }

    @Override
    public boolean isTickTimeLoggingEnabled() {
        return false;
    }

    @Override
    public void waitUntilNextTick() {
        this.runAllTasks();
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport p_177613_) {
        p_177613_.setDetail("Type", "Game test server");
        return p_177613_;
    }

    @Override
    public void onServerExit() {
        super.onServerExit();
        LOGGER.info("Game test server shutting down");
        System.exit(this.testTracker.getFailedRequiredCount());
    }

    @Override
    public void onServerCrash(CrashReport p_177623_) {
        super.onServerCrash(p_177623_);
        LOGGER.error("Game test server crashed\n{}", p_177623_.getFriendlyReport(ReportType.CRASH));
        System.exit(1);
    }

    private void startTests(ServerLevel p_177625_) {
        BlockPos blockpos = new BlockPos(p_177625_.random.nextIntBetweenInclusive(-14999992, 14999992), -59, p_177625_.random.nextIntBetweenInclusive(-14999992, 14999992));
        GameTestRunner gametestrunner = GameTestRunner.Builder.fromBatches(this.testBatches, p_177625_)
            .newStructureSpawner(new StructureGridSpawner(blockpos, 8, false))
            .build();
        Collection<GameTestInfo> collection = gametestrunner.getTestInfos();
        this.testTracker = new MultipleTestTracker(collection);
        LOGGER.info("{} tests are now running at position {}!", this.testTracker.getTotalCount(), blockpos.toShortString());
        this.stopwatch.reset();
        this.stopwatch.start();
        gametestrunner.start();
    }

    private boolean haveTestsStarted() {
        return this.testTracker != null;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return 0;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return 4;
    }

    @Override
    public boolean shouldRconBroadcast() {
        return false;
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
    public boolean isCommandBlockEnabled() {
        return true;
    }

    @Override
    public boolean isPublished() {
        return false;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile p_177617_) {
        return false;
    }
}