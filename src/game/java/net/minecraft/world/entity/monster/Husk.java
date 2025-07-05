package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class Husk extends Zombie {
    public Husk(EntityType<? extends Husk> p_32889_, Level p_32890_) {
        super(p_32889_, p_32890_);
    }

    public static boolean checkHuskSpawnRules(
        EntityType<Husk> p_218997_, ServerLevelAccessor p_218998_, EntitySpawnReason p_364340_, BlockPos p_219000_, RandomSource p_219001_
    ) {
        return checkMonsterSpawnRules(p_218997_, p_218998_, p_364340_, p_219000_, p_219001_) && (EntitySpawnReason.isSpawner(p_364340_) || p_218998_.canSeeSky(p_219000_));
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_32903_) {
        return SoundEvents.HUSK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HUSK_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.HUSK_STEP;
    }

    @Override
    public boolean doHurtTarget(ServerLevel p_366845_, Entity p_32892_) {
        boolean flag = super.doHurtTarget(p_366845_, p_32892_);
        if (flag && this.getMainHandItem().isEmpty() && p_32892_ instanceof LivingEntity) {
            float f = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            ((LivingEntity)p_32892_).addEffect(new MobEffectInstance(MobEffects.HUNGER, 140 * (int)f), this);
        }

        return flag;
    }

    @Override
    protected boolean convertsInWater() {
        return true;
    }

    @Override
    protected void doUnderWaterConversion() {
        this.convertToZombieType(EntityType.ZOMBIE);
        if (!this.isSilent()) {
            this.level().levelEvent(null, 1041, this.blockPosition(), 0);
        }
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }
}