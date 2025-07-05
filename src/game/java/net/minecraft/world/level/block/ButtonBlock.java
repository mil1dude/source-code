package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ButtonBlock extends FaceAttachedHorizontalDirectionalBlock {
    public static final MapCodec<ButtonBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_360402_ -> p_360402_.group(
                    BlockSetType.CODEC.fieldOf("block_set_type").forGetter(p_312681_ -> p_312681_.type),
                    Codec.intRange(1, 1024).fieldOf("ticks_to_stay_pressed").forGetter(p_312686_ -> p_312686_.ticksToStayPressed),
                    propertiesCodec()
                )
                .apply(p_360402_, ButtonBlock::new)
    );
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int PRESSED_DEPTH = 1;
    private static final int UNPRESSED_DEPTH = 2;
    protected static final int HALF_AABB_HEIGHT = 2;
    protected static final int HALF_AABB_WIDTH = 3;
    protected static final VoxelShape CEILING_AABB_X = Block.box(6.0, 14.0, 5.0, 10.0, 16.0, 11.0);
    protected static final VoxelShape CEILING_AABB_Z = Block.box(5.0, 14.0, 6.0, 11.0, 16.0, 10.0);
    protected static final VoxelShape FLOOR_AABB_X = Block.box(6.0, 0.0, 5.0, 10.0, 2.0, 11.0);
    protected static final VoxelShape FLOOR_AABB_Z = Block.box(5.0, 0.0, 6.0, 11.0, 2.0, 10.0);
    protected static final VoxelShape NORTH_AABB = Block.box(5.0, 6.0, 14.0, 11.0, 10.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0, 6.0, 0.0, 11.0, 10.0, 2.0);
    protected static final VoxelShape WEST_AABB = Block.box(14.0, 6.0, 5.0, 16.0, 10.0, 11.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 6.0, 5.0, 2.0, 10.0, 11.0);
    protected static final VoxelShape PRESSED_CEILING_AABB_X = Block.box(6.0, 15.0, 5.0, 10.0, 16.0, 11.0);
    protected static final VoxelShape PRESSED_CEILING_AABB_Z = Block.box(5.0, 15.0, 6.0, 11.0, 16.0, 10.0);
    protected static final VoxelShape PRESSED_FLOOR_AABB_X = Block.box(6.0, 0.0, 5.0, 10.0, 1.0, 11.0);
    protected static final VoxelShape PRESSED_FLOOR_AABB_Z = Block.box(5.0, 0.0, 6.0, 11.0, 1.0, 10.0);
    protected static final VoxelShape PRESSED_NORTH_AABB = Block.box(5.0, 6.0, 15.0, 11.0, 10.0, 16.0);
    protected static final VoxelShape PRESSED_SOUTH_AABB = Block.box(5.0, 6.0, 0.0, 11.0, 10.0, 1.0);
    protected static final VoxelShape PRESSED_WEST_AABB = Block.box(15.0, 6.0, 5.0, 16.0, 10.0, 11.0);
    protected static final VoxelShape PRESSED_EAST_AABB = Block.box(0.0, 6.0, 5.0, 1.0, 10.0, 11.0);
    private final BlockSetType type;
    private final int ticksToStayPressed;

    @Override
    public MapCodec<ButtonBlock> codec() {
        return CODEC;
    }

    protected ButtonBlock(BlockSetType p_273462_, int p_273212_, BlockBehaviour.Properties p_273290_) {
        super(p_273290_.sound(p_273462_.soundType()));
        this.type = p_273462_;
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(FACE, AttachFace.WALL)
        );
        this.ticksToStayPressed = p_273212_;
    }

    @Override
    protected VoxelShape getShape(BlockState p_51104_, BlockGetter p_51105_, BlockPos p_51106_, CollisionContext p_51107_) {
        Direction direction = p_51104_.getValue(FACING);
        boolean flag = p_51104_.getValue(POWERED);
        switch ((AttachFace)p_51104_.getValue(FACE)) {
            case FLOOR:
                if (direction.getAxis() == Direction.Axis.X) {
                    return flag ? PRESSED_FLOOR_AABB_X : FLOOR_AABB_X;
                }

                return flag ? PRESSED_FLOOR_AABB_Z : FLOOR_AABB_Z;
            case WALL:
                return switch (direction) {
                    case EAST -> flag ? PRESSED_EAST_AABB : EAST_AABB;
                    case WEST -> flag ? PRESSED_WEST_AABB : WEST_AABB;
                    case SOUTH -> flag ? PRESSED_SOUTH_AABB : SOUTH_AABB;
                    case NORTH, UP, DOWN -> flag ? PRESSED_NORTH_AABB : NORTH_AABB;
                };
            case CEILING:
            default:
                if (direction.getAxis() == Direction.Axis.X) {
                    return flag ? PRESSED_CEILING_AABB_X : CEILING_AABB_X;
                } else {
                    return flag ? PRESSED_CEILING_AABB_Z : CEILING_AABB_Z;
                }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState p_329418_, Level p_334611_, BlockPos p_332004_, Player p_330636_, BlockHitResult p_327724_) {
        if (p_329418_.getValue(POWERED)) {
            return InteractionResult.CONSUME;
        } else {
            this.press(p_329418_, p_334611_, p_332004_, p_330636_);
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    protected void onExplosionHit(BlockState p_310762_, ServerLevel p_363623_, BlockPos p_312982_, Explosion p_311820_, BiConsumer<ItemStack, BlockPos> p_312672_) {
        if (p_311820_.canTriggerBlocks() && !p_310762_.getValue(POWERED)) {
            this.press(p_310762_, p_363623_, p_312982_, null);
        }

        super.onExplosionHit(p_310762_, p_363623_, p_312982_, p_311820_, p_312672_);
    }

    public void press(BlockState p_51117_, Level p_51118_, BlockPos p_51119_, @Nullable Player p_343045_) {
        p_51118_.setBlock(p_51119_, p_51117_.setValue(POWERED, Boolean.valueOf(true)), 3);
        this.updateNeighbours(p_51117_, p_51118_, p_51119_);
        p_51118_.scheduleTick(p_51119_, this, this.ticksToStayPressed);
        this.playSound(p_343045_, p_51118_, p_51119_, true);
        p_51118_.gameEvent(p_343045_, GameEvent.BLOCK_ACTIVATE, p_51119_);
    }

    protected void playSound(@Nullable Player p_51068_, LevelAccessor p_51069_, BlockPos p_51070_, boolean p_51071_) {
        p_51069_.playSound(p_51071_ ? p_51068_ : null, p_51070_, this.getSound(p_51071_), SoundSource.BLOCKS);
    }

    protected SoundEvent getSound(boolean p_51102_) {
        return p_51102_ ? this.type.buttonClickOn() : this.type.buttonClickOff();
    }

    @Override
    protected void onRemove(BlockState p_51095_, Level p_51096_, BlockPos p_51097_, BlockState p_51098_, boolean p_51099_) {
        if (!p_51099_ && !p_51095_.is(p_51098_.getBlock())) {
            if (p_51095_.getValue(POWERED)) {
                this.updateNeighbours(p_51095_, p_51096_, p_51097_);
            }

            super.onRemove(p_51095_, p_51096_, p_51097_, p_51098_, p_51099_);
        }
    }

    @Override
    protected int getSignal(BlockState p_51078_, BlockGetter p_51079_, BlockPos p_51080_, Direction p_51081_) {
        return p_51078_.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState p_51109_, BlockGetter p_51110_, BlockPos p_51111_, Direction p_51112_) {
        return p_51109_.getValue(POWERED) && getConnectedDirection(p_51109_) == p_51112_ ? 15 : 0;
    }

    @Override
    protected boolean isSignalSource(BlockState p_51114_) {
        return true;
    }

    @Override
    protected void tick(BlockState p_220903_, ServerLevel p_220904_, BlockPos p_220905_, RandomSource p_220906_) {
        if (p_220903_.getValue(POWERED)) {
            this.checkPressed(p_220903_, p_220904_, p_220905_);
        }
    }

    @Override
    protected void entityInside(BlockState p_51083_, Level p_51084_, BlockPos p_51085_, Entity p_51086_) {
        if (!p_51084_.isClientSide && this.type.canButtonBeActivatedByArrows() && !p_51083_.getValue(POWERED)) {
            this.checkPressed(p_51083_, p_51084_, p_51085_);
        }
    }

    protected void checkPressed(BlockState p_51121_, Level p_51122_, BlockPos p_51123_) {
        AbstractArrow abstractarrow = this.type.canButtonBeActivatedByArrows()
            ? p_51122_.getEntitiesOfClass(AbstractArrow.class, p_51121_.getShape(p_51122_, p_51123_).bounds().move(p_51123_)).stream().findFirst().orElse(null)
            : null;
        boolean flag = abstractarrow != null;
        boolean flag1 = p_51121_.getValue(POWERED);
        if (flag != flag1) {
            p_51122_.setBlock(p_51123_, p_51121_.setValue(POWERED, Boolean.valueOf(flag)), 3);
            this.updateNeighbours(p_51121_, p_51122_, p_51123_);
            this.playSound(null, p_51122_, p_51123_, flag);
            p_51122_.gameEvent(abstractarrow, flag ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, p_51123_);
        }

        if (flag) {
            p_51122_.scheduleTick(new BlockPos(p_51123_), this, this.ticksToStayPressed);
        }
    }

    private void updateNeighbours(BlockState p_51125_, Level p_51126_, BlockPos p_51127_) {
        Direction direction = getConnectedDirection(p_51125_).getOpposite();
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(
            p_51126_, direction, direction.getAxis().isHorizontal() ? Direction.UP : p_51125_.getValue(FACING)
        );
        p_51126_.updateNeighborsAt(p_51127_, this, orientation);
        p_51126_.updateNeighborsAt(p_51127_.relative(direction), this, orientation);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51101_) {
        p_51101_.add(FACING, POWERED, FACE);
    }
}