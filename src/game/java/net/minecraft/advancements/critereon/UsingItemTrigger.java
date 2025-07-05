package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class UsingItemTrigger extends SimpleCriterionTrigger<UsingItemTrigger.TriggerInstance> {
    @Override
    public Codec<UsingItemTrigger.TriggerInstance> codec() {
        return UsingItemTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_163866_, ItemStack p_163867_) {
        this.trigger(p_163866_, p_163870_ -> p_163870_.matches(p_163867_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<UsingItemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325259_ -> p_325259_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UsingItemTrigger.TriggerInstance::player),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(UsingItemTrigger.TriggerInstance::item)
                    )
                    .apply(p_325259_, UsingItemTrigger.TriggerInstance::new)
        );

        public static Criterion<UsingItemTrigger.TriggerInstance> lookingAt(EntityPredicate.Builder p_163884_, ItemPredicate.Builder p_163885_) {
            return CriteriaTriggers.USING_ITEM
                .createCriterion(new UsingItemTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(p_163884_)), Optional.of(p_163885_.build())));
        }

        public boolean matches(ItemStack p_163887_) {
            return !this.item.isPresent() || this.item.get().test(p_163887_);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}