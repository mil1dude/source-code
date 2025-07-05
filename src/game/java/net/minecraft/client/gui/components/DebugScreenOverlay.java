package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debugchart.BandwidthDebugChart;
import net.minecraft.client.gui.components.debugchart.FpsDebugChart;
import net.minecraft.client.gui.components.debugchart.PingDebugChart;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;
import net.minecraft.client.gui.components.debugchart.TpsDebugChart;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugScreenOverlay {
    private static final int COLOR_GREY = 14737632;
    private static final int MARGIN_RIGHT = 2;
    private static final int MARGIN_LEFT = 2;
    private static final int MARGIN_TOP = 2;
    private static final Map<Heightmap.Types, String> HEIGHTMAP_NAMES = Util.make(new EnumMap<>(Heightmap.Types.class), p_94070_ -> {
        p_94070_.put(Heightmap.Types.WORLD_SURFACE_WG, "SW");
        p_94070_.put(Heightmap.Types.WORLD_SURFACE, "S");
        p_94070_.put(Heightmap.Types.OCEAN_FLOOR_WG, "OW");
        p_94070_.put(Heightmap.Types.OCEAN_FLOOR, "O");
        p_94070_.put(Heightmap.Types.MOTION_BLOCKING, "M");
        p_94070_.put(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "ML");
    });
    private final Minecraft minecraft;
    private final DebugScreenOverlay.AllocationRateCalculator allocationRateCalculator;
    private final Font font;
    private HitResult block;
    private HitResult liquid;
    @Nullable
    private ChunkPos lastPos;
    @Nullable
    private LevelChunk clientChunk;
    @Nullable
    private CompletableFuture<LevelChunk> serverChunk;
    private boolean renderDebug;
    private boolean renderProfilerChart;
    private boolean renderFpsCharts;
    private boolean renderNetworkCharts;
    private final LocalSampleLogger frameTimeLogger = new LocalSampleLogger(1);
    private final LocalSampleLogger tickTimeLogger = new LocalSampleLogger(TpsDebugDimensions.values().length);
    private final LocalSampleLogger pingLogger = new LocalSampleLogger(1);
    private final LocalSampleLogger bandwidthLogger = new LocalSampleLogger(1);
    private final Map<RemoteDebugSampleType, LocalSampleLogger> remoteSupportingLoggers = Map.of(RemoteDebugSampleType.TICK_TIME, this.tickTimeLogger);
    private final FpsDebugChart fpsChart;
    private final TpsDebugChart tpsChart;
    private final PingDebugChart pingChart;
    private final BandwidthDebugChart bandwidthChart;
    private final ProfilerPieChart profilerPieChart;

    public DebugScreenOverlay(Minecraft p_94039_) {
        this.minecraft = p_94039_;
        this.allocationRateCalculator = new DebugScreenOverlay.AllocationRateCalculator();
        this.font = p_94039_.font;
        this.fpsChart = new FpsDebugChart(this.font, this.frameTimeLogger);
        this.tpsChart = new TpsDebugChart(this.font, this.tickTimeLogger, () -> p_94039_.level.tickRateManager().millisecondsPerTick());
        this.pingChart = new PingDebugChart(this.font, this.pingLogger);
        this.bandwidthChart = new BandwidthDebugChart(this.font, this.bandwidthLogger);
        this.profilerPieChart = new ProfilerPieChart(this.font);
    }

    public void clearChunkCache() {
        this.serverChunk = null;
        this.clientChunk = null;
    }

    public void render(GuiGraphics p_281427_) {
        ProfilerFiller profilerfiller = Profiler.get();
        profilerfiller.push("debug");
        Entity entity = this.minecraft.getCameraEntity();
        this.block = entity.pick(20.0, 0.0F, false);
        this.liquid = entity.pick(20.0, 0.0F, true);
        this.drawGameInformation(p_281427_);
        this.drawSystemInformation(p_281427_);
        this.profilerPieChart.setBottomOffset(10);
        if (this.renderFpsCharts) {
            int i = p_281427_.guiWidth();
            int j = i / 2;
            this.fpsChart.drawChart(p_281427_, 0, this.fpsChart.getWidth(j));
            if (this.tickTimeLogger.size() > 0) {
                int k = this.tpsChart.getWidth(j);
                this.tpsChart.drawChart(p_281427_, i - k, k);
            }

            this.profilerPieChart.setBottomOffset(this.tpsChart.getFullHeight());
        }

        if (this.renderNetworkCharts) {
            int l = p_281427_.guiWidth();
            int i1 = l / 2;
            if (!this.minecraft.isLocalServer()) {
                this.bandwidthChart.drawChart(p_281427_, 0, this.bandwidthChart.getWidth(i1));
            }

            int j1 = this.pingChart.getWidth(i1);
            this.pingChart.drawChart(p_281427_, l - j1, j1);
            this.profilerPieChart.setBottomOffset(this.pingChart.getFullHeight());
        }

        try (Zone zone = profilerfiller.zone("profilerPie")) {
            this.profilerPieChart.render(p_281427_);
        }

        profilerfiller.pop();
    }

    protected void drawGameInformation(GuiGraphics p_281525_) {
        List<String> list = this.getGameInformation();
        list.add("");
        boolean flag = this.minecraft.getSingleplayerServer() != null;
        list.add(
            "Debug charts: [F3+1] Profiler "
                + (this.renderProfilerChart ? "visible" : "hidden")
                + "; [F3+2] "
                + (flag ? "FPS + TPS " : "FPS ")
                + (this.renderFpsCharts ? "visible" : "hidden")
                + "; [F3+3] "
                + (!this.minecraft.isLocalServer() ? "Bandwidth + Ping" : "Ping")
                + (this.renderNetworkCharts ? " visible" : " hidden")
        );
        list.add("For help: press F3 + Q");
        this.renderLines(p_281525_, list, true);
    }

    protected void drawSystemInformation(GuiGraphics p_281261_) {
        List<String> list = this.getSystemInformation();
        this.renderLines(p_281261_, list, false);
    }

    private void renderLines(GuiGraphics p_286519_, List<String> p_286665_, boolean p_286644_) {
        int i = 9;

        for (int j = 0; j < p_286665_.size(); j++) {
            String s = p_286665_.get(j);
            if (!Strings.isNullOrEmpty(s)) {
                int k = this.font.width(s);
                int l = p_286644_ ? 2 : p_286519_.guiWidth() - 2 - k;
                int i1 = 2 + i * j;
                p_286519_.fill(l - 1, i1 - 1, l + k + 1, i1 + i - 1, -1873784752);
            }
        }

        for (int j1 = 0; j1 < p_286665_.size(); j1++) {
            String s1 = p_286665_.get(j1);
            if (!Strings.isNullOrEmpty(s1)) {
                int k1 = this.font.width(s1);
                int l1 = p_286644_ ? 2 : p_286519_.guiWidth() - 2 - k1;
                int i2 = 2 + i * j1;
                p_286519_.drawString(this.font, s1, l1, i2, 14737632, false);
            }
        }
    }

    protected List<String> getGameInformation() {
        IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
        ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
        Connection connection = clientpacketlistener.getConnection();
        float f = connection.getAverageSentPackets();
        float f1 = connection.getAverageReceivedPackets();
        TickRateManager tickratemanager = this.getLevel().tickRateManager();
        String s1;
        if (tickratemanager.isSteppingForward()) {
            s1 = " (frozen - stepping)";
        } else if (tickratemanager.isFrozen()) {
            s1 = " (frozen)";
        } else {
            s1 = "";
        }

        String s;
        if (integratedserver != null) {
            ServerTickRateManager servertickratemanager = integratedserver.tickRateManager();
            boolean flag = servertickratemanager.isSprinting();
            if (flag) {
                s1 = " (sprinting)";
            }

            String s2 = flag ? "-" : String.format(Locale.ROOT, "%.1f", tickratemanager.millisecondsPerTick());
            s = String.format(Locale.ROOT, "Integrated server @ %.1f/%s ms%s, %.0f tx, %.0f rx", integratedserver.getCurrentSmoothedTickTime(), s2, s1, f, f1);
        } else {
            s = String.format(Locale.ROOT, "\"%s\" server%s, %.0f tx, %.0f rx", clientpacketlistener.serverBrand(), s1, f, f1);
        }

        BlockPos blockpos = this.minecraft.getCameraEntity().blockPosition();
        if (this.minecraft.showOnlyReducedInfo()) {
            return Lists.newArrayList(
                "Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")",
                this.minecraft.fpsString,
                s,
                this.minecraft.levelRenderer.getSectionStatistics(),
                this.minecraft.levelRenderer.getEntityStatistics(),
                "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(),
                this.minecraft.level.gatherChunkSourceStats(),
                "",
                String.format(Locale.ROOT, "Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15)
            );
        } else {
            Entity entity = this.minecraft.getCameraEntity();
            Direction direction = entity.getDirection();

            String $$21 = switch (direction) {
                case NORTH -> "Towards negative Z";
                case SOUTH -> "Towards positive Z";
                case WEST -> "Towards negative X";
                case EAST -> "Towards positive X";
                default -> "Invalid";
            };
            ChunkPos chunkpos = new ChunkPos(blockpos);
            if (!Objects.equals(this.lastPos, chunkpos)) {
                this.lastPos = chunkpos;
                this.clearChunkCache();
            }

            Level level = this.getLevel();
            LongSet longset = (LongSet)(level instanceof ServerLevel ? ((ServerLevel)level).getForcedChunks() : LongSets.EMPTY_SET);
            List<String> list = Lists.newArrayList(
                "Minecraft "
                    + SharedConstants.getCurrentVersion().getName()
                    + " ("
                    + this.minecraft.getLaunchedVersion()
                    + "/"
                    + ClientBrandRetriever.getClientModName()
                    + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType())
                    + ")",
                this.minecraft.fpsString,
                s,
                this.minecraft.levelRenderer.getSectionStatistics(),
                this.minecraft.levelRenderer.getEntityStatistics(),
                "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(),
                this.minecraft.level.gatherChunkSourceStats()
            );
            String s4 = this.getServerChunkStats();
            if (s4 != null) {
                list.add(s4);
            }

            list.add(this.minecraft.level.dimension().location() + " FC: " + longset.size());
            list.add("");
            list.add(
                String.format(
                    Locale.ROOT,
                    "XYZ: %.3f / %.5f / %.3f",
                    this.minecraft.getCameraEntity().getX(),
                    this.minecraft.getCameraEntity().getY(),
                    this.minecraft.getCameraEntity().getZ()
                )
            );
            list.add(
                String.format(
                    Locale.ROOT,
                    "Block: %d %d %d [%d %d %d]",
                    blockpos.getX(),
                    blockpos.getY(),
                    blockpos.getZ(),
                    blockpos.getX() & 15,
                    blockpos.getY() & 15,
                    blockpos.getZ() & 15
                )
            );
            list.add(
                String.format(
                    Locale.ROOT,
                    "Chunk: %d %d %d [%d %d in r.%d.%d.mca]",
                    chunkpos.x,
                    SectionPos.blockToSectionCoord(blockpos.getY()),
                    chunkpos.z,
                    chunkpos.getRegionLocalX(),
                    chunkpos.getRegionLocalZ(),
                    chunkpos.getRegionX(),
                    chunkpos.getRegionZ()
                )
            );
            list.add(
                String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, $$21, Mth.wrapDegrees(entity.getYRot()), Mth.wrapDegrees(entity.getXRot()))
            );
            LevelChunk levelchunk = this.getClientChunk();
            if (levelchunk.isEmpty()) {
                list.add("Waiting for chunk...");
            } else {
                int i = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(blockpos, 0);
                int j = this.minecraft.level.getBrightness(LightLayer.SKY, blockpos);
                int k = this.minecraft.level.getBrightness(LightLayer.BLOCK, blockpos);
                list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
                LevelChunk levelchunk1 = this.getServerChunk();
                StringBuilder stringbuilder = new StringBuilder("CH");

                for (Heightmap.Types heightmap$types : Heightmap.Types.values()) {
                    if (heightmap$types.sendToClient()) {
                        stringbuilder.append(" ")
                            .append(HEIGHTMAP_NAMES.get(heightmap$types))
                            .append(": ")
                            .append(levelchunk.getHeight(heightmap$types, blockpos.getX(), blockpos.getZ()));
                    }
                }

                list.add(stringbuilder.toString());
                stringbuilder.setLength(0);
                stringbuilder.append("SH");

                for (Heightmap.Types heightmap$types1 : Heightmap.Types.values()) {
                    if (heightmap$types1.keepAfterWorldgen()) {
                        stringbuilder.append(" ").append(HEIGHTMAP_NAMES.get(heightmap$types1)).append(": ");
                        if (levelchunk1 != null) {
                            stringbuilder.append(levelchunk1.getHeight(heightmap$types1, blockpos.getX(), blockpos.getZ()));
                        } else {
                            stringbuilder.append("??");
                        }
                    }
                }

                list.add(stringbuilder.toString());
                if (this.minecraft.level.isInsideBuildHeight(blockpos.getY())) {
                    list.add("Biome: " + printBiome(this.minecraft.level.getBiome(blockpos)));
                    if (levelchunk1 != null) {
                        float f2 = level.getMoonBrightness();
                        long l = levelchunk1.getInhabitedTime();
                        DifficultyInstance difficultyinstance = new DifficultyInstance(level.getDifficulty(), level.getDayTime(), l, f2);
                        list.add(
                            String.format(
                                Locale.ROOT,
                                "Local Difficulty: %.2f // %.2f (Day %d)",
                                difficultyinstance.getEffectiveDifficulty(),
                                difficultyinstance.getSpecialMultiplier(),
                                this.minecraft.level.getDayTime() / 24000L
                            )
                        );
                    } else {
                        list.add("Local Difficulty: ??");
                    }
                }

                if (levelchunk1 != null && levelchunk1.isOldNoiseGeneration()) {
                    list.add("Blending: Old");
                }
            }

            ServerLevel serverlevel = this.getServerLevel();
            if (serverlevel != null) {
                ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
                ChunkGenerator chunkgenerator = serverchunkcache.getGenerator();
                RandomState randomstate = serverchunkcache.randomState();
                chunkgenerator.addDebugScreenInfo(list, randomstate, blockpos);
                Climate.Sampler climate$sampler = randomstate.sampler();
                BiomeSource biomesource = chunkgenerator.getBiomeSource();
                biomesource.addDebugInfo(list, blockpos, climate$sampler);
                NaturalSpawner.SpawnState naturalspawner$spawnstate = serverchunkcache.getLastSpawnState();
                if (naturalspawner$spawnstate != null) {
                    Object2IntMap<MobCategory> object2intmap = naturalspawner$spawnstate.getMobCategoryCounts();
                    int i1 = naturalspawner$spawnstate.getSpawnableChunkCount();
                    list.add(
                        "SC: "
                            + i1
                            + ", "
                            + Stream.of(MobCategory.values())
                                .map(p_94068_ -> Character.toUpperCase(p_94068_.getName().charAt(0)) + ": " + object2intmap.getInt(p_94068_))
                                .collect(Collectors.joining(", "))
                    );
                } else {
                    list.add("SC: N/A");
                }
            }

            ResourceLocation resourcelocation = this.minecraft.gameRenderer.currentPostEffect();
            if (resourcelocation != null) {
                list.add("Post: " + resourcelocation);
            }

            list.add(this.minecraft.getSoundManager().getDebugString() + String.format(Locale.ROOT, " (Mood %d%%)", Math.round(this.minecraft.player.getCurrentMood() * 100.0F)));
            return list;
        }
    }

    private static String printBiome(Holder<Biome> p_205375_) {
        return p_205375_.unwrap().map(p_205377_ -> p_205377_.location().toString(), p_325324_ -> "[unregistered " + p_325324_ + "]");
    }

    @Nullable
    private ServerLevel getServerLevel() {
        IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
        return integratedserver != null ? integratedserver.getLevel(this.minecraft.level.dimension()) : null;
    }

    @Nullable
    private String getServerChunkStats() {
        ServerLevel serverlevel = this.getServerLevel();
        return serverlevel != null ? serverlevel.gatherChunkSourceStats() : null;
    }

    private Level getLevel() {
        return DataFixUtils.orElse(
            Optional.ofNullable(this.minecraft.getSingleplayerServer()).flatMap(p_374571_ -> Optional.ofNullable(p_374571_.getLevel(this.minecraft.level.dimension()))),
            this.minecraft.level
        );
    }

    @Nullable
    private LevelChunk getServerChunk() {
        if (this.serverChunk == null) {
            ServerLevel serverlevel = this.getServerLevel();
            if (serverlevel == null) {
                return null;
            }

            this.serverChunk = serverlevel.getChunkSource()
                .getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false)
                .thenApply(p_325326_ -> (LevelChunk)p_325326_.orElse(null));
        }

        return this.serverChunk.getNow(null);
    }

    private LevelChunk getClientChunk() {
        if (this.clientChunk == null) {
            this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x, this.lastPos.z);
        }

        return this.clientChunk;
    }

    protected List<String> getSystemInformation() {
        long i = Runtime.getRuntime().maxMemory();
        long j = Runtime.getRuntime().totalMemory();
        long k = Runtime.getRuntime().freeMemory();
        long l = j - k;
        List<String> list = Lists.newArrayList(
            String.format(Locale.ROOT, "Java: %s", System.getProperty("java.version")),
            String.format(Locale.ROOT, "Mem: %2d%% %03d/%03dMB", l * 100L / i, bytesToMegabytes(l), bytesToMegabytes(i)),
            String.format(Locale.ROOT, "Allocation rate: %03dMB/s", bytesToMegabytes(this.allocationRateCalculator.bytesAllocatedPerSecond(l))),
            String.format(Locale.ROOT, "Allocated: %2d%% %03dMB", j * 100L / i, bytesToMegabytes(j)),
            "",
            String.format(Locale.ROOT, "CPU: %s", GlUtil.getCpuInfo()),
            "",
            String.format(
                Locale.ROOT, "Display: %dx%d (%s)", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), GlUtil.getVendor()
            ),
            GlUtil.getRenderer(),
            GlUtil.getOpenGLVersion()
        );
        if (this.minecraft.showOnlyReducedInfo()) {
            return list;
        } else {
            if (this.block.getType() == HitResult.Type.BLOCK) {
                BlockPos blockpos = ((BlockHitResult)this.block).getBlockPos();
                BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
                list.add("");
                list.add(ChatFormatting.UNDERLINE + "Targeted Block: " + blockpos.getX() + ", " + blockpos.getY() + ", " + blockpos.getZ());
                list.add(String.valueOf(BuiltInRegistries.BLOCK.getKey(blockstate.getBlock())));

                for (Entry<Property<?>, Comparable<?>> entry : blockstate.getValues().entrySet()) {
                    list.add(this.getPropertyValueString(entry));
                }

                blockstate.getTags().map(p_325325_ -> "#" + p_325325_.location()).forEach(list::add);
            }

            if (this.liquid.getType() == HitResult.Type.BLOCK) {
                BlockPos blockpos1 = ((BlockHitResult)this.liquid).getBlockPos();
                FluidState fluidstate = this.minecraft.level.getFluidState(blockpos1);
                list.add("");
                list.add(ChatFormatting.UNDERLINE + "Targeted Fluid: " + blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ());
                list.add(String.valueOf(BuiltInRegistries.FLUID.getKey(fluidstate.getType())));

                for (Entry<Property<?>, Comparable<?>> entry1 : fluidstate.getValues().entrySet()) {
                    list.add(this.getPropertyValueString(entry1));
                }

                fluidstate.getTags().map(p_325328_ -> "#" + p_325328_.location()).forEach(list::add);
            }

            Entity entity = this.minecraft.crosshairPickEntity;
            if (entity != null) {
                list.add("");
                list.add(ChatFormatting.UNDERLINE + "Targeted Entity");
                list.add(String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())));
            }

            return list;
        }
    }

    private String getPropertyValueString(Entry<Property<?>, Comparable<?>> p_94072_) {
        Property<?> property = p_94072_.getKey();
        Comparable<?> comparable = p_94072_.getValue();
        String s = Util.getPropertyName(property, comparable);
        if (Boolean.TRUE.equals(comparable)) {
            s = ChatFormatting.GREEN + s;
        } else if (Boolean.FALSE.equals(comparable)) {
            s = ChatFormatting.RED + s;
        }

        return property.getName() + ": " + s;
    }

    private static long bytesToMegabytes(long p_94051_) {
        return p_94051_ / 1024L / 1024L;
    }

    public boolean showDebugScreen() {
        return this.renderDebug && !this.minecraft.options.hideGui;
    }

    public boolean showProfilerChart() {
        return this.showDebugScreen() && this.renderProfilerChart;
    }

    public boolean showNetworkCharts() {
        return this.showDebugScreen() && this.renderNetworkCharts;
    }

    public boolean showFpsCharts() {
        return this.showDebugScreen() && this.renderFpsCharts;
    }

    public void toggleOverlay() {
        this.renderDebug = !this.renderDebug;
    }

    public void toggleNetworkCharts() {
        this.renderNetworkCharts = !this.renderDebug || !this.renderNetworkCharts;
        if (this.renderNetworkCharts) {
            this.renderDebug = true;
            this.renderFpsCharts = false;
        }
    }

    public void toggleFpsCharts() {
        this.renderFpsCharts = !this.renderDebug || !this.renderFpsCharts;
        if (this.renderFpsCharts) {
            this.renderDebug = true;
            this.renderNetworkCharts = false;
        }
    }

    public void toggleProfilerChart() {
        this.renderProfilerChart = !this.renderDebug || !this.renderProfilerChart;
        if (this.renderProfilerChart) {
            this.renderDebug = true;
        }
    }

    public void logFrameDuration(long p_300948_) {
        this.frameTimeLogger.logSample(p_300948_);
    }

    public LocalSampleLogger getTickTimeLogger() {
        return this.tickTimeLogger;
    }

    public LocalSampleLogger getPingLogger() {
        return this.pingLogger;
    }

    public LocalSampleLogger getBandwidthLogger() {
        return this.bandwidthLogger;
    }

    public ProfilerPieChart getProfilerPieChart() {
        return this.profilerPieChart;
    }

    public void logRemoteSample(long[] p_333428_, RemoteDebugSampleType p_333591_) {
        LocalSampleLogger localsamplelogger = this.remoteSupportingLoggers.get(p_333591_);
        if (localsamplelogger != null) {
            localsamplelogger.logFullSample(p_333428_);
        }
    }

    public void reset() {
        this.renderDebug = false;
        this.tickTimeLogger.reset();
        this.pingLogger.reset();
        this.bandwidthLogger.reset();
    }

    @OnlyIn(Dist.CLIENT)
    static class AllocationRateCalculator {
        private static final int UPDATE_INTERVAL_MS = 500;
        private static final List<GarbageCollectorMXBean> GC_MBEANS = ManagementFactory.getGarbageCollectorMXBeans();
        private long lastTime = 0L;
        private long lastHeapUsage = -1L;
        private long lastGcCounts = -1L;
        private long lastRate = 0L;

        long bytesAllocatedPerSecond(long p_232517_) {
            long i = System.currentTimeMillis();
            if (i - this.lastTime < 500L) {
                return this.lastRate;
            } else {
                long j = gcCounts();
                if (this.lastTime != 0L && j == this.lastGcCounts) {
                    double d0 = (double)TimeUnit.SECONDS.toMillis(1L) / (double)(i - this.lastTime);
                    long k = p_232517_ - this.lastHeapUsage;
                    this.lastRate = Math.round((double)k * d0);
                }

                this.lastTime = i;
                this.lastHeapUsage = p_232517_;
                this.lastGcCounts = j;
                return this.lastRate;
            }
        }

        private static long gcCounts() {
            long i = 0L;

            for (GarbageCollectorMXBean garbagecollectormxbean : GC_MBEANS) {
                i += garbagecollectormxbean.getCollectionCount();
            }

            return i;
        }
    }
}