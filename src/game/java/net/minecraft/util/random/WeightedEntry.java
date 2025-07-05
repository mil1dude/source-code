package net.minecraft.util.random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface WeightedEntry {
    Weight getWeight();

    static <T> WeightedEntry.Wrapper<T> wrap(T p_146291_, int p_146292_) {
        return new WeightedEntry.Wrapper<>(p_146291_, Weight.of(p_146292_));
    }

    public static class IntrusiveBase implements WeightedEntry {
        private final Weight weight;

        public IntrusiveBase(int p_146295_) {
            this.weight = Weight.of(p_146295_);
        }

        public IntrusiveBase(Weight p_146297_) {
            this.weight = p_146297_;
        }

        @Override
        public Weight getWeight() {
            return this.weight;
        }
    }

    public static record Wrapper<T>(T data, Weight weight) implements WeightedEntry {
        @Override
        public Weight getWeight() {
            return this.weight;
        }

        public static <E> Codec<WeightedEntry.Wrapper<E>> codec(Codec<E> p_146306_) {
            return RecordCodecBuilder.create(
                p_146309_ -> p_146309_.group(
                            p_146306_.fieldOf("data").forGetter((Function<WeightedEntry.Wrapper<E>, E>)(WeightedEntry.Wrapper::data)),
                            Weight.CODEC.fieldOf("weight").forGetter(WeightedEntry.Wrapper::weight)
                        )
                        .apply(p_146309_, (BiFunction<E, Weight, WeightedEntry.Wrapper<E>>)(WeightedEntry.Wrapper::new))
            );
        }
    }
}