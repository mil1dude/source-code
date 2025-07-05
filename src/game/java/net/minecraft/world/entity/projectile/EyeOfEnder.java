package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EyeOfEnder extends Entity implements ItemSupplier {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(EyeOfEnder.class, EntityDataSerializers.ITEM_STACK);
    private double tx;
    private double ty;
    private double tz;
    private int life;
    private boolean surviveAfterDeath;

    public EyeOfEnder(EntityType<? extends EyeOfEnder> p_36957_, Level p_36958_) {
        super(p_36957_, p_36958_);
    }

    public EyeOfEnder(Level p_36960_, double p_36961_, double p_36962_, double p_36963_) {
        this(EntityType.EYE_OF_ENDER, p_36960_);
        this.setPos(p_36961_, p_36962_, p_36963_);
    }

    public void setItem(ItemStack p_36973_) {
        if (p_36973_.isEmpty()) {
            this.getEntityData().set(DATA_ITEM_STACK, this.getDefaultItem());
        } else {
            this.getEntityData().set(DATA_ITEM_STACK, p_36973_.copyWithCount(1));
        }
    }

    @Override
    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_333578_) {
        p_333578_.define(DATA_ITEM_STACK, this.getDefaultItem());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_36966_) {
        if (this.tickCount < 2 && p_36966_ < 12.25) {
            return false;
        } else {
            double d0 = this.getBoundingBox().getSize() * 4.0;
            if (Double.isNaN(d0)) {
                d0 = 4.0;
            }

            d0 *= 64.0;
            return p_36966_ < d0 * d0;
        }
    }

    public void signalTo(BlockPos p_36968_) {
        double d0 = (double)p_36968_.getX();
        int i = p_36968_.getY();
        double d1 = (double)p_36968_.getZ();
        double d2 = d0 - this.getX();
        double d3 = d1 - this.getZ();
        double d4 = Math.sqrt(d2 * d2 + d3 * d3);
        if (d4 > 12.0) {
            this.tx = this.getX() + d2 / d4 * 12.0;
            this.tz = this.getZ() + d3 / d4 * 12.0;
            this.ty = this.getY() + 8.0;
        } else {
            this.tx = d0;
            this.ty = (double)i;
            this.tz = d1;
        }

        this.life = 0;
        this.surviveAfterDeath = this.random.nextInt(5) > 0;
    }

    @Override
    public void lerpMotion(double p_36984_, double p_36985_, double p_36986_) {
        this.setDeltaMovement(p_36984_, p_36985_, p_36986_);
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = Math.sqrt(p_36984_ * p_36984_ + p_36986_ * p_36986_);
            this.setYRot((float)(Mth.atan2(p_36984_, p_36986_) * 180.0F / (float)Math.PI));
            this.setXRot((float)(Mth.atan2(p_36985_, d0) * 180.0F / (float)Math.PI));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 vec3 = this.getDeltaMovement();
        double d0 = this.getX() + vec3.x;
        double d1 = this.getY() + vec3.y;
        double d2 = this.getZ() + vec3.z;
        double d3 = vec3.horizontalDistance();
        this.setXRot(Projectile.lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, d3) * 180.0F / (float)Math.PI)));
        this.setYRot(Projectile.lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI)));
        if (!this.level().isClientSide) {
            double d4 = this.tx - d0;
            double d5 = this.tz - d2;
            float f = (float)Math.sqrt(d4 * d4 + d5 * d5);
            float f1 = (float)Mth.atan2(d5, d4);
            double d6 = Mth.lerp(0.0025, d3, (double)f);
            double d7 = vec3.y;
            if (f < 1.0F) {
                d6 *= 0.8;
                d7 *= 0.8;
            }

            int j = this.getY() < this.ty ? 1 : -1;
            vec3 = new Vec3(Math.cos((double)f1) * d6, d7 + ((double)j - d7) * 0.015F, Math.sin((double)f1) * d6);
            this.setDeltaMovement(vec3);
        }

        float f2 = 0.25F;
        if (this.isInWater()) {
            for (int i = 0; i < 4; i++) {
                this.level()
                    .addParticle(
                        ParticleTypes.BUBBLE,
                        d0 - vec3.x * 0.25,
                        d1 - vec3.y * 0.25,
                        d2 - vec3.z * 0.25,
                        vec3.x,
                        vec3.y,
                        vec3.z
                    );
            }
        } else {
            this.level()
                .addParticle(
                    ParticleTypes.PORTAL,
                    d0 - vec3.x * 0.25 + this.random.nextDouble() * 0.6 - 0.3,
                    d1 - vec3.y * 0.25 - 0.5,
                    d2 - vec3.z * 0.25 + this.random.nextDouble() * 0.6 - 0.3,
                    vec3.x,
                    vec3.y,
                    vec3.z
                );
        }

        if (!this.level().isClientSide) {
            this.setPos(d0, d1, d2);
            this.life++;
            if (this.life > 80 && !this.level().isClientSide) {
                this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
                this.discard();
                if (this.surviveAfterDeath) {
                    this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.getItem()));
                } else {
                    this.level().levelEvent(2003, this.blockPosition(), 0);
                }
            }
        } else {
            this.setPosRaw(d0, d1, d2);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_36975_) {
        p_36975_.put("Item", this.getItem().save(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_36970_) {
        if (p_36970_.contains("Item", 10)) {
            this.setItem(ItemStack.parse(this.registryAccess(), p_36970_.getCompound("Item")).orElse(this.getDefaultItem()));
        } else {
            this.setItem(this.getDefaultItem());
        }
    }

    private ItemStack getDefaultItem() {
        return new ItemStack(Items.ENDER_EYE);
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel p_361156_, DamageSource p_361721_, float p_361974_) {
        return false;
    }
}