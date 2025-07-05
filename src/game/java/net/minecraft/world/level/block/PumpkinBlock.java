package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class PumpkinBlock extends Block {
    public static final MapCodec<PumpkinBlock> CODEC = simpleCodec(PumpkinBlock::new);

    @Override
    public MapCodec<PumpkinBlock> codec() {
        return CODEC;
    }

    protected PumpkinBlock(BlockBehaviour.Properties p_55284_) {
        super(p_55284_);
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack p_330568_, BlockState p_330263_, Level p_327756_, BlockPos p_328675_, Player p_334049_, InteractionHand p_331851_, BlockHitResult p_329008_
    ) {
        if (!p_330568_.is(Items.SHEARS)) {
            return super.useItemOn(p_330568_, p_330263_, p_327756_, p_328675_, p_334049_, p_331851_, p_329008_);
        } else if (p_327756_.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            Direction direction = p_329008_.getDirection();
            Direction direction1 = direction.getAxis() == Direction.Axis.Y ? p_334049_.getDirection().getOpposite() : direction;
            p_327756_.playSound(null, p_328675_, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
            p_327756_.setBlock(p_328675_, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, direction1), 11);
            ItemEntity itementity = new ItemEntity(
                p_327756_,
                (double)p_328675_.getX() + 0.5 + (double)direction1.getStepX() * 0.65,
                (double)p_328675_.getY() + 0.1,
                (double)p_328675_.getZ() + 0.5 + (double)direction1.getStepZ() * 0.65,
                new ItemStack(Items.PUMPKIN_SEEDS, 4)
            );
            itementity.setDeltaMovement(
                0.05 * (double)direction1.getStepX() + p_327756_.random.nextDouble() * 0.02,
                0.05,
                0.05 * (double)direction1.getStepZ() + p_327756_.random.nextDouble() * 0.02
            );
            p_327756_.addFreshEntity(itementity);
            p_330568_.hurtAndBreak(1, p_334049_, LivingEntity.getSlotForHand(p_331851_));
            p_327756_.gameEvent(p_334049_, GameEvent.SHEAR, p_328675_);
            p_334049_.awardStat(Stats.ITEM_USED.get(Items.SHEARS));
            return InteractionResult.SUCCESS;
        }
    }
}