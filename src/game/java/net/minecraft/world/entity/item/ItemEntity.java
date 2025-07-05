package net.minecraft.world.entity.item;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public class ItemEntity extends Entity implements TraceableEntity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final float FLOAT_HEIGHT = 0.1F;
    public static final float EYE_HEIGHT = 0.2125F;
    private static final int LIFETIME = 6000;
    private static final int INFINITE_PICKUP_DELAY = 32767;
    private static final int INFINITE_LIFETIME = -32768;
    private int age;
    private int pickupDelay;
    private int health = 5;
    @Nullable
    private UUID thrower;
    @Nullable
    private Entity cachedThrower;
    @Nullable
    private UUID target;
    public final float bobOffs;

    public ItemEntity(EntityType<? extends ItemEntity> p_31991_, Level p_31992_) {
        super(p_31991_, p_31992_);
        this.bobOffs = this.random.nextFloat() * (float) Math.PI * 2.0F;
        this.setYRot(this.random.nextFloat() * 360.0F);
    }

    public ItemEntity(Level p_32001_, double p_32002_, double p_32003_, double p_32004_, ItemStack p_32005_) {
        this(p_32001_, p_32002_, p_32003_, p_32004_, p_32005_, p_32001_.random.nextDouble() * 0.2 - 0.1, 0.2, p_32001_.random.nextDouble() * 0.2 - 0.1);
    }

    public ItemEntity(
        Level p_149663_, double p_149664_, double p_149665_, double p_149666_, ItemStack p_149667_, double p_149668_, double p_149669_, double p_149670_
    ) {
        this(EntityType.ITEM, p_149663_);
        this.setPos(p_149664_, p_149665_, p_149666_);
        this.setDeltaMovement(p_149668_, p_149669_, p_149670_);
        this.setItem(p_149667_);
    }

    private ItemEntity(ItemEntity p_31994_) {
        super(p_31994_.getType(), p_31994_.level());
        this.setItem(p_31994_.getItem().copy());
        this.copyPosition(p_31994_);
        this.age = p_31994_.age;
        this.bobOffs = p_31994_.bobOffs;
    }

    @Override
    public boolean dampensVibrations() {
        return this.getItem().is(ItemTags.DAMPENS_VIBRATIONS);
    }

    @Nullable
    @Override
    public Entity getOwner() {
        if (this.cachedThrower != null && !this.cachedThrower.isRemoved()) {
            return this.cachedThrower;
        } else if (this.thrower != null && this.level() instanceof ServerLevel serverlevel) {
            this.cachedThrower = serverlevel.getEntity(this.thrower);
            return this.cachedThrower;
        } else {
            return null;
        }
    }

    @Override
    public void restoreFrom(Entity p_309647_) {
        super.restoreFrom(p_309647_);
        if (p_309647_ instanceof ItemEntity itementity) {
            this.cachedThrower = itementity.cachedThrower;
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_332164_) {
        p_332164_.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        if (this.getItem().isEmpty()) {
            this.discard();
        } else {
            super.tick();
            if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
                this.pickupDelay--;
            }

            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            Vec3 vec3 = this.getDeltaMovement();
            if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > 0.1F) {
                this.setUnderwaterMovement();
            } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > 0.1F) {
                this.setUnderLavaMovement();
            } else {
                this.applyGravity();
            }

            if (this.level().isClientSide) {
                this.noPhysics = false;
            } else {
                this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7));
                if (this.noPhysics) {
                    this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
                }
            }

            if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.applyEffectsFromBlocks();
                float f = 0.98F;
                if (this.onGround()) {
                    f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98F;
                }

                this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.98, (double)f));
                if (this.onGround()) {
                    Vec3 vec31 = this.getDeltaMovement();
                    if (vec31.y < 0.0) {
                        this.setDeltaMovement(vec31.multiply(1.0, -0.5, 1.0));
                    }
                }
            }

            boolean flag = Mth.floor(this.xo) != Mth.floor(this.getX())
                || Mth.floor(this.yo) != Mth.floor(this.getY())
                || Mth.floor(this.zo) != Mth.floor(this.getZ());
            int i = flag ? 2 : 40;
            if (this.tickCount % i == 0 && !this.level().isClientSide && this.isMergable()) {
                this.mergeWithNeighbours();
            }

            if (this.age != -32768) {
                this.age++;
            }

            this.hasImpulse = this.hasImpulse | this.updateInWaterStateAndDoFluidPushing();
            if (!this.level().isClientSide) {
                double d0 = this.getDeltaMovement().subtract(vec3).lengthSqr();
                if (d0 > 0.01) {
                    this.hasImpulse = true;
                }
            }

            if (!this.level().isClientSide && this.age >= 6000) {
                this.discard();
            }
        }
    }

    @Override
    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999F);
    }

    private void setUnderwaterMovement() {
        this.setFluidMovement(0.99F);
    }

    private void setUnderLavaMovement() {
        this.setFluidMovement(0.95F);
    }

    private void setFluidMovement(double p_360732_) {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * p_360732_, vec3.y + (double)(vec3.y < 0.06F ? 5.0E-4F : 0.0F), vec3.z * p_360732_);
    }

    private void mergeWithNeighbours() {
        if (this.isMergable()) {
            for (ItemEntity itementity : this.level()
                .getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5, 0.0, 0.5), p_186268_ -> p_186268_ != this && p_186268_.isMergable())) {
                if (itementity.isMergable()) {
                    this.tryToMerge(itementity);
                    if (this.isRemoved()) {
                        break;
                    }
                }
            }
        }
    }

    private boolean isMergable() {
        ItemStack itemstack = this.getItem();
        return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && itemstack.getCount() < itemstack.getMaxStackSize();
    }

    private void tryToMerge(ItemEntity p_32016_) {
        ItemStack itemstack = this.getItem();
        ItemStack itemstack1 = p_32016_.getItem();
        if (Objects.equals(this.target, p_32016_.target) && areMergable(itemstack, itemstack1)) {
            if (itemstack1.getCount() < itemstack.getCount()) {
                merge(this, itemstack, p_32016_, itemstack1);
            } else {
                merge(p_32016_, itemstack1, this, itemstack);
            }
        }
    }

    public static boolean areMergable(ItemStack p_32027_, ItemStack p_32028_) {
        return p_32028_.getCount() + p_32027_.getCount() > p_32028_.getMaxStackSize() ? false : ItemStack.isSameItemSameComponents(p_32027_, p_32028_);
    }

    public static ItemStack merge(ItemStack p_32030_, ItemStack p_32031_, int p_32032_) {
        int i = Math.min(Math.min(p_32030_.getMaxStackSize(), p_32032_) - p_32030_.getCount(), p_32031_.getCount());
        ItemStack itemstack = p_32030_.copyWithCount(p_32030_.getCount() + i);
        p_32031_.shrink(i);
        return itemstack;
    }

    private static void merge(ItemEntity p_32023_, ItemStack p_32024_, ItemStack p_32025_) {
        ItemStack itemstack = merge(p_32024_, p_32025_, 64);
        p_32023_.setItem(itemstack);
    }

    private static void merge(ItemEntity p_32018_, ItemStack p_32019_, ItemEntity p_32020_, ItemStack p_32021_) {
        merge(p_32018_, p_32019_, p_32021_);
        p_32018_.pickupDelay = Math.max(p_32018_.pickupDelay, p_32020_.pickupDelay);
        p_32018_.age = Math.min(p_32018_.age, p_32020_.age);
        if (p_32021_.isEmpty()) {
            p_32020_.discard();
        }
    }

    @Override
    public boolean fireImmune() {
        return !this.getItem().canBeHurtBy(this.damageSources().inFire()) || super.fireImmune();
    }

    @Override
    protected boolean shouldPlayLavaHurtSound() {
        return this.health <= 0 ? true : this.tickCount % 10 == 0;
    }

    @Override
    public final boolean hurtClient(DamageSource p_366723_) {
        return this.isInvulnerableToBase(p_366723_) ? false : this.getItem().canBeHurtBy(p_366723_);
    }

    @Override
    public final boolean hurtServer(ServerLevel p_362991_, DamageSource p_364841_, float p_362683_) {
        if (this.isInvulnerableToBase(p_364841_)) {
            return false;
        } else if (!p_362991_.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && p_364841_.getEntity() instanceof Mob) {
            return false;
        } else if (!this.getItem().canBeHurtBy(p_364841_)) {
            return false;
        } else {
            this.markHurt();
            this.health = (int)((float)this.health - p_362683_);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, p_364841_.getEntity());
            if (this.health <= 0) {
                this.getItem().onDestroyed(this);
                this.discard();
            }

            return true;
        }
    }

    @Override
    public boolean ignoreExplosion(Explosion p_369761_) {
        return p_369761_.shouldAffectBlocklikeEntities() ? super.ignoreExplosion(p_369761_) : true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_32050_) {
        p_32050_.putShort("Health", (short)this.health);
        p_32050_.putShort("Age", (short)this.age);
        p_32050_.putShort("PickupDelay", (short)this.pickupDelay);
        if (this.thrower != null) {
            p_32050_.putUUID("Thrower", this.thrower);
        }

        if (this.target != null) {
            p_32050_.putUUID("Owner", this.target);
        }

        if (!this.getItem().isEmpty()) {
            p_32050_.put("Item", this.getItem().save(this.registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_32034_) {
        this.health = p_32034_.getShort("Health");
        this.age = p_32034_.getShort("Age");
        if (p_32034_.contains("PickupDelay")) {
            this.pickupDelay = p_32034_.getShort("PickupDelay");
        }

        if (p_32034_.hasUUID("Owner")) {
            this.target = p_32034_.getUUID("Owner");
        }

        if (p_32034_.hasUUID("Thrower")) {
            this.thrower = p_32034_.getUUID("Thrower");
            this.cachedThrower = null;
        }

        if (p_32034_.contains("Item", 10)) {
            CompoundTag compoundtag = p_32034_.getCompound("Item");
            this.setItem(ItemStack.parse(this.registryAccess(), compoundtag).orElse(ItemStack.EMPTY));
        } else {
            this.setItem(ItemStack.EMPTY);
        }

        if (this.getItem().isEmpty()) {
            this.discard();
        }
    }

    @Override
    public void playerTouch(Player p_32040_) {
        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            Item item = itemstack.getItem();
            int i = itemstack.getCount();
            if (this.pickupDelay == 0 && (this.target == null || this.target.equals(p_32040_.getUUID())) && p_32040_.getInventory().add(itemstack)) {
                p_32040_.take(this, i);
                if (itemstack.isEmpty()) {
                    this.discard();
                    itemstack.setCount(i);
                }

                p_32040_.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
                p_32040_.onItemPickup(this);
            }
        }
    }

    @Override
    public Component getName() {
        Component component = this.getCustomName();
        return component != null ? component : this.getItem().getItemName();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Nullable
    @Override
    public Entity teleport(TeleportTransition p_365554_) {
        Entity entity = super.teleport(p_365554_);
        if (!this.level().isClientSide && entity instanceof ItemEntity itementity) {
            itementity.mergeWithNeighbours();
        }

        return entity;
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack p_32046_) {
        this.getEntityData().set(DATA_ITEM, p_32046_);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_32036_) {
        super.onSyncedDataUpdated(p_32036_);
        if (DATA_ITEM.equals(p_32036_)) {
            this.getItem().setEntityRepresentation(this);
        }
    }

    public void setTarget(@Nullable UUID p_266724_) {
        this.target = p_266724_;
    }

    public void setThrower(Entity p_310166_) {
        this.thrower = p_310166_.getUUID();
        this.cachedThrower = p_310166_;
    }

    public int getAge() {
        return this.age;
    }

    public void setDefaultPickUpDelay() {
        this.pickupDelay = 10;
    }

    public void setNoPickUpDelay() {
        this.pickupDelay = 0;
    }

    public void setNeverPickUp() {
        this.pickupDelay = 32767;
    }

    public void setPickUpDelay(int p_32011_) {
        this.pickupDelay = p_32011_;
    }

    public boolean hasPickUpDelay() {
        return this.pickupDelay > 0;
    }

    public void setUnlimitedLifetime() {
        this.age = -32768;
    }

    public void setExtendedLifetime() {
        this.age = -6000;
    }

    public void makeFakeItem() {
        this.setNeverPickUp();
        this.age = 5999;
    }

    public static float getSpin(float p_32009_, float p_369793_) {
        return p_32009_ / 20.0F + p_369793_;
    }

    public ItemEntity copy() {
        return new ItemEntity(this);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return 180.0F - getSpin((float)this.getAge() + 0.5F, this.bobOffs) / (float) (Math.PI * 2) * 360.0F;
    }

    @Override
    public SlotAccess getSlot(int p_329686_) {
        return p_329686_ == 0 ? SlotAccess.of(this::getItem, this::setItem) : super.getSlot(p_329686_);
    }
}