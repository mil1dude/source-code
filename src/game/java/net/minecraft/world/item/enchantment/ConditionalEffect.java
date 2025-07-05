package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record ConditionalEffect<T>(T effect, Optional<LootItemCondition> requirements) {
    public static Codec<LootItemCondition> conditionCodec(ContextKeySet p_361269_) {
        return LootItemCondition.DIRECT_CODEC
            .validate(
                p_359883_ -> {
                    ProblemReporter.Collector problemreporter$collector = new ProblemReporter.Collector();
                    ValidationContext validationcontext = new ValidationContext(problemreporter$collector, p_361269_);
                    p_359883_.validate(validationcontext);
                    return (DataResult)problemreporter$collector.getReport()
                        .map(p_343789_ -> DataResult.error(() -> "Validation error in enchantment effect condition: " + p_343789_))
                        .orElseGet(() -> DataResult.success(p_359883_));
                }
            );
    }

    public static <T> Codec<ConditionalEffect<T>> codec(Codec<T> p_342730_, ContextKeySet p_369317_) {
        return RecordCodecBuilder.create(
            p_359881_ -> p_359881_.group(
                        p_342730_.fieldOf("effect").forGetter(ConditionalEffect::effect),
                        conditionCodec(p_369317_).optionalFieldOf("requirements").forGetter(ConditionalEffect::requirements)
                    )
                    .apply(p_359881_, ConditionalEffect::new)
        );
    }

    public boolean matches(LootContext p_343487_) {
        return this.requirements.isEmpty() ? true : this.requirements.get().test(p_343487_);
    }
}
