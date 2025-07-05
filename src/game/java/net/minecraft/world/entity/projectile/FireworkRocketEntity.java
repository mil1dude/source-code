package net.minecraft.world.entity.projectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    private static final EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.BOOLEAN);
    private int life;
    private int lifetime;
    @Nullable
    private LivingEntity attachedToEntity;

    public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> p_37027_, Level p_37028_) {
        super(p_37027_, p_37028_);
    }

    public FireworkRocketEntity(Level p_37030_, double p_37031_, double p_37032_, double p_37033_, ItemStack p_37034_) {
        super(EntityType.FIREWORK_ROCKET, p_37030_);
        this.life = 0;
        this.setPos(p_37031_, p_37032_, p_37033_);
        this.entityData.set(DATA_ID_FIREWORKS_ITEM, p_37034_.copy());
        int i = 1;
        Fireworks fireworks = p_37034_.get(DataComponents.FIREWORKS);
        if (fireworks != null) {
            i += fireworks.flightDuration();
        }

        this.setDeltaMovement(this.random.triangle(0.0, 0.002297), 0.05, this.random.triangle(0.0, 0.002297));
        this.lifetime = 10 * i + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public FireworkRocketEntity(Level p_37036_, @Nullable Entity p_37037_, double p_37038_, double p_37039_, double p_37040_, ItemStack p_37041_) {
        this(p_37036_, p_37038_, p_37039_, p_37040_, p_37041_);
        this.setOwner(p_37037_);
    }

    public FireworkRocketEntity(Level p_37058_, ItemStack p_37059_, LivingEntity p_37060_) {
        this(p_37058_, p_37060_, p_37060_.getX(), p_37060_.getY(), p_37060_.getZ(), p_37059_);
        this.entityData.set(DATA_ATTACHED_TO_TARGET, OptionalInt.of(p_37060_.getId()));
        this.attachedToEntity = p_37060_;
    }

    public FireworkRocketEntity(Level p_37043_, ItemStack p_37044_, double p_37045_, double p_37046_, double p_37047_, boolean p_37048_) {
        this(p_37043_, p_37045_, p_37046_, p_37047_, p_37044_);
        this.entityData.set(DATA_SHOT_AT_ANGLE, p_37048_);
    }

    public FireworkRocketEntity(Level p_37050_, ItemStack p_37051_, Entity p_37052_, double p_37053_, double p_37054_, double p_37055_, boolean p_37056_) {
        this(p_37050_, p_37051_, p_37053_, p_37054_, p_37055_, p_37056_);
        this.setOwner(p_37052_);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_332895_) {
        p_332895_.define(DATA_ID_FIREWORKS_ITEM, getDefaultItem());
        p_332895_.define(DATA_ATTACHED_TO_TARGET, OptionalInt.empty());
        p_332895_.define(DATA_SHOT_AT_ANGLE, false);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_37065_) {
        return p_37065_ < 4096.0 && !this.isAttachedToEntity();
    }

    @Override
    public boolean shouldRender(double p_37083_, double p_37084_, double p_37085_) {
        return super.shouldRender(p_37083_, p_37084_, p_37085_) && !this.isAttachedToEntity();
    }

    @Override
    public void tick() {
        super.tick();
        HitResult hitresult;
        if (this.isAttachedToEntity()) {
            if (this.attachedToEntity == null) {
                this.entityData.get(DATA_ATTACHED_TO_TARGET).ifPresent(p_375168_ -> {
                    Entity entity = this.level().getEntity(p_375168_);
                    if (entity instanceof LivingEntity) {
                        this.attachedToEntity = (LivingEntity)entity;
                    }
                });
            }

            if (this.attachedToEntity != null) {
                Vec3 vec3;
                if (this.attachedToEntity.isFallFlying()) {
                    Vec3 vec31 = this.attachedToEntity.getLookAngle();
                    double d0 = 1.5;
                    double d1 = 0.1;
                    Vec3 vec32 = this.attachedToEntity.getDeltaMovement();
                    this.attachedToEntity
                        .setDeltaMovement(
                            vec32.add(
                                vec31.x * 0.1 + (vec31.x * 1.5 - vec32.x) * 0.5,
                                vec31.y * 0.1 + (vec31.y * 1.5 - vec32.y) * 0.5,
                                vec31.z * 0.1 + (vec31.z * 1.5 - vec32.z) * 0.5
                            )
                        );
                    vec3 = this.attachedToEntity.getHandHoldingItemAngle(Items.FIREWORK_ROCKET);
                } else {
                    vec3 = Vec3.ZERO;
                }

                this.setPos(this.attachedToEntity.getX() + vec3.x, this.attachedToEntity.getY() + vec3.y, this.attachedToEntity.getZ() + vec3.z);
                this.setDeltaMovement(this.attachedToEntity.getDeltaMovement());
            }

            hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        } else {
            if (!this.isShotAtAngle()) {
                double d2 = this.horizontalCollision ? 1.0 : 1.15;
                this.setDeltaMovement(this.getDeltaMovement().multiply(d2, 1.0, d2).add(0.0, 0.04, 0.0));
            }

            Vec3 vec33 = this.getDeltaMovement();
            hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            this.move(MoverType.SELF, vec33);
            this.applyEffectsFromBlocks();
            this.setDeltaMovement(vec33);
        }

        if (!this.noPhysics && this.isAlive() && hitresult.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf(hitresult);
            this.hasImpulse = true;
        }

        this.updateRotation();
        if (this.life == 0 && !this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
        }

        this.life++;
        if (this.level().isClientSide && this.life % 2 < 2) {
            this.level()
                .addParticle(
                    ParticleTypes.FIREWORK,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    this.random.nextGaussian() * 0.05,
                    -this.getDeltaMovement().y * 0.5,
                    this.random.nextGaussian() * 0.05
                );
        }

        if (this.life > this.lifetime && this.level() instanceof ServerLevel serverlevel) {
            this.explode(serverlevel);
        }
    }

    private void explode(ServerLevel p_361825_) {
        p_361825_.broadcastEntityEvent(this, (byte)17);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.dealExplosionDamage(p_361825_);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37071_) {
        super.onHitEntity(p_37071_);
        if (this.level() instanceof ServerLevel serverlevel) {
            this.explode(serverlevel);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult p_37069_) {
        BlockPos blockpos = new BlockPos(p_37069_.getBlockPos());
        this.level().getBlockState(blockpos).entityInside(this.level(), blockpos, this);
        if (this.level() instanceof ServerLevel serverlevel && this.hasExplosion()) {
            this.explode(serverlevel);
        }

        super.onHitBlock(p_37069_);
    }

    private boolean hasExplosion() {
        return !this.getExplosions().isEmpty();
    }

    private void dealExplosionDamage(ServerLevel p_364659_) {
        float f = 0.0F;
        List<FireworkExplosion> list = this.getExplosions();
        if (!list.isEmpty()) {
            f = 5.0F + (float)(list.size() * 2);
        }

        if (f > 0.0F) {
            if (this.attachedToEntity != null) {
                this.attachedToEntity.hurtServer(p_364659_, this.damageSources().fireworks(this, this.getOwner()), 5.0F + (float)(list.size() * 2));
            }

            double d0 = 5.0;
            Vec3 vec3 = this.position();

            for (LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0))) {
                if (livingentity != this.attachedToEntity && !(this.distanceToSqr(livingentity) > 25.0)) {
                    boolean flag = false;

                    for (int i = 0; i < 2; i++) {
                        Vec3 vec31 = new Vec3(livingentity.getX(), livingentity.getY(0.5 * (double)i), livingentity.getZ());
                        HitResult hitresult = this.level().clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                        if (hitresult.getType() == HitResult.Type.MISS) {
                            flag = true;
                            break;
                        }
                    }

                    if (flag) {
                        float f1 = f * (float)Math.sqrt((5.0 - (double)this.distanceTo(livingentity)) / 5.0);
                        livingentity.hurtServer(p_364659_, this.damageSources().fireworks(this, this.getOwner()), f1);
                    }
                }
            }
        }
    }

    private boolean isAttachedToEntity() {
        return this.entityData.get(DATA_ATTACHED_TO_TARGET).isPresent();
    }

    public boolean isShotAtAngle() {
        return this.entityData.get(DATA_SHOT_AT_ANGLE);
    }

    @Override
    public void handleEntityEvent(byte p_37063_) {
        if (p_37063_ == 17 && this.level().isClientSide) {
            Vec3 vec3 = this.getDeltaMovement();
            this.level().createFireworks(this.getX(), this.getY(), this.getZ(), vec3.x, vec3.y, vec3.z, this.getExplosions());
        }

        super.handleEntityEvent(p_37063_);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_37075_) {
        super.addAdditionalSaveData(p_37075_);
        p_37075_.putInt("Life", this.life);
        p_37075_.putInt("LifeTime", this.lifetime);
        p_37075_.put("FireworksItem", this.getItem().save(this.registryAccess()));
        p_37075_.putBoolean("ShotAtAngle", this.entityData.get(DATA_SHOT_AT_ANGLE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_37073_) {
        super.readAdditionalSaveData(p_37073_);
        this.life = p_37073_.getInt("Life");
        this.lifetime = p_37073_.getInt("LifeTime");
        if (p_37073_.contains("FireworksItem", 10)) {
            this.entityData
                .set(DATA_ID_FIREWORKS_ITEM, ItemStack.parse(this.registryAccess(), p_37073_.getCompound("FireworksItem")).orElseGet(FireworkRocketEntity::getDefaultItem));
        } else {
            this.entityData.set(DATA_ID_FIREWORKS_ITEM, getDefaultItem());
        }

        if (p_37073_.contains("ShotAtAngle")) {
            this.entityData.set(DATA_SHOT_AT_ANGLE, p_37073_.getBoolean("ShotAtAngle"));
        }
    }

    private List<FireworkExplosion> getExplosions() {
        ItemStack itemstack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        Fireworks fireworks = itemstack.get(DataComponents.FIREWORKS);
        return fireworks != null ? fireworks.explosions() : List.of();
    }

    @Override
    public ItemStack getItem() {
        return this.entityData.get(DATA_ID_FIREWORKS_ITEM);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    private static ItemStack getDefaultItem() {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity p_343097_, DamageSource p_343307_) {
        double d0 = p_343097_.position().x - this.position().x;
        double d1 = p_343097_.position().z - this.position().z;
        return DoubleDoubleImmutablePair.of(d0, d1);
    }
}