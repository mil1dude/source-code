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

public class EnterBlockTrigger extends SimpleCriterionTrigger<EnterBlockTrigger.TriggerInstance> {
    @Override
    public Codec<EnterBlockTrigger.TriggerInstance> codec() {
        return EnterBlockTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_31270_, BlockState p_31271_) {
        this.trigger(p_31270_, p_31277_ -> p_31277_.matches(p_31271_));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<StatePropertiesPredicate> state
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<EnterBlockTrigger.TriggerInstance> CODEC = RecordCodecBuilder.<EnterBlockTrigger.TriggerInstance>create(
                p_357621_ -> p_357621_.group(
                            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(EnterBlockTrigger.TriggerInstance::player),
                            BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(EnterBlockTrigger.TriggerInstance::block),
                            StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(EnterBlockTrigger.TriggerInstance::state)
                        )
                        .apply(p_357621_, EnterBlockTrigger.TriggerInstance::new)
            )
            .validate(EnterBlockTrigger.TriggerInstance::validate);

        private static DataResult<EnterBlockTrigger.TriggerInstance> validate(EnterBlockTrigger.TriggerInstance p_312153_) {
            return p_312153_.block
                .<DataResult<EnterBlockTrigger.TriggerInstance>>flatMap(
                    p_308123_ -> p_312153_.state
                            .<String>flatMap(p_308130_ -> p_308130_.checkState(((Block)p_308123_.value()).getStateDefinition()))
                            .map(p_308125_ -> DataResult.error(() -> "Block" + p_308123_ + " has no property " + p_308125_))
                )
                .orElseGet(() -> DataResult.success(p_312153_));
        }

        public static Criterion<EnterBlockTrigger.TriggerInstance> entersBlock(Block p_31298_) {
            return CriteriaTriggers.ENTER_BLOCK
                .createCriterion(new EnterBlockTrigger.TriggerInstance(Optional.empty(), Optional.of(p_31298_.builtInRegistryHolder()), Optional.empty()));
        }

        public boolean matches(BlockState p_31300_) {
            return this.block.isPresent() && !p_31300_.is(this.block.get())
                ? false
                : !this.state.isPresent() || this.state.get().matches(p_31300_);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}