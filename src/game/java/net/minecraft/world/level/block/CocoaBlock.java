package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CocoaBlock extends HorizontalDirectionalBlock implements BonemealableBlock {
    public static final MapCodec<CocoaBlock> CODEC = simpleCodec(CocoaBlock::new);
    public static final int MAX_AGE = 2;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    protected static final int AGE_0_WIDTH = 4;
    protected static final int AGE_0_HEIGHT = 5;
    protected static final int AGE_0_HALFWIDTH = 2;
    protected static final int AGE_1_WIDTH = 6;
    protected static final int AGE_1_HEIGHT = 7;
    protected static final int AGE_1_HALFWIDTH = 3;
    protected static final int AGE_2_WIDTH = 8;
    protected static final int AGE_2_HEIGHT = 9;
    protected static final int AGE_2_HALFWIDTH = 4;
    protected static final VoxelShape[] EAST_AABB = new VoxelShape[]{
        Block.box(11.0, 7.0, 6.0, 15.0, 12.0, 10.0), Block.box(9.0, 5.0, 5.0, 15.0, 12.0, 11.0), Block.box(7.0, 3.0, 4.0, 15.0, 12.0, 12.0)
    };
    protected static final VoxelShape[] WEST_AABB = new VoxelShape[]{
        Block.box(1.0, 7.0, 6.0, 5.0, 12.0, 10.0), Block.box(1.0, 5.0, 5.0, 7.0, 12.0, 11.0), Block.box(1.0, 3.0, 4.0, 9.0, 12.0, 12.0)
    };
    protected static final VoxelShape[] NORTH_AABB = new VoxelShape[]{
        Block.box(6.0, 7.0, 1.0, 10.0, 12.0, 5.0), Block.box(5.0, 5.0, 1.0, 11.0, 12.0, 7.0), Block.box(4.0, 3.0, 1.0, 12.0, 12.0, 9.0)
    };
    protected static final VoxelShape[] SOUTH_AABB = new VoxelShape[]{
        Block.box(6.0, 7.0, 11.0, 10.0, 12.0, 15.0), Block.box(5.0, 5.0, 9.0, 11.0, 12.0, 15.0), Block.box(4.0, 3.0, 7.0, 12.0, 12.0, 15.0)
    };

    @Override
    public MapCodec<CocoaBlock> codec() {
        return CODEC;
    }

    public CocoaBlock(BlockBehaviour.Properties p_51743_) {
        super(p_51743_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AGE, Integer.valueOf(0)));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState p_51780_) {
        return p_51780_.getValue(AGE) < 2;
    }

    @Override
    protected void randomTick(BlockState p_221000_, ServerLevel p_221001_, BlockPos p_221002_, RandomSource p_221003_) {
        if (p_221001_.random.nextInt(5) == 0) {
            int i = p_221000_.getValue(AGE);
            if (i < 2) {
                p_221001_.setBlock(p_221002_, p_221000_.setValue(AGE, Integer.valueOf(i + 1)), 2);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState p_51767_, LevelReader p_51768_, BlockPos p_51769_) {
        BlockState blockstate = p_51768_.getBlockState(p_51769_.relative(p_51767_.getValue(FACING)));
        return blockstate.is(BlockTags.JUNGLE_LOGS);
    }

    @Override
    protected VoxelShape getShape(BlockState p_51787_, BlockGetter p_51788_, BlockPos p_51789_, CollisionContext p_51790_) {
        int i = p_51787_.getValue(AGE);
        switch ((Direction)p_51787_.getValue(FACING)) {
            case SOUTH:
                return SOUTH_AABB[i];
            case NORTH:
            default:
                return NORTH_AABB[i];
            case WEST:
                return WEST_AABB[i];
            case EAST:
                return EAST_AABB[i];
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_51750_) {
        BlockState blockstate = this.defaultBlockState();
        LevelReader levelreader = p_51750_.getLevel();
        BlockPos blockpos = p_51750_.getClickedPos();

        for (Direction direction : p_51750_.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                blockstate = blockstate.setValue(FACING, direction);
                if (blockstate.canSurvive(levelreader, blockpos)) {
                    return blockstate;
                }
            }
        }

        return null;
    }

    @Override
    protected BlockState updateShape(
        BlockState p_51771_,
        LevelReader p_368588_,
        ScheduledTickAccess p_363107_,
        BlockPos p_51775_,
        Direction p_51772_,
        BlockPos p_51776_,
        BlockState p_51773_,
        RandomSource p_366816_
    ) {
        return p_51772_ == p_51771_.getValue(FACING) && !p_51771_.canSurvive(p_368588_, p_51775_)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(p_51771_, p_368588_, p_363107_, p_51775_, p_51772_, p_51776_, p_51773_, p_366816_);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader p_256189_, BlockPos p_51753_, BlockState p_51754_) {
        return p_51754_.getValue(AGE) < 2;
    }

    @Override
    public boolean isBonemealSuccess(Level p_220995_, RandomSource p_220996_, BlockPos p_220997_, BlockState p_220998_) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel p_220990_, RandomSource p_220991_, BlockPos p_220992_, BlockState p_220993_) {
        p_220990_.setBlock(p_220992_, p_220993_.setValue(AGE, Integer.valueOf(p_220993_.getValue(AGE) + 1)), 2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51778_) {
        p_51778_.add(FACING, AGE);
    }

    @Override
    protected boolean isPathfindable(BlockState p_51762_, PathComputationType p_51765_) {
        return false;
    }
}