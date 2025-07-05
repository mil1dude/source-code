package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;

public class StartRidingTrigger extends SimpleCriterionTrigger<StartRidingTrigger.TriggerInstance> {
    @Override
    public Codec<StartRidingTrigger.TriggerInstance> codec() {
        return StartRidingTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_160388_) {
        this.trigger(p_160388_, p_160394_ -> true);
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<StartRidingTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325250_ -> p_325250_.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(StartRidingTrigger.TriggerInstance::player))
                    .apply(p_325250_, StartRidingTrigger.TriggerInstance::new)
        );

        public static Criterion<StartRidingTrigger.TriggerInstance> playerStartsRiding(EntityPredicate.Builder p_160402_) {
            return CriteriaTriggers.START_RIDING_TRIGGER.createCriterion(new StartRidingTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(p_160402_))));
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}