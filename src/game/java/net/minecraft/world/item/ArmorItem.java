package net.minecraft.world.item;

import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class ArmorItem extends Item {
    public ArmorItem(ArmorMaterial p_364741_, ArmorType p_365617_, Item.Properties p_40388_) {
        super(p_364741_.humanoidProperties(p_40388_, p_365617_));
    }
}