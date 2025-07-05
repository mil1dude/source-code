package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GrindstoneBlock extends FaceAttachedHorizontalDirectionalBlock {
    public static final MapCodec<GrindstoneBlock> CODEC = simpleCodec(GrindstoneBlock::new);
    public static final VoxelShape FLOOR_NORTH_SOUTH_LEFT_POST = Block.box(2.0, 0.0, 6.0, 4.0, 7.0, 10.0);
    public static final VoxelShape FLOOR_NORTH_SOUTH_RIGHT_POST = Block.box(12.0, 0.0, 6.0, 14.0, 7.0, 10.0);
    public static final VoxelShape FLOOR_NORTH_SOUTH_LEFT_PIVOT = Block.box(2.0, 7.0, 5.0, 4.0, 13.0, 11.0);
    public static final VoxelShape FLOOR_NORTH_SOUTH_RIGHT_PIVOT = Block.box(12.0, 7.0, 5.0, 14.0, 13.0, 11.0);
    public static final VoxelShape FLOOR_NORTH_SOUTH_LEFT_LEG = Shapes.or(FLOOR_NORTH_SOUTH_LEFT_POST, FLOOR_NORTH_SOUTH_LEFT_PIVOT);
    public static final VoxelShape FLOOR_NORTH_SOUTH_RIGHT_LEG = Shapes.or(FLOOR_NORTH_SOUTH_RIGHT_POST, FLOOR_NORTH_SOUTH_RIGHT_PIVOT);
    public static final VoxelShape FLOOR_NORTH_SOUTH_ALL_LEGS = Shapes.or(FLOOR_NORTH_SOUTH_LEFT_LEG, FLOOR_NORTH_SOUTH_RIGHT_LEG);
    public static final VoxelShape FLOOR_NORTH_SOUTH_GRINDSTONE = Shapes.or(FLOOR_NORTH_SOUTH_ALL_LEGS, Block.box(4.0, 4.0, 2.0, 12.0, 16.0, 14.0));
    public static final VoxelShape FLOOR_EAST_WEST_LEFT_POST = Block.box(6.0, 0.0, 2.0, 10.0, 7.0, 4.0);
    public static final VoxelShape FLOOR_EAST_WEST_RIGHT_POST = Block.box(6.0, 0.0, 12.0, 10.0, 7.0, 14.0);
    public static final VoxelShape FLOOR_EAST_WEST_LEFT_PIVOT = Block.box(5.0, 7.0, 2.0, 11.0, 13.0, 4.0);
    public static final VoxelShape FLOOR_EAST_WEST_RIGHT_PIVOT = Block.box(5.0, 7.0, 12.0, 11.0, 13.0, 14.0);
    public static final VoxelShape FLOOR_EAST_WEST_LEFT_LEG = Shapes.or(FLOOR_EAST_WEST_LEFT_POST, FLOOR_EAST_WEST_LEFT_PIVOT);
    public static final VoxelShape FLOOR_EAST_WEST_RIGHT_LEG = Shapes.or(FLOOR_EAST_WEST_RIGHT_POST, FLOOR_EAST_WEST_RIGHT_PIVOT);
    public static final VoxelShape FLOOR_EAST_WEST_ALL_LEGS = Shapes.or(FLOOR_EAST_WEST_LEFT_LEG, FLOOR_EAST_WEST_RIGHT_LEG);
    public static final VoxelShape FLOOR_EAST_WEST_GRINDSTONE = Shapes.or(FLOOR_EAST_WEST_ALL_LEGS, Block.box(2.0, 4.0, 4.0, 14.0, 16.0, 12.0));
    public static final VoxelShape WALL_SOUTH_LEFT_POST = Block.box(2.0, 6.0, 0.0, 4.0, 10.0, 7.0);
    public static final VoxelShape WALL_SOUTH_RIGHT_POST = Block.box(12.0, 6.0, 0.0, 14.0, 10.0, 7.0);
    public static final VoxelShape WALL_SOUTH_LEFT_PIVOT = Block.box(2.0, 5.0, 7.0, 4.0, 11.0, 13.0);
    public static final VoxelShape WALL_SOUTH_RIGHT_PIVOT = Block.box(12.0, 5.0, 7.0, 14.0, 11.0, 13.0);
    public static final VoxelShape WALL_SOUTH_LEFT_LEG = Shapes.or(WALL_SOUTH_LEFT_POST, WALL_SOUTH_LEFT_PIVOT);
    public static final VoxelShape WALL_SOUTH_RIGHT_LEG = Shapes.or(WALL_SOUTH_RIGHT_POST, WALL_SOUTH_RIGHT_PIVOT);
    public static final VoxelShape WALL_SOUTH_ALL_LEGS = Shapes.or(WALL_SOUTH_LEFT_LEG, WALL_SOUTH_RIGHT_LEG);
    public static final VoxelShape WALL_SOUTH_GRINDSTONE = Shapes.or(WALL_SOUTH_ALL_LEGS, Block.box(4.0, 2.0, 4.0, 12.0, 14.0, 16.0));
    public static final VoxelShape WALL_NORTH_LEFT_POST = Block.box(2.0, 6.0, 7.0, 4.0, 10.0, 16.0);
    public static final VoxelShape WALL_NORTH_RIGHT_POST = Block.box(12.0, 6.0, 7.0, 14.0, 10.0, 16.0);
    public static final VoxelShape WALL_NORTH_LEFT_PIVOT = Block.box(2.0, 5.0, 3.0, 4.0, 11.0, 9.0);
    public static final VoxelShape WALL_NORTH_RIGHT_PIVOT = Block.box(12.0, 5.0, 3.0, 14.0, 11.0, 9.0);
    public static final VoxelShape WALL_NORTH_LEFT_LEG = Shapes.or(WALL_NORTH_LEFT_POST, WALL_NORTH_LEFT_PIVOT);
    public static final VoxelShape WALL_NORTH_RIGHT_LEG = Shapes.or(WALL_NORTH_RIGHT_POST, WALL_NORTH_RIGHT_PIVOT);
    public static final VoxelShape WALL_NORTH_ALL_LEGS = Shapes.or(WALL_NORTH_LEFT_LEG, WALL_NORTH_RIGHT_LEG);
    public static final VoxelShape WALL_NORTH_GRINDSTONE = Shapes.or(WALL_NORTH_ALL_LEGS, Block.box(4.0, 2.0, 0.0, 12.0, 14.0, 12.0));
    public static final VoxelShape WALL_WEST_LEFT_POST = Block.box(7.0, 6.0, 2.0, 16.0, 10.0, 4.0);
    public static final VoxelShape WALL_WEST_RIGHT_POST = Block.box(7.0, 6.0, 12.0, 16.0, 10.0, 14.0);
    public static final VoxelShape WALL_WEST_LEFT_PIVOT = Block.box(3.0, 5.0, 2.0, 9.0, 11.0, 4.0);
    public static final VoxelShape WALL_WEST_RIGHT_PIVOT = Block.box(3.0, 5.0, 12.0, 9.0, 11.0, 14.0);
    public static final VoxelShape WALL_WEST_LEFT_LEG = Shapes.or(WALL_WEST_LEFT_POST, WALL_WEST_LEFT_PIVOT);
    public static final VoxelShape WALL_WEST_RIGHT_LEG = Shapes.or(WALL_WEST_RIGHT_POST, WALL_WEST_RIGHT_PIVOT);
    public static final VoxelShape WALL_WEST_ALL_LEGS = Shapes.or(WALL_WEST_LEFT_LEG, WALL_WEST_RIGHT_LEG);
    public static final VoxelShape WALL_WEST_GRINDSTONE = Shapes.or(WALL_WEST_ALL_LEGS, Block.box(0.0, 2.0, 4.0, 12.0, 14.0, 12.0));
    public static final VoxelShape WALL_EAST_LEFT_POST = Block.box(0.0, 6.0, 2.0, 9.0, 10.0, 4.0);
    public static final VoxelShape WALL_EAST_RIGHT_POST = Block.box(0.0, 6.0, 12.0, 9.0, 10.0, 14.0);
    public static final VoxelShape WALL_EAST_LEFT_PIVOT = Block.box(7.0, 5.0, 2.0, 13.0, 11.0, 4.0);
    public static final VoxelShape WALL_EAST_RIGHT_PIVOT = Block.box(7.0, 5.0, 12.0, 13.0, 11.0, 14.0);
    public static final VoxelShape WALL_EAST_LEFT_LEG = Shapes.or(WALL_EAST_LEFT_POST, WALL_EAST_LEFT_PIVOT);
    public static final VoxelShape WALL_EAST_RIGHT_LEG = Shapes.or(WALL_EAST_RIGHT_POST, WALL_EAST_RIGHT_PIVOT);
    public static final VoxelShape WALL_EAST_ALL_LEGS = Shapes.or(WALL_EAST_LEFT_LEG, WALL_EAST_RIGHT_LEG);
    public static final VoxelShape WALL_EAST_GRINDSTONE = Shapes.or(WALL_EAST_ALL_LEGS, Block.box(4.0, 2.0, 4.0, 16.0, 14.0, 12.0));
    public static final VoxelShape CEILING_NORTH_SOUTH_LEFT_POST = Block.box(2.0, 9.0, 6.0, 4.0, 16.0, 10.0);
    public static final VoxelShape CEILING_NORTH_SOUTH_RIGHT_POST = Block.box(12.0, 9.0, 6.0, 14.0, 16.0, 10.0);
    public static final VoxelShape CEILING_NORTH_SOUTH_LEFT_PIVOT = Block.box(2.0, 3.0, 5.0, 4.0, 9.0, 11.0);
    public static final VoxelShape CEILING_NORTH_SOUTH_RIGHT_PIVOT = Block.box(12.0, 3.0, 5.0, 14.0, 9.0, 11.0);
    public static final VoxelShape CEILING_NORTH_SOUTH_LEFT_LEG = Shapes.or(CEILING_NORTH_SOUTH_LEFT_POST, CEILING_NORTH_SOUTH_LEFT_PIVOT);
    public static final VoxelShape CEILING_NORTH_SOUTH_RIGHT_LEG = Shapes.or(CEILING_NORTH_SOUTH_RIGHT_POST, CEILING_NORTH_SOUTH_RIGHT_PIVOT);
    public static final VoxelShape CEILING_NORTH_SOUTH_ALL_LEGS = Shapes.or(CEILING_NORTH_SOUTH_LEFT_LEG, CEILING_NORTH_SOUTH_RIGHT_LEG);
    public static final VoxelShape CEILING_NORTH_SOUTH_GRINDSTONE = Shapes.or(CEILING_NORTH_SOUTH_ALL_LEGS, Block.box(4.0, 0.0, 2.0, 12.0, 12.0, 14.0));
    public static final VoxelShape CEILING_EAST_WEST_LEFT_POST = Block.box(6.0, 9.0, 2.0, 10.0, 16.0, 4.0);
    public static final VoxelShape CEILING_EAST_WEST_RIGHT_POST = Block.box(6.0, 9.0, 12.0, 10.0, 16.0, 14.0);
    public static final VoxelShape CEILING_EAST_WEST_LEFT_PIVOT = Block.box(5.0, 3.0, 2.0, 11.0, 9.0, 4.0);
    public static final VoxelShape CEILING_EAST_WEST_RIGHT_PIVOT = Block.box(5.0, 3.0, 12.0, 11.0, 9.0, 14.0);
    public static final VoxelShape CEILING_EAST_WEST_LEFT_LEG = Shapes.or(CEILING_EAST_WEST_LEFT_POST, CEILING_EAST_WEST_LEFT_PIVOT);
    public static final VoxelShape CEILING_EAST_WEST_RIGHT_LEG = Shapes.or(CEILING_EAST_WEST_RIGHT_POST, CEILING_EAST_WEST_RIGHT_PIVOT);
    public static final VoxelShape CEILING_EAST_WEST_ALL_LEGS = Shapes.or(CEILING_EAST_WEST_LEFT_LEG, CEILING_EAST_WEST_RIGHT_LEG);
    public static final VoxelShape CEILING_EAST_WEST_GRINDSTONE = Shapes.or(CEILING_EAST_WEST_ALL_LEGS, Block.box(2.0, 0.0, 4.0, 14.0, 12.0, 12.0));
    private static final Component CONTAINER_TITLE = Component.translatable("container.grindstone_title");

    @Override
    public MapCodec<GrindstoneBlock> codec() {
        return CODEC;
    }

    protected GrindstoneBlock(BlockBehaviour.Properties p_53808_) {
        super(p_53808_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.WALL));
    }

    private VoxelShape getVoxelShape(BlockState p_53856_) {
        Direction direction = p_53856_.getValue(FACING);
        switch ((AttachFace)p_53856_.getValue(FACE)) {
            case FLOOR:
                if (direction != Direction.NORTH && direction != Direction.SOUTH) {
                    return FLOOR_EAST_WEST_GRINDSTONE;
                }

                return FLOOR_NORTH_SOUTH_GRINDSTONE;
            case WALL:
                if (direction == Direction.NORTH) {
                    return WALL_NORTH_GRINDSTONE;
                } else if (direction == Direction.SOUTH) {
                    return WALL_SOUTH_GRINDSTONE;
                } else {
                    if (direction == Direction.EAST) {
                        return WALL_EAST_GRINDSTONE;
                    }

                    return WALL_WEST_GRINDSTONE;
                }
            case CEILING:
                if (direction != Direction.NORTH && direction != Direction.SOUTH) {
                    return CEILING_EAST_WEST_GRINDSTONE;
                }

                return CEILING_NORTH_SOUTH_GRINDSTONE;
            default:
                return FLOOR_EAST_WEST_GRINDSTONE;
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState p_53851_, BlockGetter p_53852_, BlockPos p_53853_, CollisionContext p_53854_) {
        return this.getVoxelShape(p_53851_);
    }

    @Override
    protected VoxelShape getShape(BlockState p_53842_, BlockGetter p_53843_, BlockPos p_53844_, CollisionContext p_53845_) {
        return this.getVoxelShape(p_53842_);
    }

    @Override
    protected boolean canSurvive(BlockState p_53828_, LevelReader p_53829_, BlockPos p_53830_) {
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState p_53821_, Level p_53822_, BlockPos p_53823_, Player p_53824_, BlockHitResult p_53826_) {
        if (!p_53822_.isClientSide) {
            p_53824_.openMenu(p_53821_.getMenuProvider(p_53822_, p_53823_));
            p_53824_.awardStat(Stats.INTERACT_WITH_GRINDSTONE);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState p_53847_, Level p_53848_, BlockPos p_53849_) {
        return new SimpleMenuProvider(
            (p_53812_, p_53813_, p_53814_) -> new GrindstoneMenu(p_53812_, p_53813_, ContainerLevelAccess.create(p_53848_, p_53849_)), CONTAINER_TITLE
        );
    }

    @Override
    protected BlockState rotate(BlockState p_53835_, Rotation p_53836_) {
        return p_53835_.setValue(FACING, p_53836_.rotate(p_53835_.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState p_53832_, Mirror p_53833_) {
        return p_53832_.rotate(p_53833_.getRotation(p_53832_.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_53838_) {
        p_53838_.add(FACING, FACE);
    }

    @Override
    protected boolean isPathfindable(BlockState p_53816_, PathComputationType p_53819_) {
        return false;
    }
}