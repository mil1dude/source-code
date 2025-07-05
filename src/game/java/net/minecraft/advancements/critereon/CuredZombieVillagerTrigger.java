package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
    @Override
    public Codec<CuredZombieVillagerTrigger.TriggerInstance> codec() {
        return CuredZombieVillagerTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_24275_, Zombie p_24276_, Villager p_24277_) {
        LootContext lootcontext = EntityPredicate.createContext(p_24275_, p_24276_);
        LootContext lootcontext1 = EntityPredicate.createContext(p_24275_, p_24277_);
        this.trigger(p_24275_, p_24285_ -> p_24285_.matches(lootcontext, lootcontext1));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> zombie, Optional<ContextAwarePredicate> villager
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<CuredZombieVillagerTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325198_ -> p_325198_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CuredZombieVillagerTrigger.TriggerInstance::player),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("zombie").forGetter(CuredZombieVillagerTrigger.TriggerInstance::zombie),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("villager").forGetter(CuredZombieVillagerTrigger.TriggerInstance::villager)
                    )
                    .apply(p_325198_, CuredZombieVillagerTrigger.TriggerInstance::new)
        );

        public static Criterion<CuredZombieVillagerTrigger.TriggerInstance> curedZombieVillager() {
            return CriteriaTriggers.CURED_ZOMBIE_VILLAGER.createCriterion(new CuredZombieVillagerTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public boolean matches(LootContext p_24300_, LootContext p_24301_) {
            return this.zombie.isPresent() && !this.zombie.get().matches(p_24300_)
                ? false
                : !this.villager.isPresent() || this.villager.get().matches(p_24301_);
        }

        @Override
        public void validate(CriterionValidator p_309494_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_309494_);
            p_309494_.validateEntity(this.zombie, ".zombie");
            p_309494_.validateEntity(this.villager, ".villager");
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}