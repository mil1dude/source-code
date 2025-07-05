package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ShearsItem extends Item {
    public ShearsItem(Item.Properties p_43074_) {
        super(p_43074_);
    }

    public static Tool createToolProperties() {
        HolderGetter<Block> holdergetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return new Tool(
            List.of(
                Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
                Tool.Rule.overrideSpeed(holdergetter.getOrThrow(BlockTags.LEAVES), 15.0F),
                Tool.Rule.overrideSpeed(holdergetter.getOrThrow(BlockTags.WOOL), 5.0F),
                Tool.Rule.overrideSpeed(HolderSet.direct(Blocks.VINE.builtInRegistryHolder(), Blocks.GLOW_LICHEN.builtInRegistryHolder()), 2.0F)
            ),
            1.0F,
            1
        );
    }

    @Override
    public boolean mineBlock(ItemStack p_43078_, Level p_43079_, BlockState p_43080_, BlockPos p_43081_, LivingEntity p_43082_) {
        if (!p_43079_.isClientSide && !p_43080_.is(BlockTags.FIRE)) {
            p_43078_.hurtAndBreak(1, p_43082_, EquipmentSlot.MAINHAND);
        }

        return p_43080_.is(BlockTags.LEAVES)
            || p_43080_.is(Blocks.COBWEB)
            || p_43080_.is(Blocks.SHORT_GRASS)
            || p_43080_.is(Blocks.FERN)
            || p_43080_.is(Blocks.DEAD_BUSH)
            || p_43080_.is(Blocks.HANGING_ROOTS)
            || p_43080_.is(Blocks.VINE)
            || p_43080_.is(Blocks.TRIPWIRE)
            || p_43080_.is(BlockTags.WOOL);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_186371_) {
        Level level = p_186371_.getLevel();
        BlockPos blockpos = p_186371_.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.getBlock() instanceof GrowingPlantHeadBlock growingplantheadblock && !growingplantheadblock.isMaxAge(blockstate)) {
            Player player = p_186371_.getPlayer();
            ItemStack itemstack = p_186371_.getItemInHand();
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
            }

            level.playSound(player, blockpos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
            BlockState blockstate1 = growingplantheadblock.getMaxAgeState(blockstate);
            level.setBlockAndUpdate(blockpos, blockstate1);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(p_186371_.getPlayer(), blockstate1));
            if (player != null) {
                itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(p_186371_.getHand()));
            }

            return InteractionResult.SUCCESS;
        }

        return super.useOn(p_186371_);
    }
}