package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SwordItem extends Item {
    public SwordItem(ToolMaterial p_365226_, float p_361711_, float p_367826_, Item.Properties p_43272_) {
        super(p_365226_.applySwordProperties(p_43272_, p_361711_, p_367826_));
    }

    @Override
    public boolean canAttackBlock(BlockState p_43291_, Level p_43292_, BlockPos p_43293_, Player p_43294_) {
        return !p_43294_.isCreative();
    }

    @Override
    public boolean hurtEnemy(ItemStack p_43278_, LivingEntity p_43279_, LivingEntity p_43280_) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack p_342189_, LivingEntity p_344347_, LivingEntity p_343888_) {
        p_342189_.hurtAndBreak(1, p_343888_, EquipmentSlot.MAINHAND);
    }
}