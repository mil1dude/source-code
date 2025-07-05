package net.minecraft.advancements.critereon;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class CriterionValidator {
    private final ProblemReporter reporter;
    private final HolderGetter.Provider lootData;

    public CriterionValidator(ProblemReporter p_311865_, HolderGetter.Provider p_329172_) {
        this.reporter = p_311865_;
        this.lootData = p_329172_;
    }

    public void validateEntity(Optional<ContextAwarePredicate> p_311203_, String p_309703_) {
        p_311203_.ifPresent(p_312443_ -> this.validateEntity(p_312443_, p_309703_));
    }

    public void validateEntities(List<ContextAwarePredicate> p_310532_, String p_310219_) {
        this.validate(p_310532_, LootContextParamSets.ADVANCEMENT_ENTITY, p_310219_);
    }

    public void validateEntity(ContextAwarePredicate p_310373_, String p_309633_) {
        this.validate(p_310373_, LootContextParamSets.ADVANCEMENT_ENTITY, p_309633_);
    }

    public void validate(ContextAwarePredicate p_362235_, ContextKeySet p_368757_, String p_309737_) {
        p_362235_.validate(new ValidationContext(this.reporter.forChild(p_309737_), p_368757_, this.lootData));
    }

    public void validate(List<ContextAwarePredicate> p_363793_, ContextKeySet p_367816_, String p_312977_) {
        for (int i = 0; i < p_363793_.size(); i++) {
            ContextAwarePredicate contextawarepredicate = p_363793_.get(i);
            contextawarepredicate.validate(new ValidationContext(this.reporter.forChild(p_312977_ + "[" + i + "]"), p_367816_, this.lootData));
        }
    }
}