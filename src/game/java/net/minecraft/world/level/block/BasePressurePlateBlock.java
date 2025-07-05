package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BasePressurePlateBlock extends Block {
    protected static final VoxelShape PRESSED_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 0.5, 15.0);
    protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
    protected static final AABB TOUCH_AABB = new AABB(0.0625, 0.0, 0.0625, 0.9375, 0.25, 0.9375);
    protected final BlockSetType type;

    protected BasePressurePlateBlock(BlockBehaviour.Properties p_273450_, BlockSetType p_273402_) {
        super(p_273450_.sound(p_273402_.soundType()));
        this.type = p_273402_;
    }

    @Override
    protected abstract MapCodec<? extends BasePressurePlateBlock> codec();

    @Override
    protected VoxelShape getShape(BlockState p_49341_, BlockGetter p_49342_, BlockPos p_49343_, CollisionContext p_49344_) {
        return this.getSignalForState(p_49341_) > 0 ? PRESSED_AABB : AABB;
    }

    protected int getPressedTime() {
        return 20;
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState p_279155_) {
        return true;
    }

    @Override
    protected BlockState updateShape(
        BlockState p_49329_,
        LevelReader p_366632_,
        ScheduledTickAccess p_363595_,
        BlockPos p_49333_,
        Direction p_49330_,
        BlockPos p_49334_,
        BlockState p_49331_,
        RandomSource p_370163_
    ) {
        return p_49330_ == Direction.DOWN && !p_49329_.canSurvive(p_366632_, p_49333_)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(p_49329_, p_366632_, p_363595_, p_49333_, p_49330_, p_49334_, p_49331_, p_370163_);
    }

    @Override
    protected boolean canSurvive(BlockState p_49325_, LevelReader p_49326_, BlockPos p_49327_) {
        BlockPos blockpos = p_49327_.below();
        return canSupportRigidBlock(p_49326_, blockpos) || canSupportCenter(p_49326_, blockpos, Direction.UP);
    }

    @Override
    protected void tick(BlockState p_220768_, ServerLevel p_220769_, BlockPos p_220770_, RandomSource p_220771_) {
        int i = this.getSignalForState(p_220768_);
        if (i > 0) {
            this.checkPressed(null, p_220769_, p_220770_, p_220768_, i);
        }
    }

    @Override
    protected void entityInside(BlockState p_49314_, Level p_49315_, BlockPos p_49316_, Entity p_49317_) {
        if (!p_49315_.isClientSide) {
            int i = this.getSignalForState(p_49314_);
            if (i == 0) {
                this.checkPressed(p_49317_, p_49315_, p_49316_, p_49314_, i);
            }
        }
    }

    private void checkPressed(@Nullable Entity p_152144_, Level p_152145_, BlockPos p_152146_, BlockState p_152147_, int p_152148_) {
        int i = this.getSignalStrength(p_152145_, p_152146_);
        boolean flag = p_152148_ > 0;
        boolean flag1 = i > 0;
        if (p_152148_ != i) {
            BlockState blockstate = this.setSignalForState(p_152147_, i);
            p_152145_.setBlock(p_152146_, blockstate, 2);
            this.updateNeighbours(p_152145_, p_152146_);
            p_152145_.setBlocksDirty(p_152146_, p_152147_, blockstate);
        }

        if (!flag1 && flag) {
            p_152145_.playSound(null, p_152146_, this.type.pressurePlateClickOff(), SoundSource.BLOCKS);
            p_152145_.gameEvent(p_152144_, GameEvent.BLOCK_DEACTIVATE, p_152146_);
        } else if (flag1 && !flag) {
            p_152145_.playSound(null, p_152146_, this.type.pressurePlateClickOn(), SoundSource.BLOCKS);
            p_152145_.gameEvent(p_152144_, GameEvent.BLOCK_ACTIVATE, p_152146_);
        }

        if (flag1) {
            p_152145_.scheduleTick(new BlockPos(p_152146_), this, this.getPressedTime());
        }
    }

    @Override
    protected void onRemove(BlockState p_49319_, Level p_49320_, BlockPos p_49321_, BlockState p_49322_, boolean p_49323_) {
        if (!p_49323_ && !p_49319_.is(p_49322_.getBlock())) {
            if (this.getSignalForState(p_49319_) > 0) {
                this.updateNeighbours(p_49320_, p_49321_);
            }

            super.onRemove(p_49319_, p_49320_, p_49321_, p_49322_, p_49323_);
        }
    }

    protected void updateNeighbours(Level p_49292_, BlockPos p_49293_) {
        p_49292_.updateNeighborsAt(p_49293_, this);
        p_49292_.updateNeighborsAt(p_49293_.below(), this);
    }

    @Override
    protected int getSignal(BlockState p_49309_, BlockGetter p_49310_, BlockPos p_49311_, Direction p_49312_) {
        return this.getSignalForState(p_49309_);
    }

    @Override
    protected int getDirectSignal(BlockState p_49346_, BlockGetter p_49347_, BlockPos p_49348_, Direction p_49349_) {
        return p_49349_ == Direction.UP ? this.getSignalForState(p_49346_) : 0;
    }

    @Override
    protected boolean isSignalSource(BlockState p_49351_) {
        return true;
    }

    protected static int getEntityCount(Level p_289656_, AABB p_289647_, Class<? extends Entity> p_289686_) {
        return p_289656_.getEntitiesOfClass(p_289686_, p_289647_, EntitySelector.NO_SPECTATORS.and(p_289691_ -> !p_289691_.isIgnoringBlockTriggers())).size();
    }

    protected abstract int getSignalStrength(Level p_49336_, BlockPos p_49337_);

    protected abstract int getSignalForState(BlockState p_49354_);

    protected abstract BlockState setSignalForState(BlockState p_49301_, int p_49302_);
}