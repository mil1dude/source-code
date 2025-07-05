package net.minecraft.core.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class DataComponentPredicate implements Predicate<DataComponentMap> {
    public static final Codec<DataComponentPredicate> CODEC = DataComponentType.VALUE_MAP_CODEC
        .xmap(
            p_336043_ -> new DataComponentPredicate(p_336043_.entrySet().stream().map(TypedDataComponent::fromEntryUnchecked).collect(Collectors.toList())),
            p_335229_ -> p_335229_.expectedComponents
                    .stream()
                    .filter(p_332263_ -> !p_332263_.type().isTransient())
                    .collect(Collectors.toMap(TypedDataComponent::type, TypedDataComponent::value))
        );
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate> STREAM_CODEC = TypedDataComponent.STREAM_CODEC
        .apply(ByteBufCodecs.list())
        .map(DataComponentPredicate::new, p_334923_ -> p_334923_.expectedComponents);
    public static final DataComponentPredicate EMPTY = new DataComponentPredicate(List.of());
    private final List<TypedDataComponent<?>> expectedComponents;

    DataComponentPredicate(List<TypedDataComponent<?>> p_328927_) {
        this.expectedComponents = p_328927_;
    }

    public static DataComponentPredicate.Builder builder() {
        return new DataComponentPredicate.Builder();
    }

    public static DataComponentPredicate allOf(DataComponentMap p_333484_) {
        return new DataComponentPredicate(ImmutableList.copyOf(p_333484_));
    }

    public static DataComponentPredicate someOf(DataComponentMap p_362495_, DataComponentType<?>... p_369898_) {
        DataComponentPredicate.Builder datacomponentpredicate$builder = new DataComponentPredicate.Builder();

        for (DataComponentType<?> datacomponenttype : p_369898_) {
            TypedDataComponent<?> typeddatacomponent = p_362495_.getTyped(datacomponenttype);
            if (typeddatacomponent != null) {
                datacomponentpredicate$builder.expect(typeddatacomponent);
            }
        }

        return datacomponentpredicate$builder.build();
    }

    @Override
    public boolean equals(Object p_331290_) {
        if (p_331290_ instanceof DataComponentPredicate datacomponentpredicate && this.expectedComponents.equals(datacomponentpredicate.expectedComponents)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.expectedComponents.hashCode();
    }

    @Override
    public String toString() {
        return this.expectedComponents.toString();
    }

    public boolean test(DataComponentMap p_329561_) {
        for (TypedDataComponent<?> typeddatacomponent : this.expectedComponents) {
            Object object = p_329561_.get(typeddatacomponent.type());
            if (!Objects.equals(typeddatacomponent.value(), object)) {
                return false;
            }
        }

        return true;
    }

    public boolean test(DataComponentHolder p_332932_) {
        return this.test(p_332932_.getComponents());
    }

    public boolean alwaysMatches() {
        return this.expectedComponents.isEmpty();
    }

    public DataComponentPatch asPatch() {
        DataComponentPatch.Builder datacomponentpatch$builder = DataComponentPatch.builder();

        for (TypedDataComponent<?> typeddatacomponent : this.expectedComponents) {
            datacomponentpatch$builder.set(typeddatacomponent);
        }

        return datacomponentpatch$builder.build();
    }

    public static class Builder {
        private final List<TypedDataComponent<?>> expectedComponents = new ArrayList<>();

        Builder() {
        }

        public <T> DataComponentPredicate.Builder expect(TypedDataComponent<T> p_366182_) {
            return this.expect(p_366182_.type(), p_366182_.value());
        }

        public <T> DataComponentPredicate.Builder expect(DataComponentType<? super T> p_332211_, T p_330030_) {
            for (TypedDataComponent<?> typeddatacomponent : this.expectedComponents) {
                if (typeddatacomponent.type() == p_332211_) {
                    throw new IllegalArgumentException("Predicate already has component of type: '" + p_332211_ + "'");
                }
            }

            this.expectedComponents.add(new TypedDataComponent<>(p_332211_, p_330030_));
            return this;
        }

        public DataComponentPredicate build() {
            return new DataComponentPredicate(List.copyOf(this.expectedComponents));
        }
    }
}