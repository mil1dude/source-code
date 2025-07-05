package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class GlowSquid extends Squid {
    private static final EntityDataAccessor<Integer> DATA_DARK_TICKS_REMAINING = SynchedEntityData.defineId(GlowSquid.class, EntityDataSerializers.INT);

    public GlowSquid(EntityType<? extends GlowSquid> p_147111_, Level p_147112_) {
        super(p_147111_, p_147112_);
    }

    @Override
    protected ParticleOptions getInkParticle() {
        return ParticleTypes.GLOW_SQUID_INK;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_332968_) {
        super.defineSynchedData(p_332968_);
        p_332968_.define(DATA_DARK_TICKS_REMAINING, 0);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_361243_, AgeableMob p_363550_) {
        return EntityType.GLOW_SQUID.create(p_361243_, EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEvent getSquirtSound() {
        return SoundEvents.GLOW_SQUID_SQUIRT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GLOW_SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_147124_) {
        return SoundEvents.GLOW_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GLOW_SQUID_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_147122_) {
        super.addAdditionalSaveData(p_147122_);
        p_147122_.putInt("DarkTicksRemaining", this.getDarkTicksRemaining());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_147117_) {
        super.readAdditionalSaveData(p_147117_);
        this.setDarkTicks(p_147117_.getInt("DarkTicksRemaining"));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        int i = this.getDarkTicksRemaining();
        if (i > 0) {
            this.setDarkTicks(i - 1);
        }

        this.level().addParticle(ParticleTypes.GLOW, this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, 0.0, 0.0);
    }

    @Override
    public boolean hurtServer(ServerLevel p_361836_, DamageSource p_368717_, float p_361229_) {
        boolean flag = super.hurtServer(p_361836_, p_368717_, p_361229_);
        if (flag) {
            this.setDarkTicks(100);
        }

        return flag;
    }

    private void setDarkTicks(int p_147120_) {
        this.entityData.set(DATA_DARK_TICKS_REMAINING, p_147120_);
    }

    public int getDarkTicksRemaining() {
        return this.entityData.get(DATA_DARK_TICKS_REMAINING);
    }

    public static boolean checkGlowSquidSpawnRules(
        EntityType<? extends LivingEntity> p_300540_, ServerLevelAccessor p_297255_, EntitySpawnReason p_363233_, BlockPos p_299141_, RandomSource p_297395_
    ) {
        return p_299141_.getY() <= p_297255_.getSeaLevel() - 33
            && p_297255_.getRawBrightness(p_299141_, 0) == 0
            && p_297255_.getBlockState(p_299141_).is(Blocks.WATER);
    }
}