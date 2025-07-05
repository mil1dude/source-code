package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class TrialSpawnerData {
    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
    private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
    public static MapCodec<TrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_313188_ -> p_313188_.group(
                    UUIDUtil.CODEC_SET.lenientOptionalFieldOf("registered_players", Sets.newHashSet()).forGetter(p_309580_ -> p_309580_.detectedPlayers),
                    UUIDUtil.CODEC_SET.lenientOptionalFieldOf("current_mobs", Sets.newHashSet()).forGetter(p_311034_ -> p_311034_.currentMobs),
                    Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", Long.valueOf(0L)).forGetter(p_309685_ -> p_309685_.cooldownEndsAt),
                    Codec.LONG.lenientOptionalFieldOf("next_mob_spawns_at", Long.valueOf(0L)).forGetter(p_310876_ -> p_310876_.nextMobSpawnsAt),
                    Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", 0).forGetter(p_309745_ -> p_309745_.totalMobsSpawned),
                    SpawnData.CODEC.lenientOptionalFieldOf("spawn_data").forGetter(p_312904_ -> p_312904_.nextSpawnData),
                    ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("ejecting_loot_table").forGetter(p_310765_ -> p_310765_.ejectingLootTable)
                )
                .apply(p_313188_, TrialSpawnerData::new)
    );
    protected final Set<UUID> detectedPlayers = new HashSet<>();
    protected final Set<UUID> currentMobs = new HashSet<>();
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<SpawnData> nextSpawnData;
    protected Optional<ResourceKey<LootTable>> ejectingLootTable;
    @Nullable
    protected Entity displayEntity;
    @Nullable
    private SimpleWeightedRandomList<ItemStack> dispensing;
    protected double spin;
    protected double oSpin;

    public TrialSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public TrialSpawnerData(
        Set<UUID> p_312543_,
        Set<UUID> p_311274_,
        long p_312908_,
        long p_311373_,
        int p_311452_,
        Optional<SpawnData> p_311258_,
        Optional<ResourceKey<LootTable>> p_312612_
    ) {
        this.detectedPlayers.addAll(p_312543_);
        this.currentMobs.addAll(p_311274_);
        this.cooldownEndsAt = p_312908_;
        this.nextMobSpawnsAt = p_311373_;
        this.totalMobsSpawned = p_311452_;
        this.nextSpawnData = p_311258_;
        this.ejectingLootTable = p_312612_;
    }

    public void reset() {
        this.currentMobs.clear();
        this.nextSpawnData = Optional.empty();
        this.resetStatistics();
    }

    public void resetStatistics() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
    }

    public boolean hasMobToSpawn(TrialSpawner p_328530_, RandomSource p_333493_) {
        boolean flag = this.getOrCreateNextSpawnData(p_328530_, p_333493_).getEntityToSpawn().contains("id", 8);
        return flag || !p_328530_.getConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig p_310871_, int p_313160_) {
        return this.totalMobsSpawned >= p_310871_.calculateTargetTotalMobs(p_313160_);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel p_312376_, TrialSpawnerConfig p_313089_, int p_311969_) {
        return p_312376_.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < p_313089_.calculateTargetSimultaneousMobs(p_311969_);
    }

    public int countAdditionalPlayers(BlockPos p_310055_) {
        if (this.detectedPlayers.isEmpty()) {
            Util.logAndPauseIfInIde("Trial Spawner at " + p_310055_ + " has no detected players");
        }

        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel p_313049_, BlockPos p_310981_, TrialSpawner p_331326_) {
        boolean flag = (p_310981_.asLong() + p_313049_.getGameTime()) % 20L != 0L;
        if (!flag) {
            if (!p_331326_.getState().equals(TrialSpawnerState.COOLDOWN) || !p_331326_.isOminous()) {
                List<UUID> list = p_331326_.getPlayerDetector().detect(p_313049_, p_331326_.getEntitySelector(), p_310981_, (double)p_331326_.getRequiredPlayerRange(), true);
                boolean flag1;
                if (!p_331326_.isOminous() && !list.isEmpty()) {
                    Optional<Pair<Player, Holder<MobEffect>>> optional = findPlayerWithOminousEffect(p_313049_, list);
                    optional.ifPresent(p_360526_ -> {
                        Player player = p_360526_.getFirst();
                        if (p_360526_.getSecond() == MobEffects.BAD_OMEN) {
                            transformBadOmenIntoTrialOmen(player);
                        }

                        p_313049_.levelEvent(3020, BlockPos.containing(player.getEyePosition()), 0);
                        p_331326_.applyOminous(p_313049_, p_310981_);
                    });
                    flag1 = optional.isPresent();
                } else {
                    flag1 = false;
                }

                if (!p_331326_.getState().equals(TrialSpawnerState.COOLDOWN) || flag1) {
                    boolean flag2 = p_331326_.getData().detectedPlayers.isEmpty();
                    List<UUID> list1 = flag2
                        ? list
                        : p_331326_.getPlayerDetector().detect(p_313049_, p_331326_.getEntitySelector(), p_310981_, (double)p_331326_.getRequiredPlayerRange(), false);
                    if (this.detectedPlayers.addAll(list1)) {
                        this.nextMobSpawnsAt = Math.max(p_313049_.getGameTime() + 40L, this.nextMobSpawnsAt);
                        if (!flag1) {
                            int i = p_331326_.isOminous() ? 3019 : 3013;
                            p_313049_.levelEvent(i, p_310981_, this.detectedPlayers.size());
                        }
                    }
                }
            }
        }
    }

    private static Optional<Pair<Player, Holder<MobEffect>>> findPlayerWithOminousEffect(ServerLevel p_342909_, List<UUID> p_343949_) {
        Player player = null;

        for (UUID uuid : p_343949_) {
            Player player1 = p_342909_.getPlayerByUUID(uuid);
            if (player1 != null) {
                Holder<MobEffect> holder = MobEffects.TRIAL_OMEN;
                if (player1.hasEffect(holder)) {
                    return Optional.of(Pair.of(player1, holder));
                }

                if (player1.hasEffect(MobEffects.BAD_OMEN)) {
                    player = player1;
                }
            }
        }

        return Optional.ofNullable(player).map(p_341863_ -> Pair.of(p_341863_, MobEffects.BAD_OMEN));
    }

    public void resetAfterBecomingOminous(TrialSpawner p_330837_, ServerLevel p_328172_) {
        this.currentMobs.stream().map(p_328172_::getEntity).forEach(p_360528_ -> {
            if (p_360528_ != null) {
                p_328172_.levelEvent(3012, p_360528_.blockPosition(), TrialSpawner.FlameParticle.NORMAL.encode());
                if (p_360528_ instanceof Mob mob) {
                    mob.dropPreservedEquipment(p_328172_);
                }

                p_360528_.remove(Entity.RemovalReason.DISCARDED);
            }
        });
        if (!p_330837_.getOminousConfig().spawnPotentialsDefinition().isEmpty()) {
            this.nextSpawnData = Optional.empty();
        }

        this.totalMobsSpawned = 0;
        this.currentMobs.clear();
        this.nextMobSpawnsAt = p_328172_.getGameTime() + (long)p_330837_.getOminousConfig().ticksBetweenSpawn();
        p_330837_.markUpdated();
        this.cooldownEndsAt = p_328172_.getGameTime() + p_330837_.getOminousConfig().ticksBetweenItemSpawners();
    }

    private static void transformBadOmenIntoTrialOmen(Player p_327801_) {
        MobEffectInstance mobeffectinstance = p_327801_.getEffect(MobEffects.BAD_OMEN);
        if (mobeffectinstance != null) {
            int i = mobeffectinstance.getAmplifier() + 1;
            int j = 18000 * i;
            p_327801_.removeEffect(MobEffects.BAD_OMEN);
            p_327801_.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN, j, 0));
        }
    }

    public boolean isReadyToOpenShutter(ServerLevel p_311936_, float p_312381_, int p_334019_) {
        long i = this.cooldownEndsAt - (long)p_334019_;
        return (float)p_311936_.getGameTime() >= (float)i + p_312381_;
    }

    public boolean isReadyToEjectItems(ServerLevel p_309478_, float p_310189_, int p_330888_) {
        long i = this.cooldownEndsAt - (long)p_330888_;
        return (float)(p_309478_.getGameTime() - i) % p_310189_ == 0.0F;
    }

    public boolean isCooldownFinished(ServerLevel p_312277_) {
        return p_312277_.getGameTime() >= this.cooldownEndsAt;
    }

    protected SpawnData getOrCreateNextSpawnData(TrialSpawner p_311810_, RandomSource p_311692_) {
        if (this.nextSpawnData.isPresent()) {
            return this.nextSpawnData.get();
        } else {
            SimpleWeightedRandomList<SpawnData> simpleweightedrandomlist = p_311810_.getConfig().spawnPotentialsDefinition();
            Optional<SpawnData> optional = simpleweightedrandomlist.isEmpty()
                ? this.nextSpawnData
                : simpleweightedrandomlist.getRandom(p_311692_).map(WeightedEntry.Wrapper::data);
            this.nextSpawnData = Optional.of(optional.orElseGet(SpawnData::new));
            p_311810_.markUpdated();
            return this.nextSpawnData.get();
        }
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(TrialSpawner p_310895_, Level p_310374_, TrialSpawnerState p_310556_) {
        if (!p_310556_.hasSpinningMob()) {
            return null;
        } else {
            if (this.displayEntity == null) {
                CompoundTag compoundtag = this.getOrCreateNextSpawnData(p_310895_, p_310374_.getRandom()).getEntityToSpawn();
                if (compoundtag.contains("id", 8)) {
                    this.displayEntity = EntityType.loadEntityRecursive(compoundtag, p_310374_, EntitySpawnReason.TRIAL_SPAWNER, Function.identity());
                }
            }

            return this.displayEntity;
        }
    }

    public CompoundTag getUpdateTag(TrialSpawnerState p_310015_) {
        CompoundTag compoundtag = new CompoundTag();
        if (p_310015_ == TrialSpawnerState.ACTIVE) {
            compoundtag.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
        }

        this.nextSpawnData
            .ifPresent(
                p_327366_ -> compoundtag.put(
                        "spawn_data",
                        SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, p_327366_).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))
                    )
            );
        return compoundtag;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }

    SimpleWeightedRandomList<ItemStack> getDispensingItems(ServerLevel p_335070_, TrialSpawnerConfig p_328688_, BlockPos p_329742_) {
        if (this.dispensing != null) {
            return this.dispensing;
        } else {
            LootTable loottable = p_335070_.getServer().reloadableRegistries().getLootTable(p_328688_.itemsToDropWhenOminous());
            LootParams lootparams = new LootParams.Builder(p_335070_).create(LootContextParamSets.EMPTY);
            long i = lowResolutionPosition(p_335070_, p_329742_);
            ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams, i);
            if (objectarraylist.isEmpty()) {
                return SimpleWeightedRandomList.empty();
            } else {
                SimpleWeightedRandomList.Builder<ItemStack> builder = new SimpleWeightedRandomList.Builder<>();

                for (ItemStack itemstack : objectarraylist) {
                    builder.add(itemstack.copyWithCount(1), itemstack.getCount());
                }

                this.dispensing = builder.build();
                return this.dispensing;
            }
        }
    }

    private static long lowResolutionPosition(ServerLevel p_332486_, BlockPos p_332719_) {
        BlockPos blockpos = new BlockPos(
            Mth.floor((float)p_332719_.getX() / 30.0F),
            Mth.floor((float)p_332719_.getY() / 20.0F),
            Mth.floor((float)p_332719_.getZ() / 30.0F)
        );
        return p_332486_.getSeed() + blockpos.asLong();
    }
}