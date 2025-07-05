package net.minecraft.network.codec;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import io.netty.buffer.ByteBuf;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface StreamCodec<B, V> extends StreamDecoder<B, V>, StreamEncoder<B, V> {
    static <B, V> StreamCodec<B, V> of(final StreamEncoder<B, V> p_328457_, final StreamDecoder<B, V> p_332601_) {
        return new StreamCodec<B, V>() {
            @Override
            public V decode(B p_335513_) {
                return p_332601_.decode(p_335513_);
            }

            @Override
            public void encode(B p_333998_, V p_335122_) {
                p_328457_.encode(p_333998_, p_335122_);
            }
        };
    }

    static <B, V> StreamCodec<B, V> ofMember(final StreamMemberEncoder<B, V> p_330640_, final StreamDecoder<B, V> p_327818_) {
        return new StreamCodec<B, V>() {
            @Override
            public V decode(B p_331033_) {
                return p_327818_.decode(p_331033_);
            }

            @Override
            public void encode(B p_329484_, V p_332289_) {
                p_330640_.encode(p_332289_, p_329484_);
            }
        };
    }

    static <B, V> StreamCodec<B, V> unit(final V p_336240_) {
        return new StreamCodec<B, V>() {
            @Override
            public V decode(B p_328164_) {
                return p_336240_;
            }

            @Override
            public void encode(B p_336022_, V p_333291_) {
                if (!p_333291_.equals(p_336240_)) {
                    throw new IllegalStateException("Can't encode '" + p_333291_ + "', expected '" + p_336240_ + "'");
                }
            }
        };
    }

    default <O> StreamCodec<B, O> apply(StreamCodec.CodecOperation<B, V, O> p_335614_) {
        return p_335614_.apply(this);
    }

    default <O> StreamCodec<B, O> map(final Function<? super V, ? extends O> p_327720_, final Function<? super O, ? extends V> p_330478_) {
        return new StreamCodec<B, O>() {
            @Override
            public O decode(B p_328614_) {
                return (O)p_327720_.apply(StreamCodec.this.decode(p_328614_));
            }

            @Override
            public void encode(B p_336327_, O p_331146_) {
                StreamCodec.this.encode(p_336327_, (V)p_330478_.apply(p_331146_));
            }
        };
    }

    default <O extends ByteBuf> StreamCodec<O, V> mapStream(final Function<O, ? extends B> p_332075_) {
        return new StreamCodec<O, V>() {
            public V decode(O p_331759_) {
                B b = (B)p_332075_.apply(p_331759_);
                return StreamCodec.this.decode(b);
            }

            public void encode(O p_334335_, V p_336271_) {
                B b = (B)p_332075_.apply(p_334335_);
                StreamCodec.this.encode(b, p_336271_);
            }
        };
    }

    default <U> StreamCodec<B, U> dispatch(
        final Function<? super U, ? extends V> p_333836_, final Function<? super V, ? extends StreamCodec<? super B, ? extends U>> p_335469_
    ) {
        return new StreamCodec<B, U>() {
            @Override
            public U decode(B p_333769_) {
                V v = StreamCodec.this.decode(p_333769_);
                StreamCodec<? super B, ? extends U> streamcodec = (StreamCodec<? super B, ? extends U>)p_335469_.apply(v);
                return (U)streamcodec.decode(p_333769_);
            }

            @Override
            public void encode(B p_331493_, U p_333683_) {
                V v = (V)p_333836_.apply(p_333683_);
                StreamCodec<B, U> streamcodec = (StreamCodec<B, U>)p_335469_.apply(v);
                StreamCodec.this.encode(p_331493_, v);
                streamcodec.encode(p_331493_, p_333683_);
            }
        };
    }

    static <B, C, T1> StreamCodec<B, C> composite(final StreamCodec<? super B, T1> p_332516_, final Function<C, T1> p_335276_, final Function<T1, C> p_330982_) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_331843_) {
                T1 t1 = p_332516_.decode(p_331843_);
                return p_330982_.apply(t1);
            }

            @Override
            public void encode(B p_330937_, C p_333579_) {
                p_332516_.encode(p_330937_, p_335276_.apply(p_333579_));
            }
        };
    }

    static <B, C, T1, T2> StreamCodec<B, C> composite(
        final StreamCodec<? super B, T1> p_329724_,
        final Function<C, T1> p_329438_,
        final StreamCodec<? super B, T2> p_328233_,
        final Function<C, T2> p_328617_,
        final BiFunction<T1, T2, C> p_334409_
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_331897_) {
                T1 t1 = p_329724_.decode(p_331897_);
                T2 t2 = p_328233_.decode(p_331897_);
                return p_334409_.apply(t1, t2);
            }

            @Override
            public void encode(B p_334266_, C p_331042_) {
                p_329724_.encode(p_334266_, p_329438_.apply(p_331042_));
                p_328233_.encode(p_334266_, p_328617_.apply(p_331042_));
            }
        };
    }

    static <B, C, T1, T2, T3> StreamCodec<B, C> composite(
        final StreamCodec<? super B, T1> p_329473_,
        final Function<C, T1> p_334404_,
        final StreamCodec<? super B, T2> p_327967_,
        final Function<C, T2> p_330724_,
        final StreamCodec<? super B, T3> p_328162_,
        final Function<C, T3> p_333383_,
        final Function3<T1, T2, T3, C> p_334421_
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_331065_) {
                T1 t1 = p_329473_.decode(p_331065_);
                T2 t2 = p_327967_.decode(p_331065_);
                T3 t3 = p_328162_.decode(p_331065_);
                return p_334421_.apply(t1, t2, t3);
            }

            @Override
            public void encode(B p_333137_, C p_328354_) {
                p_329473_.encode(p_333137_, p_334404_.apply(p_328354_));
                p_327967_.encode(p_333137_, p_330724_.apply(p_328354_));
                p_328162_.encode(p_333137_, p_333383_.apply(p_328354_));
            }
        };
    }

    static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(
        final StreamCodec<? super B, T1> p_331397_,
        final Function<C, T1> p_331210_,
        final StreamCodec<? super B, T2> p_332449_,
        final Function<C, T2> p_329970_,
        final StreamCodec<? super B, T3> p_328015_,
        final Function<C, T3> p_333423_,
        final StreamCodec<? super B, T4> p_332358_,
        final Function<C, T4> p_331597_,
        final Function4<T1, T2, T3, T4, C> p_332476_
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_334517_) {
                T1 t1 = p_331397_.decode(p_334517_);
                T2 t2 = p_332449_.decode(p_334517_);
                T3 t3 = p_328015_.decode(p_334517_);
                T4 t4 = p_332358_.decode(p_334517_);
                return p_332476_.apply(t1, t2, t3, t4);
            }

            @Override
            public void encode(B p_336185_, C p_330170_) {
                p_331397_.encode(p_336185_, p_331210_.apply(p_330170_));
                p_332449_.encode(p_336185_, p_329970_.apply(p_330170_));
                p_328015_.encode(p_336185_, p_333423_.apply(p_330170_));
                p_332358_.encode(p_336185_, p_331597_.apply(p_330170_));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(
        final StreamCodec<? super B, T1> p_332680_,
        final Function<C, T1> p_336312_,
        final StreamCodec<? super B, T2> p_328131_,
        final Function<C, T2> p_332283_,
        final StreamCodec<? super B, T3> p_330440_,
        final Function<C, T3> p_333147_,
        final StreamCodec<? super B, T4> p_329904_,
        final Function<C, T4> p_330832_,
        final StreamCodec<? super B, T5> p_335857_,
        final Function<C, T5> p_333237_,
        final Function5<T1, T2, T3, T4, T5, C> p_328623_
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_328956_) {
                T1 t1 = p_332680_.decode(p_328956_);
                T2 t2 = p_328131_.decode(p_328956_);
                T3 t3 = p_330440_.decode(p_328956_);
                T4 t4 = p_329904_.decode(p_328956_);
                T5 t5 = p_335857_.decode(p_328956_);
                return p_328623_.apply(t1, t2, t3, t4, t5);
            }

            @Override
            public void encode(B p_328899_, C p_328944_) {
                p_332680_.encode(p_328899_, p_336312_.apply(p_328944_));
                p_328131_.encode(p_328899_, p_332283_.apply(p_328944_));
                p_330440_.encode(p_328899_, p_333147_.apply(p_328944_));
                p_329904_.encode(p_328899_, p_330832_.apply(p_328944_));
                p_335857_.encode(p_328899_, p_333237_.apply(p_328944_));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(
        final StreamCodec<? super B, T1> p_333401_,
        final Function<C, T1> p_329450_,
        final StreamCodec<? super B, T2> p_330884_,
        final Function<C, T2> p_328085_,
        final StreamCodec<? super B, T3> p_332808_,
        final Function<C, T3> p_327867_,
        final StreamCodec<? super B, T4> p_335472_,
        final Function<C, T4> p_328511_,
        final StreamCodec<? super B, T5> p_333318_,
        final Function<C, T5> p_330123_,
        final StreamCodec<? super B, T6> p_332458_,
        final Function<C, T6> p_328086_,
        final Function6<T1, T2, T3, T4, T5, T6, C> p_329947_
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_330564_) {
                T1 t1 = p_333401_.decode(p_330564_);
                T2 t2 = p_330884_.decode(p_330564_);
                T3 t3 = p_332808_.decode(p_330564_);
                T4 t4 = p_335472_.decode(p_330564_);
                T5 t5 = p_333318_.decode(p_330564_);
                T6 t6 = p_332458_.decode(p_330564_);
                return p_329947_.apply(t1, t2, t3, t4, t5, t6);
            }

            @Override
            public void encode(B p_328016_, C p_331911_) {
                p_333401_.encode(p_328016_, p_329450_.apply(p_331911_));
                p_330884_.encode(p_328016_, p_328085_.apply(p_331911_));
                p_332808_.encode(p_328016_, p_327867_.apply(p_331911_));
                p_335472_.encode(p_328016_, p_328511_.apply(p_331911_));
                p_333318_.encode(p_328016_, p_330123_.apply(p_331911_));
                p_332458_.encode(p_328016_, p_328086_.apply(p_331911_));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(
        final StreamCodec<? super B, T1> p_362813_,
        final Function<C, T1> p_366161_,
        final StreamCodec<? super B, T2> p_363894_,
        final Function<C, T2> p_362001_,
        final StreamCodec<? super B, T3> p_363671_,
        final Function<C, T3> p_367051_,
        final StreamCodec<? super B, T4> p_366641_,
        final Function<C, T4> p_367961_,
        final StreamCodec<? super B, T5> p_369011_,
        final Function<C, T5> p_368271_,
        final StreamCodec<? super B, T6> p_364705_,
        final Function<C, T6> p_363391_,
        final StreamCodec<? super B, T7> p_369569_,
        final Function<C, T7> p_365688_,
        final Function7<T1, T2, T3, T4, T5, T6, T7, C> p_370204_
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_330854_) {
                T1 t1 = p_362813_.decode(p_330854_);
                T2 t2 = p_363894_.decode(p_330854_);
                T3 t3 = p_363671_.decode(p_330854_);
                T4 t4 = p_366641_.decode(p_330854_);
                T5 t5 = p_369011_.decode(p_330854_);
                T6 t6 = p_364705_.decode(p_330854_);
                T7 t7 = p_369569_.decode(p_330854_);
                return p_370204_.apply(t1, t2, t3, t4, t5, t6, t7);
            }

            @Override
            public void encode(B p_332524_, C p_336367_) {
                p_362813_.encode(p_332524_, p_366161_.apply(p_336367_));
                p_363894_.encode(p_332524_, p_362001_.apply(p_336367_));
                p_363671_.encode(p_332524_, p_367051_.apply(p_336367_));
                p_366641_.encode(p_332524_, p_367961_.apply(p_336367_));
                p_369011_.encode(p_332524_, p_368271_.apply(p_336367_));
                p_364705_.encode(p_332524_, p_363391_.apply(p_336367_));
                p_369569_.encode(p_332524_, p_365688_.apply(p_336367_));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
        final StreamCodec<? super B, T1> p_367373_,
        final Function<C, T1> p_369557_,
        final StreamCodec<? super B, T2> p_368011_,
        final Function<C, T2> p_363664_,
        final StreamCodec<? super B, T3> p_367205_,
        final Function<C, T3> p_364055_,
        final StreamCodec<? super B, T4> p_361203_,
        final Function<C, T4> p_365006_,
        final StreamCodec<? super B, T5> p_362409_,
        final Function<C, T5> p_367771_,
        final StreamCodec<? super B, T6> p_362282_,
        final Function<C, T6> p_365852_,
        final StreamCodec<? super B, T7> p_361750_,
        final Function<C, T7> p_368272_,
        final StreamCodec<? super B, T8> p_367402_,
        final Function<C, T8> p_369297_,
        final Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> p_365425_
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_362416_) {
                T1 t1 = p_367373_.decode(p_362416_);
                T2 t2 = p_368011_.decode(p_362416_);
                T3 t3 = p_367205_.decode(p_362416_);
                T4 t4 = p_361203_.decode(p_362416_);
                T5 t5 = p_362409_.decode(p_362416_);
                T6 t6 = p_362282_.decode(p_362416_);
                T7 t7 = p_361750_.decode(p_362416_);
                T8 t8 = p_367402_.decode(p_362416_);
                return p_365425_.apply(t1, t2, t3, t4, t5, t6, t7, t8);
            }

            @Override
            public void encode(B p_366041_, C p_365657_) {
                p_367373_.encode(p_366041_, p_369557_.apply(p_365657_));
                p_368011_.encode(p_366041_, p_363664_.apply(p_365657_));
                p_367205_.encode(p_366041_, p_364055_.apply(p_365657_));
                p_361203_.encode(p_366041_, p_365006_.apply(p_365657_));
                p_362409_.encode(p_366041_, p_367771_.apply(p_365657_));
                p_362282_.encode(p_366041_, p_365852_.apply(p_365657_));
                p_361750_.encode(p_366041_, p_368272_.apply(p_365657_));
                p_367402_.encode(p_366041_, p_369297_.apply(p_365657_));
            }
        };
    }

    static <B, T> StreamCodec<B, T> recursive(final UnaryOperator<StreamCodec<B, T>> p_336362_) {
        return new StreamCodec<B, T>() {
            private final Supplier<StreamCodec<B, T>> inner = Suppliers.memoize(() -> p_336362_.apply(this));

            @Override
            public T decode(B p_366688_) {
                return this.inner.get().decode(p_366688_);
            }

            @Override
            public void encode(B p_364543_, T p_364761_) {
                this.inner.get().encode(p_364543_, p_364761_);
            }
        };
    }

    default <S extends B> StreamCodec<S, V> cast() {
        return (StreamCodec)this;
    }

    @FunctionalInterface
    public interface CodecOperation<B, S, T> {
        StreamCodec<B, T> apply(StreamCodec<B, S> p_333754_);
    }
}
