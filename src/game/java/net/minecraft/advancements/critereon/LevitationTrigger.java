package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger extends SimpleCriterionTrigger<LevitationTrigger.TriggerInstance> {
    @Override
    public Codec<LevitationTrigger.TriggerInstance> codec() {
        return LevitationTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_49117_, Vec3 p_49118_, int p_49119_) {
        this.trigger(p_49117_, p_49124_ -> p_49124_.matches(p_49117_, p_49118_, p_49119_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DistancePredicate> distance, MinMaxBounds.Ints duration)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<LevitationTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325225_ -> p_325225_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(LevitationTrigger.TriggerInstance::player),
                        DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(LevitationTrigger.TriggerInstance::distance),
                        MinMaxBounds.Ints.CODEC
                            .optionalFieldOf("duration", MinMaxBounds.Ints.ANY)
                            .forGetter(LevitationTrigger.TriggerInstance::duration)
                    )
                    .apply(p_325225_, LevitationTrigger.TriggerInstance::new)
        );

        public static Criterion<LevitationTrigger.TriggerInstance> levitated(DistancePredicate p_49145_) {
            return CriteriaTriggers.LEVITATION
                .createCriterion(new LevitationTrigger.TriggerInstance(Optional.empty(), Optional.of(p_49145_), MinMaxBounds.Ints.ANY));
        }

        public boolean matches(ServerPlayer p_49141_, Vec3 p_49142_, int p_49143_) {
            return this.distance.isPresent()
                    && !this.distance
                        .get()
                        .matches(p_49142_.x, p_49142_.y, p_49142_.z, p_49141_.getX(), p_49141_.getY(), p_49141_.getZ())
                ? false
                : this.duration.matches(p_49143_);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}