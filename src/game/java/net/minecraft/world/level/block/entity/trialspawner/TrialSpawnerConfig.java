package net.minecraft.world.level.block.entity.trialspawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public record TrialSpawnerConfig(
    int spawnRange,
    float totalMobs,
    float simultaneousMobs,
    float totalMobsAddedPerPlayer,
    float simultaneousMobsAddedPerPlayer,
    int ticksBetweenSpawn,
    SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition,
    SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject,
    ResourceKey<LootTable> itemsToDropWhenOminous
) {
    public static final TrialSpawnerConfig DEFAULT = builder().build();
    public static final Codec<TrialSpawnerConfig> DIRECT_CODEC = RecordCodecBuilder.create(
        p_327364_ -> p_327364_.group(
                    Codec.intRange(1, 128).optionalFieldOf("spawn_range", DEFAULT.spawnRange).forGetter(TrialSpawnerConfig::spawnRange),
                    Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("total_mobs", DEFAULT.totalMobs).forGetter(TrialSpawnerConfig::totalMobs),
                    Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("simultaneous_mobs", DEFAULT.simultaneousMobs).forGetter(TrialSpawnerConfig::simultaneousMobs),
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                        .optionalFieldOf("total_mobs_added_per_player", DEFAULT.totalMobsAddedPerPlayer)
                        .forGetter(TrialSpawnerConfig::totalMobsAddedPerPlayer),
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                        .optionalFieldOf("simultaneous_mobs_added_per_player", DEFAULT.simultaneousMobsAddedPerPlayer)
                        .forGetter(TrialSpawnerConfig::simultaneousMobsAddedPerPlayer),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("ticks_between_spawn", DEFAULT.ticksBetweenSpawn).forGetter(TrialSpawnerConfig::ticksBetweenSpawn),
                    SpawnData.LIST_CODEC.optionalFieldOf("spawn_potentials", SimpleWeightedRandomList.empty()).forGetter(TrialSpawnerConfig::spawnPotentialsDefinition),
                    SimpleWeightedRandomList.wrappedCodecAllowingEmpty(ResourceKey.codec(Registries.LOOT_TABLE))
                        .optionalFieldOf("loot_tables_to_eject", DEFAULT.lootTablesToEject)
                        .forGetter(TrialSpawnerConfig::lootTablesToEject),
                    ResourceKey.codec(Registries.LOOT_TABLE)
                        .optionalFieldOf("items_to_drop_when_ominous", DEFAULT.itemsToDropWhenOminous)
                        .forGetter(TrialSpawnerConfig::itemsToDropWhenOminous)
                )
                .apply(p_327364_, TrialSpawnerConfig::new)
    );
    public static final Codec<Holder<TrialSpawnerConfig>> CODEC = RegistryFileCodec.create(Registries.TRIAL_SPAWNER_CONFIG, DIRECT_CODEC);

    public int calculateTargetTotalMobs(int p_309661_) {
        return (int)Math.floor((double)(this.totalMobs + this.totalMobsAddedPerPlayer * (float)p_309661_));
    }

    public int calculateTargetSimultaneousMobs(int p_312677_) {
        return (int)Math.floor((double)(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * (float)p_312677_));
    }

    public long ticksBetweenItemSpawners() {
        return 160L;
    }

    public static TrialSpawnerConfig.Builder builder() {
        return new TrialSpawnerConfig.Builder();
    }

    public TrialSpawnerConfig withSpawning(EntityType<?> p_376640_) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(p_376640_).toString());
        SpawnData spawndata = new SpawnData(compoundtag, Optional.empty(), Optional.empty());
        return new TrialSpawnerConfig(
            this.spawnRange,
            this.totalMobs,
            this.simultaneousMobs,
            this.totalMobsAddedPerPlayer,
            this.simultaneousMobsAddedPerPlayer,
            this.ticksBetweenSpawn,
            SimpleWeightedRandomList.single(spawndata),
            this.lootTablesToEject,
            this.itemsToDropWhenOminous
        );
    }

    public static class Builder {
        private int spawnRange = 4;
        private float totalMobs = 6.0F;
        private float simultaneousMobs = 2.0F;
        private float totalMobsAddedPerPlayer = 2.0F;
        private float simultaneousMobsAddedPerPlayer = 1.0F;
        private int ticksBetweenSpawn = 40;
        private SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition = SimpleWeightedRandomList.empty();
        private SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject = SimpleWeightedRandomList.<ResourceKey<LootTable>>builder()
            .add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_CONSUMABLES)
            .add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_KEY)
            .build();
        private ResourceKey<LootTable> itemsToDropWhenOminous = BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS;

        public TrialSpawnerConfig.Builder spawnRange(int p_365760_) {
            this.spawnRange = p_365760_;
            return this;
        }

        public TrialSpawnerConfig.Builder totalMobs(float p_369310_) {
            this.totalMobs = p_369310_;
            return this;
        }

        public TrialSpawnerConfig.Builder simultaneousMobs(float p_365936_) {
            this.simultaneousMobs = p_365936_;
            return this;
        }

        public TrialSpawnerConfig.Builder totalMobsAddedPerPlayer(float p_360974_) {
            this.totalMobsAddedPerPlayer = p_360974_;
            return this;
        }

        public TrialSpawnerConfig.Builder simultaneousMobsAddedPerPlayer(float p_367537_) {
            this.simultaneousMobsAddedPerPlayer = p_367537_;
            return this;
        }

        public TrialSpawnerConfig.Builder ticksBetweenSpawn(int p_369084_) {
            this.ticksBetweenSpawn = p_369084_;
            return this;
        }

        public TrialSpawnerConfig.Builder spawnPotentialsDefinition(SimpleWeightedRandomList<SpawnData> p_363681_) {
            this.spawnPotentialsDefinition = p_363681_;
            return this;
        }

        public TrialSpawnerConfig.Builder lootTablesToEject(SimpleWeightedRandomList<ResourceKey<LootTable>> p_365621_) {
            this.lootTablesToEject = p_365621_;
            return this;
        }

        public TrialSpawnerConfig.Builder itemsToDropWhenOminous(ResourceKey<LootTable> p_369844_) {
            this.itemsToDropWhenOminous = p_369844_;
            return this;
        }

        public TrialSpawnerConfig build() {
            return new TrialSpawnerConfig(
                this.spawnRange, this.totalMobs, this.simultaneousMobs, this.totalMobsAddedPerPlayer, this.simultaneousMobsAddedPerPlayer, this.ticksBetweenSpawn, this.spawnPotentialsDefinition, this.lootTablesToEject, this.itemsToDropWhenOminous
            );
        }
    }
}