package net.minecraft.world.entity.animal;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class PolarBear extends Animal implements NeutralMob {
    private static final EntityDataAccessor<Boolean> DATA_STANDING_ID = SynchedEntityData.defineId(PolarBear.class, EntityDataSerializers.BOOLEAN);
    private static final float STAND_ANIMATION_TICKS = 6.0F;
    private float clientSideStandAnimationO;
    private float clientSideStandAnimation;
    private int warningSoundTicks;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;

    public PolarBear(EntityType<? extends PolarBear> p_29519_, Level p_29520_) {
        super(p_29519_, p_29520_);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_149005_, AgeableMob p_149006_) {
        return EntityType.POLAR_BEAR.create(p_149005_, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean isFood(ItemStack p_29565_) {
        return false;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PolarBear.PolarBearMeleeAttackGoal());
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0, p_359173_ -> p_359173_.isBaby() ? DamageTypeTags.PANIC_CAUSES : DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new PolarBear.PolarBearHurtByTargetGoal());
        this.targetSelector.addGoal(2, new PolarBear.PolarBearAttackPlayersGoal());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Fox.class, 10, true, true, null));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.FOLLOW_RANGE, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    public static boolean checkPolarBearSpawnRules(
        EntityType<PolarBear> p_218250_, LevelAccessor p_218251_, EntitySpawnReason p_362821_, BlockPos p_218253_, RandomSource p_218254_
    ) {
        Holder<Biome> holder = p_218251_.getBiome(p_218253_);
        return !holder.is(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS)
            ? checkAnimalSpawnRules(p_218250_, p_218251_, p_362821_, p_218253_, p_218254_)
            : isBrightEnoughToSpawn(p_218251_, p_218253_) && p_218251_.getBlockState(p_218253_.below()).is(BlockTags.POLAR_BEARS_SPAWNABLE_ON_ALTERNATE);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_29541_) {
        super.readAdditionalSaveData(p_29541_);
        this.readPersistentAngerSaveData(this.level(), p_29541_);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_29548_) {
        super.addAdditionalSaveData(p_29548_);
        this.addPersistentAngerSaveData(p_29548_);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int p_29543_) {
        this.remainingPersistentAngerTime = p_29543_;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID p_29539_) {
        this.persistentAngerTarget = p_29539_;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? SoundEvents.POLAR_BEAR_AMBIENT_BABY : SoundEvents.POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_29559_) {
        return SoundEvents.POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.POLAR_BEAR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos p_29545_, BlockState p_29546_) {
        this.playSound(SoundEvents.POLAR_BEAR_STEP, 0.15F, 1.0F);
    }

    protected void playWarningSound() {
        if (this.warningSoundTicks <= 0) {
            this.makeSound(SoundEvents.POLAR_BEAR_WARNING);
            this.warningSoundTicks = 40;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_335689_) {
        super.defineSynchedData(p_335689_);
        p_335689_.define(DATA_STANDING_ID, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.clientSideStandAnimation != this.clientSideStandAnimationO) {
                this.refreshDimensions();
            }

            this.clientSideStandAnimationO = this.clientSideStandAnimation;
            if (this.isStanding()) {
                this.clientSideStandAnimation = Mth.clamp(this.clientSideStandAnimation + 1.0F, 0.0F, 6.0F);
            } else {
                this.clientSideStandAnimation = Mth.clamp(this.clientSideStandAnimation - 1.0F, 0.0F, 6.0F);
            }
        }

        if (this.warningSoundTicks > 0) {
            this.warningSoundTicks--;
        }

        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose p_327808_) {
        if (this.clientSideStandAnimation > 0.0F) {
            float f = this.clientSideStandAnimation / 6.0F;
            float f1 = 1.0F + f;
            return super.getDefaultDimensions(p_327808_).scale(1.0F, f1);
        } else {
            return super.getDefaultDimensions(p_327808_);
        }
    }

    public boolean isStanding() {
        return this.entityData.get(DATA_STANDING_ID);
    }

    public void setStanding(boolean p_29568_) {
        this.entityData.set(DATA_STANDING_ID, p_29568_);
    }

    public float getStandingAnimationScale(float p_29570_) {
        return Mth.lerp(p_29570_, this.clientSideStandAnimationO, this.clientSideStandAnimation) / 6.0F;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_29533_, DifficultyInstance p_29534_, EntitySpawnReason p_369251_, @Nullable SpawnGroupData p_29536_) {
        if (p_29536_ == null) {
            p_29536_ = new AgeableMob.AgeableMobGroupData(1.0F);
        }

        return super.finalizeSpawn(p_29533_, p_29534_, p_369251_, p_29536_);
    }

    class PolarBearAttackPlayersGoal extends NearestAttackableTargetGoal<Player> {
        public PolarBearAttackPlayersGoal() {
            super(PolarBear.this, Player.class, 20, true, true, null);
        }

        @Override
        public boolean canUse() {
            if (PolarBear.this.isBaby()) {
                return false;
            } else {
                if (super.canUse()) {
                    for (PolarBear polarbear : PolarBear.this.level().getEntitiesOfClass(PolarBear.class, PolarBear.this.getBoundingBox().inflate(8.0, 4.0, 8.0))) {
                        if (polarbear.isBaby()) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.5;
        }
    }

    class PolarBearHurtByTargetGoal extends HurtByTargetGoal {
        public PolarBearHurtByTargetGoal() {
            super(PolarBear.this);
        }

        @Override
        public void start() {
            super.start();
            if (PolarBear.this.isBaby()) {
                this.alertOthers();
                this.stop();
            }
        }

        @Override
        protected void alertOther(Mob p_29580_, LivingEntity p_29581_) {
            if (p_29580_ instanceof PolarBear && !p_29580_.isBaby()) {
                super.alertOther(p_29580_, p_29581_);
            }
        }
    }

    class PolarBearMeleeAttackGoal extends MeleeAttackGoal {
        public PolarBearMeleeAttackGoal() {
            super(PolarBear.this, 1.25, true);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity p_29589_) {
            if (this.canPerformAttack(p_29589_)) {
                this.resetAttackCooldown();
                this.mob.doHurtTarget(getServerLevel(this.mob), p_29589_);
                PolarBear.this.setStanding(false);
            } else if (this.mob.distanceToSqr(p_29589_) < (double)((p_29589_.getBbWidth() + 3.0F) * (p_29589_.getBbWidth() + 3.0F))) {
                if (this.isTimeToAttack()) {
                    PolarBear.this.setStanding(false);
                    this.resetAttackCooldown();
                }

                if (this.getTicksUntilNextAttack() <= 10) {
                    PolarBear.this.setStanding(true);
                    PolarBear.this.playWarningSound();
                }
            } else {
                this.resetAttackCooldown();
                PolarBear.this.setStanding(false);
            }
        }

        @Override
        public void stop() {
            PolarBear.this.setStanding(false);
            super.stop();
        }
    }
}