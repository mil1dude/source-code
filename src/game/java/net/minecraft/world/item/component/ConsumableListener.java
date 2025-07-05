package net.minecraft.world.item.component;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ConsumableListener {
    void onConsume(Level p_362220_, LivingEntity p_367275_, ItemStack p_365737_, Consumable p_369614_);
}