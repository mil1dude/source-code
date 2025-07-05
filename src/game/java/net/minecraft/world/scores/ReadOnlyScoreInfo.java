package net.minecraft.world.scores;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;

public interface ReadOnlyScoreInfo {
    int value();

    boolean isLocked();

    @Nullable
    NumberFormat numberFormat();

    default MutableComponent formatValue(NumberFormat p_313073_) {
        return Objects.requireNonNullElse(this.numberFormat(), p_313073_).format(this.value());
    }

    static MutableComponent safeFormatValue(@Nullable ReadOnlyScoreInfo p_312063_, NumberFormat p_312422_) {
        return p_312063_ != null ? p_312063_.formatValue(p_312422_) : p_312422_.format(0);
    }
}