package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class AnyBlockInteractionTrigger extends SimpleCriterionTrigger<AnyBlockInteractionTrigger.TriggerInstance> {
    @Override
    public Codec<AnyBlockInteractionTrigger.TriggerInstance> codec() {
        return AnyBlockInteractionTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_329152_, BlockPos p_334977_, ItemStack p_334131_) {
        ServerLevel serverlevel = p_329152_.serverLevel();
        BlockState blockstate = serverlevel.getBlockState(p_334977_);
        LootParams lootparams = new LootParams.Builder(serverlevel)
            .withParameter(LootContextParams.ORIGIN, p_334977_.getCenter())
            .withParameter(LootContextParams.THIS_ENTITY, p_329152_)
            .withParameter(LootContextParams.BLOCK_STATE, blockstate)
            .withParameter(LootContextParams.TOOL, p_334131_)
            .create(LootContextParamSets.ADVANCEMENT_LOCATION);
        LootContext lootcontext = new LootContext.Builder(lootparams).create(Optional.empty());
        this.trigger(p_329152_, p_330225_ -> p_330225_.matches(lootcontext));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<AnyBlockInteractionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_335009_ -> p_335009_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(AnyBlockInteractionTrigger.TriggerInstance::player),
                        ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(AnyBlockInteractionTrigger.TriggerInstance::location)
                    )
                    .apply(p_335009_, AnyBlockInteractionTrigger.TriggerInstance::new)
        );

        public boolean matches(LootContext p_333498_) {
            return this.location.isEmpty() || this.location.get().matches(p_333498_);
        }

        @Override
        public void validate(CriterionValidator p_328875_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_328875_);
            this.location.ifPresent(p_357617_ -> p_328875_.validate(p_357617_, LootContextParamSets.ADVANCEMENT_LOCATION, ".location"));
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}