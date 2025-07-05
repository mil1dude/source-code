package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.util.Mth;

public record MovementPredicate(
    MinMaxBounds.Doubles x,
    MinMaxBounds.Doubles y,
    MinMaxBounds.Doubles z,
    MinMaxBounds.Doubles speed,
    MinMaxBounds.Doubles horizontalSpeed,
    MinMaxBounds.Doubles verticalSpeed,
    MinMaxBounds.Doubles fallDistance
) {
    public static final Codec<MovementPredicate> CODEC = RecordCodecBuilder.create(
        p_345285_ -> p_345285_.group(
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::x),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::y),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::z),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::speed),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal_speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::horizontalSpeed),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("vertical_speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::verticalSpeed),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("fall_distance", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::fallDistance)
                )
                .apply(p_345285_, MovementPredicate::new)
    );

    public static MovementPredicate speed(MinMaxBounds.Doubles p_344237_) {
        return new MovementPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            p_344237_,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY
        );
    }

    public static MovementPredicate horizontalSpeed(MinMaxBounds.Doubles p_343918_) {
        return new MovementPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            p_343918_,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY
        );
    }

    public static MovementPredicate verticalSpeed(MinMaxBounds.Doubles p_342210_) {
        return new MovementPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            p_342210_,
            MinMaxBounds.Doubles.ANY
        );
    }

    public static MovementPredicate fallDistance(MinMaxBounds.Doubles p_344473_) {
        return new MovementPredicate(
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            MinMaxBounds.Doubles.ANY,
            p_344473_
        );
    }

    public boolean matches(double p_342615_, double p_343015_, double p_344849_, double p_344583_) {
        if (this.x.matches(p_342615_) && this.y.matches(p_343015_) && this.z.matches(p_344849_)) {
            double d0 = Mth.lengthSquared(p_342615_, p_343015_, p_344849_);
            if (!this.speed.matchesSqr(d0)) {
                return false;
            } else {
                double d1 = Mth.lengthSquared(p_342615_, p_344849_);
                if (!this.horizontalSpeed.matchesSqr(d1)) {
                    return false;
                } else {
                    double d2 = Math.abs(p_343015_);
                    return !this.verticalSpeed.matches(d2) ? false : this.fallDistance.matches(p_344583_);
                }
            }
        } else {
            return false;
        }
    }
}