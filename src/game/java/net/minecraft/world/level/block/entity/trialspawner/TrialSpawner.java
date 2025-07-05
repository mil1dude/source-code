package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class TrialSpawner {
    public static final String NORMAL_CONFIG_TAG_NAME = "normal_config";
    public static final String OMINOUS_CONFIG_TAG_NAME = "ominous_config";
    public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
    private static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
    private static final int DEFAULT_PLAYER_SCAN_RANGE = 14;
    private static final int MAX_MOB_TRACKING_DISTANCE = 47;
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(47);
    private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02F;
    private Holder<TrialSpawnerConfig> normalConfig;
    private Holder<TrialSpawnerConfig> ominousConfig;
    private final TrialSpawnerData data;
    private final int requiredPlayerRange;
    private final int targetCooldownLength;
    private final TrialSpawner.StateAccessor stateAccessor;
    private PlayerDetector playerDetector;
    private final PlayerDetector.EntitySelector entitySelector;
    private boolean overridePeacefulAndMobSpawnRule;
    private boolean isOminous;

    public Codec<TrialSpawner> codec() {
        return RecordCodecBuilder.create(
            p_360522_ -> p_360522_.group(
                        TrialSpawnerConfig.CODEC
                            .optionalFieldOf("normal_config", Holder.direct(TrialSpawnerConfig.DEFAULT))
                            .forGetter(p_360521_ -> p_360521_.normalConfig),
                        TrialSpawnerConfig.CODEC
                            .optionalFieldOf("ominous_config", Holder.direct(TrialSpawnerConfig.DEFAULT))
                            .forGetter(p_360515_ -> p_360515_.ominousConfig),
                        TrialSpawnerData.MAP_CODEC.forGetter(TrialSpawner::getData),
                        Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("target_cooldown_length", 36000).forGetter(TrialSpawner::getTargetCooldownLength),
                        Codec.intRange(1, 128).optionalFieldOf("required_player_range", 14).forGetter(TrialSpawner::getRequiredPlayerRange)
                    )
                    .apply(
                        p_360522_,
                        (p_360516_, p_360517_, p_360518_, p_360519_, p_360520_) -> new TrialSpawner(
                                p_360516_, p_360517_, p_360518_, p_360519_, p_360520_, this.stateAccessor, this.playerDetector, this.entitySelector
                            )
                    )
        );
    }

    public TrialSpawner(TrialSpawner.StateAccessor p_310216_, PlayerDetector p_309626_, PlayerDetector.EntitySelector p_328170_) {
        this(
            Holder.direct(TrialSpawnerConfig.DEFAULT),
            Holder.direct(TrialSpawnerConfig.DEFAULT),
            new TrialSpawnerData(),
            36000,
            14,
            p_310216_,
            p_309626_,
            p_328170_
        );
    }

    public TrialSpawner(
        Holder<TrialSpawnerConfig> p_362438_,
        Holder<TrialSpawnerConfig> p_365001_,
        TrialSpawnerData p_330822_,
        int p_330441_,
        int p_335693_,
        TrialSpawner.StateAccessor p_310539_,
        PlayerDetector p_312974_,
        PlayerDetector.EntitySelector p_333634_
    ) {
        this.normalConfig = p_362438_;
        this.ominousConfig = p_365001_;
        this.data = p_330822_;
        this.targetCooldownLength = p_330441_;
        this.requiredPlayerRange = p_335693_;
        this.stateAccessor = p_310539_;
        this.playerDetector = p_312974_;
        this.entitySelector = p_333634_;
    }

    public TrialSpawnerConfig getConfig() {
        return this.isOminous ? this.getOminousConfig() : this.getNormalConfig();
    }

    @VisibleForTesting
    public TrialSpawnerConfig getNormalConfig() {
        return this.normalConfig.value();
    }

    @VisibleForTesting
    public TrialSpawnerConfig getOminousConfig() {
        return this.ominousConfig.value();
    }

    public void applyOminous(ServerLevel p_334207_, BlockPos p_327778_) {
        p_334207_.setBlock(p_327778_, p_334207_.getBlockState(p_327778_).setValue(TrialSpawnerBlock.OMINOUS, Boolean.valueOf(true)), 3);
        p_334207_.levelEvent(3020, p_327778_, 1);
        this.isOminous = true;
        this.data.resetAfterBecomingOminous(this, p_334207_);
    }

    public void removeOminous(ServerLevel p_336080_, BlockPos p_328593_) {
        p_336080_.setBlock(p_328593_, p_336080_.getBlockState(p_328593_).setValue(TrialSpawnerBlock.OMINOUS, Boolean.valueOf(false)), 3);
        this.isOminous = false;
    }

    public boolean isOminous() {
        return this.isOminous;
    }

    public TrialSpawnerData getData() {
        return this.data;
    }

    public int getTargetCooldownLength() {
        return this.targetCooldownLength;
    }

    public int getRequiredPlayerRange() {
        return this.requiredPlayerRange;
    }

    public TrialSpawnerState getState() {
        return this.stateAccessor.getState();
    }

    public void setState(Level p_310153_, TrialSpawnerState p_312484_) {
        this.stateAccessor.setState(p_310153_, p_312484_);
    }

    public void markUpdated() {
        this.stateAccessor.markUpdated();
    }

    public PlayerDetector getPlayerDetector() {
        return this.playerDetector;
    }

    public PlayerDetector.EntitySelector getEntitySelector() {
        return this.entitySelector;
    }

    public boolean canSpawnInLevel(ServerLevel p_361781_) {
        if (this.overridePeacefulAndMobSpawnRule) {
            return true;
        } else {
            return p_361781_.getDifficulty() == Difficulty.PEACEFUL ? false : p_361781_.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        }
    }

    public Optional<UUID> spawnMob(ServerLevel p_312690_, BlockPos p_313108_) {
        RandomSource randomsource = p_312690_.getRandom();
        SpawnData spawndata = this.data.getOrCreateNextSpawnData(this, p_312690_.getRandom());
        CompoundTag compoundtag = spawndata.entityToSpawn();
        ListTag listtag = compoundtag.getList("Pos", 6);
        Optional<EntityType<?>> optional = EntityType.by(compoundtag);
        if (optional.isEmpty()) {
            return Optional.empty();
        } else {
            int i = listtag.size();
            double d0 = i >= 1
                ? listtag.getDouble(0)
                : (double)p_313108_.getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.getConfig().spawnRange() + 0.5;
            double d1 = i >= 2 ? listtag.getDouble(1) : (double)(p_313108_.getY() + randomsource.nextInt(3) - 1);
            double d2 = i >= 3
                ? listtag.getDouble(2)
                : (double)p_313108_.getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.getConfig().spawnRange() + 0.5;
            if (!p_312690_.noCollision(optional.get().getSpawnAABB(d0, d1, d2))) {
                return Optional.empty();
            } else {
                Vec3 vec3 = new Vec3(d0, d1, d2);
                if (!inLineOfSight(p_312690_, p_313108_.getCenter(), vec3)) {
                    return Optional.empty();
                } else {
                    BlockPos blockpos = BlockPos.containing(vec3);
                    if (!SpawnPlacements.checkSpawnRules(optional.get(), p_312690_, EntitySpawnReason.TRIAL_SPAWNER, blockpos, p_312690_.getRandom())) {
                        return Optional.empty();
                    } else {
                        if (spawndata.getCustomSpawnRules().isPresent()) {
                            SpawnData.CustomSpawnRules spawndata$customspawnrules = spawndata.getCustomSpawnRules().get();
                            if (!spawndata$customspawnrules.isValidPosition(blockpos, p_312690_)) {
                                return Optional.empty();
                            }
                        }

                        Entity entity = EntityType.loadEntityRecursive(compoundtag, p_312690_, EntitySpawnReason.TRIAL_SPAWNER, p_312166_ -> {
                            p_312166_.moveTo(d0, d1, d2, randomsource.nextFloat() * 360.0F, 0.0F);
                            return p_312166_;
                        });
                        if (entity == null) {
                            return Optional.empty();
                        } else {
                            if (entity instanceof Mob mob) {
                                if (!mob.checkSpawnObstruction(p_312690_)) {
                                    return Optional.empty();
                                }

                                boolean flag = spawndata.getEntityToSpawn().size() == 1 && spawndata.getEntityToSpawn().contains("id", 8);
                                if (flag) {
                                    mob.finalizeSpawn(p_312690_, p_312690_.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.TRIAL_SPAWNER, null);
                                }

                                mob.setPersistenceRequired();
                                spawndata.getEquipment().ifPresent(mob::equip);
                            }

                            if (!p_312690_.tryAddFreshEntityWithPassengers(entity)) {
                                return Optional.empty();
                            } else {
                                TrialSpawner.FlameParticle trialspawner$flameparticle = this.isOminous
                                    ? TrialSpawner.FlameParticle.OMINOUS
                                    : TrialSpawner.FlameParticle.NORMAL;
                                p_312690_.levelEvent(3011, p_313108_, trialspawner$flameparticle.encode());
                                p_312690_.levelEvent(3012, blockpos, trialspawner$flameparticle.encode());
                                p_312690_.gameEvent(entity, GameEvent.ENTITY_PLACE, blockpos);
                                return Optional.of(entity.getUUID());
                            }
                        }
                    }
                }
            }
        }
    }

    public void ejectReward(ServerLevel p_310080_, BlockPos p_311547_, ResourceKey<LootTable> p_330647_) {
        LootTable loottable = p_310080_.getServer().reloadableRegistries().getLootTable(p_330647_);
        LootParams lootparams = new LootParams.Builder(p_310080_).create(LootContextParamSets.EMPTY);
        ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams);
        if (!objectarraylist.isEmpty()) {
            for (ItemStack itemstack : objectarraylist) {
                DefaultDispenseItemBehavior.spawnItem(p_310080_, itemstack, 2, Direction.UP, Vec3.atBottomCenterOf(p_311547_).relative(Direction.UP, 1.2));
            }

            p_310080_.levelEvent(3014, p_311547_, 0);
        }
    }

    public void tickClient(Level p_309627_, BlockPos p_311485_, boolean p_332221_) {
        TrialSpawnerState trialspawnerstate = this.getState();
        trialspawnerstate.emitParticles(p_309627_, p_311485_, p_332221_);
        if (trialspawnerstate.hasSpinningMob()) {
            double d0 = (double)Math.max(0L, this.data.nextMobSpawnsAt - p_309627_.getGameTime());
            this.data.oSpin = this.data.spin;
            this.data.spin = (this.data.spin + trialspawnerstate.spinningMobSpeed() / (d0 + 200.0)) % 360.0;
        }

        if (trialspawnerstate.isCapableOfSpawning()) {
            RandomSource randomsource = p_309627_.getRandom();
            if (randomsource.nextFloat() <= 0.02F) {
                SoundEvent soundevent = p_332221_ ? SoundEvents.TRIAL_SPAWNER_AMBIENT_OMINOUS : SoundEvents.TRIAL_SPAWNER_AMBIENT;
                p_309627_.playLocalSound(p_311485_, soundevent, SoundSource.BLOCKS, randomsource.nextFloat() * 0.25F + 0.75F, randomsource.nextFloat() + 0.5F, false);
            }
        }
    }

    public void tickServer(ServerLevel p_310996_, BlockPos p_312836_, boolean p_332881_) {
        this.isOminous = p_332881_;
        TrialSpawnerState trialspawnerstate = this.getState();
        if (this.data.currentMobs.removeIf(p_309715_ -> shouldMobBeUntracked(p_310996_, p_312836_, p_309715_))) {
            this.data.nextMobSpawnsAt = p_310996_.getGameTime() + (long)this.getConfig().ticksBetweenSpawn();
        }

        TrialSpawnerState trialspawnerstate1 = trialspawnerstate.tickAndGetNext(p_312836_, this, p_310996_);
        if (trialspawnerstate1 != trialspawnerstate) {
            this.setState(p_310996_, trialspawnerstate1);
        }
    }

    private static boolean shouldMobBeUntracked(ServerLevel p_312275_, BlockPos p_310158_, UUID p_312011_) {
        Entity entity = p_312275_.getEntity(p_312011_);
        return entity == null
            || !entity.isAlive()
            || !entity.level().dimension().equals(p_312275_.dimension())
            || entity.blockPosition().distSqr(p_310158_) > (double)MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    private static boolean inLineOfSight(Level p_311873_, Vec3 p_311845_, Vec3 p_312229_) {
        BlockHitResult blockhitresult = p_311873_.clip(
            new ClipContext(p_312229_, p_311845_, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty())
        );
        return blockhitresult.getBlockPos().equals(BlockPos.containing(p_311845_)) || blockhitresult.getType() == HitResult.Type.MISS;
    }

    public static void addSpawnParticles(Level p_333032_, BlockPos p_328008_, RandomSource p_330922_, SimpleParticleType p_331431_) {
        for (int i = 0; i < 20; i++) {
            double d0 = (double)p_328008_.getX() + 0.5 + (p_330922_.nextDouble() - 0.5) * 2.0;
            double d1 = (double)p_328008_.getY() + 0.5 + (p_330922_.nextDouble() - 0.5) * 2.0;
            double d2 = (double)p_328008_.getZ() + 0.5 + (p_330922_.nextDouble() - 0.5) * 2.0;
            p_333032_.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
            p_333032_.addParticle(p_331431_, d0, d1, d2, 0.0, 0.0, 0.0);
        }
    }

    public static void addBecomeOminousParticles(Level p_312837_, BlockPos p_311261_, RandomSource p_312356_) {
        for (int i = 0; i < 20; i++) {
            double d0 = (double)p_311261_.getX() + 0.5 + (p_312356_.nextDouble() - 0.5) * 2.0;
            double d1 = (double)p_311261_.getY() + 0.5 + (p_312356_.nextDouble() - 0.5) * 2.0;
            double d2 = (double)p_311261_.getZ() + 0.5 + (p_312356_.nextDouble() - 0.5) * 2.0;
            double d3 = p_312356_.nextGaussian() * 0.02;
            double d4 = p_312356_.nextGaussian() * 0.02;
            double d5 = p_312356_.nextGaussian() * 0.02;
            p_312837_.addParticle(ParticleTypes.TRIAL_OMEN, d0, d1, d2, d3, d4, d5);
            p_312837_.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d0, d1, d2, d3, d4, d5);
        }
    }

    public static void addDetectPlayerParticles(Level p_309415_, BlockPos p_309941_, RandomSource p_310263_, int p_310988_, ParticleOptions p_331085_) {
        for (int i = 0; i < 30 + Math.min(p_310988_, 10) * 5; i++) {
            double d0 = (double)(2.0F * p_310263_.nextFloat() - 1.0F) * 0.65;
            double d1 = (double)(2.0F * p_310263_.nextFloat() - 1.0F) * 0.65;
            double d2 = (double)p_309941_.getX() + 0.5 + d0;
            double d3 = (double)p_309941_.getY() + 0.1 + (double)p_310263_.nextFloat() * 0.8;
            double d4 = (double)p_309941_.getZ() + 0.5 + d1;
            p_309415_.addParticle(p_331085_, d2, d3, d4, 0.0, 0.0, 0.0);
        }
    }

    public static void addEjectItemParticles(Level p_311170_, BlockPos p_309958_, RandomSource p_309409_) {
        for (int i = 0; i < 20; i++) {
            double d0 = (double)p_309958_.getX() + 0.4 + p_309409_.nextDouble() * 0.2;
            double d1 = (double)p_309958_.getY() + 0.4 + p_309409_.nextDouble() * 0.2;
            double d2 = (double)p_309958_.getZ() + 0.4 + p_309409_.nextDouble() * 0.2;
            double d3 = p_309409_.nextGaussian() * 0.02;
            double d4 = p_309409_.nextGaussian() * 0.02;
            double d5 = p_309409_.nextGaussian() * 0.02;
            p_311170_.addParticle(ParticleTypes.SMALL_FLAME, d0, d1, d2, d3, d4, d5 * 0.25);
            p_311170_.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
        }
    }

    public void overrideEntityToSpawn(EntityType<?> p_378280_, Level p_376000_) {
        this.data.reset();
        this.normalConfig = Holder.direct(this.normalConfig.value().withSpawning(p_378280_));
        this.ominousConfig = Holder.direct(this.ominousConfig.value().withSpawning(p_378280_));
        this.setState(p_376000_, TrialSpawnerState.INACTIVE);
    }

    @Deprecated(
        forRemoval = true
    )
    @VisibleForTesting
    public void setPlayerDetector(PlayerDetector p_311472_) {
        this.playerDetector = p_311472_;
    }

    @Deprecated(
        forRemoval = true
    )
    @VisibleForTesting
    public void overridePeacefulAndMobSpawnRule() {
        this.overridePeacefulAndMobSpawnRule = true;
    }

    public static enum FlameParticle {
        NORMAL(ParticleTypes.FLAME),
        OMINOUS(ParticleTypes.SOUL_FIRE_FLAME);

        public final SimpleParticleType particleType;

        private FlameParticle(final SimpleParticleType p_332977_) {
            this.particleType = p_332977_;
        }

        public static TrialSpawner.FlameParticle decode(int p_333274_) {
            TrialSpawner.FlameParticle[] atrialspawner$flameparticle = values();
            return p_333274_ <= atrialspawner$flameparticle.length && p_333274_ >= 0 ? atrialspawner$flameparticle[p_333274_] : NORMAL;
        }

        public int encode() {
            return this.ordinal();
        }
    }

    public interface StateAccessor {
        void setState(Level p_309383_, TrialSpawnerState p_310563_);

        TrialSpawnerState getState();

        void markUpdated();
    }
}