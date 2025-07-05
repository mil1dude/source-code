package net.minecraft.world.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.equipment.ArmorMaterial;

public class AnimalArmorItem extends Item {
    private final AnimalArmorItem.BodyType bodyType;

    public AnimalArmorItem(ArmorMaterial p_368735_, AnimalArmorItem.BodyType p_330915_, Item.Properties p_333708_) {
        super(p_368735_.animalProperties(p_333708_, p_330915_.allowedEntities));
        this.bodyType = p_330915_;
    }

    public AnimalArmorItem(
        ArmorMaterial p_363104_, AnimalArmorItem.BodyType p_366023_, Holder<SoundEvent> p_362751_, boolean p_365968_, Item.Properties p_369550_
    ) {
        super(p_363104_.animalProperties(p_369550_, p_362751_, p_365968_, p_366023_.allowedEntities));
        this.bodyType = p_366023_;
    }

    @Override
    public SoundEvent getBreakingSound() {
        return this.bodyType.breakingSound;
    }

    public static enum BodyType {
        EQUESTRIAN(SoundEvents.ITEM_BREAK, EntityType.HORSE),
        CANINE(SoundEvents.WOLF_ARMOR_BREAK, EntityType.WOLF);

        final SoundEvent breakingSound;
        final HolderSet<EntityType<?>> allowedEntities;

        private BodyType(final SoundEvent p_335661_, final EntityType<?>... p_364750_) {
            this.breakingSound = p_335661_;
            this.allowedEntities = HolderSet.direct(EntityType::builtInRegistryHolder, p_364750_);
        }
    }
}