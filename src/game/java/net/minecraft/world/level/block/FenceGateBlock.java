package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FenceGateBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<FenceGateBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_360431_ -> p_360431_.group(WoodType.CODEC.fieldOf("wood_type").forGetter(p_311297_ -> p_311297_.type), propertiesCodec())
                .apply(p_360431_, FenceGateBlock::new)
    );
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty IN_WALL = BlockStateProperties.IN_WALL;
    protected static final VoxelShape Z_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape X_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
    protected static final VoxelShape Z_SHAPE_LOW = Block.box(0.0, 0.0, 6.0, 16.0, 13.0, 10.0);
    protected static final VoxelShape X_SHAPE_LOW = Block.box(6.0, 0.0, 0.0, 10.0, 13.0, 16.0);
    protected static final VoxelShape Z_COLLISION_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 24.0, 10.0);
    protected static final VoxelShape X_COLLISION_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 24.0, 16.0);
    protected static final VoxelShape Z_SUPPORT_SHAPE = Block.box(0.0, 5.0, 6.0, 16.0, 24.0, 10.0);
    protected static final VoxelShape X_SUPPORT_SHAPE = Block.box(6.0, 5.0, 0.0, 10.0, 24.0, 16.0);
    protected static final VoxelShape Z_OCCLUSION_SHAPE = Shapes.or(Block.box(0.0, 5.0, 7.0, 2.0, 16.0, 9.0), Block.box(14.0, 5.0, 7.0, 16.0, 16.0, 9.0));
    protected static final VoxelShape X_OCCLUSION_SHAPE = Shapes.or(Block.box(7.0, 5.0, 0.0, 9.0, 16.0, 2.0), Block.box(7.0, 5.0, 14.0, 9.0, 16.0, 16.0));
    protected static final VoxelShape Z_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(0.0, 2.0, 7.0, 2.0, 13.0, 9.0), Block.box(14.0, 2.0, 7.0, 16.0, 13.0, 9.0));
    protected static final VoxelShape X_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(7.0, 2.0, 0.0, 9.0, 13.0, 2.0), Block.box(7.0, 2.0, 14.0, 9.0, 13.0, 16.0));
    private final WoodType type;

    @Override
    public MapCodec<FenceGateBlock> codec() {
        return CODEC;
    }

    public FenceGateBlock(WoodType p_273340_, BlockBehaviour.Properties p_273352_) {
        super(p_273352_.sound(p_273340_.soundType()));
        this.type = p_273340_;
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(OPEN, Boolean.valueOf(false))
                .setValue(POWERED, Boolean.valueOf(false))
                .setValue(IN_WALL, Boolean.valueOf(false))
        );
    }

    @Override
    protected VoxelShape getShape(BlockState p_53391_, BlockGetter p_53392_, BlockPos p_53393_, CollisionContext p_53394_) {
        if (p_53391_.getValue(IN_WALL)) {
            return p_53391_.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE_LOW : Z_SHAPE_LOW;
        } else {
            return p_53391_.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
        }
    }

    @Override
    protected BlockState updateShape(
        BlockState p_53382_,
        LevelReader p_361939_,
        ScheduledTickAccess p_361210_,
        BlockPos p_53386_,
        Direction p_53383_,
        BlockPos p_53387_,
        BlockState p_53384_,
        RandomSource p_364167_
    ) {
        Direction.Axis direction$axis = p_53383_.getAxis();
        if (p_53382_.getValue(FACING).getClockWise().getAxis() != direction$axis) {
            return super.updateShape(p_53382_, p_361939_, p_361210_, p_53386_, p_53383_, p_53387_, p_53384_, p_364167_);
        } else {
            boolean flag = this.isWall(p_53384_) || this.isWall(p_361939_.getBlockState(p_53386_.relative(p_53383_.getOpposite())));
            return p_53382_.setValue(IN_WALL, Boolean.valueOf(flag));
        }
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState p_253862_, BlockGetter p_254569_, BlockPos p_254197_) {
        if (p_253862_.getValue(OPEN)) {
            return Shapes.empty();
        } else {
            return p_253862_.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_SUPPORT_SHAPE : X_SUPPORT_SHAPE;
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState p_53396_, BlockGetter p_53397_, BlockPos p_53398_, CollisionContext p_53399_) {
        if (p_53396_.getValue(OPEN)) {
            return Shapes.empty();
        } else {
            return p_53396_.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
        }
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState p_53401_) {
        if (p_53401_.getValue(IN_WALL)) {
            return p_53401_.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE_LOW : Z_OCCLUSION_SHAPE_LOW;
        } else {
            return p_53401_.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE : Z_OCCLUSION_SHAPE;
        }
    }

    @Override
    protected boolean isPathfindable(BlockState p_53360_, PathComputationType p_53363_) {
        switch (p_53363_) {
            case LAND:
                return p_53360_.getValue(OPEN);
            case WATER:
                return false;
            case AIR:
                return p_53360_.getValue(OPEN);
            default:
                return false;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_53358_) {
        Level level = p_53358_.getLevel();
        BlockPos blockpos = p_53358_.getClickedPos();
        boolean flag = level.hasNeighborSignal(blockpos);
        Direction direction = p_53358_.getHorizontalDirection();
        Direction.Axis direction$axis = direction.getAxis();
        boolean flag1 = direction$axis == Direction.Axis.Z
                && (this.isWall(level.getBlockState(blockpos.west())) || this.isWall(level.getBlockState(blockpos.east())))
            || direction$axis == Direction.Axis.X && (this.isWall(level.getBlockState(blockpos.north())) || this.isWall(level.getBlockState(blockpos.south())));
        return this.defaultBlockState()
            .setValue(FACING, direction)
            .setValue(OPEN, Boolean.valueOf(flag))
            .setValue(POWERED, Boolean.valueOf(flag))
            .setValue(IN_WALL, Boolean.valueOf(flag1));
    }

    private boolean isWall(BlockState p_53405_) {
        return p_53405_.is(BlockTags.WALLS);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState p_53365_, Level p_53366_, BlockPos p_53367_, Player p_53368_, BlockHitResult p_53370_) {
        if (p_53365_.getValue(OPEN)) {
            p_53365_ = p_53365_.setValue(OPEN, Boolean.valueOf(false));
            p_53366_.setBlock(p_53367_, p_53365_, 10);
        } else {
            Direction direction = p_53368_.getDirection();
            if (p_53365_.getValue(FACING) == direction.getOpposite()) {
                p_53365_ = p_53365_.setValue(FACING, direction);
            }

            p_53365_ = p_53365_.setValue(OPEN, Boolean.valueOf(true));
            p_53366_.setBlock(p_53367_, p_53365_, 10);
        }

        boolean flag = p_53365_.getValue(OPEN);
        p_53366_.playSound(
            p_53368_,
            p_53367_,
            flag ? this.type.fenceGateOpen() : this.type.fenceGateClose(),
            SoundSource.BLOCKS,
            1.0F,
            p_53366_.getRandom().nextFloat() * 0.1F + 0.9F
        );
        p_53366_.gameEvent(p_53368_, flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, p_53367_);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onExplosionHit(BlockState p_311339_, ServerLevel p_368190_, BlockPos p_311232_, Explosion p_312135_, BiConsumer<ItemStack, BlockPos> p_313242_) {
        if (p_312135_.canTriggerBlocks() && !p_311339_.getValue(POWERED)) {
            boolean flag = p_311339_.getValue(OPEN);
            p_368190_.setBlockAndUpdate(p_311232_, p_311339_.setValue(OPEN, Boolean.valueOf(!flag)));
            p_368190_.playSound(
                null,
                p_311232_,
                flag ? this.type.fenceGateClose() : this.type.fenceGateOpen(),
                SoundSource.BLOCKS,
                1.0F,
                p_368190_.getRandom().nextFloat() * 0.1F + 0.9F
            );
            p_368190_.gameEvent(flag ? GameEvent.BLOCK_CLOSE : GameEvent.BLOCK_OPEN, p_311232_, GameEvent.Context.of(p_311339_));
        }

        super.onExplosionHit(p_311339_, p_368190_, p_311232_, p_312135_, p_313242_);
    }

    @Override
    protected void neighborChanged(BlockState p_53372_, Level p_53373_, BlockPos p_53374_, Block p_53375_, @Nullable Orientation p_366214_, boolean p_53377_) {
        if (!p_53373_.isClientSide) {
            boolean flag = p_53373_.hasNeighborSignal(p_53374_);
            if (p_53372_.getValue(POWERED) != flag) {
                p_53373_.setBlock(p_53374_, p_53372_.setValue(POWERED, Boolean.valueOf(flag)).setValue(OPEN, Boolean.valueOf(flag)), 2);
                if (p_53372_.getValue(OPEN) != flag) {
                    p_53373_.playSound(
                        null,
                        p_53374_,
                        flag ? this.type.fenceGateOpen() : this.type.fenceGateClose(),
                        SoundSource.BLOCKS,
                        1.0F,
                        p_53373_.getRandom().nextFloat() * 0.1F + 0.9F
                    );
                    p_53373_.gameEvent(null, flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, p_53374_);
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_53389_) {
        p_53389_.add(FACING, OPEN, POWERED, IN_WALL);
    }

    public static boolean connectsToDirection(BlockState p_53379_, Direction p_53380_) {
        return p_53379_.getValue(FACING).getAxis() == p_53380_.getClockWise().getAxis();
    }
}