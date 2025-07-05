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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PickedUpItemTrigger extends SimpleCriterionTrigger<PickedUpItemTrigger.TriggerInstance> {
    @Override
    public Codec<PickedUpItemTrigger.TriggerInstance> codec() {
        return PickedUpItemTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_221299_, ItemStack p_221300_, @Nullable Entity p_221301_) {
        LootContext lootcontext = EntityPredicate.createContext(p_221299_, p_221301_);
        this.trigger(p_221299_, p_221306_ -> p_221306_.matches(p_221299_, p_221300_, lootcontext));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<ContextAwarePredicate> entity
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<PickedUpItemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325237_ -> p_325237_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PickedUpItemTrigger.TriggerInstance::player),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(PickedUpItemTrigger.TriggerInstance::item),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(PickedUpItemTrigger.TriggerInstance::entity)
                    )
                    .apply(p_325237_, PickedUpItemTrigger.TriggerInstance::new)
        );

        public static Criterion<PickedUpItemTrigger.TriggerInstance> thrownItemPickedUpByEntity(
            ContextAwarePredicate p_286865_, Optional<ItemPredicate> p_297283_, Optional<ContextAwarePredicate> p_300033_
        ) {
            return CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(new PickedUpItemTrigger.TriggerInstance(Optional.of(p_286865_), p_297283_, p_300033_));
        }

        public static Criterion<PickedUpItemTrigger.TriggerInstance> thrownItemPickedUpByPlayer(
            Optional<ContextAwarePredicate> p_299013_, Optional<ItemPredicate> p_299788_, Optional<ContextAwarePredicate> p_299814_
        ) {
            return CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.createCriterion(new PickedUpItemTrigger.TriggerInstance(p_299013_, p_299788_, p_299814_));
        }

        public boolean matches(ServerPlayer p_221323_, ItemStack p_221324_, LootContext p_221325_) {
            return this.item.isPresent() && !this.item.get().test(p_221324_)
                ? false
                : !this.entity.isPresent() || this.entity.get().matches(p_221325_);
        }

        @Override
        public void validate(CriterionValidator p_311413_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_311413_);
            p_311413_.validateEntity(this.entity, ".entity");
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}