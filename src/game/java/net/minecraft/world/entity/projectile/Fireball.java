package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class Fireball extends AbstractHurtingProjectile implements ItemSupplier {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(Fireball.class, EntityDataSerializers.ITEM_STACK);

    public Fireball(EntityType<? extends Fireball> p_37006_, Level p_37007_) {
        super(p_37006_, p_37007_);
    }

    public Fireball(EntityType<? extends Fireball> p_36990_, double p_36991_, double p_36992_, double p_36993_, Vec3 p_342452_, Level p_36997_) {
        super(p_36990_, p_36991_, p_36992_, p_36993_, p_342452_, p_36997_);
    }

    public Fireball(EntityType<? extends Fireball> p_36999_, LivingEntity p_37000_, Vec3 p_342508_, Level p_37004_) {
        super(p_36999_, p_37000_, p_342508_, p_37004_);
    }

    public void setItem(ItemStack p_37011_) {
        if (p_37011_.isEmpty()) {
            this.getEntityData().set(DATA_ITEM_STACK, this.getDefaultItem());
        } else {
            this.getEntityData().set(DATA_ITEM_STACK, p_37011_.copyWithCount(1));
        }
    }

    @Override
    protected void playEntityOnFireExtinguishedSound() {
    }

    @Override
    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_330316_) {
        p_330316_.define(DATA_ITEM_STACK, this.getDefaultItem());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_37013_) {
        super.addAdditionalSaveData(p_37013_);
        p_37013_.put("Item", this.getItem().save(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_37009_) {
        super.readAdditionalSaveData(p_37009_);
        if (p_37009_.contains("Item", 10)) {
            this.setItem(ItemStack.parse(this.registryAccess(), p_37009_.getCompound("Item")).orElse(this.getDefaultItem()));
        } else {
            this.setItem(this.getDefaultItem());
        }
    }

    private ItemStack getDefaultItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    public SlotAccess getSlot(int p_332914_) {
        return p_332914_ == 0 ? SlotAccess.of(this::getItem, this::setItem) : super.getSlot(p_332914_);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_363649_) {
        return this.tickCount < 2 && p_363649_ < 12.25 ? false : super.shouldRenderAtSqrDistance(p_363649_);
    }
}