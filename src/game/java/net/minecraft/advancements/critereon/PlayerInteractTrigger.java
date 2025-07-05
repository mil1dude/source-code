package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance> {
    @Override
    public Codec<PlayerInteractTrigger.TriggerInstance> codec() {
        return PlayerInteractTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_61495_, ItemStack p_61496_, Entity p_61497_) {
        LootContext lootcontext = EntityPredicate.createContext(p_61495_, p_61497_);
        this.trigger(p_61495_, p_61501_ -> p_61501_.matches(p_61496_, lootcontext));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<ContextAwarePredicate> entity)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<PlayerInteractTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325239_ -> p_325239_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PlayerInteractTrigger.TriggerInstance::player),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(PlayerInteractTrigger.TriggerInstance::item),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(PlayerInteractTrigger.TriggerInstance::entity)
                    )
                    .apply(p_325239_, PlayerInteractTrigger.TriggerInstance::new)
        );

        public static Criterion<PlayerInteractTrigger.TriggerInstance> itemUsedOnEntity(
            Optional<ContextAwarePredicate> p_297673_, ItemPredicate.Builder p_286235_, Optional<ContextAwarePredicate> p_301321_
        ) {
            return CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.createCriterion(new PlayerInteractTrigger.TriggerInstance(p_297673_, Optional.of(p_286235_.build()), p_301321_));
        }

        public static Criterion<PlayerInteractTrigger.TriggerInstance> itemUsedOnEntity(ItemPredicate.Builder p_286289_, Optional<ContextAwarePredicate> p_297754_) {
            return itemUsedOnEntity(Optional.empty(), p_286289_, p_297754_);
        }

        public boolean matches(ItemStack p_61522_, LootContext p_61523_) {
            return this.item.isPresent() && !this.item.get().test(p_61522_)
                ? false
                : this.entity.isEmpty() || this.entity.get().matches(p_61523_);
        }

        @Override
        public void validate(CriterionValidator p_309953_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_309953_);
            p_309953_.validateEntity(this.entity, ".entity");
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}