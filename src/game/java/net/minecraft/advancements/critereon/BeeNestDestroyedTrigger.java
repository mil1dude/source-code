package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BeeNestDestroyedTrigger extends SimpleCriterionTrigger<BeeNestDestroyedTrigger.TriggerInstance> {
    @Override
    public Codec<BeeNestDestroyedTrigger.TriggerInstance> codec() {
        return BeeNestDestroyedTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_146652_, BlockState p_146653_, ItemStack p_146654_, int p_146655_) {
        this.trigger(p_146652_, p_146660_ -> p_146660_.matches(p_146653_, p_146654_, p_146655_));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<ItemPredicate> item, MinMaxBounds.Ints beesInside
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<BeeNestDestroyedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_357618_ -> p_357618_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(BeeNestDestroyedTrigger.TriggerInstance::player),
                        BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(BeeNestDestroyedTrigger.TriggerInstance::block),
                        ItemPredicate.CODEC.optionalFieldOf("item").forGetter(BeeNestDestroyedTrigger.TriggerInstance::item),
                        MinMaxBounds.Ints.CODEC
                            .optionalFieldOf("num_bees_inside", MinMaxBounds.Ints.ANY)
                            .forGetter(BeeNestDestroyedTrigger.TriggerInstance::beesInside)
                    )
                    .apply(p_357618_, BeeNestDestroyedTrigger.TriggerInstance::new)
        );

        public static Criterion<BeeNestDestroyedTrigger.TriggerInstance> destroyedBeeNest(Block p_17513_, ItemPredicate.Builder p_17514_, MinMaxBounds.Ints p_17515_) {
            return CriteriaTriggers.BEE_NEST_DESTROYED
                .createCriterion(
                    new BeeNestDestroyedTrigger.TriggerInstance(Optional.empty(), Optional.of(p_17513_.builtInRegistryHolder()), Optional.of(p_17514_.build()), p_17515_)
                );
        }

        public boolean matches(BlockState p_146662_, ItemStack p_146663_, int p_146664_) {
            if (this.block.isPresent() && !p_146662_.is(this.block.get())) {
                return false;
            } else {
                return this.item.isPresent() && !this.item.get().test(p_146663_) ? false : this.beesInside.matches(p_146664_);
            }
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}