package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPos;

public interface WritableLevelData extends LevelData {
    void setSpawn(BlockPos p_78649_, float p_78650_);
}