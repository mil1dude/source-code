package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class SummonedEntityTrigger extends SimpleCriterionTrigger<SummonedEntityTrigger.TriggerInstance> {
    @Override
    public Codec<SummonedEntityTrigger.TriggerInstance> codec() {
        return SummonedEntityTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_68257_, Entity p_68258_) {
        LootContext lootcontext = EntityPredicate.createContext(p_68257_, p_68258_);
        this.trigger(p_68257_, p_68265_ -> p_68265_.matches(lootcontext));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entity)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<SummonedEntityTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325253_ -> p_325253_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SummonedEntityTrigger.TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(SummonedEntityTrigger.TriggerInstance::entity)
                    )
                    .apply(p_325253_, SummonedEntityTrigger.TriggerInstance::new)
        );

        public static Criterion<SummonedEntityTrigger.TriggerInstance> summonedEntity(EntityPredicate.Builder p_68276_) {
            return CriteriaTriggers.SUMMONED_ENTITY
                .createCriterion(new SummonedEntityTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(p_68276_))));
        }

        public boolean matches(LootContext p_68280_) {
            return this.entity.isEmpty() || this.entity.get().matches(p_68280_);
        }

        @Override
        public void validate(CriterionValidator p_312940_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_312940_);
            p_312940_.validateEntity(this.entity, ".entity");
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}