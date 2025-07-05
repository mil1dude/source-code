package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DeadBushBlock extends BushBlock {
    public static final MapCodec<DeadBushBlock> CODEC = simpleCodec(DeadBushBlock::new);
    protected static final float AABB_OFFSET = 6.0F;
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    @Override
    public MapCodec<DeadBushBlock> codec() {
        return CODEC;
    }

    protected DeadBushBlock(BlockBehaviour.Properties p_52417_) {
        super(p_52417_);
    }

    @Override
    protected VoxelShape getShape(BlockState p_52419_, BlockGetter p_52420_, BlockPos p_52421_, CollisionContext p_52422_) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState p_52424_, BlockGetter p_52425_, BlockPos p_52426_) {
        return p_52424_.is(BlockTags.DEAD_BUSH_MAY_PLACE_ON);
    }
}