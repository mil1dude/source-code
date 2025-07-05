package net.minecraft.world.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class DiscFragmentItem extends Item {
    public DiscFragmentItem(Item.Properties p_220029_) {
        super(p_220029_);
    }

    @Override
    public void appendHoverText(ItemStack p_220031_, Item.TooltipContext p_327830_, List<Component> p_220033_, TooltipFlag p_220034_) {
        p_220033_.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(this.descriptionId + ".desc");
    }
}