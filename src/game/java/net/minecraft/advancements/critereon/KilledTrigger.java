package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance> {
    @Override
    public Codec<KilledTrigger.TriggerInstance> codec() {
        return KilledTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_48105_, Entity p_48106_, DamageSource p_48107_) {
        LootContext lootcontext = EntityPredicate.createContext(p_48105_, p_48106_);
        this.trigger(p_48105_, p_48112_ -> p_48112_.matches(p_48105_, lootcontext, p_48107_));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entityPredicate, Optional<DamageSourcePredicate> killingBlow
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<KilledTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325224_ -> p_325224_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(KilledTrigger.TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(KilledTrigger.TriggerInstance::entityPredicate),
                        DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(KilledTrigger.TriggerInstance::killingBlow)
                    )
                    .apply(p_325224_, KilledTrigger.TriggerInstance::new)
        );

        public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(Optional<EntityPredicate> p_299523_) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY
                .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(p_299523_), Optional.empty()));
        }

        public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(EntityPredicate.Builder p_48137_) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY
                .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(p_48137_)), Optional.empty()));
        }

        public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity() {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(Optional<EntityPredicate> p_299572_, Optional<DamageSourcePredicate> p_297245_) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(p_299572_), p_297245_));
        }

        public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(EntityPredicate.Builder p_152106_, Optional<DamageSourcePredicate> p_297683_) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY
                .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(p_152106_)), p_297683_));
        }

        public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(Optional<EntityPredicate> p_300641_, DamageSourcePredicate.Builder p_300954_) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY
                .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(p_300641_), Optional.of(p_300954_.build())));
        }

        public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntity(EntityPredicate.Builder p_300999_, DamageSourcePredicate.Builder p_298768_) {
            return CriteriaTriggers.PLAYER_KILLED_ENTITY
                .createCriterion(
                    new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(p_300999_)), Optional.of(p_298768_.build()))
                );
        }

        public static Criterion<KilledTrigger.TriggerInstance> playerKilledEntityNearSculkCatalyst() {
            return CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> p_300543_) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER
                .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(p_300543_), Optional.empty()));
        }

        public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(EntityPredicate.Builder p_300131_) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER
                .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(p_300131_)), Optional.empty()));
        }

        public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer() {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> p_297719_, Optional<DamageSourcePredicate> p_298112_) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER.createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(p_297719_), p_298112_));
        }

        public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(EntityPredicate.Builder p_298074_, Optional<DamageSourcePredicate> p_300879_) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER
                .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(p_298074_)), p_300879_));
        }

        public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(Optional<EntityPredicate> p_297520_, DamageSourcePredicate.Builder p_299317_) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER
                .createCriterion(new KilledTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(p_297520_), Optional.of(p_299317_.build())));
        }

        public static Criterion<KilledTrigger.TriggerInstance> entityKilledPlayer(EntityPredicate.Builder p_152122_, DamageSourcePredicate.Builder p_299947_) {
            return CriteriaTriggers.ENTITY_KILLED_PLAYER
                .createCriterion(
                    new KilledTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(p_152122_)), Optional.of(p_299947_.build()))
                );
        }

        public boolean matches(ServerPlayer p_48131_, LootContext p_48132_, DamageSource p_48133_) {
            return this.killingBlow.isPresent() && !this.killingBlow.get().matches(p_48131_, p_48133_)
                ? false
                : this.entityPredicate.isEmpty() || this.entityPredicate.get().matches(p_48132_);
        }

        @Override
        public void validate(CriterionValidator p_311240_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_311240_);
            p_311240_.validateEntity(this.entityPredicate, ".entity");
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}