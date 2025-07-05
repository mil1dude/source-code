package net.minecraft.world.item.equipment;

import java.util.EnumMap;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;

public interface ArmorMaterials {
    ArmorMaterial LEATHER = new ArmorMaterial(5, Util.make(new EnumMap<>(ArmorType.class), p_364478_ -> {
        p_364478_.put(ArmorType.BOOTS, 1);
        p_364478_.put(ArmorType.LEGGINGS, 2);
        p_364478_.put(ArmorType.CHESTPLATE, 3);
        p_364478_.put(ArmorType.HELMET, 1);
        p_364478_.put(ArmorType.BODY, 3);
    }), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, ItemTags.REPAIRS_LEATHER_ARMOR, EquipmentAssets.LEATHER);
    ArmorMaterial CHAINMAIL = new ArmorMaterial(15, Util.make(new EnumMap<>(ArmorType.class), p_366410_ -> {
        p_366410_.put(ArmorType.BOOTS, 1);
        p_366410_.put(ArmorType.LEGGINGS, 4);
        p_366410_.put(ArmorType.CHESTPLATE, 5);
        p_366410_.put(ArmorType.HELMET, 2);
        p_366410_.put(ArmorType.BODY, 4);
    }), 12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, ItemTags.REPAIRS_CHAIN_ARMOR, EquipmentAssets.CHAINMAIL);
    ArmorMaterial IRON = new ArmorMaterial(15, Util.make(new EnumMap<>(ArmorType.class), p_363464_ -> {
        p_363464_.put(ArmorType.BOOTS, 2);
        p_363464_.put(ArmorType.LEGGINGS, 5);
        p_363464_.put(ArmorType.CHESTPLATE, 6);
        p_363464_.put(ArmorType.HELMET, 2);
        p_363464_.put(ArmorType.BODY, 5);
    }), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, ItemTags.REPAIRS_IRON_ARMOR, EquipmentAssets.IRON);
    ArmorMaterial GOLD = new ArmorMaterial(7, Util.make(new EnumMap<>(ArmorType.class), p_363003_ -> {
        p_363003_.put(ArmorType.BOOTS, 1);
        p_363003_.put(ArmorType.LEGGINGS, 3);
        p_363003_.put(ArmorType.CHESTPLATE, 5);
        p_363003_.put(ArmorType.HELMET, 2);
        p_363003_.put(ArmorType.BODY, 7);
    }), 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, ItemTags.REPAIRS_GOLD_ARMOR, EquipmentAssets.GOLD);
    ArmorMaterial DIAMOND = new ArmorMaterial(33, Util.make(new EnumMap<>(ArmorType.class), p_367072_ -> {
        p_367072_.put(ArmorType.BOOTS, 3);
        p_367072_.put(ArmorType.LEGGINGS, 6);
        p_367072_.put(ArmorType.CHESTPLATE, 8);
        p_367072_.put(ArmorType.HELMET, 3);
        p_367072_.put(ArmorType.BODY, 11);
    }), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, ItemTags.REPAIRS_DIAMOND_ARMOR, EquipmentAssets.DIAMOND);
    ArmorMaterial TURTLE_SCUTE = new ArmorMaterial(25, Util.make(new EnumMap<>(ArmorType.class), p_368874_ -> {
        p_368874_.put(ArmorType.BOOTS, 2);
        p_368874_.put(ArmorType.LEGGINGS, 5);
        p_368874_.put(ArmorType.CHESTPLATE, 6);
        p_368874_.put(ArmorType.HELMET, 2);
        p_368874_.put(ArmorType.BODY, 5);
    }), 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, ItemTags.REPAIRS_TURTLE_HELMET, EquipmentAssets.TURTLE_SCUTE);
    ArmorMaterial NETHERITE = new ArmorMaterial(37, Util.make(new EnumMap<>(ArmorType.class), p_360716_ -> {
        p_360716_.put(ArmorType.BOOTS, 3);
        p_360716_.put(ArmorType.LEGGINGS, 6);
        p_360716_.put(ArmorType.CHESTPLATE, 8);
        p_360716_.put(ArmorType.HELMET, 3);
        p_360716_.put(ArmorType.BODY, 11);
    }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, ItemTags.REPAIRS_NETHERITE_ARMOR, EquipmentAssets.NETHERITE);
    ArmorMaterial ARMADILLO_SCUTE = new ArmorMaterial(4, Util.make(new EnumMap<>(ArmorType.class), p_366176_ -> {
        p_366176_.put(ArmorType.BOOTS, 3);
        p_366176_.put(ArmorType.LEGGINGS, 6);
        p_366176_.put(ArmorType.CHESTPLATE, 8);
        p_366176_.put(ArmorType.HELMET, 3);
        p_366176_.put(ArmorType.BODY, 11);
    }), 10, SoundEvents.ARMOR_EQUIP_WOLF, 0.0F, 0.0F, ItemTags.REPAIRS_WOLF_ARMOR, EquipmentAssets.ARMADILLO_SCUTE);
}