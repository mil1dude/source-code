package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface BonemealableBlock {
    boolean isValidBonemealTarget(LevelReader p_256559_, BlockPos p_50898_, BlockState p_50899_);

    boolean isBonemealSuccess(Level p_220878_, RandomSource p_220879_, BlockPos p_220880_, BlockState p_220881_);

    void performBonemeal(ServerLevel p_220874_, RandomSource p_220875_, BlockPos p_220876_, BlockState p_220877_);

    default BlockPos getParticlePos(BlockPos p_335812_) {
        return switch (this.getType()) {
            case NEIGHBOR_SPREADER -> p_335812_.above();
            case GROWER -> p_335812_;
        };
    }

    default BonemealableBlock.Type getType() {
        return BonemealableBlock.Type.GROWER;
    }

    public static enum Type {
        NEIGHBOR_SPREADER,
        GROWER;
    }
}