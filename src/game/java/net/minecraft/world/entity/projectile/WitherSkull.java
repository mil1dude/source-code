package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WitherSkull extends AbstractHurtingProjectile {
    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(WitherSkull.class, EntityDataSerializers.BOOLEAN);

    public WitherSkull(EntityType<? extends WitherSkull> p_37598_, Level p_37599_) {
        super(p_37598_, p_37599_);
    }

    public WitherSkull(Level p_37609_, LivingEntity p_37610_, Vec3 p_343204_) {
        super(EntityType.WITHER_SKULL, p_37610_, p_343204_, p_37609_);
    }

    @Override
    protected float getInertia() {
        return this.isDangerous() ? 0.73F : super.getInertia();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public float getBlockExplosionResistance(Explosion p_37619_, BlockGetter p_37620_, BlockPos p_37621_, BlockState p_37622_, FluidState p_37623_, float p_37624_) {
        return this.isDangerous() && WitherBoss.canDestroy(p_37622_) ? Math.min(0.8F, p_37624_) : p_37624_;
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37626_) {
        super.onHitEntity(p_37626_);
        if (this.level() instanceof ServerLevel serverlevel) {
            Entity entity = p_37626_.getEntity();
            boolean flag;
            if (this.getOwner() instanceof LivingEntity livingentity) {
                DamageSource damagesource = this.damageSources().witherSkull(this, livingentity);
                flag = entity.hurtServer(serverlevel, damagesource, 8.0F);
                if (flag) {
                    if (entity.isAlive()) {
                        EnchantmentHelper.doPostAttackEffects(serverlevel, entity, damagesource);
                    } else {
                        livingentity.heal(5.0F);
                    }
                }
            } else {
                flag = entity.hurtServer(serverlevel, this.damageSources().magic(), 5.0F);
            }

            if (flag && entity instanceof LivingEntity livingentity1) {
                int i = 0;
                if (this.level().getDifficulty() == Difficulty.NORMAL) {
                    i = 10;
                } else if (this.level().getDifficulty() == Difficulty.HARD) {
                    i = 40;
                }

                if (i > 0) {
                    livingentity1.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * i, 1), this.getEffectSource());
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult p_37628_) {
        super.onHit(p_37628_);
        if (!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_333142_) {
        p_333142_.define(DATA_DANGEROUS, false);
    }

    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    public void setDangerous(boolean p_37630_) {
        this.entityData.set(DATA_DANGEROUS, p_37630_);
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_311705_) {
        super.addAdditionalSaveData(p_311705_);
        p_311705_.putBoolean("dangerous", this.isDangerous());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_310393_) {
        super.readAdditionalSaveData(p_310393_);
        this.setDangerous(p_310393_.getBoolean("dangerous"));
    }
}