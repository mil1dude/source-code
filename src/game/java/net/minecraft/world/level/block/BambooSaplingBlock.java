package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BambooSaplingBlock extends Block implements BonemealableBlock {
    public static final MapCodec<BambooSaplingBlock> CODEC = simpleCodec(BambooSaplingBlock::new);
    protected static final float SAPLING_AABB_OFFSET = 4.0F;
    protected static final VoxelShape SAPLING_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0);

    @Override
    public MapCodec<BambooSaplingBlock> codec() {
        return CODEC;
    }

    public BambooSaplingBlock(BlockBehaviour.Properties p_48957_) {
        super(p_48957_);
    }

    @Override
    protected VoxelShape getShape(BlockState p_49003_, BlockGetter p_49004_, BlockPos p_49005_, CollisionContext p_49006_) {
        Vec3 vec3 = p_49003_.getOffset(p_49005_);
        return SAPLING_SHAPE.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    protected void randomTick(BlockState p_220753_, ServerLevel p_220754_, BlockPos p_220755_, RandomSource p_220756_) {
        if (p_220756_.nextInt(3) == 0 && p_220754_.isEmptyBlock(p_220755_.above()) && p_220754_.getRawBrightness(p_220755_.above(), 0) >= 9) {
            this.growBamboo(p_220754_, p_220755_);
        }
    }

    @Override
    protected boolean canSurvive(BlockState p_48986_, LevelReader p_48987_, BlockPos p_48988_) {
        return p_48987_.getBlockState(p_48988_.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
    }

    @Override
    protected BlockState updateShape(
        BlockState p_48990_,
        LevelReader p_366200_,
        ScheduledTickAccess p_365833_,
        BlockPos p_48994_,
        Direction p_48991_,
        BlockPos p_48995_,
        BlockState p_48992_,
        RandomSource p_365774_
    ) {
        if (!p_48990_.canSurvive(p_366200_, p_48994_)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            return p_48991_ == Direction.UP && p_48992_.is(Blocks.BAMBOO)
                ? Blocks.BAMBOO.defaultBlockState()
                : super.updateShape(p_48990_, p_366200_, p_365833_, p_48994_, p_48991_, p_48995_, p_48992_, p_365774_);
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader p_312659_, BlockPos p_48965_, BlockState p_48966_, boolean p_376584_) {
        return new ItemStack(Items.BAMBOO);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader p_256136_, BlockPos p_256527_, BlockState p_255620_) {
        return p_256136_.getBlockState(p_256527_.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level p_220748_, RandomSource p_220749_, BlockPos p_220750_, BlockState p_220751_) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel p_220743_, RandomSource p_220744_, BlockPos p_220745_, BlockState p_220746_) {
        this.growBamboo(p_220743_, p_220745_);
    }

    @Override
    protected float getDestroyProgress(BlockState p_48981_, Player p_48982_, BlockGetter p_48983_, BlockPos p_48984_) {
        return p_48982_.getMainHandItem().getItem() instanceof SwordItem ? 1.0F : super.getDestroyProgress(p_48981_, p_48982_, p_48983_, p_48984_);
    }

    protected void growBamboo(Level p_48973_, BlockPos p_48974_) {
        p_48973_.setBlock(p_48974_.above(), Blocks.BAMBOO.defaultBlockState().setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL), 3);
    }
}