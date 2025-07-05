package net.minecraft.client.gui;

import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ItemSlotMouseAction {
    boolean matches(Slot p_365739_);

    boolean onMouseScrolled(double p_370036_, double p_361041_, int p_368981_, ItemStack p_367075_);

    void onStopHovering(Slot p_367712_);

    void onSlotClicked(Slot p_363429_, ClickType p_362059_);
}