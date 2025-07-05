package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record RemoveBinomial(LevelBasedValue chance) implements EnchantmentValueEffect {
    public static final MapCodec<RemoveBinomial> CODEC = RecordCodecBuilder.mapCodec(
        p_345282_ -> p_345282_.group(LevelBasedValue.CODEC.fieldOf("chance").forGetter(RemoveBinomial::chance)).apply(p_345282_, RemoveBinomial::new)
    );

    @Override
    public float process(int p_345007_, RandomSource p_342090_, float p_344829_) {
        float f = this.chance.calculate(p_345007_);
        int i = 0;

        for (int j = 0; (float)j < p_344829_; j++) {
            if (p_342090_.nextFloat() < f) {
                i++;
            }
        }

        return p_344829_ - (float)i;
    }

    @Override
    public MapCodec<RemoveBinomial> codec() {
        return CODEC;
    }
}