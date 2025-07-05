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

public class ShotCrossbowTrigger extends SimpleCriterionTrigger<ShotCrossbowTrigger.TriggerInstance> {
    @Override
    public Codec<ShotCrossbowTrigger.TriggerInstance> codec() {
        return ShotCrossbowTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_65463_, ItemStack p_65464_) {
        this.trigger(p_65463_, p_65467_ -> p_65467_.matches(p_65464_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ShotCrossbowTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325245_ -> p_325245_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ShotCrossbowTrigger.TriggerInstance::player),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(ShotCrossbowTrigger.TriggerInstance::item)
                    )
                    .apply(p_325245_, ShotCrossbowTrigger.TriggerInstance::new)
        );

        public static Criterion<ShotCrossbowTrigger.TriggerInstance> shotCrossbow(Optional<ItemPredicate> p_299474_) {
            return CriteriaTriggers.SHOT_CROSSBOW.createCriterion(new ShotCrossbowTrigger.TriggerInstance(Optional.empty(), p_299474_));
        }

        public static Criterion<ShotCrossbowTrigger.TriggerInstance> shotCrossbow(HolderGetter<Item> p_365112_, ItemLike p_65484_) {
            return CriteriaTriggers.SHOT_CROSSBOW
                .createCriterion(
                    new ShotCrossbowTrigger.TriggerInstance(
                        Optional.empty(), Optional.of(ItemPredicate.Builder.item().of(p_365112_, p_65484_).build())
                    )
                );
        }

        public boolean matches(ItemStack p_65482_) {
            return this.item.isEmpty() || this.item.get().test(p_65482_);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}