package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.FireworkExplosion;

public class FireworkStarItem extends Item {
    public FireworkStarItem(Item.Properties p_41248_) {
        super(p_41248_);
    }

    @Override
    public void appendHoverText(ItemStack p_332511_, Item.TooltipContext p_330557_, List<Component> p_41258_, TooltipFlag p_328121_) {
        FireworkExplosion fireworkexplosion = p_332511_.get(DataComponents.FIREWORK_EXPLOSION);
        if (fireworkexplosion != null) {
            fireworkexplosion.addToTooltip(p_330557_, p_41258_::add, p_328121_);
        }
    }
}