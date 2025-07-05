package net.minecraft.world.entity.animal;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Fox extends Animal implements VariantHolder<Fox.Variant> {
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.BYTE);
    private static final int FLAG_SITTING = 1;
    public static final int FLAG_CROUCHING = 4;
    public static final int FLAG_INTERESTED = 8;
    public static final int FLAG_POUNCING = 16;
    private static final int FLAG_SLEEPING = 32;
    private static final int FLAG_FACEPLANTED = 64;
    private static final int FLAG_DEFENDING = 128;
    private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_0 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_1 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_UUID);
    static final Predicate<ItemEntity> ALLOWED_ITEMS = p_359161_ -> !p_359161_.hasPickUpDelay() && p_359161_.isAlive();
    private static final Predicate<Entity> TRUSTED_TARGET_SELECTOR = p_287481_ -> !(p_287481_ instanceof LivingEntity livingentity)
            ? false
            : livingentity.getLastHurtMob() != null && livingentity.getLastHurtMobTimestamp() < livingentity.tickCount + 600;
    static final Predicate<Entity> STALKABLE_PREY = p_28498_ -> p_28498_ instanceof Chicken || p_28498_ instanceof Rabbit;
    private static final Predicate<Entity> AVOID_PLAYERS = p_28463_ -> !p_28463_.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(p_28463_);
    private static final int MIN_TICKS_BEFORE_EAT = 600;
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.FOX.getDimensions().scale(0.5F).withEyeHeight(0.2975F);
    private Goal landTargetGoal;
    private Goal turtleEggTargetGoal;
    private Goal fishTargetGoal;
    private float interestedAngle;
    private float interestedAngleO;
    float crouchAmount;
    float crouchAmountO;
    private int ticksSinceEaten;

    public Fox(EntityType<? extends Fox> p_28451_, Level p_28452_) {
        super(p_28451_, p_28452_);
        this.lookControl = new Fox.FoxLookControl();
        this.moveControl = new Fox.FoxMoveControl();
        this.setPathfindingMalus(PathType.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 0.0F);
        this.setCanPickUpLoot(true);
        this.getNavigation().setRequiredPathLength(32.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_334599_) {
        super.defineSynchedData(p_334599_);
        p_334599_.define(DATA_TRUSTED_ID_0, Optional.empty());
        p_334599_.define(DATA_TRUSTED_ID_1, Optional.empty());
        p_334599_.define(DATA_TYPE_ID, 0);
        p_334599_.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    protected void registerGoals() {
        this.landTargetGoal = new NearestAttackableTargetGoal<>(
            this, Animal.class, 10, false, false, (p_28604_, p_362352_) -> p_28604_ instanceof Chicken || p_28604_ instanceof Rabbit
        );
        this.turtleEggTargetGoal = new NearestAttackableTargetGoal<>(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR);
        this.fishTargetGoal = new NearestAttackableTargetGoal<>(
            this, AbstractFish.class, 20, false, false, (p_28600_, p_367025_) -> p_28600_ instanceof AbstractSchoolingFish
        );
        this.goalSelector.addGoal(0, new Fox.FoxFloatGoal());
        this.goalSelector.addGoal(0, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
        this.goalSelector.addGoal(1, new Fox.FaceplantGoal());
        this.goalSelector.addGoal(2, new Fox.FoxPanicGoal(2.2));
        this.goalSelector.addGoal(3, new Fox.FoxBreedGoal(1.0));
        this.goalSelector
            .addGoal(
                4,
                new AvoidEntityGoal<>(
                    this, Player.class, 16.0F, 1.6, 1.4, p_359165_ -> AVOID_PLAYERS.test(p_359165_) && !this.trusts(p_359165_.getUUID()) && !this.isDefending()
                )
            );
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Wolf.class, 8.0F, 1.6, 1.4, p_359162_ -> !((Wolf)p_359162_).isTame() && !this.isDefending()));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, PolarBear.class, 8.0F, 1.6, 1.4, p_28585_ -> !this.isDefending()));
        this.goalSelector.addGoal(5, new Fox.StalkPreyGoal());
        this.goalSelector.addGoal(6, new Fox.FoxPounceGoal());
        this.goalSelector.addGoal(6, new Fox.SeekShelterGoal(1.25));
        this.goalSelector.addGoal(7, new Fox.FoxMeleeAttackGoal(1.2F, true));
        this.goalSelector.addGoal(7, new Fox.SleepGoal());
        this.goalSelector.addGoal(8, new Fox.FoxFollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(9, new Fox.FoxStrollThroughVillageGoal(32, 200));
        this.goalSelector.addGoal(10, new Fox.FoxEatBerriesGoal(1.2F, 12, 1));
        this.goalSelector.addGoal(10, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(11, new Fox.FoxSearchForItemsGoal());
        this.goalSelector.addGoal(12, new Fox.FoxLookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(13, new Fox.PerchAndSearchGoal());
        this.targetSelector
            .addGoal(
                3,
                new Fox.DefendTrustedTargetGoal(
                    LivingEntity.class, false, false, (p_359163_, p_359164_) -> TRUSTED_TARGET_SELECTOR.test(p_359163_) && !this.trusts(p_359163_.getUUID())
                )
            );
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
            this.ticksSinceEaten++;
            ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (this.canEat(itemstack)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack itemstack1 = itemstack.finishUsingItem(this.level(), this);
                    if (!itemstack1.isEmpty()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, itemstack1);
                    }

                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1F) {
                    this.playEatingSound();
                    this.level().broadcastEntityEvent(this, (byte)45);
                }
            }

            LivingEntity livingentity = this.getTarget();
            if (livingentity == null || !livingentity.isAlive()) {
                this.setIsCrouching(false);
                this.setIsInterested(false);
            }
        }

        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }

        super.aiStep();
        if (this.isDefending() && this.random.nextFloat() < 0.05F) {
            this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
        }
    }

    @Override
    protected boolean isImmobile() {
        return this.isDeadOrDying();
    }

    private boolean canEat(ItemStack p_28598_) {
        return p_28598_.has(DataComponents.FOOD) && this.getTarget() == null && this.onGround() && !this.isSleeping();
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource p_218171_, DifficultyInstance p_218172_) {
        if (p_218171_.nextFloat() < 0.2F) {
            float f = p_218171_.nextFloat();
            ItemStack itemstack;
            if (f < 0.05F) {
                itemstack = new ItemStack(Items.EMERALD);
            } else if (f < 0.2F) {
                itemstack = new ItemStack(Items.EGG);
            } else if (f < 0.4F) {
                itemstack = p_218171_.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
            } else if (f < 0.6F) {
                itemstack = new ItemStack(Items.WHEAT);
            } else if (f < 0.8F) {
                itemstack = new ItemStack(Items.LEATHER);
            } else {
                itemstack = new ItemStack(Items.FEATHER);
            }

            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
        }
    }

    @Override
    public void handleEntityEvent(byte p_28456_) {
        if (p_28456_ == 45) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemstack.isEmpty()) {
                for (int i = 0; i < 8; i++) {
                    Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0)
                        .xRot(-this.getXRot() * (float) (Math.PI / 180.0))
                        .yRot(-this.getYRot() * (float) (Math.PI / 180.0));
                    this.level()
                        .addParticle(
                            new ItemParticleOption(ParticleTypes.ITEM, itemstack),
                            this.getX() + this.getLookAngle().x / 2.0,
                            this.getY(),
                            this.getZ() + this.getLookAngle().z / 2.0,
                            vec3.x,
                            vec3.y + 0.05,
                            vec3.z
                        );
                }
            }
        } else {
            super.handleEntityEvent(p_28456_);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.3F)
            .add(Attributes.MAX_HEALTH, 10.0)
            .add(Attributes.ATTACK_DAMAGE, 2.0)
            .add(Attributes.SAFE_FALL_DISTANCE, 5.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Nullable
    public Fox getBreedOffspring(ServerLevel p_148912_, AgeableMob p_148913_) {
        Fox fox = EntityType.FOX.create(p_148912_, EntitySpawnReason.BREEDING);
        if (fox != null) {
            fox.setVariant(this.random.nextBoolean() ? this.getVariant() : ((Fox)p_148913_).getVariant());
        }

        return fox;
    }

    public static boolean checkFoxSpawnRules(EntityType<Fox> p_218176_, LevelAccessor p_218177_, EntitySpawnReason p_363927_, BlockPos p_218179_, RandomSource p_218180_) {
        return p_218177_.getBlockState(p_218179_.below()).is(BlockTags.FOXES_SPAWNABLE_ON) && isBrightEnoughToSpawn(p_218177_, p_218179_);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_28487_, DifficultyInstance p_28488_, EntitySpawnReason p_365391_, @Nullable SpawnGroupData p_28490_) {
        Holder<Biome> holder = p_28487_.getBiome(this.blockPosition());
        Fox.Variant fox$variant = Fox.Variant.byBiome(holder);
        boolean flag = false;
        if (p_28490_ instanceof Fox.FoxGroupData fox$foxgroupdata) {
            fox$variant = fox$foxgroupdata.variant;
            if (fox$foxgroupdata.getGroupSize() >= 2) {
                flag = true;
            }
        } else {
            p_28490_ = new Fox.FoxGroupData(fox$variant);
        }

        this.setVariant(fox$variant);
        if (flag) {
            this.setAge(-24000);
        }

        if (p_28487_ instanceof ServerLevel) {
            this.setTargetGoals();
        }

        this.populateDefaultEquipmentSlots(p_28487_.getRandom(), p_28488_);
        return super.finalizeSpawn(p_28487_, p_28488_, p_365391_, p_28490_);
    }

    private void setTargetGoals() {
        if (this.getVariant() == Fox.Variant.RED) {
            this.targetSelector.addGoal(4, this.landTargetGoal);
            this.targetSelector.addGoal(4, this.turtleEggTargetGoal);
            this.targetSelector.addGoal(6, this.fishTargetGoal);
        } else {
            this.targetSelector.addGoal(4, this.fishTargetGoal);
            this.targetSelector.addGoal(6, this.landTargetGoal);
            this.targetSelector.addGoal(6, this.turtleEggTargetGoal);
        }
    }

    @Override
    protected void playEatingSound() {
        this.playSound(SoundEvents.FOX_EAT, 1.0F, 1.0F);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose p_327774_) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(p_327774_);
    }

    public Fox.Variant getVariant() {
        return Fox.Variant.byId(this.entityData.get(DATA_TYPE_ID));
    }

    public void setVariant(Fox.Variant p_367787_) {
        this.entityData.set(DATA_TYPE_ID, p_367787_.getId());
    }

    List<UUID> getTrustedUUIDs() {
        Optional<UUID> optional = this.entityData.get(DATA_TRUSTED_ID_0);
        Optional<UUID> optional1 = this.entityData.get(DATA_TRUSTED_ID_1);
        if (optional.isPresent() && optional1.isPresent()) {
            return List.of(optional.get(), optional1.get());
        } else if (optional.isPresent()) {
            return List.of(optional.get());
        } else {
            return optional1.isPresent() ? List.of(optional1.get()) : List.of();
        }
    }

    void addTrustedUUID(@Nullable UUID p_28516_) {
        if (this.entityData.get(DATA_TRUSTED_ID_0).isPresent()) {
            this.entityData.set(DATA_TRUSTED_ID_1, Optional.ofNullable(p_28516_));
        } else {
            this.entityData.set(DATA_TRUSTED_ID_0, Optional.ofNullable(p_28516_));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_28518_) {
        super.addAdditionalSaveData(p_28518_);
        List<UUID> list = this.getTrustedUUIDs();
        ListTag listtag = new ListTag();

        for (UUID uuid : list) {
            listtag.add(NbtUtils.createUUID(uuid));
        }

        p_28518_.put("Trusted", listtag);
        p_28518_.putBoolean("Sleeping", this.isSleeping());
        p_28518_.putString("Type", this.getVariant().getSerializedName());
        p_28518_.putBoolean("Sitting", this.isSitting());
        p_28518_.putBoolean("Crouching", this.isCrouching());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_28493_) {
        super.readAdditionalSaveData(p_28493_);

        for (Tag tag : p_28493_.getList("Trusted", 11)) {
            this.addTrustedUUID(NbtUtils.loadUUID(tag));
        }

        this.setSleeping(p_28493_.getBoolean("Sleeping"));
        this.setVariant(Fox.Variant.byName(p_28493_.getString("Type")));
        this.setSitting(p_28493_.getBoolean("Sitting"));
        this.setIsCrouching(p_28493_.getBoolean("Crouching"));
        if (this.level() instanceof ServerLevel) {
            this.setTargetGoals();
        }
    }

    public boolean isSitting() {
        return this.getFlag(1);
    }

    public void setSitting(boolean p_28611_) {
        this.setFlag(1, p_28611_);
    }

    public boolean isFaceplanted() {
        return this.getFlag(64);
    }

    void setFaceplanted(boolean p_28619_) {
        this.setFlag(64, p_28619_);
    }

    boolean isDefending() {
        return this.getFlag(128);
    }

    void setDefending(boolean p_28623_) {
        this.setFlag(128, p_28623_);
    }

    @Override
    public boolean isSleeping() {
        return this.getFlag(32);
    }

    void setSleeping(boolean p_28627_) {
        this.setFlag(32, p_28627_);
    }

    private void setFlag(int p_28533_, boolean p_28534_) {
        if (p_28534_) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | p_28533_));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~p_28533_));
        }
    }

    private boolean getFlag(int p_28609_) {
        return (this.entityData.get(DATA_FLAGS_ID) & p_28609_) != 0;
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot p_361716_) {
        return p_361716_ == EquipmentSlot.MAINHAND && this.canPickUpLoot();
    }

    @Override
    public boolean canHoldItem(ItemStack p_28578_) {
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return itemstack.isEmpty() || this.ticksSinceEaten > 0 && p_28578_.has(DataComponents.FOOD) && !itemstack.has(DataComponents.FOOD);
    }

    private void spitOutItem(ItemStack p_28602_) {
        if (!p_28602_.isEmpty() && !this.level().isClientSide) {
            ItemEntity itementity = new ItemEntity(
                this.level(), this.getX() + this.getLookAngle().x, this.getY() + 1.0, this.getZ() + this.getLookAngle().z, p_28602_
            );
            itementity.setPickUpDelay(40);
            itementity.setThrower(this);
            this.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
            this.level().addFreshEntity(itementity);
        }
    }

    private void dropItemStack(ItemStack p_28606_) {
        ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), p_28606_);
        this.level().addFreshEntity(itementity);
    }

    @Override
    protected void pickUpItem(ServerLevel p_369173_, ItemEntity p_28514_) {
        ItemStack itemstack = p_28514_.getItem();
        if (this.canHoldItem(itemstack)) {
            int i = itemstack.getCount();
            if (i > 1) {
                this.dropItemStack(itemstack.split(i - 1));
            }

            this.spitOutItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
            this.onItemPickup(p_28514_);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack.split(1));
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take(p_28514_, itemstack.getCount());
            p_28514_.discard();
            this.ticksSinceEaten = 0;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isEffectiveAi()) {
            boolean flag = this.isInWater();
            if (flag || this.getTarget() != null || this.level().isThundering()) {
                this.wakeUp();
            }

            if (flag || this.isSleeping()) {
                this.setSitting(false);
            }

            if (this.isFaceplanted() && this.level().random.nextFloat() < 0.2F) {
                BlockPos blockpos = this.blockPosition();
                BlockState blockstate = this.level().getBlockState(blockpos);
                this.level().levelEvent(2001, blockpos, Block.getId(blockstate));
            }
        }

        this.interestedAngleO = this.interestedAngle;
        if (this.isInterested()) {
            this.interestedAngle = this.interestedAngle + (1.0F - this.interestedAngle) * 0.4F;
        } else {
            this.interestedAngle = this.interestedAngle + (0.0F - this.interestedAngle) * 0.4F;
        }

        this.crouchAmountO = this.crouchAmount;
        if (this.isCrouching()) {
            this.crouchAmount += 0.2F;
            if (this.crouchAmount > 3.0F) {
                this.crouchAmount = 3.0F;
            }
        } else {
            this.crouchAmount = 0.0F;
        }
    }

    @Override
    public boolean isFood(ItemStack p_28594_) {
        return p_28594_.is(ItemTags.FOX_FOOD);
    }

    @Override
    protected void onOffspringSpawnedFromEgg(Player p_28481_, Mob p_28482_) {
        ((Fox)p_28482_).addTrustedUUID(p_28481_.getUUID());
    }

    public boolean isPouncing() {
        return this.getFlag(16);
    }

    public void setIsPouncing(boolean p_28613_) {
        this.setFlag(16, p_28613_);
    }

    public boolean isJumping() {
        return this.jumping;
    }

    public boolean isFullyCrouched() {
        return this.crouchAmount == 3.0F;
    }

    public void setIsCrouching(boolean p_28615_) {
        this.setFlag(4, p_28615_);
    }

    @Override
    public boolean isCrouching() {
        return this.getFlag(4);
    }

    public void setIsInterested(boolean p_28617_) {
        this.setFlag(8, p_28617_);
    }

    public boolean isInterested() {
        return this.getFlag(8);
    }

    public float getHeadRollAngle(float p_28621_) {
        return Mth.lerp(p_28621_, this.interestedAngleO, this.interestedAngle) * 0.11F * (float) Math.PI;
    }

    public float getCrouchAmount(float p_28625_) {
        return Mth.lerp(p_28625_, this.crouchAmountO, this.crouchAmount);
    }

    @Override
    public void setTarget(@Nullable LivingEntity p_28574_) {
        if (this.isDefending() && p_28574_ == null) {
            this.setDefending(false);
        }

        super.setTarget(p_28574_);
    }

    void wakeUp() {
        this.setSleeping(false);
    }

    void clearStates() {
        this.setIsInterested(false);
        this.setIsCrouching(false);
        this.setSitting(false);
        this.setSleeping(false);
        this.setDefending(false);
        this.setFaceplanted(false);
    }

    boolean canMove() {
        return !this.isSleeping() && !this.isSitting() && !this.isFaceplanted();
    }

    @Override
    public void playAmbientSound() {
        SoundEvent soundevent = this.getAmbientSound();
        if (soundevent == SoundEvents.FOX_SCREECH) {
            this.playSound(soundevent, 2.0F, this.getVoicePitch());
        } else {
            super.playAmbientSound();
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSleeping()) {
            return SoundEvents.FOX_SLEEP;
        } else {
            if (!this.level().isDay() && this.random.nextFloat() < 0.1F) {
                List<Player> list = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0, 16.0, 16.0), EntitySelector.NO_SPECTATORS);
                if (list.isEmpty()) {
                    return SoundEvents.FOX_SCREECH;
                }
            }

            return SoundEvents.FOX_AMBIENT;
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_28548_) {
        return SoundEvents.FOX_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    boolean trusts(UUID p_28530_) {
        return this.getTrustedUUIDs().contains(p_28530_);
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel p_343480_, DamageSource p_28536_) {
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!itemstack.isEmpty()) {
            this.spawnAtLocation(p_343480_, itemstack);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        super.dropAllDeathLoot(p_343480_, p_28536_);
    }

    public static boolean isPathClear(Fox p_28472_, LivingEntity p_28473_) {
        double d0 = p_28473_.getZ() - p_28472_.getZ();
        double d1 = p_28473_.getX() - p_28472_.getX();
        double d2 = d0 / d1;
        int i = 6;

        for (int j = 0; j < 6; j++) {
            double d3 = d2 == 0.0 ? 0.0 : d0 * (double)((float)j / 6.0F);
            double d4 = d2 == 0.0 ? d1 * (double)((float)j / 6.0F) : d3 / d2;

            for (int k = 1; k < 4; k++) {
                if (!p_28472_.level()
                    .getBlockState(BlockPos.containing(p_28472_.getX() + d4, p_28472_.getY() + (double)k, p_28472_.getZ() + d3))
                    .canBeReplaced()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.55F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    class DefendTrustedTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {
        @Nullable
        private LivingEntity trustedLastHurtBy;
        @Nullable
        private LivingEntity trustedLastHurt;
        private int timestamp;

        public DefendTrustedTargetGoal(
            final Class<LivingEntity> p_28634_, final boolean p_28635_, final boolean p_28636_, @Nullable final TargetingConditions.Selector p_363693_
        ) {
            super(Fox.this, p_28634_, 10, p_28635_, p_28636_, p_363693_);
        }

        @Override
        public boolean canUse() {
            if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
                return false;
            } else {
                ServerLevel serverlevel = getServerLevel(Fox.this.level());

                for (UUID uuid : Fox.this.getTrustedUUIDs()) {
                    if (serverlevel.getEntity(uuid) instanceof LivingEntity livingentity) {
                        this.trustedLastHurt = livingentity;
                        this.trustedLastHurtBy = livingentity.getLastHurtByMob();
                        int i = livingentity.getLastHurtByMobTimestamp();
                        return i != this.timestamp && this.canAttack(this.trustedLastHurtBy, this.targetConditions);
                    }
                }

                return false;
            }
        }

        @Override
        public void start() {
            this.setTarget(this.trustedLastHurtBy);
            this.target = this.trustedLastHurtBy;
            if (this.trustedLastHurt != null) {
                this.timestamp = this.trustedLastHurt.getLastHurtByMobTimestamp();
            }

            Fox.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
            Fox.this.setDefending(true);
            Fox.this.wakeUp();
            super.start();
        }
    }

    class FaceplantGoal extends Goal {
        int countdown;

        public FaceplantGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return Fox.this.isFaceplanted();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && this.countdown > 0;
        }

        @Override
        public void start() {
            this.countdown = this.adjustedTickDelay(40);
        }

        @Override
        public void stop() {
            Fox.this.setFaceplanted(false);
        }

        @Override
        public void tick() {
            this.countdown--;
        }
    }

    public class FoxAlertableEntitiesSelector implements TargetingConditions.Selector {
        @Override
        public boolean test(LivingEntity p_28653_, ServerLevel p_366449_) {
            if (p_28653_ instanceof Fox) {
                return false;
            } else if (p_28653_ instanceof Chicken || p_28653_ instanceof Rabbit || p_28653_ instanceof Monster) {
                return true;
            } else if (p_28653_ instanceof TamableAnimal) {
                return !((TamableAnimal)p_28653_).isTame();
            } else if (!(p_28653_ instanceof Player) || !p_28653_.isSpectator() && !((Player)p_28653_).isCreative()) {
                return Fox.this.trusts(p_28653_.getUUID()) ? false : !p_28653_.isSleeping() && !p_28653_.isDiscrete();
            } else {
                return false;
            }
        }
    }

    abstract class FoxBehaviorGoal extends Goal {
        private final TargetingConditions alertableTargeting = TargetingConditions.forCombat()
            .range(12.0)
            .ignoreLineOfSight()
            .selector(Fox.this.new FoxAlertableEntitiesSelector());

        protected boolean hasShelter() {
            BlockPos blockpos = BlockPos.containing(Fox.this.getX(), Fox.this.getBoundingBox().maxY, Fox.this.getZ());
            return !Fox.this.level().canSeeSky(blockpos) && Fox.this.getWalkTargetValue(blockpos) >= 0.0F;
        }

        protected boolean alertable() {
            return !getServerLevel(Fox.this.level())
                .getNearbyEntities(LivingEntity.class, this.alertableTargeting, Fox.this, Fox.this.getBoundingBox().inflate(12.0, 6.0, 12.0))
                .isEmpty();
        }
    }

    class FoxBreedGoal extends BreedGoal {
        public FoxBreedGoal(final double p_28668_) {
            super(Fox.this, p_28668_);
        }

        @Override
        public void start() {
            ((Fox)this.animal).clearStates();
            ((Fox)this.partner).clearStates();
            super.start();
        }

        @Override
        protected void breed() {
            ServerLevel serverlevel = this.level;
            Fox fox = (Fox)this.animal.getBreedOffspring(serverlevel, this.partner);
            if (fox != null) {
                ServerPlayer serverplayer = this.animal.getLoveCause();
                ServerPlayer serverplayer1 = this.partner.getLoveCause();
                ServerPlayer serverplayer2 = serverplayer;
                if (serverplayer != null) {
                    fox.addTrustedUUID(serverplayer.getUUID());
                } else {
                    serverplayer2 = serverplayer1;
                }

                if (serverplayer1 != null && serverplayer != serverplayer1) {
                    fox.addTrustedUUID(serverplayer1.getUUID());
                }

                if (serverplayer2 != null) {
                    serverplayer2.awardStat(Stats.ANIMALS_BRED);
                    CriteriaTriggers.BRED_ANIMALS.trigger(serverplayer2, this.animal, this.partner, fox);
                }

                this.animal.setAge(6000);
                this.partner.setAge(6000);
                this.animal.resetLove();
                this.partner.resetLove();
                fox.setAge(-24000);
                fox.moveTo(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
                serverlevel.addFreshEntityWithPassengers(fox);
                this.level.broadcastEntityEvent(this.animal, (byte)18);
                if (serverlevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                    this.level
                        .addFreshEntity(
                            new ExperienceOrb(
                                this.level,
                                this.animal.getX(),
                                this.animal.getY(),
                                this.animal.getZ(),
                                this.animal.getRandom().nextInt(7) + 1
                            )
                        );
                }
            }
        }
    }

    public class FoxEatBerriesGoal extends MoveToBlockGoal {
        private static final int WAIT_TICKS = 40;
        protected int ticksWaited;

        public FoxEatBerriesGoal(final double p_28675_, final int p_28676_, final int p_28677_) {
            super(Fox.this, p_28675_, p_28676_, p_28677_);
        }

        @Override
        public double acceptedDistance() {
            return 2.0;
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 100 == 0;
        }

        @Override
        protected boolean isValidTarget(LevelReader p_28680_, BlockPos p_28681_) {
            BlockState blockstate = p_28680_.getBlockState(p_28681_);
            return blockstate.is(Blocks.SWEET_BERRY_BUSH) && blockstate.getValue(SweetBerryBushBlock.AGE) >= 2 || CaveVines.hasGlowBerries(blockstate);
        }

        @Override
        public void tick() {
            if (this.isReachedTarget()) {
                if (this.ticksWaited >= 40) {
                    this.onReachedTarget();
                } else {
                    this.ticksWaited++;
                }
            } else if (!this.isReachedTarget() && Fox.this.random.nextFloat() < 0.05F) {
                Fox.this.playSound(SoundEvents.FOX_SNIFF, 1.0F, 1.0F);
            }

            super.tick();
        }

        protected void onReachedTarget() {
            if (getServerLevel(Fox.this.level()).getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                BlockState blockstate = Fox.this.level().getBlockState(this.blockPos);
                if (blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                    this.pickSweetBerries(blockstate);
                } else if (CaveVines.hasGlowBerries(blockstate)) {
                    this.pickGlowBerry(blockstate);
                }
            }
        }

        private void pickGlowBerry(BlockState p_148927_) {
            CaveVines.use(Fox.this, p_148927_, Fox.this.level(), this.blockPos);
        }

        private void pickSweetBerries(BlockState p_148929_) {
            int i = p_148929_.getValue(SweetBerryBushBlock.AGE);
            p_148929_.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1));
            int j = 1 + Fox.this.level().random.nextInt(2) + (i == 3 ? 1 : 0);
            ItemStack itemstack = Fox.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (itemstack.isEmpty()) {
                Fox.this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
                j--;
            }

            if (j > 0) {
                Block.popResource(Fox.this.level(), this.blockPos, new ItemStack(Items.SWEET_BERRIES, j));
            }

            Fox.this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
            Fox.this.level().setBlock(this.blockPos, p_148929_.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1)), 2);
            Fox.this.level().gameEvent(GameEvent.BLOCK_CHANGE, this.blockPos, GameEvent.Context.of(Fox.this));
        }

        @Override
        public boolean canUse() {
            return !Fox.this.isSleeping() && super.canUse();
        }

        @Override
        public void start() {
            this.ticksWaited = 0;
            Fox.this.setSitting(false);
            super.start();
        }
    }

    class FoxFloatGoal extends FloatGoal {
        public FoxFloatGoal() {
            super(Fox.this);
        }

        @Override
        public void start() {
            super.start();
            Fox.this.clearStates();
        }

        @Override
        public boolean canUse() {
            return Fox.this.isInWater() && Fox.this.getFluidHeight(FluidTags.WATER) > 0.25 || Fox.this.isInLava();
        }
    }

    static class FoxFollowParentGoal extends FollowParentGoal {
        private final Fox fox;

        public FoxFollowParentGoal(Fox p_28695_, double p_28697_) {
            super(p_28695_, p_28697_);
            this.fox = p_28695_;
        }

        @Override
        public boolean canUse() {
            return !this.fox.isDefending() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.fox.isDefending() && super.canContinueToUse();
        }

        @Override
        public void start() {
            this.fox.clearStates();
            super.start();
        }
    }

    public static class FoxGroupData extends AgeableMob.AgeableMobGroupData {
        public final Fox.Variant variant;

        public FoxGroupData(Fox.Variant p_366131_) {
            super(false);
            this.variant = p_366131_;
        }
    }

    class FoxLookAtPlayerGoal extends LookAtPlayerGoal {
        public FoxLookAtPlayerGoal(final Mob p_28707_, final Class<? extends LivingEntity> p_28708_, final float p_28709_) {
            super(p_28707_, p_28708_, p_28709_);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !Fox.this.isFaceplanted() && !Fox.this.isInterested();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !Fox.this.isFaceplanted() && !Fox.this.isInterested();
        }
    }

    public class FoxLookControl extends LookControl {
        public FoxLookControl() {
            super(Fox.this);
        }

        @Override
        public void tick() {
            if (!Fox.this.isSleeping()) {
                super.tick();
            }
        }

        @Override
        protected boolean resetXRotOnTick() {
            return !Fox.this.isPouncing() && !Fox.this.isCrouching() && !Fox.this.isInterested() && !Fox.this.isFaceplanted();
        }
    }

    class FoxMeleeAttackGoal extends MeleeAttackGoal {
        public FoxMeleeAttackGoal(final double p_28720_, final boolean p_28721_) {
            super(Fox.this, p_28720_, p_28721_);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity p_28724_) {
            if (this.canPerformAttack(p_28724_)) {
                this.resetAttackCooldown();
                this.mob.doHurtTarget(getServerLevel(this.mob), p_28724_);
                Fox.this.playSound(SoundEvents.FOX_BITE, 1.0F, 1.0F);
            }
        }

        @Override
        public void start() {
            Fox.this.setIsInterested(false);
            super.start();
        }

        @Override
        public boolean canUse() {
            return !Fox.this.isSitting() && !Fox.this.isSleeping() && !Fox.this.isCrouching() && !Fox.this.isFaceplanted() && super.canUse();
        }
    }

    class FoxMoveControl extends MoveControl {
        public FoxMoveControl() {
            super(Fox.this);
        }

        @Override
        public void tick() {
            if (Fox.this.canMove()) {
                super.tick();
            }
        }
    }

    class FoxPanicGoal extends PanicGoal {
        public FoxPanicGoal(final double p_28734_) {
            super(Fox.this, p_28734_);
        }

        @Override
        public boolean shouldPanic() {
            return !Fox.this.isDefending() && super.shouldPanic();
        }
    }

    public class FoxPounceGoal extends JumpGoal {
        @Override
        public boolean canUse() {
            if (!Fox.this.isFullyCrouched()) {
                return false;
            } else {
                LivingEntity livingentity = Fox.this.getTarget();
                if (livingentity != null && livingentity.isAlive()) {
                    if (livingentity.getMotionDirection() != livingentity.getDirection()) {
                        return false;
                    } else {
                        boolean flag = Fox.isPathClear(Fox.this, livingentity);
                        if (!flag) {
                            Fox.this.getNavigation().createPath(livingentity, 0);
                            Fox.this.setIsCrouching(false);
                            Fox.this.setIsInterested(false);
                        }

                        return flag;
                    }
                } else {
                    return false;
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingentity = Fox.this.getTarget();
            if (livingentity != null && livingentity.isAlive()) {
                double d0 = Fox.this.getDeltaMovement().y;
                return (!(d0 * d0 < 0.05F) || !(Math.abs(Fox.this.getXRot()) < 15.0F) || !Fox.this.onGround()) && !Fox.this.isFaceplanted();
            } else {
                return false;
            }
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public void start() {
            Fox.this.setJumping(true);
            Fox.this.setIsPouncing(true);
            Fox.this.setIsInterested(false);
            LivingEntity livingentity = Fox.this.getTarget();
            if (livingentity != null) {
                Fox.this.getLookControl().setLookAt(livingentity, 60.0F, 30.0F);
                Vec3 vec3 = new Vec3(
                        livingentity.getX() - Fox.this.getX(),
                        livingentity.getY() - Fox.this.getY(),
                        livingentity.getZ() - Fox.this.getZ()
                    )
                    .normalize();
                Fox.this.setDeltaMovement(Fox.this.getDeltaMovement().add(vec3.x * 0.8, 0.9, vec3.z * 0.8));
            }

            Fox.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            Fox.this.setIsCrouching(false);
            Fox.this.crouchAmount = 0.0F;
            Fox.this.crouchAmountO = 0.0F;
            Fox.this.setIsInterested(false);
            Fox.this.setIsPouncing(false);
        }

        @Override
        public void tick() {
            LivingEntity livingentity = Fox.this.getTarget();
            if (livingentity != null) {
                Fox.this.getLookControl().setLookAt(livingentity, 60.0F, 30.0F);
            }

            if (!Fox.this.isFaceplanted()) {
                Vec3 vec3 = Fox.this.getDeltaMovement();
                if (vec3.y * vec3.y < 0.03F && Fox.this.getXRot() != 0.0F) {
                    Fox.this.setXRot(Mth.rotLerp(0.2F, Fox.this.getXRot(), 0.0F));
                } else {
                    double d0 = vec3.horizontalDistance();
                    double d1 = Math.signum(-vec3.y) * Math.acos(d0 / vec3.length()) * 180.0F / (float)Math.PI;
                    Fox.this.setXRot((float)d1);
                }
            }

            if (livingentity != null && Fox.this.distanceTo(livingentity) <= 2.0F) {
                Fox.this.doHurtTarget(getServerLevel(Fox.this.level()), livingentity);
            } else if (Fox.this.getXRot() > 0.0F
                && Fox.this.onGround()
                && (float)Fox.this.getDeltaMovement().y != 0.0F
                && Fox.this.level().getBlockState(Fox.this.blockPosition()).is(Blocks.SNOW)) {
                Fox.this.setXRot(60.0F);
                Fox.this.setTarget(null);
                Fox.this.setFaceplanted(true);
            }
        }
    }

    class FoxSearchForItemsGoal extends Goal {
        public FoxSearchForItemsGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                return false;
            } else if (Fox.this.getTarget() != null || Fox.this.getLastHurtByMob() != null) {
                return false;
            } else if (!Fox.this.canMove()) {
                return false;
            } else if (Fox.this.getRandom().nextInt(reducedTickDelay(10)) != 0) {
                return false;
            } else {
                List<ItemEntity> list = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Fox.ALLOWED_ITEMS);
                return !list.isEmpty() && Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
            }
        }

        @Override
        public void tick() {
            List<ItemEntity> list = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Fox.ALLOWED_ITEMS);
            ItemStack itemstack = Fox.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (itemstack.isEmpty() && !list.isEmpty()) {
                Fox.this.getNavigation().moveTo(list.get(0), 1.2F);
            }
        }

        @Override
        public void start() {
            List<ItemEntity> list = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Fox.ALLOWED_ITEMS);
            if (!list.isEmpty()) {
                Fox.this.getNavigation().moveTo(list.get(0), 1.2F);
            }
        }
    }

    class FoxStrollThroughVillageGoal extends StrollThroughVillageGoal {
        public FoxStrollThroughVillageGoal(final int p_28754_, final int p_28755_) {
            super(Fox.this, p_28755_);
        }

        @Override
        public void start() {
            Fox.this.clearStates();
            super.start();
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.canFoxMove();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.canFoxMove();
        }

        private boolean canFoxMove() {
            return !Fox.this.isSleeping() && !Fox.this.isSitting() && !Fox.this.isDefending() && Fox.this.getTarget() == null;
        }
    }

    class PerchAndSearchGoal extends Fox.FoxBehaviorGoal {
        private double relX;
        private double relZ;
        private int lookTime;
        private int looksRemaining;

        public PerchAndSearchGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return Fox.this.getLastHurtByMob() == null
                && Fox.this.getRandom().nextFloat() < 0.02F
                && !Fox.this.isSleeping()
                && Fox.this.getTarget() == null
                && Fox.this.getNavigation().isDone()
                && !this.alertable()
                && !Fox.this.isPouncing()
                && !Fox.this.isCrouching();
        }

        @Override
        public boolean canContinueToUse() {
            return this.looksRemaining > 0;
        }

        @Override
        public void start() {
            this.resetLook();
            this.looksRemaining = 2 + Fox.this.getRandom().nextInt(3);
            Fox.this.setSitting(true);
            Fox.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            Fox.this.setSitting(false);
        }

        @Override
        public void tick() {
            this.lookTime--;
            if (this.lookTime <= 0) {
                this.looksRemaining--;
                this.resetLook();
            }

            Fox.this.getLookControl()
                .setLookAt(
                    Fox.this.getX() + this.relX,
                    Fox.this.getEyeY(),
                    Fox.this.getZ() + this.relZ,
                    (float)Fox.this.getMaxHeadYRot(),
                    (float)Fox.this.getMaxHeadXRot()
                );
        }

        private void resetLook() {
            double d0 = (Math.PI * 2) * Fox.this.getRandom().nextDouble();
            this.relX = Math.cos(d0);
            this.relZ = Math.sin(d0);
            this.lookTime = this.adjustedTickDelay(80 + Fox.this.getRandom().nextInt(20));
        }
    }

    class SeekShelterGoal extends FleeSunGoal {
        private int interval = reducedTickDelay(100);

        public SeekShelterGoal(final double p_28777_) {
            super(Fox.this, p_28777_);
        }

        @Override
        public boolean canUse() {
            if (!Fox.this.isSleeping() && this.mob.getTarget() == null) {
                if (Fox.this.level().isThundering() && Fox.this.level().canSeeSky(this.mob.blockPosition())) {
                    return this.setWantedPos();
                } else if (this.interval > 0) {
                    this.interval--;
                    return false;
                } else {
                    this.interval = 100;
                    BlockPos blockpos = this.mob.blockPosition();
                    return Fox.this.level().isDay()
                        && Fox.this.level().canSeeSky(blockpos)
                        && !((ServerLevel)Fox.this.level()).isVillage(blockpos)
                        && this.setWantedPos();
                }
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            Fox.this.clearStates();
            super.start();
        }
    }

    class SleepGoal extends Fox.FoxBehaviorGoal {
        private static final int WAIT_TIME_BEFORE_SLEEP = reducedTickDelay(140);
        private int countdown = Fox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);

        public SleepGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return Fox.this.xxa == 0.0F && Fox.this.yya == 0.0F && Fox.this.zza == 0.0F ? this.canSleep() || Fox.this.isSleeping() : false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canSleep();
        }

        private boolean canSleep() {
            if (this.countdown > 0) {
                this.countdown--;
                return false;
            } else {
                return Fox.this.level().isDay() && this.hasShelter() && !this.alertable() && !Fox.this.isInPowderSnow;
            }
        }

        @Override
        public void stop() {
            this.countdown = Fox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            Fox.this.clearStates();
        }

        @Override
        public void start() {
            Fox.this.setSitting(false);
            Fox.this.setIsCrouching(false);
            Fox.this.setIsInterested(false);
            Fox.this.setJumping(false);
            Fox.this.setSleeping(true);
            Fox.this.getNavigation().stop();
            Fox.this.getMoveControl().setWantedPosition(Fox.this.getX(), Fox.this.getY(), Fox.this.getZ(), 0.0);
        }
    }

    class StalkPreyGoal extends Goal {
        public StalkPreyGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (Fox.this.isSleeping()) {
                return false;
            } else {
                LivingEntity livingentity = Fox.this.getTarget();
                return livingentity != null
                    && livingentity.isAlive()
                    && Fox.STALKABLE_PREY.test(livingentity)
                    && Fox.this.distanceToSqr(livingentity) > 36.0
                    && !Fox.this.isCrouching()
                    && !Fox.this.isInterested()
                    && !Fox.this.jumping;
            }
        }

        @Override
        public void start() {
            Fox.this.setSitting(false);
            Fox.this.setFaceplanted(false);
        }

        @Override
        public void stop() {
            LivingEntity livingentity = Fox.this.getTarget();
            if (livingentity != null && Fox.isPathClear(Fox.this, livingentity)) {
                Fox.this.setIsInterested(true);
                Fox.this.setIsCrouching(true);
                Fox.this.getNavigation().stop();
                Fox.this.getLookControl().setLookAt(livingentity, (float)Fox.this.getMaxHeadYRot(), (float)Fox.this.getMaxHeadXRot());
            } else {
                Fox.this.setIsInterested(false);
                Fox.this.setIsCrouching(false);
            }
        }

        @Override
        public void tick() {
            LivingEntity livingentity = Fox.this.getTarget();
            if (livingentity != null) {
                Fox.this.getLookControl().setLookAt(livingentity, (float)Fox.this.getMaxHeadYRot(), (float)Fox.this.getMaxHeadXRot());
                if (Fox.this.distanceToSqr(livingentity) <= 36.0) {
                    Fox.this.setIsInterested(true);
                    Fox.this.setIsCrouching(true);
                    Fox.this.getNavigation().stop();
                } else {
                    Fox.this.getNavigation().moveTo(livingentity, 1.5);
                }
            }
        }
    }

    public static enum Variant implements StringRepresentable {
        RED(0, "red"),
        SNOW(1, "snow");

        public static final StringRepresentable.EnumCodec<Fox.Variant> CODEC = StringRepresentable.fromEnum(Fox.Variant::values);
        private static final IntFunction<Fox.Variant> BY_ID = ByIdMap.continuous(Fox.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        private final int id;
        private final String name;

        private Variant(final int p_363982_, final String p_364032_) {
            this.id = p_363982_;
            this.name = p_364032_;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getId() {
            return this.id;
        }

        public static Fox.Variant byName(String p_361500_) {
            return CODEC.byName(p_361500_, RED);
        }

        public static Fox.Variant byId(int p_369301_) {
            return BY_ID.apply(p_369301_);
        }

        public static Fox.Variant byBiome(Holder<Biome> p_362553_) {
            return p_362553_.is(BiomeTags.SPAWNS_SNOW_FOXES) ? SNOW : RED;
        }
    }
}