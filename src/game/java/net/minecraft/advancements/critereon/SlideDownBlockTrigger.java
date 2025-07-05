package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SlideDownBlockTrigger extends SimpleCriterionTrigger<SlideDownBlockTrigger.TriggerInstance> {
    @Override
    public Codec<SlideDownBlockTrigger.TriggerInstance> codec() {
        return SlideDownBlockTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_66979_, BlockState p_66980_) {
        this.trigger(p_66979_, p_66986_ -> p_66986_.matches(p_66980_));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<StatePropertiesPredicate> state
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<SlideDownBlockTrigger.TriggerInstance> CODEC = RecordCodecBuilder.<SlideDownBlockTrigger.TriggerInstance>create(
                p_357632_ -> p_357632_.group(
                            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SlideDownBlockTrigger.TriggerInstance::player),
                            BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(SlideDownBlockTrigger.TriggerInstance::block),
                            StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(SlideDownBlockTrigger.TriggerInstance::state)
                        )
                        .apply(p_357632_, SlideDownBlockTrigger.TriggerInstance::new)
            )
            .validate(SlideDownBlockTrigger.TriggerInstance::validate);

        private static DataResult<SlideDownBlockTrigger.TriggerInstance> validate(SlideDownBlockTrigger.TriggerInstance p_312534_) {
            return p_312534_.block
                .<DataResult<SlideDownBlockTrigger.TriggerInstance>>flatMap(
                    p_308148_ -> p_312534_.state
                            .<String>flatMap(p_308151_ -> p_308151_.checkState(((Block)p_308148_.value()).getStateDefinition()))
                            .map(p_308154_ -> DataResult.error(() -> "Block" + p_308148_ + " has no property " + p_308154_))
                )
                .orElseGet(() -> DataResult.success(p_312534_));
        }

        public static Criterion<SlideDownBlockTrigger.TriggerInstance> slidesDownBlock(Block p_67007_) {
            return CriteriaTriggers.HONEY_BLOCK_SLIDE
                .createCriterion(new SlideDownBlockTrigger.TriggerInstance(Optional.empty(), Optional.of(p_67007_.builtInRegistryHolder()), Optional.empty()));
        }

        public boolean matches(BlockState p_67009_) {
            return this.block.isPresent() && !p_67009_.is(this.block.get())
                ? false
                : !this.state.isPresent() || this.state.get().matches(p_67009_);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}