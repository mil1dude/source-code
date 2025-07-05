package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
    @Override
    public Codec<EffectsChangedTrigger.TriggerInstance> codec() {
        return EffectsChangedTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_149263_, @Nullable Entity p_149264_) {
        LootContext lootcontext = p_149264_ != null ? EntityPredicate.createContext(p_149263_, p_149264_) : null;
        this.trigger(p_149263_, p_149268_ -> p_149268_.matches(p_149263_, lootcontext));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<MobEffectsPredicate> effects, Optional<ContextAwarePredicate> source
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<EffectsChangedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325203_ -> p_325203_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(EffectsChangedTrigger.TriggerInstance::player),
                        MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(EffectsChangedTrigger.TriggerInstance::effects),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("source").forGetter(EffectsChangedTrigger.TriggerInstance::source)
                    )
                    .apply(p_325203_, EffectsChangedTrigger.TriggerInstance::new)
        );

        public static Criterion<EffectsChangedTrigger.TriggerInstance> hasEffects(MobEffectsPredicate.Builder p_300809_) {
            return CriteriaTriggers.EFFECTS_CHANGED.createCriterion(new EffectsChangedTrigger.TriggerInstance(Optional.empty(), p_300809_.build(), Optional.empty()));
        }

        public static Criterion<EffectsChangedTrigger.TriggerInstance> gotEffectsFrom(EntityPredicate.Builder p_298504_) {
            return CriteriaTriggers.EFFECTS_CHANGED
                .createCriterion(
                    new EffectsChangedTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(p_298504_.build())))
                );
        }

        public boolean matches(ServerPlayer p_149275_, @Nullable LootContext p_149276_) {
            return this.effects.isPresent() && !this.effects.get().matches(p_149275_)
                ? false
                : !this.source.isPresent() || p_149276_ != null && this.source.get().matches(p_149276_);
        }

        @Override
        public void validate(CriterionValidator p_312004_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_312004_);
            p_312004_.validateEntity(this.source, ".source");
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}