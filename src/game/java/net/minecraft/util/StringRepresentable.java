package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;

public interface StringRepresentable {
    int PRE_BUILT_MAP_THRESHOLD = 16;

    String getSerializedName();

    static <E extends Enum<E> & StringRepresentable> StringRepresentable.EnumCodec<E> fromEnum(Supplier<E[]> p_216440_) {
        return fromEnumWithMapping(p_216440_, p_312201_ -> p_312201_);
    }

    static <E extends Enum<E> & StringRepresentable> StringRepresentable.EnumCodec<E> fromEnumWithMapping(Supplier<E[]> p_275615_, Function<String, String> p_275259_) {
        E[] ae = (E[])p_275615_.get();
        Function<String, E> function = createNameLookup(ae, p_275259_);
        return new StringRepresentable.EnumCodec<>(ae, function);
    }

    static <T extends StringRepresentable> Codec<T> fromValues(Supplier<T[]> p_311788_) {
        T[] at = (T[])p_311788_.get();
        Function<String, T> function = createNameLookup(at, p_308975_ -> p_308975_);
        ToIntFunction<T> tointfunction = Util.createIndexLookup(Arrays.asList(at));
        return new StringRepresentable.StringRepresentableCodec<>(at, function, tointfunction);
    }

    static <T extends StringRepresentable> Function<String, T> createNameLookup(T[] p_312243_, Function<String, String> p_313109_) {
        if (p_312243_.length > 16) {
            Map<String, T> map = Arrays.<StringRepresentable>stream(p_312243_)
                .collect(Collectors.toMap(p_308977_ -> p_313109_.apply(p_308977_.getSerializedName()), p_311743_ -> (T)p_311743_));
            return p_308974_ -> p_308974_ == null ? null : map.get(p_308974_);
        } else {
            return p_308972_ -> {
                for (T t : p_312243_) {
                    if (p_313109_.apply(t.getSerializedName()).equals(p_308972_)) {
                        return t;
                    }
                }

                return null;
            };
        }
    }

    static Keyable keys(final StringRepresentable[] p_14358_) {
        return new Keyable() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> p_184758_) {
                return Arrays.stream(p_14358_).map(StringRepresentable::getSerializedName).map(p_184758_::createString);
            }
        };
    }

    @Deprecated
    public static class EnumCodec<E extends Enum<E> & StringRepresentable> extends StringRepresentable.StringRepresentableCodec<E> {
        private final Function<String, E> resolver;

        public EnumCodec(E[] p_216447_, Function<String, E> p_216448_) {
            super(p_216447_, p_216448_, p_216454_ -> p_216454_.ordinal());
            this.resolver = p_216448_;
        }

        @Nullable
        public E byName(@Nullable String p_216456_) {
            return this.resolver.apply(p_216456_);
        }

        public E byName(@Nullable String p_263077_, E p_263115_) {
            return Objects.requireNonNullElse(this.byName(p_263077_), p_263115_);
        }

        public E byName(@Nullable String p_367164_, Supplier<? extends E> p_363447_) {
            return Objects.requireNonNullElseGet(this.byName(p_367164_), p_363447_);
        }
    }

    public static class StringRepresentableCodec<S extends StringRepresentable> implements Codec<S> {
        private final Codec<S> codec;

        public StringRepresentableCodec(S[] p_309730_, Function<String, S> p_311107_, ToIntFunction<S> p_312549_) {
            this.codec = ExtraCodecs.orCompressed(
                Codec.stringResolver(StringRepresentable::getSerializedName, p_311107_),
                ExtraCodecs.idResolverCodec(p_312549_, p_312747_ -> p_312747_ >= 0 && p_312747_ < p_309730_.length ? p_309730_[p_312747_] : null, -1)
            );
        }

        @Override
        public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> p_310491_, T p_312317_) {
            return this.codec.decode(p_310491_, p_312317_);
        }

        public <T> DataResult<T> encode(S p_312413_, DynamicOps<T> p_310685_, T p_312430_) {
            return this.codec.encode(p_312413_, p_310685_, p_312430_);
        }
    }
}