package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HopperBlock extends BaseEntityBlock {
    public static final MapCodec<HopperBlock> CODEC = simpleCodec(HopperBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING_HOPPER;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    private static final VoxelShape TOP = Block.box(0.0, 10.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape FUNNEL = Block.box(4.0, 4.0, 4.0, 12.0, 10.0, 12.0);
    private static final VoxelShape CONVEX_BASE = Shapes.or(FUNNEL, TOP);
    private static final VoxelShape INSIDE = box(2.0, 11.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape BASE = Shapes.join(CONVEX_BASE, INSIDE, BooleanOp.ONLY_FIRST);
    private static final VoxelShape DOWN_SHAPE = Shapes.or(BASE, Block.box(6.0, 0.0, 6.0, 10.0, 4.0, 10.0));
    private static final VoxelShape EAST_SHAPE = Shapes.or(BASE, Block.box(12.0, 4.0, 6.0, 16.0, 8.0, 10.0));
    private static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, Block.box(6.0, 4.0, 0.0, 10.0, 8.0, 4.0));
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, Block.box(6.0, 4.0, 12.0, 10.0, 8.0, 16.0));
    private static final VoxelShape WEST_SHAPE = Shapes.or(BASE, Block.box(0.0, 4.0, 6.0, 4.0, 8.0, 10.0));
    private static final VoxelShape DOWN_INTERACTION_SHAPE = INSIDE;
    private static final VoxelShape EAST_INTERACTION_SHAPE = Shapes.or(INSIDE, Block.box(12.0, 8.0, 6.0, 16.0, 10.0, 10.0));
    private static final VoxelShape NORTH_INTERACTION_SHAPE = Shapes.or(INSIDE, Block.box(6.0, 8.0, 0.0, 10.0, 10.0, 4.0));
    private static final VoxelShape SOUTH_INTERACTION_SHAPE = Shapes.or(INSIDE, Block.box(6.0, 8.0, 12.0, 10.0, 10.0, 16.0));
    private static final VoxelShape WEST_INTERACTION_SHAPE = Shapes.or(INSIDE, Block.box(0.0, 8.0, 6.0, 4.0, 10.0, 10.0));

    @Override
    public MapCodec<HopperBlock> codec() {
        return CODEC;
    }

    public HopperBlock(BlockBehaviour.Properties p_54039_) {
        super(p_54039_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(ENABLED, Boolean.valueOf(true)));
    }

    @Override
    protected VoxelShape getShape(BlockState p_54105_, BlockGetter p_54106_, BlockPos p_54107_, CollisionContext p_54108_) {
        switch ((Direction)p_54105_.getValue(FACING)) {
            case DOWN:
                return DOWN_SHAPE;
            case NORTH:
                return NORTH_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case WEST:
                return WEST_SHAPE;
            case EAST:
                return EAST_SHAPE;
            default:
                return BASE;
        }
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState p_54099_, BlockGetter p_54100_, BlockPos p_54101_) {
        switch ((Direction)p_54099_.getValue(FACING)) {
            case DOWN:
                return DOWN_INTERACTION_SHAPE;
            case NORTH:
                return NORTH_INTERACTION_SHAPE;
            case SOUTH:
                return SOUTH_INTERACTION_SHAPE;
            case WEST:
                return WEST_INTERACTION_SHAPE;
            case EAST:
                return EAST_INTERACTION_SHAPE;
            default:
                return INSIDE;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_54041_) {
        Direction direction = p_54041_.getClickedFace().getOpposite();
        return this.defaultBlockState()
            .setValue(FACING, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction)
            .setValue(ENABLED, Boolean.valueOf(true));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_153382_, BlockState p_153383_) {
        return new HopperBlockEntity(p_153382_, p_153383_);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153378_, BlockState p_153379_, BlockEntityType<T> p_153380_) {
        return p_153378_.isClientSide ? null : createTickerHelper(p_153380_, BlockEntityType.HOPPER, HopperBlockEntity::pushItemsTick);
    }

    @Override
    protected void onPlace(BlockState p_54110_, Level p_54111_, BlockPos p_54112_, BlockState p_54113_, boolean p_54114_) {
        if (!p_54113_.is(p_54110_.getBlock())) {
            this.checkPoweredState(p_54111_, p_54112_, p_54110_);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState p_54071_, Level p_54072_, BlockPos p_54073_, Player p_54074_, BlockHitResult p_54076_) {
        if (!p_54072_.isClientSide && p_54072_.getBlockEntity(p_54073_) instanceof HopperBlockEntity hopperblockentity) {
            p_54074_.openMenu(hopperblockentity);
            p_54074_.awardStat(Stats.INSPECT_HOPPER);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState p_54078_, Level p_54079_, BlockPos p_54080_, Block p_54081_, @Nullable Orientation p_364751_, boolean p_54083_) {
        this.checkPoweredState(p_54079_, p_54080_, p_54078_);
    }

    private void checkPoweredState(Level p_275499_, BlockPos p_275298_, BlockState p_275611_) {
        boolean flag = !p_275499_.hasNeighborSignal(p_275298_);
        if (flag != p_275611_.getValue(ENABLED)) {
            p_275499_.setBlock(p_275298_, p_275611_.setValue(ENABLED, Boolean.valueOf(flag)), 2);
        }
    }

    @Override
    protected void onRemove(BlockState p_54085_, Level p_54086_, BlockPos p_54087_, BlockState p_54088_, boolean p_54089_) {
        Containers.dropContentsOnDestroy(p_54085_, p_54088_, p_54086_, p_54087_);
        super.onRemove(p_54085_, p_54086_, p_54087_, p_54088_, p_54089_);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState p_54055_) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState p_54062_, Level p_54063_, BlockPos p_54064_) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(p_54063_.getBlockEntity(p_54064_));
    }

    @Override
    protected BlockState rotate(BlockState p_54094_, Rotation p_54095_) {
        return p_54094_.setValue(FACING, p_54095_.rotate(p_54094_.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState p_54091_, Mirror p_54092_) {
        return p_54091_.rotate(p_54092_.getRotation(p_54091_.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_54097_) {
        p_54097_.add(FACING, ENABLED);
    }

    @Override
    protected void entityInside(BlockState p_54066_, Level p_54067_, BlockPos p_54068_, Entity p_54069_) {
        BlockEntity blockentity = p_54067_.getBlockEntity(p_54068_);
        if (blockentity instanceof HopperBlockEntity) {
            HopperBlockEntity.entityInside(p_54067_, p_54068_, p_54066_, p_54069_, (HopperBlockEntity)blockentity);
        }
    }

    @Override
    protected boolean isPathfindable(BlockState p_54057_, PathComputationType p_54060_) {
        return false;
    }
}