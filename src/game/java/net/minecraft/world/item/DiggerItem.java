package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;

public class DiggerItem extends Item {
    protected DiggerItem(ToolMaterial p_367988_, TagKey<Block> p_204111_, float p_363669_, float p_364017_, Item.Properties p_204112_) {
        super(p_367988_.applyToolProperties(p_204112_, p_204111_, p_363669_, p_364017_));
    }

    @Override
    public boolean hurtEnemy(ItemStack p_40994_, LivingEntity p_40995_, LivingEntity p_40996_) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack p_345276_, LivingEntity p_342379_, LivingEntity p_342949_) {
        p_345276_.hurtAndBreak(2, p_342949_, EquipmentSlot.MAINHAND);
    }
}