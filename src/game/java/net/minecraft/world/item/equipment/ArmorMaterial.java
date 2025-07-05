package net.minecraft.world.item.equipment;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ArmorMaterial(
    int durability,
    Map<ArmorType, Integer> defense,
    int enchantmentValue,
    Holder<SoundEvent> equipSound,
    float toughness,
    float knockbackResistance,
    TagKey<Item> repairIngredient,
    ResourceKey<EquipmentAsset> assetId
) {
    public Item.Properties humanoidProperties(Item.Properties p_365115_, ArmorType p_369272_) {
        return p_365115_.durability(p_369272_.getDurability(this.durability))
            .attributes(this.createAttributes(p_369272_))
            .enchantable(this.enchantmentValue)
            .component(DataComponents.EQUIPPABLE, Equippable.builder(p_369272_.getSlot()).setEquipSound(this.equipSound).setAsset(this.assetId).build())
            .repairable(this.repairIngredient);
    }

    public Item.Properties animalProperties(Item.Properties p_364372_, HolderSet<EntityType<?>> p_370025_) {
        return p_364372_.durability(ArmorType.BODY.getDurability(this.durability))
            .attributes(this.createAttributes(ArmorType.BODY))
            .repairable(this.repairIngredient)
            .component(
                DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.BODY).setEquipSound(this.equipSound).setAsset(this.assetId).setAllowedEntities(p_370025_).build()
            );
    }

    public Item.Properties animalProperties(Item.Properties p_369061_, Holder<SoundEvent> p_362547_, boolean p_365564_, HolderSet<EntityType<?>> p_362091_) {
        if (p_365564_) {
            p_369061_ = p_369061_.durability(ArmorType.BODY.getDurability(this.durability)).repairable(this.repairIngredient);
        }

        return p_369061_.attributes(this.createAttributes(ArmorType.BODY))
            .component(
                DataComponents.EQUIPPABLE,
                Equippable.builder(EquipmentSlot.BODY).setEquipSound(p_362547_).setAsset(this.assetId).setAllowedEntities(p_362091_).setDamageOnHurt(p_365564_).build()
            );
    }

    private ItemAttributeModifiers createAttributes(ArmorType p_361798_) {
        int i = this.defense.getOrDefault(p_361798_, 0);
        ItemAttributeModifiers.Builder itemattributemodifiers$builder = ItemAttributeModifiers.builder();
        EquipmentSlotGroup equipmentslotgroup = EquipmentSlotGroup.bySlot(p_361798_.getSlot());
        ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("armor." + p_361798_.getName());
        itemattributemodifiers$builder.add(
            Attributes.ARMOR, new AttributeModifier(resourcelocation, (double)i, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup
        );
        itemattributemodifiers$builder.add(
            Attributes.ARMOR_TOUGHNESS, new AttributeModifier(resourcelocation, (double)this.toughness, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup
        );
        if (this.knockbackResistance > 0.0F) {
            itemattributemodifiers$builder.add(
                Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(resourcelocation, (double)this.knockbackResistance, AttributeModifier.Operation.ADD_VALUE), equipmentslotgroup
            );
        }

        return itemattributemodifiers$builder.build();
    }
}