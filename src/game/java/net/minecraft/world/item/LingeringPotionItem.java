package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

public class LingeringPotionItem extends ThrowablePotionItem {
    public LingeringPotionItem(Item.Properties p_42836_) {
        super(p_42836_);
    }

    @Override
    public void appendHoverText(ItemStack p_42838_, Item.TooltipContext p_327733_, List<Component> p_42840_, TooltipFlag p_42841_) {
        PotionContents potioncontents = p_42838_.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        potioncontents.addPotionTooltip(p_42840_::add, 0.25F, p_327733_.tickRate());
    }

    @Override
    public InteractionResult use(Level p_42843_, Player p_42844_, InteractionHand p_42845_) {
        p_42843_.playSound(
            null,
            p_42844_.getX(),
            p_42844_.getY(),
            p_42844_.getZ(),
            SoundEvents.LINGERING_POTION_THROW,
            SoundSource.NEUTRAL,
            0.5F,
            0.4F / (p_42843_.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        return super.use(p_42843_, p_42844_, p_42845_);
    }
}