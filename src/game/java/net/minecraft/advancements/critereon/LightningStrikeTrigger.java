package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger extends SimpleCriterionTrigger<LightningStrikeTrigger.TriggerInstance> {
    @Override
    public Codec<LightningStrikeTrigger.TriggerInstance> codec() {
        return LightningStrikeTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_153392_, LightningBolt p_153393_, List<Entity> p_153394_) {
        List<LootContext> list = p_153394_.stream().map(p_153390_ -> EntityPredicate.createContext(p_153392_, p_153390_)).collect(Collectors.toList());
        LootContext lootcontext = EntityPredicate.createContext(p_153392_, p_153393_);
        this.trigger(p_153392_, p_153402_ -> p_153402_.matches(lootcontext, list));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> lightning, Optional<ContextAwarePredicate> bystander
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<LightningStrikeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325228_ -> p_325228_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(LightningStrikeTrigger.TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("lightning").forGetter(LightningStrikeTrigger.TriggerInstance::lightning),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("bystander").forGetter(LightningStrikeTrigger.TriggerInstance::bystander)
                    )
                    .apply(p_325228_, LightningStrikeTrigger.TriggerInstance::new)
        );

        public static Criterion<LightningStrikeTrigger.TriggerInstance> lightningStrike(Optional<EntityPredicate> p_301310_, Optional<EntityPredicate> p_299336_) {
            return CriteriaTriggers.LIGHTNING_STRIKE
                .createCriterion(
                    new LightningStrikeTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(p_301310_), EntityPredicate.wrap(p_299336_))
                );
        }

        public boolean matches(LootContext p_153419_, List<LootContext> p_153420_) {
            return this.lightning.isPresent() && !this.lightning.get().matches(p_153419_)
                ? false
                : !this.bystander.isPresent() || !p_153420_.stream().noneMatch(this.bystander.get()::matches);
        }

        @Override
        public void validate(CriterionValidator p_312134_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_312134_);
            p_312134_.validateEntity(this.lightning, ".lightning");
            p_312134_.validateEntity(this.bystander, ".bystander");
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}