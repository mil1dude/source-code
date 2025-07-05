package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class ItemUsedOnLocationTrigger extends SimpleCriterionTrigger<ItemUsedOnLocationTrigger.TriggerInstance> {
    @Override
    public Codec<ItemUsedOnLocationTrigger.TriggerInstance> codec() {
        return ItemUsedOnLocationTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_286813_, BlockPos p_286625_, ItemStack p_286620_) {
        ServerLevel serverlevel = p_286813_.serverLevel();
        BlockState blockstate = serverlevel.getBlockState(p_286625_);
        LootParams lootparams = new LootParams.Builder(serverlevel)
            .withParameter(LootContextParams.ORIGIN, p_286625_.getCenter())
            .withParameter(LootContextParams.THIS_ENTITY, p_286813_)
            .withParameter(LootContextParams.BLOCK_STATE, blockstate)
            .withParameter(LootContextParams.TOOL, p_286620_)
            .create(LootContextParamSets.ADVANCEMENT_LOCATION);
        LootContext lootcontext = new LootContext.Builder(lootparams).create(Optional.empty());
        this.trigger(p_286813_, p_286596_ -> p_286596_.matches(lootcontext));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ItemUsedOnLocationTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325222_ -> p_325222_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ItemUsedOnLocationTrigger.TriggerInstance::player),
                        ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(ItemUsedOnLocationTrigger.TriggerInstance::location)
                    )
                    .apply(p_325222_, ItemUsedOnLocationTrigger.TriggerInstance::new)
        );

        public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlock(Block p_286530_) {
            ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(p_286530_).build());
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(contextawarepredicate)));
        }

        public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlock(LootItemCondition.Builder... p_286365_) {
            ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(
                Arrays.stream(p_286365_).map(LootItemCondition.Builder::build).toArray(LootItemCondition[]::new)
            );
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(contextawarepredicate)));
        }

        private static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnLocation(LocationPredicate.Builder p_286740_, ItemPredicate.Builder p_286777_) {
            ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(
                LocationCheck.checkLocation(p_286740_).build(), MatchTool.toolMatches(p_286777_).build()
            );
            return new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(contextawarepredicate));
        }

        public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> itemUsedOnBlock(LocationPredicate.Builder p_286808_, ItemPredicate.Builder p_286486_) {
            return CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(itemUsedOnLocation(p_286808_, p_286486_));
        }

        public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> allayDropItemOnBlock(LocationPredicate.Builder p_286325_, ItemPredicate.Builder p_286531_) {
            return CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.createCriterion(itemUsedOnLocation(p_286325_, p_286531_));
        }

        public boolean matches(LootContext p_286800_) {
            return this.location.isEmpty() || this.location.get().matches(p_286800_);
        }

        @Override
        public void validate(CriterionValidator p_310790_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_310790_);
            this.location.ifPresent(p_357626_ -> p_310790_.validate(p_357626_, LootContextParamSets.ADVANCEMENT_LOCATION, ".location"));
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}