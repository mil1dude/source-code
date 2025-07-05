package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaterlilyBlock extends BushBlock {
    public static final MapCodec<WaterlilyBlock> CODEC = simpleCodec(WaterlilyBlock::new);
    protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.5, 15.0);

    @Override
    public MapCodec<WaterlilyBlock> codec() {
        return CODEC;
    }

    protected WaterlilyBlock(BlockBehaviour.Properties p_58162_) {
        super(p_58162_);
    }

    @Override
    protected void entityInside(BlockState p_58164_, Level p_58165_, BlockPos p_58166_, Entity p_58167_) {
        super.entityInside(p_58164_, p_58165_, p_58166_, p_58167_);
        if (p_58165_ instanceof ServerLevel && p_58167_ instanceof AbstractBoat) {
            p_58165_.destroyBlock(new BlockPos(p_58166_), true, p_58167_);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState p_58169_, BlockGetter p_58170_, BlockPos p_58171_, CollisionContext p_58172_) {
        return AABB;
    }

    @Override
    protected boolean mayPlaceOn(BlockState p_58174_, BlockGetter p_58175_, BlockPos p_58176_) {
        FluidState fluidstate = p_58175_.getFluidState(p_58176_);
        FluidState fluidstate1 = p_58175_.getFluidState(p_58176_.above());
        return (fluidstate.getType() == Fluids.WATER || p_58174_.getBlock() instanceof IceBlock) && fluidstate1.getType() == Fluids.EMPTY;
    }
}