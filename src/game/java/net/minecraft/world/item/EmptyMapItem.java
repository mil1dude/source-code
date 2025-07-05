package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EmptyMapItem extends Item {
    public EmptyMapItem(Item.Properties p_41143_) {
        super(p_41143_);
    }

    @Override
    public InteractionResult use(Level p_41145_, Player p_41146_, InteractionHand p_41147_) {
        ItemStack itemstack = p_41146_.getItemInHand(p_41147_);
        if (p_41145_.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            itemstack.consume(1, p_41146_);
            p_41146_.awardStat(Stats.ITEM_USED.get(this));
            p_41146_.level().playSound(null, p_41146_, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, p_41146_.getSoundSource(), 1.0F, 1.0F);
            ItemStack itemstack1 = MapItem.create(p_41145_, p_41146_.getBlockX(), p_41146_.getBlockZ(), (byte)0, true, false);
            if (itemstack.isEmpty()) {
                return InteractionResult.SUCCESS.heldItemTransformedTo(itemstack1);
            } else {
                if (!p_41146_.getInventory().add(itemstack1.copy())) {
                    p_41146_.drop(itemstack1, false);
                }

                return InteractionResult.SUCCESS;
            }
        }
    }
}