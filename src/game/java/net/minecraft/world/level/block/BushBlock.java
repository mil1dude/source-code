package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

public abstract class BushBlock extends Block {
    protected BushBlock(BlockBehaviour.Properties p_51021_) {
        super(p_51021_);
    }

    @Override
    protected abstract MapCodec<? extends BushBlock> codec();

    protected boolean mayPlaceOn(BlockState p_51042_, BlockGetter p_51043_, BlockPos p_51044_) {
        return p_51042_.is(BlockTags.DIRT) || p_51042_.is(Blocks.FARMLAND);
    }

    @Override
    protected BlockState updateShape(
        BlockState p_51032_,
        LevelReader p_366208_,
        ScheduledTickAccess p_369931_,
        BlockPos p_51036_,
        Direction p_51033_,
        BlockPos p_51037_,
        BlockState p_51034_,
        RandomSource p_365527_
    ) {
        return !p_51032_.canSurvive(p_366208_, p_51036_)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(p_51032_, p_366208_, p_369931_, p_51036_, p_51033_, p_51037_, p_51034_, p_365527_);
    }

    @Override
    protected boolean canSurvive(BlockState p_51028_, LevelReader p_51029_, BlockPos p_51030_) {
        BlockPos blockpos = p_51030_.below();
        return this.mayPlaceOn(p_51029_.getBlockState(blockpos), p_51029_, blockpos);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState p_51039_) {
        return p_51039_.getFluidState().isEmpty();
    }

    @Override
    protected boolean isPathfindable(BlockState p_51023_, PathComputationType p_51026_) {
        return p_51026_ == PathComputationType.AIR && !this.hasCollision ? true : super.isPathfindable(p_51023_, p_51026_);
    }
}