package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger extends SimpleCriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
    @Override
    public Codec<EntityHurtPlayerTrigger.TriggerInstance> codec() {
        return EntityHurtPlayerTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_35175_, DamageSource p_35176_, float p_35177_, float p_35178_, boolean p_35179_) {
        this.trigger(p_35175_, p_35186_ -> p_35186_.matches(p_35175_, p_35176_, p_35177_, p_35178_, p_35179_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DamagePredicate> damage)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<EntityHurtPlayerTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325211_ -> p_325211_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(EntityHurtPlayerTrigger.TriggerInstance::player),
                        DamagePredicate.CODEC.optionalFieldOf("damage").forGetter(EntityHurtPlayerTrigger.TriggerInstance::damage)
                    )
                    .apply(p_325211_, EntityHurtPlayerTrigger.TriggerInstance::new)
        );

        public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer() {
            return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
        }

        public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer(DamagePredicate p_150188_) {
            return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.of(p_150188_)));
        }

        public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer(DamagePredicate.Builder p_35207_) {
            return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.of(p_35207_.build())));
        }

        public boolean matches(ServerPlayer p_35201_, DamageSource p_35202_, float p_35203_, float p_35204_, boolean p_35205_) {
            return !this.damage.isPresent() || this.damage.get().matches(p_35201_, p_35202_, p_35203_, p_35204_, p_35205_);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}