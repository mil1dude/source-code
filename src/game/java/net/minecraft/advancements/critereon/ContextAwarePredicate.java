package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ContextAwarePredicate {
    public static final Codec<ContextAwarePredicate> CODEC = LootItemCondition.DIRECT_CODEC
        .listOf()
        .xmap(ContextAwarePredicate::new, p_309450_ -> p_309450_.conditions);
    private final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositePredicates;

    ContextAwarePredicate(List<LootItemCondition> p_301186_) {
        this.conditions = p_301186_;
        this.compositePredicates = Util.allOf(p_301186_);
    }

    public static ContextAwarePredicate create(LootItemCondition... p_286844_) {
        return new ContextAwarePredicate(List.of(p_286844_));
    }

    public boolean matches(LootContext p_286260_) {
        return this.compositePredicates.test(p_286260_);
    }

    public void validate(ValidationContext p_309801_) {
        for (int i = 0; i < this.conditions.size(); i++) {
            LootItemCondition lootitemcondition = this.conditions.get(i);
            lootitemcondition.validate(p_309801_.forChild("[" + i + "]"));
        }
    }
}