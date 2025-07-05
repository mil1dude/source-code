package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StairBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<StairBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_360454_ -> p_360454_.group(BlockState.CODEC.fieldOf("base_state").forGetter(p_309296_ -> p_309296_.baseState), propertiesCodec())
                .apply(p_360454_, StairBlock::new)
    );
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape TOP_AABB = SlabBlock.TOP_AABB;
    protected static final VoxelShape BOTTOM_AABB = SlabBlock.BOTTOM_AABB;
    protected static final VoxelShape OCTET_NNN = Block.box(0.0, 0.0, 0.0, 8.0, 8.0, 8.0);
    protected static final VoxelShape OCTET_NNP = Block.box(0.0, 0.0, 8.0, 8.0, 8.0, 16.0);
    protected static final VoxelShape OCTET_NPN = Block.box(0.0, 8.0, 0.0, 8.0, 16.0, 8.0);
    protected static final VoxelShape OCTET_NPP = Block.box(0.0, 8.0, 8.0, 8.0, 16.0, 16.0);
    protected static final VoxelShape OCTET_PNN = Block.box(8.0, 0.0, 0.0, 16.0, 8.0, 8.0);
    protected static final VoxelShape OCTET_PNP = Block.box(8.0, 0.0, 8.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape OCTET_PPN = Block.box(8.0, 8.0, 0.0, 16.0, 16.0, 8.0);
    protected static final VoxelShape OCTET_PPP = Block.box(8.0, 8.0, 8.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape[] TOP_SHAPES = makeShapes(TOP_AABB, OCTET_NNN, OCTET_PNN, OCTET_NNP, OCTET_PNP);
    protected static final VoxelShape[] BOTTOM_SHAPES = makeShapes(BOTTOM_AABB, OCTET_NPN, OCTET_PPN, OCTET_NPP, OCTET_PPP);
    private static final int[] SHAPE_BY_STATE = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
    private final Block base;
    protected final BlockState baseState;

    @Override
    public MapCodec<? extends StairBlock> codec() {
        return CODEC;
    }

    private static VoxelShape[] makeShapes(VoxelShape p_56934_, VoxelShape p_56935_, VoxelShape p_56936_, VoxelShape p_56937_, VoxelShape p_56938_) {
        return IntStream.range(0, 16).mapToObj(p_56945_ -> makeStairShape(p_56945_, p_56934_, p_56935_, p_56936_, p_56937_, p_56938_)).toArray(VoxelShape[]::new);
    }

    private static VoxelShape makeStairShape(int p_56865_, VoxelShape p_56866_, VoxelShape p_56867_, VoxelShape p_56868_, VoxelShape p_56869_, VoxelShape p_56870_) {
        VoxelShape voxelshape = p_56866_;
        if ((p_56865_ & 1) != 0) {
            voxelshape = Shapes.or(p_56866_, p_56867_);
        }

        if ((p_56865_ & 2) != 0) {
            voxelshape = Shapes.or(voxelshape, p_56868_);
        }

        if ((p_56865_ & 4) != 0) {
            voxelshape = Shapes.or(voxelshape, p_56869_);
        }

        if ((p_56865_ & 8) != 0) {
            voxelshape = Shapes.or(voxelshape, p_56870_);
        }

        return voxelshape;
    }

    protected StairBlock(BlockState p_56862_, BlockBehaviour.Properties p_56863_) {
        super(p_56863_);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, Half.BOTTOM)
                .setValue(SHAPE, StairsShape.STRAIGHT)
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
        this.base = p_56862_.getBlock();
        this.baseState = p_56862_;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState p_56967_) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState p_56956_, BlockGetter p_56957_, BlockPos p_56958_, CollisionContext p_56959_) {
        return (p_56956_.getValue(HALF) == Half.TOP ? TOP_SHAPES : BOTTOM_SHAPES)[SHAPE_BY_STATE[this.getShapeIndex(p_56956_)]];
    }

    private int getShapeIndex(BlockState p_56983_) {
        return p_56983_.getValue(SHAPE).ordinal() * 4 + p_56983_.getValue(FACING).get2DDataValue();
    }

    @Override
    public float getExplosionResistance() {
        return this.base.getExplosionResistance();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_56872_) {
        Direction direction = p_56872_.getClickedFace();
        BlockPos blockpos = p_56872_.getClickedPos();
        FluidState fluidstate = p_56872_.getLevel().getFluidState(blockpos);
        BlockState blockstate = this.defaultBlockState()
            .setValue(FACING, p_56872_.getHorizontalDirection())
            .setValue(
                HALF,
                direction != Direction.DOWN && (direction == Direction.UP || !(p_56872_.getClickLocation().y - (double)blockpos.getY() > 0.5))
                    ? Half.BOTTOM
                    : Half.TOP
            )
            .setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
        return blockstate.setValue(SHAPE, getStairsShape(blockstate, p_56872_.getLevel(), blockpos));
    }

    @Override
    protected BlockState updateShape(
        BlockState p_56925_,
        LevelReader p_369543_,
        ScheduledTickAccess p_369679_,
        BlockPos p_56929_,
        Direction p_56926_,
        BlockPos p_56930_,
        BlockState p_56927_,
        RandomSource p_367682_
    ) {
        if (p_56925_.getValue(WATERLOGGED)) {
            p_369679_.scheduleTick(p_56929_, Fluids.WATER, Fluids.WATER.getTickDelay(p_369543_));
        }

        return p_56926_.getAxis().isHorizontal()
            ? p_56925_.setValue(SHAPE, getStairsShape(p_56925_, p_369543_, p_56929_))
            : super.updateShape(p_56925_, p_369543_, p_369679_, p_56929_, p_56926_, p_56930_, p_56927_, p_367682_);
    }

    private static StairsShape getStairsShape(BlockState p_56977_, BlockGetter p_56978_, BlockPos p_56979_) {
        Direction direction = p_56977_.getValue(FACING);
        BlockState blockstate = p_56978_.getBlockState(p_56979_.relative(direction));
        if (isStairs(blockstate) && p_56977_.getValue(HALF) == blockstate.getValue(HALF)) {
            Direction direction1 = blockstate.getValue(FACING);
            if (direction1.getAxis() != p_56977_.getValue(FACING).getAxis() && canTakeShape(p_56977_, p_56978_, p_56979_, direction1.getOpposite())) {
                if (direction1 == direction.getCounterClockWise()) {
                    return StairsShape.OUTER_LEFT;
                }

                return StairsShape.OUTER_RIGHT;
            }
        }

        BlockState blockstate1 = p_56978_.getBlockState(p_56979_.relative(direction.getOpposite()));
        if (isStairs(blockstate1) && p_56977_.getValue(HALF) == blockstate1.getValue(HALF)) {
            Direction direction2 = blockstate1.getValue(FACING);
            if (direction2.getAxis() != p_56977_.getValue(FACING).getAxis() && canTakeShape(p_56977_, p_56978_, p_56979_, direction2)) {
                if (direction2 == direction.getCounterClockWise()) {
                    return StairsShape.INNER_LEFT;
                }

                return StairsShape.INNER_RIGHT;
            }
        }

        return StairsShape.STRAIGHT;
    }

    private static boolean canTakeShape(BlockState p_56971_, BlockGetter p_56972_, BlockPos p_56973_, Direction p_56974_) {
        BlockState blockstate = p_56972_.getBlockState(p_56973_.relative(p_56974_));
        return !isStairs(blockstate)
            || blockstate.getValue(FACING) != p_56971_.getValue(FACING)
            || blockstate.getValue(HALF) != p_56971_.getValue(HALF);
    }

    public static boolean isStairs(BlockState p_56981_) {
        return p_56981_.getBlock() instanceof StairBlock;
    }

    @Override
    protected BlockState rotate(BlockState p_56922_, Rotation p_56923_) {
        return p_56922_.setValue(FACING, p_56923_.rotate(p_56922_.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState p_56919_, Mirror p_56920_) {
        Direction direction = p_56919_.getValue(FACING);
        StairsShape stairsshape = p_56919_.getValue(SHAPE);
        switch (p_56920_) {
            case LEFT_RIGHT:
                if (direction.getAxis() == Direction.Axis.Z) {
                    switch (stairsshape) {
                        case INNER_LEFT:
                            return p_56919_.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                        case INNER_RIGHT:
                            return p_56919_.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                        case OUTER_LEFT:
                            return p_56919_.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                        case OUTER_RIGHT:
                            return p_56919_.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                        default:
                            return p_56919_.rotate(Rotation.CLOCKWISE_180);
                    }
                }
                break;
            case FRONT_BACK:
                if (direction.getAxis() == Direction.Axis.X) {
                    switch (stairsshape) {
                        case INNER_LEFT:
                            return p_56919_.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                        case INNER_RIGHT:
                            return p_56919_.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                        case OUTER_LEFT:
                            return p_56919_.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                        case OUTER_RIGHT:
                            return p_56919_.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                        case STRAIGHT:
                            return p_56919_.rotate(Rotation.CLOCKWISE_180);
                    }
                }
        }

        return super.mirror(p_56919_, p_56920_);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_56932_) {
        p_56932_.add(FACING, HALF, SHAPE, WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState p_56969_) {
        return p_56969_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_56969_);
    }

    @Override
    protected boolean isPathfindable(BlockState p_56891_, PathComputationType p_56894_) {
        return false;
    }
}