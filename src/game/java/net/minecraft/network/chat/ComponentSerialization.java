package net.minecraft.network.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;

public class ComponentSerialization {
    public static final Codec<Component> CODEC = Codec.recursive("Component", ComponentSerialization::createCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, Component> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Component>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs::optional);
    public static final StreamCodec<RegistryFriendlyByteBuf, Component> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Component>> TRUSTED_OPTIONAL_STREAM_CODEC = TRUSTED_STREAM_CODEC.apply(ByteBufCodecs::optional);
    public static final StreamCodec<ByteBuf, Component> TRUSTED_CONTEXT_FREE_STREAM_CODEC = ByteBufCodecs.fromCodecTrusted(CODEC);
    public static final Codec<Component> FLAT_CODEC = flatCodec(Integer.MAX_VALUE);

    public static Codec<Component> flatCodec(int p_330224_) {
        final Codec<String> codec = Codec.string(0, p_330224_);
        return new Codec<Component>() {
            @Override
            public <T> DataResult<Pair<Component, T>> decode(DynamicOps<T> p_334494_, T p_334478_) {
                DynamicOps<JsonElement> dynamicops = asJsonOps(p_334494_);
                return codec.decode(p_334494_, p_334478_).flatMap(p_328894_ -> {
                    try {
                        JsonElement jsonelement = JsonParser.parseString(p_328894_.getFirst());
                        return ComponentSerialization.CODEC.parse(dynamicops, jsonelement).map(p_334008_ -> Pair.of(p_334008_, (T)p_328894_.getSecond()));
                    } catch (JsonParseException jsonparseexception) {
                        return DataResult.error(jsonparseexception::getMessage);
                    }
                });
            }

            public <T> DataResult<T> encode(Component p_330654_, DynamicOps<T> p_330879_, T p_336296_) {
                DynamicOps<JsonElement> dynamicops = asJsonOps(p_330879_);
                return ComponentSerialization.CODEC.encodeStart(dynamicops, p_330654_).flatMap(p_332275_ -> {
                    try {
                        return codec.encodeStart(p_330879_, GsonHelper.toStableString(p_332275_));
                    } catch (IllegalArgumentException illegalargumentexception) {
                        return DataResult.error(illegalargumentexception::getMessage);
                    }
                });
            }

            private static <T> DynamicOps<JsonElement> asJsonOps(DynamicOps<T> p_331374_) {
                return (DynamicOps<JsonElement>)(p_331374_ instanceof RegistryOps<T> registryops ? registryops.withParent(JsonOps.INSTANCE) : JsonOps.INSTANCE);
            }
        };
    }

    private static MutableComponent createFromList(List<Component> p_312708_) {
        MutableComponent mutablecomponent = p_312708_.get(0).copy();

        for (int i = 1; i < p_312708_.size(); i++) {
            mutablecomponent.append(p_312708_.get(i));
        }

        return mutablecomponent;
    }

    public static <T extends StringRepresentable, E> MapCodec<E> createLegacyComponentMatcher(
        T[] p_312620_, Function<T, MapCodec<? extends E>> p_312447_, Function<E, T> p_309774_, String p_311665_
    ) {
        MapCodec<E> mapcodec = new ComponentSerialization.FuzzyCodec<>(
            Stream.<T>of(p_312620_).map(p_312447_).toList(), p_312251_ -> p_312447_.apply(p_309774_.apply(p_312251_))
        );
        Codec<T> codec = StringRepresentable.fromValues((Supplier<T[]>)(() -> p_312620_));
        MapCodec<E> mapcodec1 = codec.dispatchMap(p_311665_, p_309774_, p_312447_);
        MapCodec<E> mapcodec2 = new ComponentSerialization.StrictEither<>(p_311665_, mapcodec1, mapcodec);
        return ExtraCodecs.orCompressed(mapcodec2, mapcodec1);
    }

    private static Codec<Component> createCodec(Codec<Component> p_310353_) {
        ComponentContents.Type<?>[] type = new ComponentContents.Type[]{
            PlainTextContents.TYPE,
            TranslatableContents.TYPE,
            KeybindContents.TYPE,
            ScoreContents.TYPE,
            SelectorContents.TYPE,
            NbtContents.TYPE
        };
        MapCodec<ComponentContents> mapcodec = createLegacyComponentMatcher(type, ComponentContents.Type::codec, ComponentContents::type, "type");
        Codec<Component> codec = RecordCodecBuilder.create(
            p_326064_ -> p_326064_.group(
                        mapcodec.forGetter(Component::getContents),
                        ExtraCodecs.nonEmptyList(p_310353_.listOf()).optionalFieldOf("extra", List.of()).forGetter(Component::getSiblings),
                        Style.Serializer.MAP_CODEC.forGetter(Component::getStyle)
                    )
                    .apply(p_326064_, MutableComponent::new)
        );
        return Codec.either(Codec.either(Codec.STRING, ExtraCodecs.nonEmptyList(p_310353_.listOf())), codec)
            .xmap(
                p_312362_ -> p_312362_.map(
                        p_310114_ -> p_310114_.map(Component::literal, ComponentSerialization::createFromList), p_310523_ -> (Component)p_310523_
                    ),
                p_312558_ -> {
                    String s = p_312558_.tryCollapseToString();
                    return s != null ? Either.left(Either.left(s)) : Either.right(p_312558_);
                }
            );
    }

    static class FuzzyCodec<T> extends MapCodec<T> {
        private final List<MapCodec<? extends T>> codecs;
        private final Function<T, MapEncoder<? extends T>> encoderGetter;

        public FuzzyCodec(List<MapCodec<? extends T>> p_313195_, Function<T, MapEncoder<? extends T>> p_313105_) {
            this.codecs = p_313195_;
            this.encoderGetter = p_313105_;
        }

        @Override
        public <S> DataResult<T> decode(DynamicOps<S> p_311662_, MapLike<S> p_310979_) {
            for (MapDecoder<? extends T> mapdecoder : this.codecs) {
                DataResult<? extends T> dataresult = mapdecoder.decode(p_311662_, p_310979_);
                if (dataresult.result().isPresent()) {
                    return (DataResult<T>)dataresult;
                }
            }

            return DataResult.error(() -> "No matching codec found");
        }

        @Override
        public <S> RecordBuilder<S> encode(T p_310202_, DynamicOps<S> p_312954_, RecordBuilder<S> p_312771_) {
            MapEncoder<T> mapencoder = (MapEncoder<T>)this.encoderGetter.apply(p_310202_);
            return mapencoder.encode(p_310202_, p_312954_, p_312771_);
        }

        @Override
        public <S> Stream<S> keys(DynamicOps<S> p_311118_) {
            return this.codecs.stream().flatMap(p_310919_ -> p_310919_.keys(p_311118_)).distinct();
        }

        @Override
        public String toString() {
            return "FuzzyCodec[" + this.codecs + "]";
        }
    }

    static class StrictEither<T> extends MapCodec<T> {
        private final String typeFieldName;
        private final MapCodec<T> typed;
        private final MapCodec<T> fuzzy;

        public StrictEither(String p_310206_, MapCodec<T> p_312028_, MapCodec<T> p_312603_) {
            this.typeFieldName = p_310206_;
            this.typed = p_312028_;
            this.fuzzy = p_312603_;
        }

        @Override
        public <O> DataResult<T> decode(DynamicOps<O> p_310941_, MapLike<O> p_311041_) {
            return p_311041_.get(this.typeFieldName) != null ? this.typed.decode(p_310941_, p_311041_) : this.fuzzy.decode(p_310941_, p_311041_);
        }

        @Override
        public <O> RecordBuilder<O> encode(T p_310960_, DynamicOps<O> p_310726_, RecordBuilder<O> p_310170_) {
            return this.fuzzy.encode(p_310960_, p_310726_, p_310170_);
        }

        @Override
        public <T1> Stream<T1> keys(DynamicOps<T1> p_310134_) {
            return Stream.concat(this.typed.keys(p_310134_), this.fuzzy.keys(p_310134_)).distinct();
        }
    }
}