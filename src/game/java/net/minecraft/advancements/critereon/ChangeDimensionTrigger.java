package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
    @Override
    public Codec<ChangeDimensionTrigger.TriggerInstance> codec() {
        return ChangeDimensionTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_19758_, ResourceKey<Level> p_19759_, ResourceKey<Level> p_19760_) {
        this.trigger(p_19758_, p_19768_ -> p_19768_.matches(p_19759_, p_19760_));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<ResourceKey<Level>> from, Optional<ResourceKey<Level>> to
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ChangeDimensionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325194_ -> p_325194_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ChangeDimensionTrigger.TriggerInstance::player),
                        ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("from").forGetter(ChangeDimensionTrigger.TriggerInstance::from),
                        ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("to").forGetter(ChangeDimensionTrigger.TriggerInstance::to)
                    )
                    .apply(p_325194_, ChangeDimensionTrigger.TriggerInstance::new)
        );

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimension() {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimension(ResourceKey<Level> p_301176_, ResourceKey<Level> p_298639_) {
            return CriteriaTriggers.CHANGED_DIMENSION
                .createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.of(p_301176_), Optional.of(p_298639_)));
        }

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimensionTo(ResourceKey<Level> p_19783_) {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(p_19783_)));
        }

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimensionFrom(ResourceKey<Level> p_147564_) {
            return CriteriaTriggers.CHANGED_DIMENSION.createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.of(p_147564_), Optional.empty()));
        }

        public boolean matches(ResourceKey<Level> p_19785_, ResourceKey<Level> p_19786_) {
            return this.from.isPresent() && this.from.get() != p_19785_ ? false : !this.to.isPresent() || this.to.get() == p_19786_;
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}