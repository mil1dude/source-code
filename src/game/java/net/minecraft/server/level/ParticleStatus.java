package net.minecraft.server.level;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

public enum ParticleStatus implements OptionEnum {
    ALL(0, "options.particles.all"),
    DECREASED(1, "options.particles.decreased"),
    MINIMAL(2, "options.particles.minimal");

    private static final IntFunction<ParticleStatus> BY_ID = ByIdMap.continuous(ParticleStatus::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    private final int id;
    private final String key;

    private ParticleStatus(final int p_363607_, final String p_370037_) {
        this.id = p_363607_;
        this.key = p_370037_;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public static ParticleStatus byId(int p_364580_) {
        return BY_ID.apply(p_364580_);
    }
}