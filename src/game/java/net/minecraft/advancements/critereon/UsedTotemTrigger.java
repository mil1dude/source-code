package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger extends SimpleCriterionTrigger<UsedTotemTrigger.TriggerInstance> {
    @Override
    public Codec<UsedTotemTrigger.TriggerInstance> codec() {
        return UsedTotemTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_74432_, ItemStack p_74433_) {
        this.trigger(p_74432_, p_74436_ -> p_74436_.matches(p_74433_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<UsedTotemTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325258_ -> p_325258_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UsedTotemTrigger.TriggerInstance::player),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(UsedTotemTrigger.TriggerInstance::item)
                    )
                    .apply(p_325258_, UsedTotemTrigger.TriggerInstance::new)
        );

        public static Criterion<UsedTotemTrigger.TriggerInstance> usedTotem(ItemPredicate p_298404_) {
            return CriteriaTriggers.USED_TOTEM.createCriterion(new UsedTotemTrigger.TriggerInstance(Optional.empty(), Optional.of(p_298404_)));
        }

        public static Criterion<UsedTotemTrigger.TriggerInstance> usedTotem(HolderGetter<Item> p_367990_, ItemLike p_300178_) {
            return CriteriaTriggers.USED_TOTEM
                .createCriterion(
                    new UsedTotemTrigger.TriggerInstance(
                        Optional.empty(), Optional.of(ItemPredicate.Builder.item().of(p_367990_, p_300178_).build())
                    )
                );
        }

        public boolean matches(ItemStack p_74451_) {
            return this.item.isEmpty() || this.item.get().test(p_74451_);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}