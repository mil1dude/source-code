package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseRailBlock extends Block implements SimpleWaterloggedBlock {
    protected static final VoxelShape FLAT_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    protected static final VoxelShape HALF_BLOCK_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final boolean isStraight;

    public static boolean isRail(Level p_49365_, BlockPos p_49366_) {
        return isRail(p_49365_.getBlockState(p_49366_));
    }

    public static boolean isRail(BlockState p_49417_) {
        return p_49417_.is(BlockTags.RAILS) && p_49417_.getBlock() instanceof BaseRailBlock;
    }

    protected BaseRailBlock(boolean p_49360_, BlockBehaviour.Properties p_49361_) {
        super(p_49361_);
        this.isStraight = p_49360_;
    }

    @Override
    protected abstract MapCodec<? extends BaseRailBlock> codec();

    public boolean isStraight() {
        return this.isStraight;
    }

    @Override
    protected VoxelShape getShape(BlockState p_49403_, BlockGetter p_49404_, BlockPos p_49405_, CollisionContext p_49406_) {
        RailShape railshape = p_49403_.is(this) ? p_49403_.getValue(this.getShapeProperty()) : null;
        return railshape != null && railshape.isSlope() ? HALF_BLOCK_AABB : FLAT_AABB;
    }

    @Override
    protected boolean canSurvive(BlockState p_49395_, LevelReader p_49396_, BlockPos p_49397_) {
        return canSupportRigidBlock(p_49396_, p_49397_.below());
    }

    @Override
    protected void onPlace(BlockState p_49408_, Level p_49409_, BlockPos p_49410_, BlockState p_49411_, boolean p_49412_) {
        if (!p_49411_.is(p_49408_.getBlock())) {
            this.updateState(p_49408_, p_49409_, p_49410_, p_49412_);
        }
    }

    protected BlockState updateState(BlockState p_49390_, Level p_49391_, BlockPos p_49392_, boolean p_49393_) {
        p_49390_ = this.updateDir(p_49391_, p_49392_, p_49390_, true);
        if (this.isStraight) {
            p_49391_.neighborChanged(p_49390_, p_49392_, this, null, p_49393_);
        }

        return p_49390_;
    }

    @Override
    protected void neighborChanged(BlockState p_49377_, Level p_49378_, BlockPos p_49379_, Block p_49380_, @Nullable Orientation p_362860_, boolean p_49382_) {
        if (!p_49378_.isClientSide && p_49378_.getBlockState(p_49379_).is(this)) {
            RailShape railshape = p_49377_.getValue(this.getShapeProperty());
            if (shouldBeRemoved(p_49379_, p_49378_, railshape)) {
                dropResources(p_49377_, p_49378_, p_49379_);
                p_49378_.removeBlock(p_49379_, p_49382_);
            } else {
                this.updateState(p_49377_, p_49378_, p_49379_, p_49380_);
            }
        }
    }

    private static boolean shouldBeRemoved(BlockPos p_49399_, Level p_49400_, RailShape p_49401_) {
        if (!canSupportRigidBlock(p_49400_, p_49399_.below())) {
            return true;
        } else {
            switch (p_49401_) {
                case ASCENDING_EAST:
                    return !canSupportRigidBlock(p_49400_, p_49399_.east());
                case ASCENDING_WEST:
                    return !canSupportRigidBlock(p_49400_, p_49399_.west());
                case ASCENDING_NORTH:
                    return !canSupportRigidBlock(p_49400_, p_49399_.north());
                case ASCENDING_SOUTH:
                    return !canSupportRigidBlock(p_49400_, p_49399_.south());
                default:
                    return false;
            }
        }
    }

    protected void updateState(BlockState p_49372_, Level p_49373_, BlockPos p_49374_, Block p_49375_) {
    }

    protected BlockState updateDir(Level p_49368_, BlockPos p_49369_, BlockState p_49370_, boolean p_49371_) {
        if (p_49368_.isClientSide) {
            return p_49370_;
        } else {
            RailShape railshape = p_49370_.getValue(this.getShapeProperty());
            return new RailState(p_49368_, p_49369_, p_49370_).place(p_49368_.hasNeighborSignal(p_49369_), p_49371_, railshape).getState();
        }
    }

    @Override
    protected void onRemove(BlockState p_49384_, Level p_49385_, BlockPos p_49386_, BlockState p_49387_, boolean p_49388_) {
        if (!p_49388_) {
            super.onRemove(p_49384_, p_49385_, p_49386_, p_49387_, p_49388_);
            if (p_49384_.getValue(this.getShapeProperty()).isSlope()) {
                p_49385_.updateNeighborsAt(p_49386_.above(), this);
            }

            if (this.isStraight) {
                p_49385_.updateNeighborsAt(p_49386_, this);
                p_49385_.updateNeighborsAt(p_49386_.below(), this);
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_49363_) {
        FluidState fluidstate = p_49363_.getLevel().getFluidState(p_49363_.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        BlockState blockstate = super.defaultBlockState();
        Direction direction = p_49363_.getHorizontalDirection();
        boolean flag1 = direction == Direction.EAST || direction == Direction.WEST;
        return blockstate.setValue(this.getShapeProperty(), flag1 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(flag));
    }

    public abstract Property<RailShape> getShapeProperty();

    @Override
    protected BlockState updateShape(
        BlockState p_152151_,
        LevelReader p_363749_,
        ScheduledTickAccess p_365089_,
        BlockPos p_152155_,
        Direction p_152152_,
        BlockPos p_152156_,
        BlockState p_152153_,
        RandomSource p_368260_
    ) {
        if (p_152151_.getValue(WATERLOGGED)) {
            p_365089_.scheduleTick(p_152155_, Fluids.WATER, Fluids.WATER.getTickDelay(p_363749_));
        }

        return super.updateShape(p_152151_, p_363749_, p_365089_, p_152155_, p_152152_, p_152156_, p_152153_, p_368260_);
    }

    @Override
    protected FluidState getFluidState(BlockState p_152158_) {
        return p_152158_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_152158_);
    }
}