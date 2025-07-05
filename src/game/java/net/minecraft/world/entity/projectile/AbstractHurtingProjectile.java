package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractHurtingProjectile extends Projectile {
    public static final double INITAL_ACCELERATION_POWER = 0.1;
    public static final double DEFLECTION_SCALE = 0.5;
    public double accelerationPower = 0.1;

    protected AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> p_36833_, Level p_36834_) {
        super(p_36833_, p_36834_);
    }

    protected AbstractHurtingProjectile(
        EntityType<? extends AbstractHurtingProjectile> p_310629_, double p_311590_, double p_312782_, double p_309484_, Level p_311660_
    ) {
        this(p_310629_, p_311660_);
        this.setPos(p_311590_, p_312782_, p_309484_);
    }

    public AbstractHurtingProjectile(
        EntityType<? extends AbstractHurtingProjectile> p_36817_, double p_36818_, double p_36819_, double p_36820_, Vec3 p_343716_, Level p_36824_
    ) {
        this(p_36817_, p_36824_);
        this.moveTo(p_36818_, p_36819_, p_36820_, this.getYRot(), this.getXRot());
        this.reapplyPosition();
        this.assignDirectionalMovement(p_343716_, this.accelerationPower);
    }

    public AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> p_36826_, LivingEntity p_36827_, Vec3 p_343596_, Level p_36831_) {
        this(p_36826_, p_36827_.getX(), p_36827_.getY(), p_36827_.getZ(), p_343596_, p_36831_);
        this.setOwner(p_36827_);
        this.setRot(p_36827_.getYRot(), p_36827_.getXRot());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_330369_) {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_36837_) {
        double d0 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d0)) {
            d0 = 4.0;
        }

        d0 *= 64.0;
        return p_36837_ < d0 * d0;
    }

    protected ClipContext.Block getClipType() {
        return ClipContext.Block.COLLIDER;
    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();
        this.applyInertia();
        if (this.level().isClientSide || (entity == null || !entity.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
            HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity, this.getClipType());
            Vec3 vec3;
            if (hitresult.getType() != HitResult.Type.MISS) {
                vec3 = hitresult.getLocation();
            } else {
                vec3 = this.position().add(this.getDeltaMovement());
            }

            ProjectileUtil.rotateTowardsMovement(this, 0.2F);
            this.setPos(vec3);
            this.applyEffectsFromBlocks();
            super.tick();
            if (this.shouldBurn()) {
                this.igniteForSeconds(1.0F);
            }

            if (hitresult.getType() != HitResult.Type.MISS && this.isAlive()) {
                this.hitTargetOrDeflectSelf(hitresult);
            }

            this.createParticleTrail();
        } else {
            this.discard();
        }
    }

    private void applyInertia() {
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 vec31 = this.position();
        float f;
        if (this.isInWater()) {
            for (int i = 0; i < 4; i++) {
                float f1 = 0.25F;
                this.level()
                    .addParticle(
                        ParticleTypes.BUBBLE,
                        vec31.x - vec3.x * 0.25,
                        vec31.y - vec3.y * 0.25,
                        vec31.z - vec3.z * 0.25,
                        vec3.x,
                        vec3.y,
                        vec3.z
                    );
            }

            f = this.getLiquidInertia();
        } else {
            f = this.getInertia();
        }

        this.setDeltaMovement(vec3.add(vec3.normalize().scale(this.accelerationPower)).scale((double)f));
    }

    private void createParticleTrail() {
        ParticleOptions particleoptions = this.getTrailParticle();
        Vec3 vec3 = this.position();
        if (particleoptions != null) {
            this.level().addParticle(particleoptions, vec3.x, vec3.y + 0.5, vec3.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel p_361321_, DamageSource p_362421_, float p_360832_) {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity p_36842_) {
        return super.canHitEntity(p_36842_) && !p_36842_.noPhysics;
    }

    protected boolean shouldBurn() {
        return true;
    }

    @Nullable
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.SMOKE;
    }

    protected float getInertia() {
        return 0.95F;
    }

    protected float getLiquidInertia() {
        return 0.8F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_36848_) {
        super.addAdditionalSaveData(p_36848_);
        p_36848_.putDouble("acceleration_power", this.accelerationPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_36844_) {
        super.readAdditionalSaveData(p_36844_);
        if (p_36844_.contains("acceleration_power", 6)) {
            this.accelerationPower = p_36844_.getDouble("acceleration_power");
        }
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    private void assignDirectionalMovement(Vec3 p_342200_, double p_343156_) {
        this.setDeltaMovement(p_342200_.normalize().scale(p_343156_));
        this.hasImpulse = true;
    }

    @Override
    protected void onDeflection(@Nullable Entity p_334459_, boolean p_331188_) {
        super.onDeflection(p_334459_, p_331188_);
        if (p_331188_) {
            this.accelerationPower = 0.1;
        } else {
            this.accelerationPower *= 0.5;
        }
    }
}