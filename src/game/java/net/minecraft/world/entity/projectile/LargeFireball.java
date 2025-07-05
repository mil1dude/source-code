package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LargeFireball extends Fireball {
    private int explosionPower = 1;

    public LargeFireball(EntityType<? extends LargeFireball> p_37199_, Level p_37200_) {
        super(p_37199_, p_37200_);
    }

    public LargeFireball(Level p_181151_, LivingEntity p_181152_, Vec3 p_342986_, int p_181156_) {
        super(EntityType.FIREBALL, p_181152_, p_342986_, p_181151_);
        this.explosionPower = p_181156_;
    }

    @Override
    protected void onHit(HitResult p_37218_) {
        super.onHit(p_37218_);
        if (this.level() instanceof ServerLevel serverlevel) {
            boolean flag = serverlevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, flag, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37216_) {
        super.onHitEntity(p_37216_);
        if (this.level() instanceof ServerLevel serverlevel) {
            Entity entity1 = p_37216_.getEntity();
            Entity $$4 = this.getOwner();
            DamageSource $$5 = this.damageSources().fireball(this, $$4);
            entity1.hurtServer(serverlevel, $$5, 6.0F);
            EnchantmentHelper.doPostAttackEffects(serverlevel, entity1, $$5);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_37222_) {
        super.addAdditionalSaveData(p_37222_);
        p_37222_.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_37220_) {
        super.readAdditionalSaveData(p_37220_);
        if (p_37220_.contains("ExplosionPower", 99)) {
            this.explosionPower = p_37220_.getByte("ExplosionPower");
        }
    }
}