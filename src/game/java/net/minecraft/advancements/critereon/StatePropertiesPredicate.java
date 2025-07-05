package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public record StatePropertiesPredicate(List<StatePropertiesPredicate.PropertyMatcher> properties) {
    private static final Codec<List<StatePropertiesPredicate.PropertyMatcher>> PROPERTIES_CODEC = Codec.unboundedMap(
            Codec.STRING, StatePropertiesPredicate.ValueMatcher.CODEC
        )
        .xmap(
            p_296151_ -> p_296151_.entrySet()
                    .stream()
                    .map(p_296150_ -> new StatePropertiesPredicate.PropertyMatcher(p_296150_.getKey(), p_296150_.getValue()))
                    .toList(),
            p_296146_ -> p_296146_.stream()
                    .collect(Collectors.toMap(StatePropertiesPredicate.PropertyMatcher::name, StatePropertiesPredicate.PropertyMatcher::valueMatcher))
        );
    public static final Codec<StatePropertiesPredicate> CODEC = PROPERTIES_CODEC.xmap(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);
    public static final StreamCodec<ByteBuf, StatePropertiesPredicate> STREAM_CODEC = StatePropertiesPredicate.PropertyMatcher.STREAM_CODEC
        .apply(ByteBufCodecs.list())
        .map(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);

    public <S extends StateHolder<?, S>> boolean matches(StateDefinition<?, S> p_67670_, S p_67671_) {
        for (StatePropertiesPredicate.PropertyMatcher statepropertiespredicate$propertymatcher : this.properties) {
            if (!statepropertiespredicate$propertymatcher.match(p_67670_, p_67671_)) {
                return false;
            }
        }

        return true;
    }

    public boolean matches(BlockState p_67668_) {
        return this.matches(p_67668_.getBlock().getStateDefinition(), p_67668_);
    }

    public boolean matches(FluidState p_67685_) {
        return this.matches(p_67685_.getType().getStateDefinition(), p_67685_);
    }

    public Optional<String> checkState(StateDefinition<?, ?> p_299112_) {
        for (StatePropertiesPredicate.PropertyMatcher statepropertiespredicate$propertymatcher : this.properties) {
            Optional<String> optional = statepropertiespredicate$propertymatcher.checkState(p_299112_);
            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    public static class Builder {
        private final ImmutableList.Builder<StatePropertiesPredicate.PropertyMatcher> matchers = ImmutableList.builder();

        private Builder() {
        }

        public static StatePropertiesPredicate.Builder properties() {
            return new StatePropertiesPredicate.Builder();
        }

        public StatePropertiesPredicate.Builder hasProperty(Property<?> p_67701_, String p_67702_) {
            this.matchers.add(new StatePropertiesPredicate.PropertyMatcher(p_67701_.getName(), new StatePropertiesPredicate.ExactMatcher(p_67702_)));
            return this;
        }

        public StatePropertiesPredicate.Builder hasProperty(Property<Integer> p_67695_, int p_67696_) {
            return this.hasProperty(p_67695_, Integer.toString(p_67696_));
        }

        public StatePropertiesPredicate.Builder hasProperty(Property<Boolean> p_67704_, boolean p_67705_) {
            return this.hasProperty(p_67704_, Boolean.toString(p_67705_));
        }

        public <T extends Comparable<T> & StringRepresentable> StatePropertiesPredicate.Builder hasProperty(Property<T> p_67698_, T p_67699_) {
            return this.hasProperty(p_67698_, p_67699_.getSerializedName());
        }

        public Optional<StatePropertiesPredicate> build() {
            return Optional.of(new StatePropertiesPredicate(this.matchers.build()));
        }
    }

    static record ExactMatcher(String value) implements StatePropertiesPredicate.ValueMatcher {
        public static final Codec<StatePropertiesPredicate.ExactMatcher> CODEC = Codec.STRING
            .xmap(StatePropertiesPredicate.ExactMatcher::new, StatePropertiesPredicate.ExactMatcher::value);
        public static final StreamCodec<ByteBuf, StatePropertiesPredicate.ExactMatcher> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
            .map(StatePropertiesPredicate.ExactMatcher::new, StatePropertiesPredicate.ExactMatcher::value);

        @Override
        public <T extends Comparable<T>> boolean match(StateHolder<?, ?> p_301115_, Property<T> p_299705_) {
            T t = p_301115_.getValue(p_299705_);
            Optional<T> optional = p_299705_.getValue(this.value);
            return optional.isPresent() && t.compareTo(optional.get()) == 0;
        }
    }

    static record PropertyMatcher(String name, StatePropertiesPredicate.ValueMatcher valueMatcher) {
        public static final StreamCodec<ByteBuf, StatePropertiesPredicate.PropertyMatcher> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            StatePropertiesPredicate.PropertyMatcher::name,
            StatePropertiesPredicate.ValueMatcher.STREAM_CODEC,
            StatePropertiesPredicate.PropertyMatcher::valueMatcher,
            StatePropertiesPredicate.PropertyMatcher::new
        );

        public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> p_67719_, S p_67720_) {
            Property<?> property = p_67719_.getProperty(this.name);
            return property != null && this.valueMatcher.match(p_67720_, property);
        }

        public Optional<String> checkState(StateDefinition<?, ?> p_67722_) {
            Property<?> property = p_67722_.getProperty(this.name);
            return property != null ? Optional.empty() : Optional.of(this.name);
        }
    }

    static record RangedMatcher(Optional<String> minValue, Optional<String> maxValue) implements StatePropertiesPredicate.ValueMatcher {
        public static final Codec<StatePropertiesPredicate.RangedMatcher> CODEC = RecordCodecBuilder.create(
            p_325251_ -> p_325251_.group(
                        Codec.STRING.optionalFieldOf("min").forGetter(StatePropertiesPredicate.RangedMatcher::minValue),
                        Codec.STRING.optionalFieldOf("max").forGetter(StatePropertiesPredicate.RangedMatcher::maxValue)
                    )
                    .apply(p_325251_, StatePropertiesPredicate.RangedMatcher::new)
        );
        public static final StreamCodec<ByteBuf, StatePropertiesPredicate.RangedMatcher> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            StatePropertiesPredicate.RangedMatcher::minValue,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            StatePropertiesPredicate.RangedMatcher::maxValue,
            StatePropertiesPredicate.RangedMatcher::new
        );

        @Override
        public <T extends Comparable<T>> boolean match(StateHolder<?, ?> p_299120_, Property<T> p_300783_) {
            T t = p_299120_.getValue(p_300783_);
            if (this.minValue.isPresent()) {
                Optional<T> optional = p_300783_.getValue(this.minValue.get());
                if (optional.isEmpty() || t.compareTo(optional.get()) < 0) {
                    return false;
                }
            }

            if (this.maxValue.isPresent()) {
                Optional<T> optional1 = p_300783_.getValue(this.maxValue.get());
                if (optional1.isEmpty() || t.compareTo(optional1.get()) > 0) {
                    return false;
                }
            }

            return true;
        }
    }

    interface ValueMatcher {
        Codec<StatePropertiesPredicate.ValueMatcher> CODEC = Codec.either(
                StatePropertiesPredicate.ExactMatcher.CODEC, StatePropertiesPredicate.RangedMatcher.CODEC
            )
            .xmap(Either::unwrap, p_297370_ -> {
                if (p_297370_ instanceof StatePropertiesPredicate.ExactMatcher statepropertiespredicate$exactmatcher) {
                    return Either.left(statepropertiespredicate$exactmatcher);
                } else if (p_297370_ instanceof StatePropertiesPredicate.RangedMatcher statepropertiespredicate$rangedmatcher) {
                    return Either.right(statepropertiespredicate$rangedmatcher);
                } else {
                    throw new UnsupportedOperationException();
                }
            });
        StreamCodec<ByteBuf, StatePropertiesPredicate.ValueMatcher> STREAM_CODEC = ByteBufCodecs.either(
                StatePropertiesPredicate.ExactMatcher.STREAM_CODEC, StatePropertiesPredicate.RangedMatcher.STREAM_CODEC
            )
            .map(Either::unwrap, p_325252_ -> {
                if (p_325252_ instanceof StatePropertiesPredicate.ExactMatcher statepropertiespredicate$exactmatcher) {
                    return Either.left(statepropertiespredicate$exactmatcher);
                } else if (p_325252_ instanceof StatePropertiesPredicate.RangedMatcher statepropertiespredicate$rangedmatcher) {
                    return Either.right(statepropertiespredicate$rangedmatcher);
                } else {
                    throw new UnsupportedOperationException();
                }
            });

        <T extends Comparable<T>> boolean match(StateHolder<?, ?> p_301268_, Property<T> p_300938_);
    }
}