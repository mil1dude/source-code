package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KelpBlock extends GrowingPlantHeadBlock implements LiquidBlockContainer {
    public static final MapCodec<KelpBlock> CODEC = simpleCodec(KelpBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);
    private static final double GROW_PER_TICK_PROBABILITY = 0.14;

    @Override
    public MapCodec<KelpBlock> codec() {
        return CODEC;
    }

    protected KelpBlock(BlockBehaviour.Properties p_54300_) {
        super(p_54300_, Direction.UP, SHAPE, true, 0.14);
    }

    @Override
    protected boolean canGrowInto(BlockState p_54321_) {
        return p_54321_.is(Blocks.WATER);
    }

    @Override
    protected Block getBodyBlock() {
        return Blocks.KELP_PLANT;
    }

    @Override
    protected boolean canAttachTo(BlockState p_153455_) {
        return !p_153455_.is(Blocks.MAGMA_BLOCK);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player p_299149_, BlockGetter p_54304_, BlockPos p_54305_, BlockState p_54306_, Fluid p_54307_) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor p_54309_, BlockPos p_54310_, BlockState p_54311_, FluidState p_54312_) {
        return false;
    }

    @Override
    protected int getBlocksToGrowWhenBonemealed(RandomSource p_221366_) {
        return 1;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_54302_) {
        FluidState fluidstate = p_54302_.getLevel().getFluidState(p_54302_.getClickedPos());
        return fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8 ? super.getStateForPlacement(p_54302_) : null;
    }

    @Override
    protected FluidState getFluidState(BlockState p_54319_) {
        return Fluids.WATER.getSource(false);
    }
}