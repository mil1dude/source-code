package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger extends SimpleCriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
    @Override
    public Codec<ItemDurabilityTrigger.TriggerInstance> codec() {
        return ItemDurabilityTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_43670_, ItemStack p_43671_, int p_43672_) {
        this.trigger(p_43670_, p_43676_ -> p_43676_.matches(p_43671_, p_43672_));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, MinMaxBounds.Ints durability, MinMaxBounds.Ints delta
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ItemDurabilityTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325220_ -> p_325220_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ItemDurabilityTrigger.TriggerInstance::player),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(ItemDurabilityTrigger.TriggerInstance::item),
                        MinMaxBounds.Ints.CODEC
                            .optionalFieldOf("durability", MinMaxBounds.Ints.ANY)
                            .forGetter(ItemDurabilityTrigger.TriggerInstance::durability),
                        MinMaxBounds.Ints.CODEC
                            .optionalFieldOf("delta", MinMaxBounds.Ints.ANY)
                            .forGetter(ItemDurabilityTrigger.TriggerInstance::delta)
                    )
                    .apply(p_325220_, ItemDurabilityTrigger.TriggerInstance::new)
        );

        public static Criterion<ItemDurabilityTrigger.TriggerInstance> changedDurability(Optional<ItemPredicate> p_300870_, MinMaxBounds.Ints p_151288_) {
            return changedDurability(Optional.empty(), p_300870_, p_151288_);
        }

        public static Criterion<ItemDurabilityTrigger.TriggerInstance> changedDurability(
            Optional<ContextAwarePredicate> p_299530_, Optional<ItemPredicate> p_300893_, MinMaxBounds.Ints p_286730_
        ) {
            return CriteriaTriggers.ITEM_DURABILITY_CHANGED.createCriterion(new ItemDurabilityTrigger.TriggerInstance(p_299530_, p_300893_, p_286730_, MinMaxBounds.Ints.ANY));
        }

        public boolean matches(ItemStack p_43699_, int p_43700_) {
            if (this.item.isPresent() && !this.item.get().test(p_43699_)) {
                return false;
            } else {
                return !this.durability.matches(p_43699_.getMaxDamage() - p_43700_) ? false : this.delta.matches(p_43699_.getDamageValue() - p_43700_);
            }
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}