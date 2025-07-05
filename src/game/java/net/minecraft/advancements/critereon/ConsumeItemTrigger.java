package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ConsumeItemTrigger extends SimpleCriterionTrigger<ConsumeItemTrigger.TriggerInstance> {
    @Override
    public Codec<ConsumeItemTrigger.TriggerInstance> codec() {
        return ConsumeItemTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_23683_, ItemStack p_23684_) {
        this.trigger(p_23683_, p_23687_ -> p_23687_.matches(p_23684_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ConsumeItemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325197_ -> p_325197_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ConsumeItemTrigger.TriggerInstance::player),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(ConsumeItemTrigger.TriggerInstance::item)
                    )
                    .apply(p_325197_, ConsumeItemTrigger.TriggerInstance::new)
        );

        public static Criterion<ConsumeItemTrigger.TriggerInstance> usedItem() {
            return CriteriaTriggers.CONSUME_ITEM.createCriterion(new ConsumeItemTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
        }

        public static Criterion<ConsumeItemTrigger.TriggerInstance> usedItem(HolderGetter<Item> p_367236_, ItemLike p_299577_) {
            return usedItem(ItemPredicate.Builder.item().of(p_367236_, p_299577_.asItem()));
        }

        public static Criterion<ConsumeItemTrigger.TriggerInstance> usedItem(ItemPredicate.Builder p_297282_) {
            return CriteriaTriggers.CONSUME_ITEM.createCriterion(new ConsumeItemTrigger.TriggerInstance(Optional.empty(), Optional.of(p_297282_.build())));
        }

        public boolean matches(ItemStack p_23702_) {
            return this.item.isEmpty() || this.item.get().test(p_23702_);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}