package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class Stray extends AbstractSkeleton {
    public Stray(EntityType<? extends Stray> p_33836_, Level p_33837_) {
        super(p_33836_, p_33837_);
    }

    public static boolean checkStraySpawnRules(
        EntityType<Stray> p_219121_, ServerLevelAccessor p_219122_, EntitySpawnReason p_364808_, BlockPos p_219124_, RandomSource p_219125_
    ) {
        BlockPos blockpos = p_219124_;

        do {
            blockpos = blockpos.above();
        } while (p_219122_.getBlockState(blockpos).is(Blocks.POWDER_SNOW));

        return checkMonsterSpawnRules(p_219121_, p_219122_, p_364808_, p_219124_, p_219125_)
            && (EntitySpawnReason.isSpawner(p_364808_) || p_219122_.canSeeSky(blockpos.below()));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.STRAY_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_33850_) {
        return SoundEvents.STRAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRAY_DEATH;
    }

    @Override
    SoundEvent getStepSound() {
        return SoundEvents.STRAY_STEP;
    }

    @Override
    protected AbstractArrow getArrow(ItemStack p_33846_, float p_33847_, @Nullable ItemStack p_343428_) {
        AbstractArrow abstractarrow = super.getArrow(p_33846_, p_33847_, p_343428_);
        if (abstractarrow instanceof Arrow) {
            ((Arrow)abstractarrow).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600));
        }

        return abstractarrow;
    }
}