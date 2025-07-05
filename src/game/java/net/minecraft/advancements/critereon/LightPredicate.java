package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record LightPredicate(MinMaxBounds.Ints composite) {
    public static final Codec<LightPredicate> CODEC = RecordCodecBuilder.create(
        p_325226_ -> p_325226_.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("light", MinMaxBounds.Ints.ANY).forGetter(LightPredicate::composite))
                .apply(p_325226_, LightPredicate::new)
    );

    public boolean matches(ServerLevel p_51342_, BlockPos p_51343_) {
        return !p_51342_.isLoaded(p_51343_) ? false : this.composite.matches(p_51342_.getMaxLocalRawBrightness(p_51343_));
    }

    public static class Builder {
        private MinMaxBounds.Ints composite = MinMaxBounds.Ints.ANY;

        public static LightPredicate.Builder light() {
            return new LightPredicate.Builder();
        }

        public LightPredicate.Builder setComposite(MinMaxBounds.Ints p_153105_) {
            this.composite = p_153105_;
            return this;
        }

        public LightPredicate build() {
            return new LightPredicate(this.composite);
        }
    }
}