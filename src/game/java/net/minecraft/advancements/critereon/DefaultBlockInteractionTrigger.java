package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class DefaultBlockInteractionTrigger extends SimpleCriterionTrigger<DefaultBlockInteractionTrigger.TriggerInstance> {
    @Override
    public Codec<DefaultBlockInteractionTrigger.TriggerInstance> codec() {
        return DefaultBlockInteractionTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_328009_, BlockPos p_332070_) {
        ServerLevel serverlevel = p_328009_.serverLevel();
        BlockState blockstate = serverlevel.getBlockState(p_332070_);
        LootParams lootparams = new LootParams.Builder(serverlevel)
            .withParameter(LootContextParams.ORIGIN, p_332070_.getCenter())
            .withParameter(LootContextParams.THIS_ENTITY, p_328009_)
            .withParameter(LootContextParams.BLOCK_STATE, blockstate)
            .create(LootContextParamSets.BLOCK_USE);
        LootContext lootcontext = new LootContext.Builder(lootparams).create(Optional.empty());
        this.trigger(p_328009_, p_335442_ -> p_335442_.matches(lootcontext));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<DefaultBlockInteractionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_330508_ -> p_330508_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(DefaultBlockInteractionTrigger.TriggerInstance::player),
                        ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(DefaultBlockInteractionTrigger.TriggerInstance::location)
                    )
                    .apply(p_330508_, DefaultBlockInteractionTrigger.TriggerInstance::new)
        );

        public boolean matches(LootContext p_336077_) {
            return this.location.isEmpty() || this.location.get().matches(p_336077_);
        }

        @Override
        public void validate(CriterionValidator p_336311_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_336311_);
            this.location.ifPresent(p_357620_ -> p_336311_.validate(p_357620_, LootContextParamSets.BLOCK_USE, ".location"));
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}