package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class Bogged extends AbstractSkeleton implements Shearable {
    private static final int HARD_ATTACK_INTERVAL = 50;
    private static final int NORMAL_ATTACK_INTERVAL = 70;
    private static final EntityDataAccessor<Boolean> DATA_SHEARED = SynchedEntityData.defineId(Bogged.class, EntityDataSerializers.BOOLEAN);
    public static final String SHEARED_TAG_NAME = "sheared";

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes().add(Attributes.MAX_HEALTH, 16.0);
    }

    public Bogged(EntityType<? extends Bogged> p_333629_, Level p_333576_) {
        super(p_333629_, p_333576_);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_335065_) {
        super.defineSynchedData(p_335065_);
        p_335065_.define(DATA_SHEARED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_334451_) {
        super.addAdditionalSaveData(p_334451_);
        p_334451_.putBoolean("sheared", this.isSheared());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_335260_) {
        super.readAdditionalSaveData(p_335260_);
        this.setSheared(p_335260_.getBoolean("sheared"));
    }

    public boolean isSheared() {
        return this.entityData.get(DATA_SHEARED);
    }

    public void setSheared(boolean p_329679_) {
        this.entityData.set(DATA_SHEARED, p_329679_);
    }

    @Override
    protected InteractionResult mobInteract(Player p_335303_, InteractionHand p_335145_) {
        ItemStack itemstack = p_335303_.getItemInHand(p_335145_);
        if (itemstack.is(Items.SHEARS) && this.readyForShearing()) {
            if (this.level() instanceof ServerLevel serverlevel) {
                this.shear(serverlevel, SoundSource.PLAYERS, itemstack);
                this.gameEvent(GameEvent.SHEAR, p_335303_);
                itemstack.hurtAndBreak(1, p_335303_, getSlotForHand(p_335145_));
            }

            return InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(p_335303_, p_335145_);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BOGGED_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_331504_) {
        return SoundEvents.BOGGED_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BOGGED_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.BOGGED_STEP;
    }

    @Override
    protected AbstractArrow getArrow(ItemStack p_333556_, float p_332689_, @Nullable ItemStack p_343334_) {
        AbstractArrow abstractarrow = super.getArrow(p_333556_, p_332689_, p_343334_);
        if (abstractarrow instanceof Arrow arrow) {
            arrow.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
        }

        return abstractarrow;
    }

    @Override
    protected int getHardAttackInterval() {
        return 50;
    }

    @Override
    protected int getAttackInterval() {
        return 70;
    }

    @Override
    public void shear(ServerLevel p_364124_, SoundSource p_332421_, ItemStack p_368753_) {
        p_364124_.playSound(null, this, SoundEvents.BOGGED_SHEAR, p_332421_, 1.0F, 1.0F);
        this.spawnShearedMushrooms(p_364124_, p_368753_);
        this.setSheared(true);
    }

    private void spawnShearedMushrooms(ServerLevel p_362178_, ItemStack p_364629_) {
        this.dropFromShearingLootTable(p_362178_, BuiltInLootTables.BOGGED_SHEAR, p_364629_, (p_375131_, p_375132_) -> this.spawnAtLocation(p_375131_, p_375132_, this.getBbHeight()));
    }

    @Override
    public boolean readyForShearing() {
        return !this.isSheared() && this.isAlive();
    }
}