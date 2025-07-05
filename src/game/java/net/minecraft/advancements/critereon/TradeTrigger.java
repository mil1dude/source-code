package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
    @Override
    public Codec<TradeTrigger.TriggerInstance> codec() {
        return TradeTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_70960_, AbstractVillager p_70961_, ItemStack p_70962_) {
        LootContext lootcontext = EntityPredicate.createContext(p_70960_, p_70961_);
        this.trigger(p_70960_, p_70970_ -> p_70970_.matches(lootcontext, p_70962_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> villager, Optional<ItemPredicate> item)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TradeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325256_ -> p_325256_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TradeTrigger.TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("villager").forGetter(TradeTrigger.TriggerInstance::villager),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TradeTrigger.TriggerInstance::item)
                    )
                    .apply(p_325256_, TradeTrigger.TriggerInstance::new)
        );

        public static Criterion<TradeTrigger.TriggerInstance> tradedWithVillager() {
            return CriteriaTriggers.TRADE.createCriterion(new TradeTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<TradeTrigger.TriggerInstance> tradedWithVillager(EntityPredicate.Builder p_191437_) {
            return CriteriaTriggers.TRADE
                .createCriterion(new TradeTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(p_191437_)), Optional.empty(), Optional.empty()));
        }

        public boolean matches(LootContext p_70985_, ItemStack p_70986_) {
            return this.villager.isPresent() && !this.villager.get().matches(p_70985_)
                ? false
                : !this.item.isPresent() || this.item.get().test(p_70986_);
        }

        @Override
        public void validate(CriterionValidator p_309853_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_309853_);
            p_309853_.validateEntity(this.villager, ".villager");
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}