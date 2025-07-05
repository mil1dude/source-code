package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class RailBlock extends BaseRailBlock {
    public static final MapCodec<RailBlock> CODEC = simpleCodec(RailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

    @Override
    public MapCodec<RailBlock> codec() {
        return CODEC;
    }

    protected RailBlock(BlockBehaviour.Properties p_55395_) {
        super(false, p_55395_);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    protected void updateState(BlockState p_55397_, Level p_55398_, BlockPos p_55399_, Block p_55400_) {
        if (p_55400_.defaultBlockState().isSignalSource() && new RailState(p_55398_, p_55399_, p_55397_).countPotentialConnections() == 3) {
            this.updateDir(p_55398_, p_55399_, p_55397_, false);
        }
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState p_55405_, Rotation p_55406_) {
        RailShape railshape = p_55405_.getValue(SHAPE);

        return p_55405_.setValue(SHAPE, switch (p_55406_) {
            case CLOCKWISE_180 -> {
                switch (railshape) {
                    case NORTH_SOUTH:
                        yield RailShape.NORTH_SOUTH;
                    case EAST_WEST:
                        yield RailShape.EAST_WEST;
                    case ASCENDING_EAST:
                        yield RailShape.ASCENDING_WEST;
                    case ASCENDING_WEST:
                        yield RailShape.ASCENDING_EAST;
                    case ASCENDING_NORTH:
                        yield RailShape.ASCENDING_SOUTH;
                    case ASCENDING_SOUTH:
                        yield RailShape.ASCENDING_NORTH;
                    case SOUTH_EAST:
                        yield RailShape.NORTH_WEST;
                    case SOUTH_WEST:
                        yield RailShape.NORTH_EAST;
                    case NORTH_WEST:
                        yield RailShape.SOUTH_EAST;
                    case NORTH_EAST:
                        yield RailShape.SOUTH_WEST;
                    default:
                        throw new MatchException(null, null);
                }
            }
            case COUNTERCLOCKWISE_90 -> {
                switch (railshape) {
                    case NORTH_SOUTH:
                        yield RailShape.EAST_WEST;
                    case EAST_WEST:
                        yield RailShape.NORTH_SOUTH;
                    case ASCENDING_EAST:
                        yield RailShape.ASCENDING_NORTH;
                    case ASCENDING_WEST:
                        yield RailShape.ASCENDING_SOUTH;
                    case ASCENDING_NORTH:
                        yield RailShape.ASCENDING_WEST;
                    case ASCENDING_SOUTH:
                        yield RailShape.ASCENDING_EAST;
                    case SOUTH_EAST:
                        yield RailShape.NORTH_EAST;
                    case SOUTH_WEST:
                        yield RailShape.SOUTH_EAST;
                    case NORTH_WEST:
                        yield RailShape.SOUTH_WEST;
                    case NORTH_EAST:
                        yield RailShape.NORTH_WEST;
                    default:
                        throw new MatchException(null, null);
                }
            }
            case CLOCKWISE_90 -> {
                switch (railshape) {
                    case NORTH_SOUTH:
                        yield RailShape.EAST_WEST;
                    case EAST_WEST:
                        yield RailShape.NORTH_SOUTH;
                    case ASCENDING_EAST:
                        yield RailShape.ASCENDING_SOUTH;
                    case ASCENDING_WEST:
                        yield RailShape.ASCENDING_NORTH;
                    case ASCENDING_NORTH:
                        yield RailShape.ASCENDING_EAST;
                    case ASCENDING_SOUTH:
                        yield RailShape.ASCENDING_WEST;
                    case SOUTH_EAST:
                        yield RailShape.SOUTH_WEST;
                    case SOUTH_WEST:
                        yield RailShape.NORTH_WEST;
                    case NORTH_WEST:
                        yield RailShape.NORTH_EAST;
                    case NORTH_EAST:
                        yield RailShape.SOUTH_EAST;
                    default:
                        throw new MatchException(null, null);
                }
            }
            default -> railshape;
        });
    }

    @Override
    protected BlockState mirror(BlockState p_55402_, Mirror p_55403_) {
        RailShape railshape = p_55402_.getValue(SHAPE);
        switch (p_55403_) {
            case LEFT_RIGHT:
                switch (railshape) {
                    case ASCENDING_NORTH:
                        return p_55402_.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
                    case ASCENDING_SOUTH:
                        return p_55402_.setValue(SHAPE, RailShape.ASCENDING_NORTH);
                    case SOUTH_EAST:
                        return p_55402_.setValue(SHAPE, RailShape.NORTH_EAST);
                    case SOUTH_WEST:
                        return p_55402_.setValue(SHAPE, RailShape.NORTH_WEST);
                    case NORTH_WEST:
                        return p_55402_.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case NORTH_EAST:
                        return p_55402_.setValue(SHAPE, RailShape.SOUTH_EAST);
                    default:
                        return super.mirror(p_55402_, p_55403_);
                }
            case FRONT_BACK:
                switch (railshape) {
                    case ASCENDING_EAST:
                        return p_55402_.setValue(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_WEST:
                        return p_55402_.setValue(SHAPE, RailShape.ASCENDING_EAST);
                    case ASCENDING_NORTH:
                    case ASCENDING_SOUTH:
                    default:
                        break;
                    case SOUTH_EAST:
                        return p_55402_.setValue(SHAPE, RailShape.SOUTH_WEST);
                    case SOUTH_WEST:
                        return p_55402_.setValue(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_WEST:
                        return p_55402_.setValue(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_EAST:
                        return p_55402_.setValue(SHAPE, RailShape.NORTH_WEST);
                }
        }

        return super.mirror(p_55402_, p_55403_);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_55408_) {
        p_55408_.add(SHAPE, WATERLOGGED);
    }
}