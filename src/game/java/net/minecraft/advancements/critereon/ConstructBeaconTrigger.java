package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;

public class ConstructBeaconTrigger extends SimpleCriterionTrigger<ConstructBeaconTrigger.TriggerInstance> {
    @Override
    public Codec<ConstructBeaconTrigger.TriggerInstance> codec() {
        return ConstructBeaconTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_148030_, int p_148031_) {
        this.trigger(p_148030_, p_148028_ -> p_148028_.matches(p_148031_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints level)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ConstructBeaconTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325196_ -> p_325196_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ConstructBeaconTrigger.TriggerInstance::player),
                        MinMaxBounds.Ints.CODEC
                            .optionalFieldOf("level", MinMaxBounds.Ints.ANY)
                            .forGetter(ConstructBeaconTrigger.TriggerInstance::level)
                    )
                    .apply(p_325196_, ConstructBeaconTrigger.TriggerInstance::new)
        );

        public static Criterion<ConstructBeaconTrigger.TriggerInstance> constructedBeacon() {
            return CriteriaTriggers.CONSTRUCT_BEACON.createCriterion(new ConstructBeaconTrigger.TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY));
        }

        public static Criterion<ConstructBeaconTrigger.TriggerInstance> constructedBeacon(MinMaxBounds.Ints p_22766_) {
            return CriteriaTriggers.CONSTRUCT_BEACON.createCriterion(new ConstructBeaconTrigger.TriggerInstance(Optional.empty(), p_22766_));
        }

        public boolean matches(int p_148033_) {
            return this.level.matches(p_148033_);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}