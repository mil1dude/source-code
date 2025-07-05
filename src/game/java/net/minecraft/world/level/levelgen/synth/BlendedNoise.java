package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Locale;
import java.util.stream.IntStream;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class BlendedNoise implements DensityFunction.SimpleFunction {
    private static final Codec<Double> SCALE_RANGE = Codec.doubleRange(0.001, 1000.0);
    private static final MapCodec<BlendedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(
        p_230486_ -> p_230486_.group(
                    SCALE_RANGE.fieldOf("xz_scale").forGetter(p_230497_ -> p_230497_.xzScale),
                    SCALE_RANGE.fieldOf("y_scale").forGetter(p_230495_ -> p_230495_.yScale),
                    SCALE_RANGE.fieldOf("xz_factor").forGetter(p_230493_ -> p_230493_.xzFactor),
                    SCALE_RANGE.fieldOf("y_factor").forGetter(p_230490_ -> p_230490_.yFactor),
                    Codec.doubleRange(1.0, 8.0).fieldOf("smear_scale_multiplier").forGetter(p_230488_ -> p_230488_.smearScaleMultiplier)
                )
                .apply(p_230486_, BlendedNoise::createUnseeded)
    );
    public static final KeyDispatchDataCodec<BlendedNoise> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);
    private final PerlinNoise minLimitNoise;
    private final PerlinNoise maxLimitNoise;
    private final PerlinNoise mainNoise;
    private final double xzMultiplier;
    private final double yMultiplier;
    private final double xzFactor;
    private final double yFactor;
    private final double smearScaleMultiplier;
    private final double maxValue;
    private final double xzScale;
    private final double yScale;

    public static BlendedNoise createUnseeded(double p_230478_, double p_230479_, double p_230480_, double p_230481_, double p_230482_) {
        return new BlendedNoise(new XoroshiroRandomSource(0L), p_230478_, p_230479_, p_230480_, p_230481_, p_230482_);
    }

    private BlendedNoise(
        PerlinNoise p_230469_,
        PerlinNoise p_230470_,
        PerlinNoise p_230471_,
        double p_230472_,
        double p_230473_,
        double p_230474_,
        double p_230475_,
        double p_230476_
    ) {
        this.minLimitNoise = p_230469_;
        this.maxLimitNoise = p_230470_;
        this.mainNoise = p_230471_;
        this.xzScale = p_230472_;
        this.yScale = p_230473_;
        this.xzFactor = p_230474_;
        this.yFactor = p_230475_;
        this.smearScaleMultiplier = p_230476_;
        this.xzMultiplier = 684.412 * this.xzScale;
        this.yMultiplier = 684.412 * this.yScale;
        this.maxValue = p_230469_.maxBrokenValue(this.yMultiplier);
    }

    @VisibleForTesting
    public BlendedNoise(RandomSource p_230462_, double p_230463_, double p_230464_, double p_230465_, double p_230466_, double p_230467_) {
        this(
            PerlinNoise.createLegacyForBlendedNoise(p_230462_, IntStream.rangeClosed(-15, 0)),
            PerlinNoise.createLegacyForBlendedNoise(p_230462_, IntStream.rangeClosed(-15, 0)),
            PerlinNoise.createLegacyForBlendedNoise(p_230462_, IntStream.rangeClosed(-7, 0)),
            p_230463_,
            p_230464_,
            p_230465_,
            p_230466_,
            p_230467_
        );
    }

    public BlendedNoise withNewRandom(RandomSource p_230484_) {
        return new BlendedNoise(p_230484_, this.xzScale, this.yScale, this.xzFactor, this.yFactor, this.smearScaleMultiplier);
    }

    @Override
    public double compute(DensityFunction.FunctionContext p_210621_) {
        double d0 = (double)p_210621_.blockX() * this.xzMultiplier;
        double d1 = (double)p_210621_.blockY() * this.yMultiplier;
        double d2 = (double)p_210621_.blockZ() * this.xzMultiplier;
        double d3 = d0 / this.xzFactor;
        double d4 = d1 / this.yFactor;
        double d5 = d2 / this.xzFactor;
        double d6 = this.yMultiplier * this.smearScaleMultiplier;
        double d7 = d6 / this.yFactor;
        double d8 = 0.0;
        double d9 = 0.0;
        double d10 = 0.0;
        boolean flag = true;
        double d11 = 1.0;

        for (int i = 0; i < 8; i++) {
            ImprovedNoise improvednoise = this.mainNoise.getOctaveNoise(i);
            if (improvednoise != null) {
                d10 += improvednoise.noise(
                        PerlinNoise.wrap(d3 * d11), PerlinNoise.wrap(d4 * d11), PerlinNoise.wrap(d5 * d11), d7 * d11, d4 * d11
                    )
                    / d11;
            }

            d11 /= 2.0;
        }

        double d16 = (d10 / 10.0 + 1.0) / 2.0;
        boolean flag1 = d16 >= 1.0;
        boolean flag2 = d16 <= 0.0;
        d11 = 1.0;

        for (int j = 0; j < 16; j++) {
            double d12 = PerlinNoise.wrap(d0 * d11);
            double d13 = PerlinNoise.wrap(d1 * d11);
            double d14 = PerlinNoise.wrap(d2 * d11);
            double d15 = d6 * d11;
            if (!flag1) {
                ImprovedNoise improvednoise1 = this.minLimitNoise.getOctaveNoise(j);
                if (improvednoise1 != null) {
                    d8 += improvednoise1.noise(d12, d13, d14, d15, d1 * d11) / d11;
                }
            }

            if (!flag2) {
                ImprovedNoise improvednoise2 = this.maxLimitNoise.getOctaveNoise(j);
                if (improvednoise2 != null) {
                    d9 += improvednoise2.noise(d12, d13, d14, d15, d1 * d11) / d11;
                }
            }

            d11 /= 2.0;
        }

        return Mth.clampedLerp(d8 / 512.0, d9 / 512.0, d16) / 128.0;
    }

    @Override
    public double minValue() {
        return -this.maxValue();
    }

    @Override
    public double maxValue() {
        return this.maxValue;
    }

    @VisibleForTesting
    public void parityConfigString(StringBuilder p_192818_) {
        p_192818_.append("BlendedNoise{minLimitNoise=");
        this.minLimitNoise.parityConfigString(p_192818_);
        p_192818_.append(", maxLimitNoise=");
        this.maxLimitNoise.parityConfigString(p_192818_);
        p_192818_.append(", mainNoise=");
        this.mainNoise.parityConfigString(p_192818_);
        p_192818_.append(
                String.format(
                    Locale.ROOT,
                    ", xzScale=%.3f, yScale=%.3f, xzMainScale=%.3f, yMainScale=%.3f, cellWidth=4, cellHeight=8",
                    684.412,
                    684.412,
                    8.555150000000001,
                    4.277575000000001
                )
            )
            .append('}');
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}