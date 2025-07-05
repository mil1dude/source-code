package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;

public abstract class Level implements LevelAccessor, AutoCloseable {
    public static final Codec<ResourceKey<Level>> RESOURCE_KEY_CODEC = ResourceKey.codec(Registries.DIMENSION);
    public static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld"));
    public static final ResourceKey<Level> NETHER = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("the_nether"));
    public static final ResourceKey<Level> END = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("the_end"));
    public static final int MAX_LEVEL_SIZE = 30000000;
    public static final int LONG_PARTICLE_CLIP_RANGE = 512;
    public static final int SHORT_PARTICLE_CLIP_RANGE = 32;
    public static final int MAX_BRIGHTNESS = 15;
    public static final int TICKS_PER_DAY = 24000;
    public static final int MAX_ENTITY_SPAWN_Y = 20000000;
    public static final int MIN_ENTITY_SPAWN_Y = -20000000;
    protected final List<TickingBlockEntity> blockEntityTickers = Lists.newArrayList();
    protected final NeighborUpdater neighborUpdater;
    private final List<TickingBlockEntity> pendingBlockEntityTickers = Lists.newArrayList();
    private boolean tickingBlockEntities;
    private final Thread thread;
    private final boolean isDebug;
    private int skyDarken;
    protected int randValue = RandomSource.create().nextInt();
    protected final int addend = 1013904223;
    protected float oRainLevel;
    protected float rainLevel;
    protected float oThunderLevel;
    protected float thunderLevel;
    public final RandomSource random = RandomSource.create();
    @Deprecated
    private final RandomSource threadSafeRandom = RandomSource.createThreadSafe();
    private final Holder<DimensionType> dimensionTypeRegistration;
    protected final WritableLevelData levelData;
    public final boolean isClientSide;
    private final WorldBorder worldBorder;
    private final BiomeManager biomeManager;
    private final ResourceKey<Level> dimension;
    private final RegistryAccess registryAccess;
    private final DamageSources damageSources;
    private long subTickCount;

    protected Level(
        WritableLevelData p_270739_,
        ResourceKey<Level> p_270683_,
        RegistryAccess p_270200_,
        Holder<DimensionType> p_270240_,
        boolean p_270904_,
        boolean p_270470_,
        long p_270248_,
        int p_270466_
    ) {
        this.levelData = p_270739_;
        this.dimensionTypeRegistration = p_270240_;
        final DimensionType dimensiontype = p_270240_.value();
        this.dimension = p_270683_;
        this.isClientSide = p_270904_;
        if (dimensiontype.coordinateScale() != 1.0) {
            this.worldBorder = new WorldBorder() {
                @Override
                public double getCenterX() {
                    return super.getCenterX() / dimensiontype.coordinateScale();
                }

                @Override
                public double getCenterZ() {
                    return super.getCenterZ() / dimensiontype.coordinateScale();
                }
            };
        } else {
            this.worldBorder = new WorldBorder();
        }

        this.thread = Thread.currentThread();
        this.biomeManager = new BiomeManager(this, p_270248_);
        this.isDebug = p_270470_;
        this.neighborUpdater = new CollectingNeighborUpdater(this, p_270466_);
        this.registryAccess = p_270200_;
        this.damageSources = new DamageSources(p_270200_);
    }

    @Override
    public boolean isClientSide() {
        return this.isClientSide;
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return null;
    }

    public boolean isInWorldBounds(BlockPos p_46740_) {
        return !this.isOutsideBuildHeight(p_46740_) && isInWorldBoundsHorizontal(p_46740_);
    }

    public static boolean isInSpawnableBounds(BlockPos p_46742_) {
        return !isOutsideSpawnableHeight(p_46742_.getY()) && isInWorldBoundsHorizontal(p_46742_);
    }

    private static boolean isInWorldBoundsHorizontal(BlockPos p_46458_) {
        return p_46458_.getX() >= -30000000 && p_46458_.getZ() >= -30000000 && p_46458_.getX() < 30000000 && p_46458_.getZ() < 30000000;
    }

    private static boolean isOutsideSpawnableHeight(int p_46725_) {
        return p_46725_ < -20000000 || p_46725_ >= 20000000;
    }

    public LevelChunk getChunkAt(BlockPos p_46746_) {
        return this.getChunk(SectionPos.blockToSectionCoord(p_46746_.getX()), SectionPos.blockToSectionCoord(p_46746_.getZ()));
    }

    public LevelChunk getChunk(int p_46727_, int p_46728_) {
        return (LevelChunk)this.getChunk(p_46727_, p_46728_, ChunkStatus.FULL);
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int p_46502_, int p_46503_, ChunkStatus p_330379_, boolean p_46505_) {
        ChunkAccess chunkaccess = this.getChunkSource().getChunk(p_46502_, p_46503_, p_330379_, p_46505_);
        if (chunkaccess == null && p_46505_) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        } else {
            return chunkaccess;
        }
    }

    @Override
    public boolean setBlock(BlockPos p_46601_, BlockState p_46602_, int p_46603_) {
        return this.setBlock(p_46601_, p_46602_, p_46603_, 512);
    }

    @Override
    public boolean setBlock(BlockPos p_46605_, BlockState p_46606_, int p_46607_, int p_46608_) {
        if (this.isOutsideBuildHeight(p_46605_)) {
            return false;
        } else if (!this.isClientSide && this.isDebug()) {
            return false;
        } else {
            LevelChunk levelchunk = this.getChunkAt(p_46605_);
            Block block = p_46606_.getBlock();
            BlockState blockstate = levelchunk.setBlockState(p_46605_, p_46606_, (p_46607_ & 64) != 0);
            if (blockstate == null) {
                return false;
            } else {
                BlockState blockstate1 = this.getBlockState(p_46605_);
                if (blockstate1 == p_46606_) {
                    if (blockstate != blockstate1) {
                        this.setBlocksDirty(p_46605_, blockstate, blockstate1);
                    }

                    if ((p_46607_ & 2) != 0
                        && (!this.isClientSide || (p_46607_ & 4) == 0)
                        && (this.isClientSide || levelchunk.getFullStatus() != null && levelchunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING))) {
                        this.sendBlockUpdated(p_46605_, blockstate, p_46606_, p_46607_);
                    }

                    if ((p_46607_ & 1) != 0) {
                        this.blockUpdated(p_46605_, blockstate.getBlock());
                        if (!this.isClientSide && p_46606_.hasAnalogOutputSignal()) {
                            this.updateNeighbourForOutputSignal(p_46605_, block);
                        }
                    }

                    if ((p_46607_ & 16) == 0 && p_46608_ > 0) {
                        int i = p_46607_ & -34;
                        blockstate.updateIndirectNeighbourShapes(this, p_46605_, i, p_46608_ - 1);
                        p_46606_.updateNeighbourShapes(this, p_46605_, i, p_46608_ - 1);
                        p_46606_.updateIndirectNeighbourShapes(this, p_46605_, i, p_46608_ - 1);
                    }

                    this.onBlockStateChange(p_46605_, blockstate, blockstate1);
                }

                return true;
            }
        }
    }

    public void onBlockStateChange(BlockPos p_46609_, BlockState p_46610_, BlockState p_46611_) {
    }

    @Override
    public boolean removeBlock(BlockPos p_46623_, boolean p_46624_) {
        FluidState fluidstate = this.getFluidState(p_46623_);
        return this.setBlock(p_46623_, fluidstate.createLegacyBlock(), 3 | (p_46624_ ? 64 : 0));
    }

    @Override
    public boolean destroyBlock(BlockPos p_46626_, boolean p_46627_, @Nullable Entity p_46628_, int p_46629_) {
        BlockState blockstate = this.getBlockState(p_46626_);
        if (blockstate.isAir()) {
            return false;
        } else {
            FluidState fluidstate = this.getFluidState(p_46626_);
            if (!(blockstate.getBlock() instanceof BaseFireBlock)) {
                this.levelEvent(2001, p_46626_, Block.getId(blockstate));
            }

            if (p_46627_) {
                BlockEntity blockentity = blockstate.hasBlockEntity() ? this.getBlockEntity(p_46626_) : null;
                Block.dropResources(blockstate, this, p_46626_, blockentity, p_46628_, ItemStack.EMPTY);
            }

            boolean flag = this.setBlock(p_46626_, fluidstate.createLegacyBlock(), 3, p_46629_);
            if (flag) {
                this.gameEvent(GameEvent.BLOCK_DESTROY, p_46626_, GameEvent.Context.of(p_46628_, blockstate));
            }

            return flag;
        }
    }

    public void addDestroyBlockEffect(BlockPos p_151531_, BlockState p_151532_) {
    }

    public boolean setBlockAndUpdate(BlockPos p_46598_, BlockState p_46599_) {
        return this.setBlock(p_46598_, p_46599_, 3);
    }

    public abstract void sendBlockUpdated(BlockPos p_46612_, BlockState p_46613_, BlockState p_46614_, int p_46615_);

    public void setBlocksDirty(BlockPos p_46678_, BlockState p_46679_, BlockState p_46680_) {
    }

    public void updateNeighborsAt(BlockPos p_46673_, Block p_46674_) {
    }

    public void updateNeighborsAt(BlockPos p_369886_, Block p_361495_, @Nullable Orientation p_362848_) {
    }

    public void updateNeighborsAtExceptFromFacing(BlockPos p_46591_, Block p_46592_, Direction p_46593_, @Nullable Orientation p_361214_) {
    }

    public void neighborChanged(BlockPos p_220380_, Block p_220381_, @Nullable Orientation p_361070_) {
    }

    public void neighborChanged(BlockState p_366856_, BlockPos p_46587_, Block p_46588_, @Nullable Orientation p_366620_, boolean p_360947_) {
    }

    @Override
    public void neighborShapeChanged(Direction p_220385_, BlockPos p_220387_, BlockPos p_220388_, BlockState p_220386_, int p_220389_, int p_220390_) {
        this.neighborUpdater.shapeUpdate(p_220385_, p_220386_, p_220387_, p_220388_, p_220389_, p_220390_);
    }

    @Override
    public int getHeight(Heightmap.Types p_46571_, int p_46572_, int p_46573_) {
        int i;
        if (p_46572_ >= -30000000 && p_46573_ >= -30000000 && p_46572_ < 30000000 && p_46573_ < 30000000) {
            if (this.hasChunk(SectionPos.blockToSectionCoord(p_46572_), SectionPos.blockToSectionCoord(p_46573_))) {
                i = this.getChunk(SectionPos.blockToSectionCoord(p_46572_), SectionPos.blockToSectionCoord(p_46573_)).getHeight(p_46571_, p_46572_ & 15, p_46573_ & 15) + 1;
            } else {
                i = this.getMinY();
            }
        } else {
            i = this.getSeaLevel() + 1;
        }

        return i;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.getChunkSource().getLightEngine();
    }

    @Override
    public BlockState getBlockState(BlockPos p_46732_) {
        if (this.isOutsideBuildHeight(p_46732_)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            LevelChunk levelchunk = this.getChunk(SectionPos.blockToSectionCoord(p_46732_.getX()), SectionPos.blockToSectionCoord(p_46732_.getZ()));
            return levelchunk.getBlockState(p_46732_);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos p_46671_) {
        if (this.isOutsideBuildHeight(p_46671_)) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            LevelChunk levelchunk = this.getChunkAt(p_46671_);
            return levelchunk.getFluidState(p_46671_);
        }
    }

    public boolean isDay() {
        return !this.dimensionType().hasFixedTime() && this.skyDarken < 4;
    }

    public boolean isNight() {
        return !this.dimensionType().hasFixedTime() && !this.isDay();
    }

    public void playSound(@Nullable Entity p_252137_, BlockPos p_251749_, SoundEvent p_248842_, SoundSource p_251104_, float p_249531_, float p_250763_) {
        this.playSound(p_252137_ instanceof Player player ? player : null, p_251749_, p_248842_, p_251104_, p_249531_, p_250763_);
    }

    @Override
    public void playSound(@Nullable Player p_46560_, BlockPos p_46561_, SoundEvent p_46562_, SoundSource p_46563_, float p_46564_, float p_46565_) {
        this.playSound(
            p_46560_,
            (double)p_46561_.getX() + 0.5,
            (double)p_46561_.getY() + 0.5,
            (double)p_46561_.getZ() + 0.5,
            p_46562_,
            p_46563_,
            p_46564_,
            p_46565_
        );
    }

    public abstract void playSeededSound(
        @Nullable Player p_262953_,
        double p_263004_,
        double p_263398_,
        double p_263376_,
        Holder<SoundEvent> p_263359_,
        SoundSource p_263020_,
        float p_263055_,
        float p_262914_,
        long p_262991_
    );

    public void playSeededSound(
        @Nullable Player p_220363_,
        double p_220364_,
        double p_220365_,
        double p_220366_,
        SoundEvent p_220367_,
        SoundSource p_220368_,
        float p_220369_,
        float p_220370_,
        long p_220371_
    ) {
        this.playSeededSound(p_220363_, p_220364_, p_220365_, p_220366_, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(p_220367_), p_220368_, p_220369_, p_220370_, p_220371_);
    }

    public abstract void playSeededSound(
        @Nullable Player p_220372_, Entity p_220373_, Holder<SoundEvent> p_263500_, SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_
    );

    public void playSound(@Nullable Player p_312141_, double p_310370_, double p_311188_, double p_309961_, SoundEvent p_313224_, SoundSource p_312451_) {
        this.playSound(p_312141_, p_310370_, p_311188_, p_309961_, p_313224_, p_312451_, 1.0F, 1.0F);
    }

    public void playSound(
        @Nullable Player p_46543_, double p_46544_, double p_46545_, double p_46546_, SoundEvent p_46547_, SoundSource p_46548_, float p_46549_, float p_46550_
    ) {
        this.playSeededSound(p_46543_, p_46544_, p_46545_, p_46546_, p_46547_, p_46548_, p_46549_, p_46550_, this.threadSafeRandom.nextLong());
    }

    public void playSound(
        @Nullable Player p_345448_,
        double p_343911_,
        double p_342136_,
        double p_343905_,
        Holder<SoundEvent> p_343950_,
        SoundSource p_345316_,
        float p_344093_,
        float p_343901_
    ) {
        this.playSeededSound(p_345448_, p_343911_, p_342136_, p_343905_, p_343950_, p_345316_, p_344093_, p_343901_, this.threadSafeRandom.nextLong());
    }

    public void playSound(@Nullable Player p_46551_, Entity p_46552_, SoundEvent p_46553_, SoundSource p_46554_, float p_46555_, float p_46556_) {
        this.playSeededSound(p_46551_, p_46552_, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(p_46553_), p_46554_, p_46555_, p_46556_, this.threadSafeRandom.nextLong());
    }

    public void playLocalSound(BlockPos p_250938_, SoundEvent p_252209_, SoundSource p_249161_, float p_249980_, float p_250277_, boolean p_250151_) {
        this.playLocalSound(
            (double)p_250938_.getX() + 0.5,
            (double)p_250938_.getY() + 0.5,
            (double)p_250938_.getZ() + 0.5,
            p_252209_,
            p_249161_,
            p_249980_,
            p_250277_,
            p_250151_
        );
    }

    public void playLocalSound(Entity p_312682_, SoundEvent p_309977_, SoundSource p_310337_, float p_311199_, float p_311168_) {
    }

    public void playLocalSound(
        double p_46482_, double p_46483_, double p_46484_, SoundEvent p_46485_, SoundSource p_46486_, float p_46487_, float p_46488_, boolean p_46489_
    ) {
    }

    @Override
    public void addParticle(ParticleOptions p_46631_, double p_46632_, double p_46633_, double p_46634_, double p_46635_, double p_46636_, double p_46637_) {
    }

    public void addParticle(
        ParticleOptions p_46638_,
        boolean p_46639_,
        boolean p_376942_,
        double p_46640_,
        double p_46641_,
        double p_46642_,
        double p_46643_,
        double p_46644_,
        double p_46645_
    ) {
    }

    public void addAlwaysVisibleParticle(ParticleOptions p_46684_, double p_46685_, double p_46686_, double p_46687_, double p_46688_, double p_46689_, double p_46690_) {
    }

    public void addAlwaysVisibleParticle(
        ParticleOptions p_46691_, boolean p_46692_, double p_46693_, double p_46694_, double p_46695_, double p_46696_, double p_46697_, double p_46698_
    ) {
    }

    public float getSunAngle(float p_46491_) {
        float f = this.getTimeOfDay(p_46491_);
        return f * (float) (Math.PI * 2);
    }

    public void addBlockEntityTicker(TickingBlockEntity p_151526_) {
        (this.tickingBlockEntities ? this.pendingBlockEntityTickers : this.blockEntityTickers).add(p_151526_);
    }

    protected void tickBlockEntities() {
        ProfilerFiller profilerfiller = Profiler.get();
        profilerfiller.push("blockEntities");
        this.tickingBlockEntities = true;
        if (!this.pendingBlockEntityTickers.isEmpty()) {
            this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
            this.pendingBlockEntityTickers.clear();
        }

        Iterator<TickingBlockEntity> iterator = this.blockEntityTickers.iterator();
        boolean flag = this.tickRateManager().runsNormally();

        while (iterator.hasNext()) {
            TickingBlockEntity tickingblockentity = iterator.next();
            if (tickingblockentity.isRemoved()) {
                iterator.remove();
            } else if (flag && this.shouldTickBlocksAt(tickingblockentity.getPos())) {
                tickingblockentity.tick();
            }
        }

        this.tickingBlockEntities = false;
        profilerfiller.pop();
    }

    public <T extends Entity> void guardEntityTick(Consumer<T> p_46654_, T p_46655_) {
        try {
            p_46654_.accept(p_46655_);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking entity");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being ticked");
            p_46655_.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    public boolean shouldTickDeath(Entity p_186458_) {
        return true;
    }

    public boolean shouldTickBlocksAt(long p_186456_) {
        return true;
    }

    public boolean shouldTickBlocksAt(BlockPos p_220394_) {
        return this.shouldTickBlocksAt(ChunkPos.asLong(p_220394_));
    }

    public void explode(
        @Nullable Entity p_312521_, double p_309783_, double p_312776_, double p_310505_, float p_310209_, Level.ExplosionInteraction p_310628_
    ) {
        this.explode(
            p_312521_,
            Explosion.getDefaultDamageSource(this, p_312521_),
            null,
            p_309783_,
            p_312776_,
            p_310505_,
            p_310209_,
            false,
            p_310628_,
            ParticleTypes.EXPLOSION,
            ParticleTypes.EXPLOSION_EMITTER,
            SoundEvents.GENERIC_EXPLODE
        );
    }

    public void explode(
        @Nullable Entity p_256599_,
        double p_255914_,
        double p_255684_,
        double p_255843_,
        float p_256310_,
        boolean p_366060_,
        Level.ExplosionInteraction p_256178_
    ) {
        this.explode(
            p_256599_,
            Explosion.getDefaultDamageSource(this, p_256599_),
            null,
            p_255914_,
            p_255684_,
            p_255843_,
            p_256310_,
            p_366060_,
            p_256178_,
            ParticleTypes.EXPLOSION,
            ParticleTypes.EXPLOSION_EMITTER,
            SoundEvents.GENERIC_EXPLODE
        );
    }

    public void explode(
        @Nullable Entity p_256233_,
        @Nullable DamageSource p_255861_,
        @Nullable ExplosionDamageCalculator p_255867_,
        Vec3 p_368610_,
        float p_256013_,
        boolean p_256228_,
        Level.ExplosionInteraction p_255784_
    ) {
        this.explode(
            p_256233_,
            p_255861_,
            p_255867_,
            p_368610_.x(),
            p_368610_.y(),
            p_368610_.z(),
            p_256013_,
            p_256228_,
            p_255784_,
            ParticleTypes.EXPLOSION,
            ParticleTypes.EXPLOSION_EMITTER,
            SoundEvents.GENERIC_EXPLODE
        );
    }

    public void explode(
        @Nullable Entity p_255682_,
        @Nullable DamageSource p_364137_,
        @Nullable ExplosionDamageCalculator p_361760_,
        double p_255803_,
        double p_256403_,
        double p_256538_,
        float p_255674_,
        boolean p_256634_,
        Level.ExplosionInteraction p_256111_
    ) {
        this.explode(
            p_255682_,
            p_364137_,
            p_361760_,
            p_255803_,
            p_256403_,
            p_256538_,
            p_255674_,
            p_256634_,
            p_256111_,
            ParticleTypes.EXPLOSION,
            ParticleTypes.EXPLOSION_EMITTER,
            SoundEvents.GENERIC_EXPLODE
        );
    }

    public abstract void explode(
        @Nullable Entity p_364471_,
        @Nullable DamageSource p_369329_,
        @Nullable ExplosionDamageCalculator p_366630_,
        double p_365048_,
        double p_368231_,
        double p_360983_,
        float p_369516_,
        boolean p_365605_,
        Level.ExplosionInteraction p_361840_,
        ParticleOptions p_365102_,
        ParticleOptions p_364764_,
        Holder<SoundEvent> p_367028_
    );

    public abstract String gatherChunkSourceStats();

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos p_46716_) {
        if (this.isOutsideBuildHeight(p_46716_)) {
            return null;
        } else {
            return !this.isClientSide && Thread.currentThread() != this.thread
                ? null
                : this.getChunkAt(p_46716_).getBlockEntity(p_46716_, LevelChunk.EntityCreationType.IMMEDIATE);
        }
    }

    public void setBlockEntity(BlockEntity p_151524_) {
        BlockPos blockpos = p_151524_.getBlockPos();
        if (!this.isOutsideBuildHeight(blockpos)) {
            this.getChunkAt(blockpos).addAndRegisterBlockEntity(p_151524_);
        }
    }

    public void removeBlockEntity(BlockPos p_46748_) {
        if (!this.isOutsideBuildHeight(p_46748_)) {
            this.getChunkAt(p_46748_).removeBlockEntity(p_46748_);
        }
    }

    public boolean isLoaded(BlockPos p_46750_) {
        return this.isOutsideBuildHeight(p_46750_)
            ? false
            : this.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(p_46750_.getX()), SectionPos.blockToSectionCoord(p_46750_.getZ()));
    }

    public boolean loadedAndEntityCanStandOnFace(BlockPos p_46579_, Entity p_46580_, Direction p_46581_) {
        if (this.isOutsideBuildHeight(p_46579_)) {
            return false;
        } else {
            ChunkAccess chunkaccess = this.getChunk(
                SectionPos.blockToSectionCoord(p_46579_.getX()), SectionPos.blockToSectionCoord(p_46579_.getZ()), ChunkStatus.FULL, false
            );
            return chunkaccess == null ? false : chunkaccess.getBlockState(p_46579_).entityCanStandOnFace(this, p_46579_, p_46580_, p_46581_);
        }
    }

    public boolean loadedAndEntityCanStandOn(BlockPos p_46576_, Entity p_46577_) {
        return this.loadedAndEntityCanStandOnFace(p_46576_, p_46577_, Direction.UP);
    }

    public void updateSkyBrightness() {
        double d0 = 1.0 - (double)(this.getRainLevel(1.0F) * 5.0F) / 16.0;
        double d1 = 1.0 - (double)(this.getThunderLevel(1.0F) * 5.0F) / 16.0;
        double d2 = 0.5 + 2.0 * Mth.clamp((double)Mth.cos(this.getTimeOfDay(1.0F) * (float) (Math.PI * 2)), -0.25, 0.25);
        this.skyDarken = (int)((1.0 - d2 * d0 * d1) * 11.0);
    }

    public void setSpawnSettings(boolean p_46704_) {
        this.getChunkSource().setSpawnSettings(p_46704_);
    }

    public BlockPos getSharedSpawnPos() {
        BlockPos blockpos = this.levelData.getSpawnPos();
        if (!this.getWorldBorder().isWithinBounds(blockpos)) {
            blockpos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
        }

        return blockpos;
    }

    public float getSharedSpawnAngle() {
        return this.levelData.getSpawnAngle();
    }

    protected void prepareWeather() {
        if (this.levelData.isRaining()) {
            this.rainLevel = 1.0F;
            if (this.levelData.isThundering()) {
                this.thunderLevel = 1.0F;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.getChunkSource().close();
    }

    @Nullable
    @Override
    public BlockGetter getChunkForCollisions(int p_46711_, int p_46712_) {
        return this.getChunk(p_46711_, p_46712_, ChunkStatus.FULL, false);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity p_46536_, AABB p_46537_, Predicate<? super Entity> p_46538_) {
        Profiler.get().incrementCounter("getEntities");
        List<Entity> list = Lists.newArrayList();
        this.getEntities().get(p_46537_, p_375317_ -> {
            if (p_375317_ != p_46536_ && p_46538_.test(p_375317_)) {
                list.add(p_375317_);
            }
        });

        for (EnderDragonPart enderdragonpart : this.dragonParts()) {
            if (enderdragonpart != p_46536_
                && enderdragonpart.parentMob != p_46536_
                && p_46538_.test(enderdragonpart)
                && p_46537_.intersects(enderdragonpart.getBoundingBox())) {
                list.add(enderdragonpart);
            }
        }

        return list;
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> p_151528_, AABB p_151529_, Predicate<? super T> p_151530_) {
        List<T> list = Lists.newArrayList();
        this.getEntities(p_151528_, p_151529_, p_151530_, list);
        return list;
    }

    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> p_261899_, AABB p_261837_, Predicate<? super T> p_261519_, List<? super T> p_262046_) {
        this.getEntities(p_261899_, p_261837_, p_261519_, p_262046_, Integer.MAX_VALUE);
    }

    public <T extends Entity> void getEntities(
        EntityTypeTest<Entity, T> p_261885_, AABB p_262086_, Predicate<? super T> p_261688_, List<? super T> p_262071_, int p_261858_
    ) {
        Profiler.get().incrementCounter("getEntities");
        this.getEntities().get(p_261885_, p_262086_, p_261454_ -> {
            if (p_261688_.test(p_261454_)) {
                p_262071_.add(p_261454_);
                if (p_262071_.size() >= p_261858_) {
                    return AbortableIterationConsumer.Continuation.ABORT;
                }
            }

            if (p_261454_ instanceof EnderDragon enderdragon) {
                for (EnderDragonPart enderdragonpart : enderdragon.getSubEntities()) {
                    T t = p_261885_.tryCast(enderdragonpart);
                    if (t != null && p_261688_.test(t)) {
                        p_262071_.add(t);
                        if (p_262071_.size() >= p_261858_) {
                            return AbortableIterationConsumer.Continuation.ABORT;
                        }
                    }
                }
            }

            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
    }

    @Nullable
    public abstract Entity getEntity(int p_46492_);

    public abstract Collection<EnderDragonPart> dragonParts();

    public void blockEntityChanged(BlockPos p_151544_) {
        if (this.hasChunkAt(p_151544_)) {
            this.getChunkAt(p_151544_).markUnsaved();
        }
    }

    public void disconnect() {
    }

    public long getGameTime() {
        return this.levelData.getGameTime();
    }

    public long getDayTime() {
        return this.levelData.getDayTime();
    }

    public boolean mayInteract(Player p_46557_, BlockPos p_46558_) {
        return true;
    }

    public void broadcastEntityEvent(Entity p_46509_, byte p_46510_) {
    }

    public void broadcastDamageEvent(Entity p_270831_, DamageSource p_270361_) {
    }

    public void blockEvent(BlockPos p_46582_, Block p_46583_, int p_46584_, int p_46585_) {
        this.getBlockState(p_46582_).triggerEvent(this, p_46582_, p_46584_, p_46585_);
    }

    @Override
    public LevelData getLevelData() {
        return this.levelData;
    }

    public abstract TickRateManager tickRateManager();

    public float getThunderLevel(float p_46662_) {
        return Mth.lerp(p_46662_, this.oThunderLevel, this.thunderLevel) * this.getRainLevel(p_46662_);
    }

    public void setThunderLevel(float p_46708_) {
        float f = Mth.clamp(p_46708_, 0.0F, 1.0F);
        this.oThunderLevel = f;
        this.thunderLevel = f;
    }

    public float getRainLevel(float p_46723_) {
        return Mth.lerp(p_46723_, this.oRainLevel, this.rainLevel);
    }

    public void setRainLevel(float p_46735_) {
        float f = Mth.clamp(p_46735_, 0.0F, 1.0F);
        this.oRainLevel = f;
        this.rainLevel = f;
    }

    private boolean canHaveWeather() {
        return this.dimensionType().hasSkyLight() && !this.dimensionType().hasCeiling();
    }

    public boolean isThundering() {
        return this.canHaveWeather() && (double)this.getThunderLevel(1.0F) > 0.9;
    }

    public boolean isRaining() {
        return this.canHaveWeather() && (double)this.getRainLevel(1.0F) > 0.2;
    }

    public boolean isRainingAt(BlockPos p_46759_) {
        if (!this.isRaining()) {
            return false;
        } else if (!this.canSeeSky(p_46759_)) {
            return false;
        } else if (this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, p_46759_).getY() > p_46759_.getY()) {
            return false;
        } else {
            Biome biome = this.getBiome(p_46759_).value();
            return biome.getPrecipitationAt(p_46759_, this.getSeaLevel()) == Biome.Precipitation.RAIN;
        }
    }

    @Nullable
    public abstract MapItemSavedData getMapData(MapId p_335212_);

    public abstract void setMapData(MapId p_332598_, MapItemSavedData p_151534_);

    public abstract MapId getFreeMapId();

    public void globalLevelEvent(int p_46665_, BlockPos p_46666_, int p_46667_) {
    }

    public CrashReportCategory fillReportDetails(CrashReport p_46656_) {
        CrashReportCategory crashreportcategory = p_46656_.addCategory("Affected level", 1);
        crashreportcategory.setDetail("All players", () -> this.players().size() + " total; " + this.players());
        crashreportcategory.setDetail("Chunk stats", this.getChunkSource()::gatherStats);
        crashreportcategory.setDetail("Level dimension", () -> this.dimension().location().toString());

        try {
            this.levelData.fillCrashReportCategory(crashreportcategory, this);
        } catch (Throwable throwable) {
            crashreportcategory.setDetailError("Level Data Unobtainable", throwable);
        }

        return crashreportcategory;
    }

    public abstract void destroyBlockProgress(int p_46506_, BlockPos p_46507_, int p_46508_);

    public void createFireworks(double p_46475_, double p_46476_, double p_46477_, double p_46478_, double p_46479_, double p_46480_, List<FireworkExplosion> p_333978_) {
    }

    public abstract Scoreboard getScoreboard();

    public void updateNeighbourForOutputSignal(BlockPos p_46718_, Block p_46719_) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = p_46718_.relative(direction);
            if (this.hasChunkAt(blockpos)) {
                BlockState blockstate = this.getBlockState(blockpos);
                if (blockstate.is(Blocks.COMPARATOR)) {
                    this.neighborChanged(blockstate, blockpos, p_46719_, null, false);
                } else if (blockstate.isRedstoneConductor(this, blockpos)) {
                    blockpos = blockpos.relative(direction);
                    blockstate = this.getBlockState(blockpos);
                    if (blockstate.is(Blocks.COMPARATOR)) {
                        this.neighborChanged(blockstate, blockpos, p_46719_, null, false);
                    }
                }
            }
        }
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos p_46730_) {
        long i = 0L;
        float f = 0.0F;
        if (this.hasChunkAt(p_46730_)) {
            f = this.getMoonBrightness();
            i = this.getChunkAt(p_46730_).getInhabitedTime();
        }

        return new DifficultyInstance(this.getDifficulty(), this.getDayTime(), i, f);
    }

    @Override
    public int getSkyDarken() {
        return this.skyDarken;
    }

    public void setSkyFlashTime(int p_46709_) {
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }

    public void sendPacketToServer(Packet<?> p_46657_) {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }

    @Override
    public DimensionType dimensionType() {
        return this.dimensionTypeRegistration.value();
    }

    public Holder<DimensionType> dimensionTypeRegistration() {
        return this.dimensionTypeRegistration;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    @Override
    public RandomSource getRandom() {
        return this.random;
    }

    @Override
    public boolean isStateAtPosition(BlockPos p_46620_, Predicate<BlockState> p_46621_) {
        return p_46621_.test(this.getBlockState(p_46620_));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos p_151541_, Predicate<FluidState> p_151542_) {
        return p_151542_.test(this.getFluidState(p_151541_));
    }

    public abstract RecipeAccess recipeAccess();

    public BlockPos getBlockRandomPos(int p_46497_, int p_46498_, int p_46499_, int p_46500_) {
        this.randValue = this.randValue * 3 + 1013904223;
        int i = this.randValue >> 2;
        return new BlockPos(p_46497_ + (i & 15), p_46498_ + (i >> 16 & p_46500_), p_46499_ + (i >> 8 & 15));
    }

    public boolean noSave() {
        return false;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.biomeManager;
    }

    public final boolean isDebug() {
        return this.isDebug;
    }

    protected abstract LevelEntityGetter<Entity> getEntities();

    @Override
    public long nextSubTickCount() {
        return this.subTickCount++;
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.registryAccess;
    }

    public DamageSources damageSources() {
        return this.damageSources;
    }

    public abstract PotionBrewing potionBrewing();

    public abstract FuelValues fuelValues();

    public static enum ExplosionInteraction implements StringRepresentable {
        NONE("none"),
        BLOCK("block"),
        MOB("mob"),
        TNT("tnt"),
        TRIGGER("trigger");

        public static final Codec<Level.ExplosionInteraction> CODEC = StringRepresentable.fromEnum(Level.ExplosionInteraction::values);
        private final String id;

        private ExplosionInteraction(final String p_344888_) {
            this.id = p_344888_;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }
}