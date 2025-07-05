package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class Chicken extends Animal {
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.CHICKEN.getDimensions().scale(0.5F).withEyeHeight(0.2975F);
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    public float flapping = 1.0F;
    private float nextFlap = 1.0F;
    public int eggTime = this.random.nextInt(6000) + 6000;
    public boolean isChickenJockey;

    public Chicken(EntityType<? extends Chicken> p_28236_, Level p_28237_) {
        super(p_28236_, p_28237_);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, p_334579_ -> p_334579_.is(ItemTags.CHICKEN_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose p_329747_) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(p_329747_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed = this.flapSpeed + (this.onGround() ? -1.0F : 4.0F) * 0.3F;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
        if (!this.onGround() && this.flapping < 1.0F) {
            this.flapping = 1.0F;
        }

        this.flapping *= 0.9F;
        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < 0.0) {
            this.setDeltaMovement(vec3.multiply(1.0, 0.6, 1.0));
        }

        this.flap = this.flap + this.flapping * 2.0F;
        if (this.level() instanceof ServerLevel serverlevel && this.isAlive() && !this.isBaby() && !this.isChickenJockey() && --this.eggTime <= 0) {
            if (this.dropFromGiftLootTable(serverlevel, BuiltInLootTables.CHICKEN_LAY, this::spawnAtLocation)) {
                this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                this.gameEvent(GameEvent.ENTITY_PLACE);
            }

            this.eggTime = this.random.nextInt(6000) + 6000;
        }
    }

    @Override
    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    @Override
    protected void onFlap() {
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_28262_) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHICKEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos p_28254_, BlockState p_28255_) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Nullable
    public Chicken getBreedOffspring(ServerLevel p_148884_, AgeableMob p_148885_) {
        return EntityType.CHICKEN.create(p_148884_, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean isFood(ItemStack p_28271_) {
        return p_28271_.is(ItemTags.CHICKEN_FOOD);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel p_368453_) {
        return this.isChickenJockey() ? 10 : super.getBaseExperienceReward(p_368453_);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_28243_) {
        super.readAdditionalSaveData(p_28243_);
        this.isChickenJockey = p_28243_.getBoolean("IsChickenJockey");
        if (p_28243_.contains("EggLayTime")) {
            this.eggTime = p_28243_.getInt("EggLayTime");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_28257_) {
        super.addAdditionalSaveData(p_28257_);
        p_28257_.putBoolean("IsChickenJockey", this.isChickenJockey);
        p_28257_.putInt("EggLayTime", this.eggTime);
    }

    @Override
    public boolean removeWhenFarAway(double p_28266_) {
        return this.isChickenJockey();
    }

    @Override
    protected void positionRider(Entity p_289537_, Entity.MoveFunction p_289541_) {
        super.positionRider(p_289537_, p_289541_);
        if (p_289537_ instanceof LivingEntity) {
            ((LivingEntity)p_289537_).yBodyRot = this.yBodyRot;
        }
    }

    public boolean isChickenJockey() {
        return this.isChickenJockey;
    }

    public void setChickenJockey(boolean p_28274_) {
        this.isChickenJockey = p_28274_;
    }
}