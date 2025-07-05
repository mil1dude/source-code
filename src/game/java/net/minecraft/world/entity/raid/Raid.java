package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class Raid {
    public static final SpawnPlacementType RAVAGER_SPAWN_PLACEMENT_TYPE = SpawnPlacements.getPlacementType(EntityType.RAVAGER);
    private static final int ALLOW_SPAWNING_WITHIN_VILLAGE_SECONDS_THRESHOLD = 7;
    private static final int SECTION_RADIUS_FOR_FINDING_NEW_VILLAGE_CENTER = 2;
    private static final int VILLAGE_SEARCH_RADIUS = 32;
    private static final int RAID_TIMEOUT_TICKS = 48000;
    private static final int NUM_SPAWN_ATTEMPTS = 5;
    private static final Component OMINOUS_BANNER_PATTERN_NAME = Component.translatable("block.minecraft.ominous_banner");
    private static final String RAIDERS_REMAINING = "event.minecraft.raid.raiders_remaining";
    public static final int VILLAGE_RADIUS_BUFFER = 16;
    private static final int POST_RAID_TICK_LIMIT = 40;
    private static final int DEFAULT_PRE_RAID_TICKS = 300;
    public static final int MAX_NO_ACTION_TIME = 2400;
    public static final int MAX_CELEBRATION_TICKS = 600;
    private static final int OUTSIDE_RAID_BOUNDS_TIMEOUT = 30;
    public static final int TICKS_PER_DAY = 24000;
    public static final int DEFAULT_MAX_RAID_OMEN_LEVEL = 5;
    private static final int LOW_MOB_THRESHOLD = 2;
    private static final Component RAID_NAME_COMPONENT = Component.translatable("event.minecraft.raid");
    private static final Component RAID_BAR_VICTORY_COMPONENT = Component.translatable("event.minecraft.raid.victory.full");
    private static final Component RAID_BAR_DEFEAT_COMPONENT = Component.translatable("event.minecraft.raid.defeat.full");
    private static final int HERO_OF_THE_VILLAGE_DURATION = 48000;
    private static final int VALID_RAID_RADIUS = 96;
    public static final int VALID_RAID_RADIUS_SQR = 9216;
    public static final int RAID_REMOVAL_THRESHOLD_SQR = 12544;
    private final Map<Integer, Raider> groupToLeaderMap = Maps.newHashMap();
    private final Map<Integer, Set<Raider>> groupRaiderMap = Maps.newHashMap();
    private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
    private long ticksActive;
    private BlockPos center;
    private final ServerLevel level;
    private boolean started;
    private final int id;
    private float totalHealth;
    private int raidOmenLevel;
    private boolean active;
    private int groupsSpawned;
    private final ServerBossEvent raidEvent = new ServerBossEvent(RAID_NAME_COMPONENT, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private int postRaidTicks;
    private int raidCooldownTicks;
    private final RandomSource random = RandomSource.create();
    private final int numGroups;
    private Raid.RaidStatus status;
    private int celebrationTicks;
    private Optional<BlockPos> waveSpawnPos = Optional.empty();

    public Raid(int p_37692_, ServerLevel p_37693_, BlockPos p_37694_) {
        this.id = p_37692_;
        this.level = p_37693_;
        this.active = true;
        this.raidCooldownTicks = 300;
        this.raidEvent.setProgress(0.0F);
        this.center = p_37694_;
        this.numGroups = this.getNumGroups(p_37693_.getDifficulty());
        this.status = Raid.RaidStatus.ONGOING;
    }

    public Raid(ServerLevel p_37696_, CompoundTag p_37697_) {
        this.level = p_37696_;
        this.id = p_37697_.getInt("Id");
        this.started = p_37697_.getBoolean("Started");
        this.active = p_37697_.getBoolean("Active");
        this.ticksActive = p_37697_.getLong("TicksActive");
        this.raidOmenLevel = p_37697_.getInt("BadOmenLevel");
        this.groupsSpawned = p_37697_.getInt("GroupsSpawned");
        this.raidCooldownTicks = p_37697_.getInt("PreRaidTicks");
        this.postRaidTicks = p_37697_.getInt("PostRaidTicks");
        this.totalHealth = p_37697_.getFloat("TotalHealth");
        this.center = new BlockPos(p_37697_.getInt("CX"), p_37697_.getInt("CY"), p_37697_.getInt("CZ"));
        this.numGroups = p_37697_.getInt("NumGroups");
        this.status = Raid.RaidStatus.getByName(p_37697_.getString("Status"));
        this.heroesOfTheVillage.clear();
        if (p_37697_.contains("HeroesOfTheVillage", 9)) {
            for (Tag tag : p_37697_.getList("HeroesOfTheVillage", 11)) {
                this.heroesOfTheVillage.add(NbtUtils.loadUUID(tag));
            }
        }
    }

    public boolean isOver() {
        return this.isVictory() || this.isLoss();
    }

    public boolean isBetweenWaves() {
        return this.hasFirstWaveSpawned() && this.getTotalRaidersAlive() == 0 && this.raidCooldownTicks > 0;
    }

    public boolean hasFirstWaveSpawned() {
        return this.groupsSpawned > 0;
    }

    public boolean isStopped() {
        return this.status == Raid.RaidStatus.STOPPED;
    }

    public boolean isVictory() {
        return this.status == Raid.RaidStatus.VICTORY;
    }

    public boolean isLoss() {
        return this.status == Raid.RaidStatus.LOSS;
    }

    public float getTotalHealth() {
        return this.totalHealth;
    }

    public Set<Raider> getAllRaiders() {
        Set<Raider> set = Sets.newHashSet();

        for (Set<Raider> set1 : this.groupRaiderMap.values()) {
            set.addAll(set1);
        }

        return set;
    }

    public Level getLevel() {
        return this.level;
    }

    public boolean isStarted() {
        return this.started;
    }

    public int getGroupsSpawned() {
        return this.groupsSpawned;
    }

    private Predicate<ServerPlayer> validPlayer() {
        return p_375183_ -> {
            BlockPos blockpos = p_375183_.blockPosition();
            return p_375183_.isAlive() && this.level.getRaidAt(blockpos) == this;
        };
    }

    private void updatePlayers() {
        Set<ServerPlayer> set = Sets.newHashSet(this.raidEvent.getPlayers());
        List<ServerPlayer> list = this.level.getPlayers(this.validPlayer());

        for (ServerPlayer serverplayer : list) {
            if (!set.contains(serverplayer)) {
                this.raidEvent.addPlayer(serverplayer);
            }
        }

        for (ServerPlayer serverplayer1 : set) {
            if (!list.contains(serverplayer1)) {
                this.raidEvent.removePlayer(serverplayer1);
            }
        }
    }

    public int getMaxRaidOmenLevel() {
        return 5;
    }

    public int getRaidOmenLevel() {
        return this.raidOmenLevel;
    }

    public void setRaidOmenLevel(int p_334840_) {
        this.raidOmenLevel = p_334840_;
    }

    public boolean absorbRaidOmen(ServerPlayer p_329612_) {
        MobEffectInstance mobeffectinstance = p_329612_.getEffect(MobEffects.RAID_OMEN);
        if (mobeffectinstance == null) {
            return false;
        } else {
            this.raidOmenLevel = this.raidOmenLevel + mobeffectinstance.getAmplifier() + 1;
            this.raidOmenLevel = Mth.clamp(this.raidOmenLevel, 0, this.getMaxRaidOmenLevel());
            if (!this.hasFirstWaveSpawned()) {
                p_329612_.awardStat(Stats.RAID_TRIGGER);
                CriteriaTriggers.RAID_OMEN.trigger(p_329612_);
            }

            return true;
        }
    }

    public void stop() {
        this.active = false;
        this.raidEvent.removeAllPlayers();
        this.status = Raid.RaidStatus.STOPPED;
    }

    public void tick() {
        if (!this.isStopped()) {
            if (this.status == Raid.RaidStatus.ONGOING) {
                boolean flag = this.active;
                this.active = this.level.hasChunkAt(this.center);
                if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
                    this.stop();
                    return;
                }

                if (flag != this.active) {
                    this.raidEvent.setVisible(this.active);
                }

                if (!this.active) {
                    return;
                }

                if (!this.level.isVillage(this.center)) {
                    this.moveRaidCenterToNearbyVillageSection();
                }

                if (!this.level.isVillage(this.center)) {
                    if (this.groupsSpawned > 0) {
                        this.status = Raid.RaidStatus.LOSS;
                    } else {
                        this.stop();
                    }
                }

                this.ticksActive++;
                if (this.ticksActive >= 48000L) {
                    this.stop();
                    return;
                }

                int i = this.getTotalRaidersAlive();
                if (i == 0 && this.hasMoreWaves()) {
                    if (this.raidCooldownTicks <= 0) {
                        if (this.raidCooldownTicks == 0 && this.groupsSpawned > 0) {
                            this.raidCooldownTicks = 300;
                            this.raidEvent.setName(RAID_NAME_COMPONENT);
                            return;
                        }
                    } else {
                        boolean flag1 = this.waveSpawnPos.isPresent();
                        boolean flag2 = !flag1 && this.raidCooldownTicks % 5 == 0;
                        if (flag1 && !this.level.isPositionEntityTicking(this.waveSpawnPos.get())) {
                            flag2 = true;
                        }

                        if (flag2) {
                            this.waveSpawnPos = this.getValidSpawnPos();
                        }

                        if (this.raidCooldownTicks == 300 || this.raidCooldownTicks % 20 == 0) {
                            this.updatePlayers();
                        }

                        this.raidCooldownTicks--;
                        this.raidEvent.setProgress(Mth.clamp((float)(300 - this.raidCooldownTicks) / 300.0F, 0.0F, 1.0F));
                    }
                }

                if (this.ticksActive % 20L == 0L) {
                    this.updatePlayers();
                    this.updateRaiders();
                    if (i > 0) {
                        if (i <= 2) {
                            this.raidEvent
                                .setName(RAID_NAME_COMPONENT.copy().append(" - ").append(Component.translatable("event.minecraft.raid.raiders_remaining", i)));
                        } else {
                            this.raidEvent.setName(RAID_NAME_COMPONENT);
                        }
                    } else {
                        this.raidEvent.setName(RAID_NAME_COMPONENT);
                    }
                }

                boolean flag3 = false;
                int j = 0;

                while (this.shouldSpawnGroup()) {
                    BlockPos blockpos = this.waveSpawnPos.orElseGet(() -> this.findRandomSpawnPos(20));
                    if (blockpos != null) {
                        this.started = true;
                        this.spawnGroup(blockpos);
                        if (!flag3) {
                            this.playSound(blockpos);
                            flag3 = true;
                        }
                    } else {
                        j++;
                    }

                    if (j > 5) {
                        this.stop();
                        break;
                    }
                }

                if (this.isStarted() && !this.hasMoreWaves() && i == 0) {
                    if (this.postRaidTicks < 40) {
                        this.postRaidTicks++;
                    } else {
                        this.status = Raid.RaidStatus.VICTORY;

                        for (UUID uuid : this.heroesOfTheVillage) {
                            Entity entity = this.level.getEntity(uuid);
                            if (entity instanceof LivingEntity) {
                                LivingEntity livingentity = (LivingEntity)entity;
                                if (!entity.isSpectator()) {
                                    livingentity.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 48000, this.raidOmenLevel - 1, false, false, true));
                                    if (livingentity instanceof ServerPlayer serverplayer) {
                                        serverplayer.awardStat(Stats.RAID_WIN);
                                        CriteriaTriggers.RAID_WIN.trigger(serverplayer);
                                    }
                                }
                            }
                        }
                    }
                }

                this.setDirty();
            } else if (this.isOver()) {
                this.celebrationTicks++;
                if (this.celebrationTicks >= 600) {
                    this.stop();
                    return;
                }

                if (this.celebrationTicks % 20 == 0) {
                    this.updatePlayers();
                    this.raidEvent.setVisible(true);
                    if (this.isVictory()) {
                        this.raidEvent.setProgress(0.0F);
                        this.raidEvent.setName(RAID_BAR_VICTORY_COMPONENT);
                    } else {
                        this.raidEvent.setName(RAID_BAR_DEFEAT_COMPONENT);
                    }
                }
            }
        }
    }

    private void moveRaidCenterToNearbyVillageSection() {
        Stream<SectionPos> stream = SectionPos.cube(SectionPos.of(this.center), 2);
        stream.filter(this.level::isVillage)
            .map(SectionPos::center)
            .min(Comparator.comparingDouble(p_37766_ -> p_37766_.distSqr(this.center)))
            .ifPresent(this::setCenter);
    }

    private Optional<BlockPos> getValidSpawnPos() {
        BlockPos blockpos = this.findRandomSpawnPos(8);
        return blockpos != null ? Optional.of(blockpos) : Optional.empty();
    }

    private boolean hasMoreWaves() {
        return this.hasBonusWave() ? !this.hasSpawnedBonusWave() : !this.isFinalWave();
    }

    private boolean isFinalWave() {
        return this.getGroupsSpawned() == this.numGroups;
    }

    private boolean hasBonusWave() {
        return this.raidOmenLevel > 1;
    }

    private boolean hasSpawnedBonusWave() {
        return this.getGroupsSpawned() > this.numGroups;
    }

    private boolean shouldSpawnBonusGroup() {
        return this.isFinalWave() && this.getTotalRaidersAlive() == 0 && this.hasBonusWave();
    }

    private void updateRaiders() {
        Iterator<Set<Raider>> iterator = this.groupRaiderMap.values().iterator();
        Set<Raider> set = Sets.newHashSet();

        while (iterator.hasNext()) {
            Set<Raider> set1 = iterator.next();

            for (Raider raider : set1) {
                BlockPos blockpos = raider.blockPosition();
                if (raider.isRemoved() || raider.level().dimension() != this.level.dimension() || this.center.distSqr(blockpos) >= 12544.0) {
                    set.add(raider);
                } else if (raider.tickCount > 600) {
                    if (this.level.getEntity(raider.getUUID()) == null) {
                        set.add(raider);
                    }

                    if (!this.level.isVillage(blockpos) && raider.getNoActionTime() > 2400) {
                        raider.setTicksOutsideRaid(raider.getTicksOutsideRaid() + 1);
                    }

                    if (raider.getTicksOutsideRaid() >= 30) {
                        set.add(raider);
                    }
                }
            }
        }

        for (Raider raider1 : set) {
            this.removeFromRaid(raider1, true);
            if (raider1.isPatrolLeader()) {
                this.removeLeader(raider1.getWave());
            }
        }
    }

    private void playSound(BlockPos p_37744_) {
        float f = 13.0F;
        int i = 64;
        Collection<ServerPlayer> collection = this.raidEvent.getPlayers();
        long j = this.random.nextLong();

        for (ServerPlayer serverplayer : this.level.players()) {
            Vec3 vec3 = serverplayer.position();
            Vec3 vec31 = Vec3.atCenterOf(p_37744_);
            double d0 = Math.sqrt(
                (vec31.x - vec3.x) * (vec31.x - vec3.x) + (vec31.z - vec3.z) * (vec31.z - vec3.z)
            );
            double d1 = vec3.x + 13.0 / d0 * (vec31.x - vec3.x);
            double d2 = vec3.z + 13.0 / d0 * (vec31.z - vec3.z);
            if (d0 <= 64.0 || collection.contains(serverplayer)) {
                serverplayer.connection
                    .send(new ClientboundSoundPacket(SoundEvents.RAID_HORN, SoundSource.NEUTRAL, d1, serverplayer.getY(), d2, 64.0F, 1.0F, j));
            }
        }
    }

    private void spawnGroup(BlockPos p_37756_) {
        boolean flag = false;
        int i = this.groupsSpawned + 1;
        this.totalHealth = 0.0F;
        DifficultyInstance difficultyinstance = this.level.getCurrentDifficultyAt(p_37756_);
        boolean flag1 = this.shouldSpawnBonusGroup();

        for (Raid.RaiderType raid$raidertype : Raid.RaiderType.VALUES) {
            int j = this.getDefaultNumSpawns(raid$raidertype, i, flag1) + this.getPotentialBonusSpawns(raid$raidertype, this.random, i, difficultyinstance, flag1);
            int k = 0;

            for (int l = 0; l < j; l++) {
                Raider raider = raid$raidertype.entityType.create(this.level, EntitySpawnReason.EVENT);
                if (raider == null) {
                    break;
                }

                if (!flag && raider.canBeLeader()) {
                    raider.setPatrolLeader(true);
                    this.setLeader(i, raider);
                    flag = true;
                }

                this.joinRaid(i, raider, p_37756_, false);
                if (raid$raidertype.entityType == EntityType.RAVAGER) {
                    Raider raider1 = null;
                    if (i == this.getNumGroups(Difficulty.NORMAL)) {
                        raider1 = EntityType.PILLAGER.create(this.level, EntitySpawnReason.EVENT);
                    } else if (i >= this.getNumGroups(Difficulty.HARD)) {
                        if (k == 0) {
                            raider1 = EntityType.EVOKER.create(this.level, EntitySpawnReason.EVENT);
                        } else {
                            raider1 = EntityType.VINDICATOR.create(this.level, EntitySpawnReason.EVENT);
                        }
                    }

                    k++;
                    if (raider1 != null) {
                        this.joinRaid(i, raider1, p_37756_, false);
                        raider1.moveTo(p_37756_, 0.0F, 0.0F);
                        raider1.startRiding(raider);
                    }
                }
            }
        }

        this.waveSpawnPos = Optional.empty();
        this.groupsSpawned++;
        this.updateBossbar();
        this.setDirty();
    }

    public void joinRaid(int p_37714_, Raider p_37715_, @Nullable BlockPos p_37716_, boolean p_37717_) {
        boolean flag = this.addWaveMob(p_37714_, p_37715_);
        if (flag) {
            p_37715_.setCurrentRaid(this);
            p_37715_.setWave(p_37714_);
            p_37715_.setCanJoinRaid(true);
            p_37715_.setTicksOutsideRaid(0);
            if (!p_37717_ && p_37716_ != null) {
                p_37715_.setPos((double)p_37716_.getX() + 0.5, (double)p_37716_.getY() + 1.0, (double)p_37716_.getZ() + 0.5);
                p_37715_.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(p_37716_), EntitySpawnReason.EVENT, null);
                p_37715_.applyRaidBuffs(this.level, p_37714_, false);
                p_37715_.setOnGround(true);
                this.level.addFreshEntityWithPassengers(p_37715_);
            }
        }
    }

    public void updateBossbar() {
        this.raidEvent.setProgress(Mth.clamp(this.getHealthOfLivingRaiders() / this.totalHealth, 0.0F, 1.0F));
    }

    public float getHealthOfLivingRaiders() {
        float f = 0.0F;

        for (Set<Raider> set : this.groupRaiderMap.values()) {
            for (Raider raider : set) {
                f += raider.getHealth();
            }
        }

        return f;
    }

    private boolean shouldSpawnGroup() {
        return this.raidCooldownTicks == 0 && (this.groupsSpawned < this.numGroups || this.shouldSpawnBonusGroup()) && this.getTotalRaidersAlive() == 0;
    }

    public int getTotalRaidersAlive() {
        return this.groupRaiderMap.values().stream().mapToInt(Set::size).sum();
    }

    public void removeFromRaid(Raider p_37741_, boolean p_37742_) {
        Set<Raider> set = this.groupRaiderMap.get(p_37741_.getWave());
        if (set != null) {
            boolean flag = set.remove(p_37741_);
            if (flag) {
                if (p_37742_) {
                    this.totalHealth = this.totalHealth - p_37741_.getHealth();
                }

                p_37741_.setCurrentRaid(null);
                this.updateBossbar();
                this.setDirty();
            }
        }
    }

    private void setDirty() {
        this.level.getRaids().setDirty();
    }

    public static ItemStack getOminousBannerInstance(HolderGetter<BannerPattern> p_360793_) {
        ItemStack itemstack = new ItemStack(Items.WHITE_BANNER);
        BannerPatternLayers bannerpatternlayers = new BannerPatternLayers.Builder()
            .addIfRegistered(p_360793_, BannerPatterns.RHOMBUS_MIDDLE, DyeColor.CYAN)
            .addIfRegistered(p_360793_, BannerPatterns.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY)
            .addIfRegistered(p_360793_, BannerPatterns.STRIPE_CENTER, DyeColor.GRAY)
            .addIfRegistered(p_360793_, BannerPatterns.BORDER, DyeColor.LIGHT_GRAY)
            .addIfRegistered(p_360793_, BannerPatterns.STRIPE_MIDDLE, DyeColor.BLACK)
            .addIfRegistered(p_360793_, BannerPatterns.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY)
            .addIfRegistered(p_360793_, BannerPatterns.CIRCLE_MIDDLE, DyeColor.LIGHT_GRAY)
            .addIfRegistered(p_360793_, BannerPatterns.BORDER, DyeColor.BLACK)
            .build();
        itemstack.set(DataComponents.BANNER_PATTERNS, bannerpatternlayers);
        itemstack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        itemstack.set(DataComponents.ITEM_NAME, OMINOUS_BANNER_PATTERN_NAME);
        itemstack.set(DataComponents.RARITY, Rarity.UNCOMMON);
        return itemstack;
    }

    @Nullable
    public Raider getLeader(int p_37751_) {
        return this.groupToLeaderMap.get(p_37751_);
    }

    @Nullable
    private BlockPos findRandomSpawnPos(int p_37708_) {
        int i = this.raidCooldownTicks / 20;
        float f = 0.22F * (float)i - 0.24F;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        float f1 = this.level.random.nextFloat() * (float) (Math.PI * 2);

        for (int i1 = 0; i1 < p_37708_; i1++) {
            float f2 = f1 + (float) Math.PI * (float)i1 / 8.0F;
            int j = this.center.getX() + Mth.floor(Mth.cos(f2) * 32.0F * f) + this.level.random.nextInt(3) * Mth.floor(f);
            int l = this.center.getZ() + Mth.floor(Mth.sin(f2) * 32.0F * f) + this.level.random.nextInt(3) * Mth.floor(f);
            int k = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, j, l);
            if (Mth.abs(k - this.center.getY()) <= 96) {
                blockpos$mutableblockpos.set(j, k, l);
                if (!this.level.isVillage(blockpos$mutableblockpos) || i <= 7) {
                    int j1 = 10;
                    if (this.level
                            .hasChunksAt(
                                blockpos$mutableblockpos.getX() - 10,
                                blockpos$mutableblockpos.getZ() - 10,
                                blockpos$mutableblockpos.getX() + 10,
                                blockpos$mutableblockpos.getZ() + 10
                            )
                        && this.level.isPositionEntityTicking(blockpos$mutableblockpos)
                        && (
                            RAVAGER_SPAWN_PLACEMENT_TYPE.isSpawnPositionOk(this.level, blockpos$mutableblockpos, EntityType.RAVAGER)
                                || this.level.getBlockState(blockpos$mutableblockpos.below()).is(Blocks.SNOW)
                                    && this.level.getBlockState(blockpos$mutableblockpos).isAir()
                        )) {
                        return blockpos$mutableblockpos;
                    }
                }
            }
        }

        return null;
    }

    private boolean addWaveMob(int p_37753_, Raider p_37754_) {
        return this.addWaveMob(p_37753_, p_37754_, true);
    }

    public boolean addWaveMob(int p_37719_, Raider p_37720_, boolean p_37721_) {
        this.groupRaiderMap.computeIfAbsent(p_37719_, p_37746_ -> Sets.newHashSet());
        Set<Raider> set = this.groupRaiderMap.get(p_37719_);
        Raider raider = null;

        for (Raider raider1 : set) {
            if (raider1.getUUID().equals(p_37720_.getUUID())) {
                raider = raider1;
                break;
            }
        }

        if (raider != null) {
            set.remove(raider);
            set.add(p_37720_);
        }

        set.add(p_37720_);
        if (p_37721_) {
            this.totalHealth = this.totalHealth + p_37720_.getHealth();
        }

        this.updateBossbar();
        this.setDirty();
        return true;
    }

    public void setLeader(int p_37711_, Raider p_37712_) {
        this.groupToLeaderMap.put(p_37711_, p_37712_);
        p_37712_.setItemSlot(EquipmentSlot.HEAD, getOminousBannerInstance(p_37712_.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
        p_37712_.setDropChance(EquipmentSlot.HEAD, 2.0F);
    }

    public void removeLeader(int p_37759_) {
        this.groupToLeaderMap.remove(p_37759_);
    }

    public BlockPos getCenter() {
        return this.center;
    }

    private void setCenter(BlockPos p_37761_) {
        this.center = p_37761_;
    }

    public int getId() {
        return this.id;
    }

    private int getDefaultNumSpawns(Raid.RaiderType p_37731_, int p_37732_, boolean p_37733_) {
        return p_37733_ ? p_37731_.spawnsPerWaveBeforeBonus[this.numGroups] : p_37731_.spawnsPerWaveBeforeBonus[p_37732_];
    }

    private int getPotentialBonusSpawns(Raid.RaiderType p_219829_, RandomSource p_219830_, int p_219831_, DifficultyInstance p_219832_, boolean p_219833_) {
        Difficulty difficulty = p_219832_.getDifficulty();
        boolean flag = difficulty == Difficulty.EASY;
        boolean flag1 = difficulty == Difficulty.NORMAL;
        int i;
        switch (p_219829_) {
            case VINDICATOR:
            case PILLAGER:
                if (flag) {
                    i = p_219830_.nextInt(2);
                } else if (flag1) {
                    i = 1;
                } else {
                    i = 2;
                }
                break;
            case EVOKER:
            default:
                return 0;
            case WITCH:
                if (flag || p_219831_ <= 2 || p_219831_ == 4) {
                    return 0;
                }

                i = 1;
                break;
            case RAVAGER:
                i = !flag && p_219833_ ? 1 : 0;
        }

        return i > 0 ? p_219830_.nextInt(i + 1) : 0;
    }

    public boolean isActive() {
        return this.active;
    }

    public CompoundTag save(CompoundTag p_37748_) {
        p_37748_.putInt("Id", this.id);
        p_37748_.putBoolean("Started", this.started);
        p_37748_.putBoolean("Active", this.active);
        p_37748_.putLong("TicksActive", this.ticksActive);
        p_37748_.putInt("BadOmenLevel", this.raidOmenLevel);
        p_37748_.putInt("GroupsSpawned", this.groupsSpawned);
        p_37748_.putInt("PreRaidTicks", this.raidCooldownTicks);
        p_37748_.putInt("PostRaidTicks", this.postRaidTicks);
        p_37748_.putFloat("TotalHealth", this.totalHealth);
        p_37748_.putInt("NumGroups", this.numGroups);
        p_37748_.putString("Status", this.status.getName());
        p_37748_.putInt("CX", this.center.getX());
        p_37748_.putInt("CY", this.center.getY());
        p_37748_.putInt("CZ", this.center.getZ());
        ListTag listtag = new ListTag();

        for (UUID uuid : this.heroesOfTheVillage) {
            listtag.add(NbtUtils.createUUID(uuid));
        }

        p_37748_.put("HeroesOfTheVillage", listtag);
        return p_37748_;
    }

    public int getNumGroups(Difficulty p_37725_) {
        switch (p_37725_) {
            case EASY:
                return 3;
            case NORMAL:
                return 5;
            case HARD:
                return 7;
            default:
                return 0;
        }
    }

    public float getEnchantOdds() {
        int i = this.getRaidOmenLevel();
        if (i == 2) {
            return 0.1F;
        } else if (i == 3) {
            return 0.25F;
        } else if (i == 4) {
            return 0.5F;
        } else {
            return i == 5 ? 0.75F : 0.0F;
        }
    }

    public void addHeroOfTheVillage(Entity p_37727_) {
        this.heroesOfTheVillage.add(p_37727_.getUUID());
    }

    static enum RaidStatus {
        ONGOING,
        VICTORY,
        LOSS,
        STOPPED;

        private static final Raid.RaidStatus[] VALUES = values();

        static Raid.RaidStatus getByName(String p_37804_) {
            for (Raid.RaidStatus raid$raidstatus : VALUES) {
                if (p_37804_.equalsIgnoreCase(raid$raidstatus.name())) {
                    return raid$raidstatus;
                }
            }

            return ONGOING;
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    static enum RaiderType {
        VINDICATOR(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5}),
        EVOKER(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2}),
        PILLAGER(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2}),
        WITCH(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1}),
        RAVAGER(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});

        static final Raid.RaiderType[] VALUES = values();
        final EntityType<? extends Raider> entityType;
        final int[] spawnsPerWaveBeforeBonus;

        private RaiderType(final EntityType<? extends Raider> p_37821_, final int[] p_37822_) {
            this.entityType = p_37821_;
            this.spawnsPerWaveBeforeBonus = p_37822_;
        }
    }
}