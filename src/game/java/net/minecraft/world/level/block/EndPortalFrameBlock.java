package net.minecraft.world.level.block;

import com.google.common.base.Predicates;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EndPortalFrameBlock extends Block {
    public static final MapCodec<EndPortalFrameBlock> CODEC = simpleCodec(EndPortalFrameBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_EYE = BlockStateProperties.EYE;
    protected static final VoxelShape BASE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 13.0, 16.0);
    protected static final VoxelShape EYE_SHAPE = Block.box(4.0, 13.0, 4.0, 12.0, 16.0, 12.0);
    protected static final VoxelShape FULL_SHAPE = Shapes.or(BASE_SHAPE, EYE_SHAPE);
    private static BlockPattern portalShape;

    @Override
    public MapCodec<EndPortalFrameBlock> codec() {
        return CODEC;
    }

    public EndPortalFrameBlock(BlockBehaviour.Properties p_53050_) {
        super(p_53050_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_EYE, Boolean.valueOf(false)));
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState p_53079_) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState p_53073_, BlockGetter p_53074_, BlockPos p_53075_, CollisionContext p_53076_) {
        return p_53073_.getValue(HAS_EYE) ? FULL_SHAPE : BASE_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_53052_) {
        return this.defaultBlockState().setValue(FACING, p_53052_.getHorizontalDirection().getOpposite()).setValue(HAS_EYE, Boolean.valueOf(false));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState p_53054_) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState p_53061_, Level p_53062_, BlockPos p_53063_) {
        return p_53061_.getValue(HAS_EYE) ? 15 : 0;
    }

    @Override
    protected BlockState rotate(BlockState p_53068_, Rotation p_53069_) {
        return p_53068_.setValue(FACING, p_53069_.rotate(p_53068_.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState p_53065_, Mirror p_53066_) {
        return p_53065_.rotate(p_53066_.getRotation(p_53065_.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_53071_) {
        p_53071_.add(FACING, HAS_EYE);
    }

    public static BlockPattern getOrCreatePortalShape() {
        if (portalShape == null) {
            portalShape = BlockPatternBuilder.start()
                .aisle("?vvv?", ">???<", ">???<", ">???<", "?^^^?")
                .where('?', BlockInWorld.hasState(BlockStatePredicate.ANY))
                .where(
                    '^',
                    BlockInWorld.hasState(
                        BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(HAS_EYE, Predicates.equalTo(true))
                            .where(FACING, Predicates.equalTo(Direction.SOUTH))
                    )
                )
                .where(
                    '>',
                    BlockInWorld.hasState(
                        BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(HAS_EYE, Predicates.equalTo(true))
                            .where(FACING, Predicates.equalTo(Direction.WEST))
                    )
                )
                .where(
                    'v',
                    BlockInWorld.hasState(
                        BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(HAS_EYE, Predicates.equalTo(true))
                            .where(FACING, Predicates.equalTo(Direction.NORTH))
                    )
                )
                .where(
                    '<',
                    BlockInWorld.hasState(
                        BlockStatePredicate.forBlock(Blocks.END_PORTAL_FRAME)
                            .where(HAS_EYE, Predicates.equalTo(true))
                            .where(FACING, Predicates.equalTo(Direction.EAST))
                    )
                )
                .build();
        }

        return portalShape;
    }

    @Override
    protected boolean isPathfindable(BlockState p_53056_, PathComputationType p_53059_) {
        return false;
    }
}