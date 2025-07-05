package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class Selector {
    private static final Selector EMPTY = new Selector(ImmutableList.of());
    private static final Comparator<Property.Value<?>> COMPARE_BY_NAME = Comparator.comparing(p_376774_ -> p_376774_.property().getName());
    private final List<Property.Value<?>> values;

    public Selector extend(Property.Value<?> p_376266_) {
        return new Selector(ImmutableList.<Property.Value<?>>builder().addAll(this.values).add(p_376266_).build());
    }

    public Selector extend(Selector p_376155_) {
        return new Selector(ImmutableList.<Property.Value<?>>builder().addAll(this.values).addAll(p_376155_.values).build());
    }

    private Selector(List<Property.Value<?>> p_378787_) {
        this.values = p_378787_;
    }

    public static Selector empty() {
        return EMPTY;
    }

    public static Selector of(Property.Value<?>... p_378231_) {
        return new Selector(ImmutableList.copyOf(p_378231_));
    }

    @Override
    public boolean equals(Object p_377697_) {
        return this == p_377697_ || p_377697_ instanceof Selector && this.values.equals(((Selector)p_377697_).values);
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    public String getKey() {
        return this.values.stream().sorted(COMPARE_BY_NAME).map(Property.Value::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}