package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByArrowTrigger extends SimpleCriterionTrigger<KilledByArrowTrigger.TriggerInstance> {
    @Override
    public Codec<KilledByArrowTrigger.TriggerInstance> codec() {
        return KilledByArrowTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_365402_, Collection<Entity> p_364982_, @Nullable ItemStack p_367008_) {
        List<LootContext> list = Lists.newArrayList();
        Set<EntityType<?>> set = Sets.newHashSet();

        for (Entity entity : p_364982_) {
            set.add(entity.getType());
            list.add(EntityPredicate.createContext(p_365402_, entity));
        }

        this.trigger(p_365402_, p_363274_ -> p_363274_.matches(list, set.size(), p_367008_));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims, MinMaxBounds.Ints uniqueEntityTypes, Optional<ItemPredicate> firedFromWeapon
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<KilledByArrowTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_363742_ -> p_363742_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(KilledByArrowTrigger.TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(KilledByArrowTrigger.TriggerInstance::victims),
                        MinMaxBounds.Ints.CODEC
                            .optionalFieldOf("unique_entity_types", MinMaxBounds.Ints.ANY)
                            .forGetter(KilledByArrowTrigger.TriggerInstance::uniqueEntityTypes),
                        ItemPredicate.CODEC.optionalFieldOf("fired_from_weapon").forGetter(KilledByArrowTrigger.TriggerInstance::firedFromWeapon)
                    )
                    .apply(p_363742_, KilledByArrowTrigger.TriggerInstance::new)
        );

        public static Criterion<KilledByArrowTrigger.TriggerInstance> crossbowKilled(HolderGetter<Item> p_363971_, EntityPredicate.Builder... p_360903_) {
            return CriteriaTriggers.KILLED_BY_ARROW
                .createCriterion(
                    new KilledByArrowTrigger.TriggerInstance(
                        Optional.empty(),
                        EntityPredicate.wrap(p_360903_),
                        MinMaxBounds.Ints.ANY,
                        Optional.of(ItemPredicate.Builder.item().of(p_363971_, Items.CROSSBOW).build())
                    )
                );
        }

        public static Criterion<KilledByArrowTrigger.TriggerInstance> crossbowKilled(HolderGetter<Item> p_368687_, MinMaxBounds.Ints p_363327_) {
            return CriteriaTriggers.KILLED_BY_ARROW
                .createCriterion(
                    new KilledByArrowTrigger.TriggerInstance(
                        Optional.empty(), List.of(), p_363327_, Optional.of(ItemPredicate.Builder.item().of(p_368687_, Items.CROSSBOW).build())
                    )
                );
        }

        public boolean matches(Collection<LootContext> p_367924_, int p_369717_, @Nullable ItemStack p_366570_) {
            if (!this.firedFromWeapon.isPresent() || p_366570_ != null && this.firedFromWeapon.get().test(p_366570_)) {
                if (!this.victims.isEmpty()) {
                    List<LootContext> list = Lists.newArrayList(p_367924_);

                    for (ContextAwarePredicate contextawarepredicate : this.victims) {
                        boolean flag = false;
                        Iterator<LootContext> iterator = list.iterator();

                        while (iterator.hasNext()) {
                            LootContext lootcontext = iterator.next();
                            if (contextawarepredicate.matches(lootcontext)) {
                                iterator.remove();
                                flag = true;
                                break;
                            }
                        }

                        if (!flag) {
                            return false;
                        }
                    }
                }

                return this.uniqueEntityTypes.matches(p_369717_);
            } else {
                return false;
            }
        }

        @Override
        public void validate(CriterionValidator p_365351_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_365351_);
            p_365351_.validateEntities(this.victims, ".victims");
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}