package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingBlockRenderState extends EntityRenderState implements BlockAndTintGetter {
    public BlockPos startBlockPos = BlockPos.ZERO;
    public BlockPos blockPos = BlockPos.ZERO;
    public BlockState blockState = Blocks.SAND.defaultBlockState();
    @Nullable
    public Holder<Biome> biome;
    public BlockAndTintGetter level = EmptyBlockAndTintGetter.INSTANCE;

    @Override
    public float getShade(Direction p_361965_, boolean p_367683_) {
        return this.level.getShade(p_361965_, p_367683_);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos p_368873_, ColorResolver p_367190_) {
        return this.biome == null ? -1 : p_367190_.getColor(this.biome.value(), (double)p_368873_.getX(), (double)p_368873_.getZ());
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos p_360711_) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos p_362889_) {
        return p_362889_.equals(this.blockPos) ? this.blockState : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos p_364632_) {
        return this.getBlockState(p_364632_).getFluidState();
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public int getMinY() {
        return this.blockPos.getY();
    }
}