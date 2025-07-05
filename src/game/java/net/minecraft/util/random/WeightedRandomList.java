package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;

public class WeightedRandomList<E extends WeightedEntry> {
    private final int totalWeight;
    private final ImmutableList<E> items;

    WeightedRandomList(List<? extends E> p_146327_) {
        this.items = ImmutableList.copyOf(p_146327_);
        this.totalWeight = WeightedRandom.getTotalWeight(p_146327_);
    }

    public static <E extends WeightedEntry> WeightedRandomList<E> create() {
        return new WeightedRandomList<>(ImmutableList.of());
    }

    @SafeVarargs
    public static <E extends WeightedEntry> WeightedRandomList<E> create(E... p_146331_) {
        return new WeightedRandomList<>(ImmutableList.copyOf(p_146331_));
    }

    public static <E extends WeightedEntry> WeightedRandomList<E> create(List<E> p_146329_) {
        return new WeightedRandomList<>(p_146329_);
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public Optional<E> getRandom(RandomSource p_216830_) {
        if (this.totalWeight == 0) {
            return Optional.empty();
        } else {
            int i = p_216830_.nextInt(this.totalWeight);
            return WeightedRandom.getWeightedItem(this.items, i);
        }
    }

    public List<E> unwrap() {
        return this.items;
    }

    public static <E extends WeightedEntry> Codec<WeightedRandomList<E>> codec(Codec<E> p_146334_) {
        return p_146334_.listOf().xmap(WeightedRandomList::create, WeightedRandomList::unwrap);
    }

    @Override
    public boolean equals(@Nullable Object p_334541_) {
        if (this == p_334541_) {
            return true;
        } else if (p_334541_ != null && this.getClass() == p_334541_.getClass()) {
            WeightedRandomList<?> weightedrandomlist = (WeightedRandomList<?>)p_334541_;
            return this.totalWeight == weightedrandomlist.totalWeight && Objects.equals(this.items, weightedrandomlist.items);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.totalWeight, this.items);
    }
}