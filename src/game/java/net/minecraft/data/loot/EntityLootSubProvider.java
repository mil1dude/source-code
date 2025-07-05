package net.minecraft.data.loot;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SheepPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

public abstract class EntityLootSubProvider implements LootTableSubProvider {
    protected final HolderLookup.Provider registries;
    private final FeatureFlagSet allowed;
    private final FeatureFlagSet required;
    private final Map<EntityType<?>, Map<ResourceKey<LootTable>, LootTable.Builder>> map = Maps.newHashMap();

    protected final AnyOfCondition.Builder shouldSmeltLoot() {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return AnyOfCondition.anyOf(
            LootItemEntityPropertyCondition.hasProperties(
                LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true))
            ),
            LootItemEntityPropertyCondition.hasProperties(
                LootContext.EntityTarget.DIRECT_ATTACKER,
                EntityPredicate.Builder.entity()
                    .equipment(
                        EntityEquipmentPredicate.Builder.equipment()
                            .mainhand(
                                ItemPredicate.Builder.item()
                                    .withSubPredicate(
                                        ItemSubPredicates.ENCHANTMENTS,
                                        ItemEnchantmentsPredicate.enchantments(
                                            List.of(new EnchantmentPredicate(registrylookup.getOrThrow(EnchantmentTags.SMELTS_LOOT), MinMaxBounds.Ints.ANY))
                                        )
                                    )
                            )
                    )
            )
        );
    }

    protected EntityLootSubProvider(FeatureFlagSet p_251971_, HolderLookup.Provider p_343057_) {
        this(p_251971_, p_251971_, p_343057_);
    }

    protected EntityLootSubProvider(FeatureFlagSet p_266989_, FeatureFlagSet p_267138_, HolderLookup.Provider p_342748_) {
        this.allowed = p_266989_;
        this.required = p_267138_;
        this.registries = p_342748_;
    }

    public static LootPool.Builder createSheepDispatchPool(Map<DyeColor, ResourceKey<LootTable>> p_362310_) {
        AlternativesEntry.Builder alternativesentry$builder = AlternativesEntry.alternatives();

        for (Entry<DyeColor, ResourceKey<LootTable>> entry : p_362310_.entrySet()) {
            alternativesentry$builder = alternativesentry$builder.otherwise(
                NestedLootTable.lootTableReference(entry.getValue())
                    .when(
                        LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().subPredicate(SheepPredicate.hasWool(entry.getKey()))
                        )
                    )
            );
        }

        return LootPool.lootPool().add(alternativesentry$builder);
    }

    public abstract void generate();

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> p_251751_) {
        this.generate();
        Set<ResourceKey<LootTable>> set = new HashSet<>();
        BuiltInRegistries.ENTITY_TYPE
            .listElements()
            .forEach(
                p_358214_ -> {
                    EntityType<?> entitytype = p_358214_.value();
                    if (entitytype.isEnabled(this.allowed)) {
                        Optional<ResourceKey<LootTable>> optional = entitytype.getDefaultLootTable();
                        if (optional.isPresent()) {
                            Map<ResourceKey<LootTable>, LootTable.Builder> map = this.map.remove(entitytype);
                            if (entitytype.isEnabled(this.required) && (map == null || !map.containsKey(optional.get()))) {
                                throw new IllegalStateException(
                                    String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", optional.get(), p_358214_.key().location())
                                );
                            }

                            if (map != null) {
                                map.forEach(
                                    (p_329509_, p_250972_) -> {
                                        if (!set.add((ResourceKey<LootTable>)p_329509_)) {
                                            throw new IllegalStateException(
                                                String.format(Locale.ROOT, "Duplicate loottable '%s' for '%s'", p_329509_, p_358214_.key().location())
                                            );
                                        } else {
                                            p_251751_.accept((ResourceKey<LootTable>)p_329509_, p_250972_);
                                        }
                                    }
                                );
                            }
                        } else {
                            Map<ResourceKey<LootTable>, LootTable.Builder> map1 = this.map.remove(entitytype);
                            if (map1 != null) {
                                throw new IllegalStateException(
                                    String.format(
                                        Locale.ROOT,
                                        "Weird loottables '%s' for '%s', not a LivingEntity so should not have loot",
                                        map1.keySet().stream().map(p_325849_ -> p_325849_.location().toString()).collect(Collectors.joining(",")),
                                        p_358214_.key().location()
                                    )
                                );
                            }
                        }
                    }
                }
            );
        if (!this.map.isEmpty()) {
            throw new IllegalStateException("Created loot tables for entities not supported by datapack: " + this.map.keySet());
        }
    }

    protected LootItemCondition.Builder killedByFrog(HolderGetter<EntityType<?>> p_369169_) {
        return DamageSourceCondition.hasDamageSource(
            DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of(p_369169_, EntityType.FROG))
        );
    }

    protected LootItemCondition.Builder killedByFrogVariant(HolderGetter<EntityType<?>> p_364902_, ResourceKey<FrogVariant> p_330466_) {
        return DamageSourceCondition.hasDamageSource(
            DamageSourcePredicate.Builder.damageType()
                .source(
                    EntityPredicate.Builder.entity()
                        .of(p_364902_, EntityType.FROG)
                        .subPredicate(EntitySubPredicates.frogVariant(BuiltInRegistries.FROG_VARIANT.getOrThrow(p_330466_)))
                )
        );
    }

    protected void add(EntityType<?> p_248740_, LootTable.Builder p_249440_) {
        this.add(p_248740_, p_248740_.getDefaultLootTable().orElseThrow(() -> new IllegalStateException("Entity " + p_248740_ + " has no loot table")), p_249440_);
    }

    protected void add(EntityType<?> p_252130_, ResourceKey<LootTable> p_332898_, LootTable.Builder p_249357_) {
        this.map.computeIfAbsent(p_252130_, p_251466_ -> new HashMap<>()).put(p_332898_, p_249357_);
    }
}