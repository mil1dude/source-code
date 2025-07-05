package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SmallFireball extends Fireball {
    public SmallFireball(EntityType<? extends SmallFireball> p_37364_, Level p_37365_) {
        super(p_37364_, p_37365_);
    }

    public SmallFireball(Level p_37367_, LivingEntity p_342424_, Vec3 p_344527_) {
        super(EntityType.SMALL_FIREBALL, p_342424_, p_344527_, p_37367_);
    }

    public SmallFireball(Level p_37375_, double p_37377_, double p_37378_, double p_37379_, Vec3 p_343728_) {
        super(EntityType.SMALL_FIREBALL, p_37377_, p_37378_, p_37379_, p_343728_, p_37375_);
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37386_) {
        super.onHitEntity(p_37386_);
        if (this.level() instanceof ServerLevel serverlevel) {
            Entity entity1 = p_37386_.getEntity();
            Entity $$4 = this.getOwner();
            int $$5 = entity1.getRemainingFireTicks();
            entity1.igniteForSeconds(5.0F);
            DamageSource $$6 = this.damageSources().fireball(this, $$4);
            if (!entity1.hurtServer(serverlevel, $$6, 5.0F)) {
                entity1.setRemainingFireTicks($$5);
            } else {
                EnchantmentHelper.doPostAttackEffects(serverlevel, entity1, $$6);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult p_37384_) {
        super.onHitBlock(p_37384_);
        if (this.level() instanceof ServerLevel serverlevel) {
            Entity entity = this.getOwner();
            if (!(entity instanceof Mob) || serverlevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                BlockPos blockpos = p_37384_.getBlockPos().relative(p_37384_.getDirection());
                if (this.level().isEmptyBlock(blockpos)) {
                    this.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level(), blockpos));
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult p_37388_) {
        super.onHit(p_37388_);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}