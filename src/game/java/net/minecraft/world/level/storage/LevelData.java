package net.minecraft.world.level.storage;

import java.util.Locale;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelHeightAccessor;

public interface LevelData {
    BlockPos getSpawnPos();

    float getSpawnAngle();

    long getGameTime();

    long getDayTime();

    boolean isThundering();

    boolean isRaining();

    void setRaining(boolean p_78171_);

    boolean isHardcore();

    Difficulty getDifficulty();

    boolean isDifficultyLocked();

    default void fillCrashReportCategory(CrashReportCategory p_164873_, LevelHeightAccessor p_164874_) {
        p_164873_.setDetail("Level spawn location", () -> CrashReportCategory.formatLocation(p_164874_, this.getSpawnPos()));
        p_164873_.setDetail("Level time", () -> String.format(Locale.ROOT, "%d game time, %d day time", this.getGameTime(), this.getDayTime()));
    }
}