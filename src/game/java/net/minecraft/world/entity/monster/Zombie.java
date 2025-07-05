package net.minecraft.world.entity.monster;

import com.google.common.annotations.VisibleForTesting;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Zombie extends Monster {
    private static final ResourceLocation SPEED_MODIFIER_BABY_ID = ResourceLocation.withDefaultNamespace("baby");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_ID, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final ResourceLocation REINFORCEMENT_CALLER_CHARGE_ID = ResourceLocation.withDefaultNamespace("reinforcement_caller_charge");
    private static final AttributeModifier ZOMBIE_REINFORCEMENT_CALLEE_CHARGE = new AttributeModifier(
        ResourceLocation.withDefaultNamespace("reinforcement_callee_charge"), -0.05F, AttributeModifier.Operation.ADD_VALUE
    );
    private static final ResourceLocation LEADER_ZOMBIE_BONUS_ID = ResourceLocation.withDefaultNamespace("leader_zombie_bonus");
    private static final ResourceLocation ZOMBIE_RANDOM_SPAWN_BONUS_ID = ResourceLocation.withDefaultNamespace("zombie_random_spawn_bonus");
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SPECIAL_TYPE_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_DROWNED_CONVERSION_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
    public static final float ZOMBIE_LEADER_CHANCE = 0.05F;
    public static final int REINFORCEMENT_ATTEMPTS = 50;
    public static final int REINFORCEMENT_RANGE_MAX = 40;
    public static final int REINFORCEMENT_RANGE_MIN = 7;
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ZOMBIE.getDimensions().scale(0.5F).withEyeHeight(0.93F);
    private static final float BREAK_DOOR_CHANCE = 0.1F;
    private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = p_34284_ -> p_34284_ == Difficulty.HARD;
    private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, DOOR_BREAKING_PREDICATE);
    private boolean canBreakDoors;
    private int inWaterTime;
    private int conversionTime;

    public Zombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    public Zombie(Level p_34274_) {
        this(EntityType.ZOMBIE, p_34274_);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new Zombie.ZombieAttackTurtleEggGoal(this, 1.0, 3));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, 35.0)
            .add(Attributes.MOVEMENT_SPEED, 0.23F)
            .add(Attributes.ATTACK_DAMAGE, 3.0)
            .add(Attributes.ARMOR, 2.0)
            .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_336115_) {
        super.defineSynchedData(p_336115_);
        p_336115_.define(DATA_BABY_ID, false);
        p_336115_.define(DATA_SPECIAL_TYPE_ID, 0);
        p_336115_.define(DATA_DROWNED_CONVERSION_ID, false);
    }

    public boolean isUnderWaterConverting() {
        return this.getEntityData().get(DATA_DROWNED_CONVERSION_ID);
    }

    public boolean canBreakDoors() {
        return this.canBreakDoors;
    }

    public void setCanBreakDoors(boolean p_34337_) {
        if (GoalUtils.hasGroundPathNavigation(this)) {
            if (this.canBreakDoors != p_34337_) {
                this.canBreakDoors = p_34337_;
                ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(p_34337_);
                if (p_34337_) {
                    this.goalSelector.addGoal(1, this.breakDoorGoal);
                } else {
                    this.goalSelector.removeGoal(this.breakDoorGoal);
                }
            }
        } else if (this.canBreakDoors) {
            this.goalSelector.removeGoal(this.breakDoorGoal);
            this.canBreakDoors = false;
        }
    }

    @Override
    public boolean isBaby() {
        return this.getEntityData().get(DATA_BABY_ID);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel p_365075_) {
        if (this.isBaby()) {
            this.xpReward = (int)((double)this.xpReward * 2.5);
        }

        return super.getBaseExperienceReward(p_365075_);
    }

    @Override
    public void setBaby(boolean p_34309_) {
        this.getEntityData().set(DATA_BABY_ID, p_34309_);
        if (this.level() != null && !this.level().isClientSide) {
            AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attributeinstance.removeModifier(SPEED_MODIFIER_BABY_ID);
            if (p_34309_) {
                attributeinstance.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_34307_) {
        if (DATA_BABY_ID.equals(p_34307_)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(p_34307_);
    }

    protected boolean convertsInWater() {
        return true;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && !this.isNoAi()) {
            if (this.isUnderWaterConverting()) {
                this.conversionTime--;
                if (this.conversionTime < 0) {
                    this.doUnderWaterConversion();
                }
            } else if (this.convertsInWater()) {
                if (this.isEyeInFluid(FluidTags.WATER)) {
                    this.inWaterTime++;
                    if (this.inWaterTime >= 600) {
                        this.startUnderWaterConversion(300);
                    }
                } else {
                    this.inWaterTime = -1;
                }
            }
        }

        super.tick();
    }

    @Override
    public void aiStep() {
        if (this.isAlive()) {
            boolean flag = this.isSunSensitive() && this.isSunBurnTick();
            if (flag) {
                ItemStack itemstack = this.getItemBySlot(EquipmentSlot.HEAD);
                if (!itemstack.isEmpty()) {
                    if (itemstack.isDamageableItem()) {
                        Item item = itemstack.getItem();
                        itemstack.setDamageValue(itemstack.getDamageValue() + this.random.nextInt(2));
                        if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
                            this.onEquippedItemBroken(item, EquipmentSlot.HEAD);
                            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }

                    flag = false;
                }

                if (flag) {
                    this.igniteForSeconds(8.0F);
                }
            }
        }

        super.aiStep();
    }

    private void startUnderWaterConversion(int p_34279_) {
        this.conversionTime = p_34279_;
        this.getEntityData().set(DATA_DROWNED_CONVERSION_ID, true);
    }

    protected void doUnderWaterConversion() {
        this.convertToZombieType(EntityType.DROWNED);
        if (!this.isSilent()) {
            this.level().levelEvent(null, 1040, this.blockPosition(), 0);
        }
    }

    protected void convertToZombieType(EntityType<? extends Zombie> p_34311_) {
        this.convertTo(
            p_34311_,
            ConversionParams.single(this, true, true),
            p_375145_ -> p_375145_.handleAttributes(p_375145_.level().getCurrentDifficultyAt(p_375145_.blockPosition()).getSpecialMultiplier())
        );
    }

    @VisibleForTesting
    public boolean convertVillagerToZombieVillager(ServerLevel p_369430_, Villager p_360910_) {
        ZombieVillager zombievillager = p_360910_.convertTo(EntityType.ZOMBIE_VILLAGER, ConversionParams.single(p_360910_, true, true), p_359258_ -> {
            p_359258_.finalizeSpawn(p_369430_, p_369430_.getCurrentDifficultyAt(p_359258_.blockPosition()), EntitySpawnReason.CONVERSION, new Zombie.ZombieGroupData(false, true));
            p_359258_.setVillagerData(p_360910_.getVillagerData());
            p_359258_.setGossips(p_360910_.getGossips().store(NbtOps.INSTANCE));
            p_359258_.setTradeOffers(p_360910_.getOffers().copy());
            p_359258_.setVillagerXp(p_360910_.getVillagerXp());
            if (!this.isSilent()) {
                p_369430_.levelEvent(null, 1026, this.blockPosition(), 0);
            }
        });
        return zombievillager != null;
    }

    protected boolean isSunSensitive() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel p_363771_, DamageSource p_363403_, float p_367238_) {
        if (!super.hurtServer(p_363771_, p_363403_, p_367238_)) {
            return false;
        } else {
            LivingEntity livingentity = this.getTarget();
            if (livingentity == null && p_363403_.getEntity() instanceof LivingEntity) {
                livingentity = (LivingEntity)p_363403_.getEntity();
            }

            if (livingentity != null
                && p_363771_.getDifficulty() == Difficulty.HARD
                && (double)this.random.nextFloat() < this.getAttributeValue(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                && p_363771_.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                int i = Mth.floor(this.getX());
                int j = Mth.floor(this.getY());
                int k = Mth.floor(this.getZ());
                EntityType<? extends Zombie> entitytype = this.getType();
                Zombie zombie = entitytype.create(p_363771_, EntitySpawnReason.REINFORCEMENT);
                if (zombie == null) {
                    return true;
                }

                for (int l = 0; l < 50; l++) {
                    int i1 = i + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
                    int j1 = j + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
                    int k1 = k + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
                    BlockPos blockpos = new BlockPos(i1, j1, k1);
                    if (SpawnPlacements.isSpawnPositionOk(entitytype, p_363771_, blockpos)
                        && SpawnPlacements.checkSpawnRules(entitytype, p_363771_, EntitySpawnReason.REINFORCEMENT, blockpos, p_363771_.random)) {
                        zombie.setPos((double)i1, (double)j1, (double)k1);
                        if (!p_363771_.hasNearbyAlivePlayer((double)i1, (double)j1, (double)k1, 7.0)
                            && p_363771_.isUnobstructed(zombie)
                            && p_363771_.noCollision(zombie)
                            && (zombie.canSpawnInLiquids() || !p_363771_.containsAnyLiquid(zombie.getBoundingBox()))) {
                            zombie.setTarget(livingentity);
                            zombie.finalizeSpawn(p_363771_, p_363771_.getCurrentDifficultyAt(zombie.blockPosition()), EntitySpawnReason.REINFORCEMENT, null);
                            p_363771_.addFreshEntityWithPassengers(zombie);
                            AttributeInstance attributeinstance = this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
                            AttributeModifier attributemodifier = attributeinstance.getModifier(REINFORCEMENT_CALLER_CHARGE_ID);
                            double d0 = attributemodifier != null ? attributemodifier.amount() : 0.0;
                            attributeinstance.removeModifier(REINFORCEMENT_CALLER_CHARGE_ID);
                            attributeinstance.addPermanentModifier(new AttributeModifier(REINFORCEMENT_CALLER_CHARGE_ID, d0 - 0.05, AttributeModifier.Operation.ADD_VALUE));
                            zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(ZOMBIE_REINFORCEMENT_CALLEE_CHARGE);
                            break;
                        }
                    }
                }
            }

            return true;
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel p_365730_, Entity p_34276_) {
        boolean flag = super.doHurtTarget(p_365730_, p_34276_);
        if (flag) {
            float f = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
                p_34276_.igniteForSeconds((float)(2 * (int)f));
            }
        }

        return flag;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_34327_) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    @Override
    protected void playStepSound(BlockPos p_34316_, BlockState p_34317_) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    @Override
    public EntityType<? extends Zombie> getType() {
        return (EntityType<? extends Zombie>)super.getType();
    }

    protected boolean canSpawnInLiquids() {
        return false;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource p_219165_, DifficultyInstance p_219166_) {
        super.populateDefaultEquipmentSlots(p_219165_, p_219166_);
        if (p_219165_.nextFloat() < (this.level().getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
            int i = p_219165_.nextInt(3);
            if (i == 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_34319_) {
        super.addAdditionalSaveData(p_34319_);
        p_34319_.putBoolean("IsBaby", this.isBaby());
        p_34319_.putBoolean("CanBreakDoors", this.canBreakDoors());
        p_34319_.putInt("InWaterTime", this.isInWater() ? this.inWaterTime : -1);
        p_34319_.putInt("DrownedConversionTime", this.isUnderWaterConverting() ? this.conversionTime : -1);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_34305_) {
        super.readAdditionalSaveData(p_34305_);
        this.setBaby(p_34305_.getBoolean("IsBaby"));
        this.setCanBreakDoors(p_34305_.getBoolean("CanBreakDoors"));
        this.inWaterTime = p_34305_.getInt("InWaterTime");
        if (p_34305_.contains("DrownedConversionTime", 99) && p_34305_.getInt("DrownedConversionTime") > -1) {
            this.startUnderWaterConversion(p_34305_.getInt("DrownedConversionTime"));
        }
    }

    @Override
    public boolean killedEntity(ServerLevel p_219160_, LivingEntity p_219161_) {
        boolean flag = super.killedEntity(p_219160_, p_219161_);
        if ((p_219160_.getDifficulty() == Difficulty.NORMAL || p_219160_.getDifficulty() == Difficulty.HARD) && p_219161_ instanceof Villager villager) {
            if (p_219160_.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
                return flag;
            }

            if (this.convertVillagerToZombieVillager(p_219160_, villager)) {
                flag = false;
            }
        }

        return flag;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose p_328975_) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(p_328975_);
    }

    @Override
    public boolean canHoldItem(ItemStack p_34332_) {
        return p_34332_.is(Items.EGG) && this.isBaby() && this.isPassenger() ? false : super.canHoldItem(p_34332_);
    }

    @Override
    public boolean wantsToPickUp(ServerLevel p_365411_, ItemStack p_182400_) {
        return p_182400_.is(Items.GLOW_INK_SAC) ? false : super.wantsToPickUp(p_365411_, p_182400_);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_34297_, DifficultyInstance p_34298_, EntitySpawnReason p_367250_, @Nullable SpawnGroupData p_34300_) {
        RandomSource randomsource = p_34297_.getRandom();
        p_34300_ = super.finalizeSpawn(p_34297_, p_34298_, p_367250_, p_34300_);
        float f = p_34298_.getSpecialMultiplier();
        if (p_367250_ != EntitySpawnReason.CONVERSION) {
            this.setCanPickUpLoot(randomsource.nextFloat() < 0.55F * f);
        }

        if (p_34300_ == null) {
            p_34300_ = new Zombie.ZombieGroupData(getSpawnAsBabyOdds(randomsource), true);
        }

        if (p_34300_ instanceof Zombie.ZombieGroupData zombie$zombiegroupdata) {
            if (zombie$zombiegroupdata.isBaby) {
                this.setBaby(true);
                if (zombie$zombiegroupdata.canSpawnJockey) {
                    if ((double)randomsource.nextFloat() < 0.05) {
                        List<Chicken> list = p_34297_.getEntitiesOfClass(Chicken.class, this.getBoundingBox().inflate(5.0, 3.0, 5.0), EntitySelector.ENTITY_NOT_BEING_RIDDEN);
                        if (!list.isEmpty()) {
                            Chicken chicken = list.get(0);
                            chicken.setChickenJockey(true);
                            this.startRiding(chicken);
                        }
                    } else if ((double)randomsource.nextFloat() < 0.05) {
                        Chicken chicken1 = EntityType.CHICKEN.create(this.level(), EntitySpawnReason.JOCKEY);
                        if (chicken1 != null) {
                            chicken1.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                            chicken1.finalizeSpawn(p_34297_, p_34298_, EntitySpawnReason.JOCKEY, null);
                            chicken1.setChickenJockey(true);
                            this.startRiding(chicken1);
                            p_34297_.addFreshEntity(chicken1);
                        }
                    }
                }
            }

            this.setCanBreakDoors(randomsource.nextFloat() < f * 0.1F);
            if (p_367250_ != EntitySpawnReason.CONVERSION) {
                this.populateDefaultEquipmentSlots(randomsource, p_34298_);
                this.populateDefaultEquipmentEnchantments(p_34297_, randomsource, p_34298_);
            }
        }

        if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localdate = LocalDate.now();
            int i = localdate.get(ChronoField.DAY_OF_MONTH);
            int j = localdate.get(ChronoField.MONTH_OF_YEAR);
            if (j == 10 && i == 31 && randomsource.nextFloat() < 0.25F) {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(randomsource.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
            }
        }

        this.handleAttributes(f);
        return p_34300_;
    }

    @VisibleForTesting
    public void setInWaterTime(int p_361653_) {
        this.inWaterTime = p_361653_;
    }

    @VisibleForTesting
    public void setConversionTime(int p_365870_) {
        this.conversionTime = p_365870_;
    }

    public static boolean getSpawnAsBabyOdds(RandomSource p_219163_) {
        return p_219163_.nextFloat() < 0.05F;
    }

    protected void handleAttributes(float p_34340_) {
        this.randomizeReinforcementsChance();
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE)
            .addOrReplacePermanentModifier(new AttributeModifier(RANDOM_SPAWN_BONUS_ID, this.random.nextDouble() * 0.05F, AttributeModifier.Operation.ADD_VALUE));
        double d0 = this.random.nextDouble() * 1.5 * (double)p_34340_;
        if (d0 > 1.0) {
            this.getAttribute(Attributes.FOLLOW_RANGE).addOrReplacePermanentModifier(new AttributeModifier(ZOMBIE_RANDOM_SPAWN_BONUS_ID, d0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        if (this.random.nextFloat() < p_34340_ * 0.05F) {
            this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                .addOrReplacePermanentModifier(new AttributeModifier(LEADER_ZOMBIE_BONUS_ID, this.random.nextDouble() * 0.25 + 0.5, AttributeModifier.Operation.ADD_VALUE));
            this.getAttribute(Attributes.MAX_HEALTH)
                .addOrReplacePermanentModifier(new AttributeModifier(LEADER_ZOMBIE_BONUS_ID, this.random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            this.setCanBreakDoors(true);
        }
    }

    protected void randomizeReinforcementsChance() {
        this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.random.nextDouble() * 0.1F);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel p_344090_, DamageSource p_34291_, boolean p_34293_) {
        super.dropCustomDeathLoot(p_344090_, p_34291_, p_34293_);
        if (p_34291_.getEntity() instanceof Creeper creeper && creeper.canDropMobsSkull()) {
            ItemStack itemstack = this.getSkull();
            if (!itemstack.isEmpty()) {
                creeper.increaseDroppedSkulls();
                this.spawnAtLocation(p_344090_, itemstack);
            }
        }
    }

    protected ItemStack getSkull() {
        return new ItemStack(Items.ZOMBIE_HEAD);
    }

    class ZombieAttackTurtleEggGoal extends RemoveBlockGoal {
        ZombieAttackTurtleEggGoal(final PathfinderMob p_34344_, final double p_34345_, final int p_34346_) {
            super(Blocks.TURTLE_EGG, p_34344_, p_34345_, p_34346_);
        }

        @Override
        public void playDestroyProgressSound(LevelAccessor p_34351_, BlockPos p_34352_) {
            p_34351_.playSound(null, p_34352_, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 0.5F, 0.9F + Zombie.this.random.nextFloat() * 0.2F);
        }

        @Override
        public void playBreakSound(Level p_34348_, BlockPos p_34349_) {
            p_34348_.playSound(null, p_34349_, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + p_34348_.random.nextFloat() * 0.2F);
        }

        @Override
        public double acceptedDistance() {
            return 1.14;
        }
    }

    public static class ZombieGroupData implements SpawnGroupData {
        public final boolean isBaby;
        public final boolean canSpawnJockey;

        public ZombieGroupData(boolean p_34357_, boolean p_34358_) {
            this.isBaby = p_34357_;
            this.canSpawnJockey = p_34358_;
        }
    }
}