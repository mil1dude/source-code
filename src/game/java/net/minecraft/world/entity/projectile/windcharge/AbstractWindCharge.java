package net.minecraft.world.entity.projectile.windcharge;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractWindCharge extends AbstractHurtingProjectile implements ItemSupplier {
    public static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
        true, false, Optional.empty(), BuiltInRegistries.BLOCK.get(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );
    public static final double JUMP_SCALE = 0.25;

    public AbstractWindCharge(EntityType<? extends AbstractWindCharge> p_328415_, Level p_334141_) {
        super(p_328415_, p_334141_);
        this.accelerationPower = 0.0;
    }

    public AbstractWindCharge(
        EntityType<? extends AbstractWindCharge> p_328691_, Level p_334245_, Entity p_335850_, double p_331511_, double p_328582_, double p_333875_
    ) {
        super(p_328691_, p_331511_, p_328582_, p_333875_, p_334245_);
        this.setOwner(p_335850_);
        this.accelerationPower = 0.0;
    }

    AbstractWindCharge(
        EntityType<? extends AbstractWindCharge> p_334984_, double p_330891_, double p_328515_, double p_329380_, Vec3 p_342847_, Level p_329917_
    ) {
        super(p_334984_, p_330891_, p_328515_, p_329380_, p_342847_, p_329917_);
        this.accelerationPower = 0.0;
    }

    @Override
    protected AABB makeBoundingBox(Vec3 p_375941_) {
        float f = this.getType().getDimensions().width() / 2.0F;
        float f1 = this.getType().getDimensions().height();
        float f2 = 0.15F;
        return new AABB(
            p_375941_.x - (double)f,
            p_375941_.y - 0.15F,
            p_375941_.z - (double)f,
            p_375941_.x + (double)f,
            p_375941_.y - 0.15F + (double)f1,
            p_375941_.z + (double)f
        );
    }

    @Override
    public boolean canCollideWith(Entity p_328571_) {
        return p_328571_ instanceof AbstractWindCharge ? false : super.canCollideWith(p_328571_);
    }

    @Override
    protected boolean canHitEntity(Entity p_333197_) {
        if (p_333197_ instanceof AbstractWindCharge) {
            return false;
        } else {
            return p_333197_.getType() == EntityType.END_CRYSTAL ? false : super.canHitEntity(p_333197_);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult p_328561_) {
        super.onHitEntity(p_328561_);
        if (this.level() instanceof ServerLevel serverlevel) {
            LivingEntity livingentity2 = this.getOwner() instanceof LivingEntity livingentity ? livingentity : null;
            Entity entity = p_328561_.getEntity();
            if (livingentity2 != null) {
                livingentity2.setLastHurtMob(entity);
            }

            DamageSource damagesource = this.damageSources().windCharge(this, livingentity2);
            if (entity.hurtServer(serverlevel, damagesource, 1.0F) && entity instanceof LivingEntity livingentity1) {
                EnchantmentHelper.doPostAttackEffects(serverlevel, livingentity1, damagesource);
            }

            this.explode(this.position());
        }
    }

    @Override
    public void push(double p_328125_, double p_336037_, double p_328448_) {
    }

    protected abstract void explode(Vec3 p_342463_);

    @Override
    protected void onHitBlock(BlockHitResult p_330277_) {
        super.onHitBlock(p_330277_);
        if (!this.level().isClientSide) {
            Vec3i vec3i = p_330277_.getDirection().getUnitVec3i();
            Vec3 vec3 = Vec3.atLowerCornerOf(vec3i).multiply(0.25, 0.25, 0.25);
            Vec3 vec31 = p_330277_.getLocation().add(vec3);
            this.explode(vec31);
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult p_328815_) {
        super.onHit(p_328815_);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    protected float getLiquidInertia() {
        return this.getInertia();
    }

    @Nullable
    @Override
    protected ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.getBlockY() > this.level().getMaxY() + 30) {
            this.explode(this.position());
            this.discard();
        } else {
            super.tick();
        }
    }
}