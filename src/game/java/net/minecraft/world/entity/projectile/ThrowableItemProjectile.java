package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class ThrowableItemProjectile extends ThrowableProjectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(ThrowableItemProjectile.class, EntityDataSerializers.ITEM_STACK);

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> p_37442_, Level p_37443_) {
        super(p_37442_, p_37443_);
    }

    public ThrowableItemProjectile(
        EntityType<? extends ThrowableItemProjectile> p_37438_, double p_367916_, double p_363262_, double p_370181_, Level p_37440_, ItemStack p_366106_
    ) {
        super(p_37438_, p_367916_, p_363262_, p_370181_, p_37440_);
        this.setItem(p_366106_);
    }

    public ThrowableItemProjectile(EntityType<? extends ThrowableItemProjectile> p_37432_, LivingEntity p_364177_, Level p_37436_, ItemStack p_369279_) {
        this(p_37432_, p_364177_.getX(), p_364177_.getEyeY() - 0.1F, p_364177_.getZ(), p_37436_, p_369279_);
        this.setOwner(p_364177_);
    }

    public void setItem(ItemStack p_37447_) {
        this.getEntityData().set(DATA_ITEM_STACK, p_37447_.copyWithCount(1));
    }

    protected abstract Item getDefaultItem();

    @Override
    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_330671_) {
        p_330671_.define(DATA_ITEM_STACK, new ItemStack(this.getDefaultItem()));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_37449_) {
        super.addAdditionalSaveData(p_37449_);
        p_37449_.put("Item", this.getItem().save(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_37445_) {
        super.readAdditionalSaveData(p_37445_);
        if (p_37445_.contains("Item", 10)) {
            this.setItem(ItemStack.parse(this.registryAccess(), p_37445_.getCompound("Item")).orElseGet(() -> new ItemStack(this.getDefaultItem())));
        } else {
            this.setItem(new ItemStack(this.getDefaultItem()));
        }
    }
}