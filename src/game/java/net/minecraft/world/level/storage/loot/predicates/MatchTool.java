package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record MatchTool(Optional<ItemPredicate> predicate) implements LootItemCondition {
    public static final MapCodec<MatchTool> CODEC = RecordCodecBuilder.mapCodec(
        p_327654_ -> p_327654_.group(ItemPredicate.CODEC.optionalFieldOf("predicate").forGetter(MatchTool::predicate)).apply(p_327654_, MatchTool::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.MATCH_TOOL;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.TOOL);
    }

    public boolean test(LootContext p_82000_) {
        ItemStack itemstack = p_82000_.getOptionalParameter(LootContextParams.TOOL);
        return itemstack != null && (this.predicate.isEmpty() || this.predicate.get().test(itemstack));
    }

    public static LootItemCondition.Builder toolMatches(ItemPredicate.Builder p_81998_) {
        return () -> new MatchTool(Optional.of(p_81998_.build()));
    }
}