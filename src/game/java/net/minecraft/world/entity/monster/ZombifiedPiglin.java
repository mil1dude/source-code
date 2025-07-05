package net.minecraft.world.entity.monster;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;

public class ZombifiedPiglin extends Zombie implements NeutralMob {
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ZOMBIFIED_PIGLIN.getDimensions().scale(0.5F).withEyeHeight(0.97F);
    private static final ResourceLocation SPEED_MODIFIER_ATTACKING_ID = ResourceLocation.withDefaultNamespace("attacking");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_ID, 0.05, AttributeModifier.Operation.ADD_VALUE);
    private static final UniformInt FIRST_ANGER_SOUND_DELAY = TimeUtil.rangeOfSeconds(0, 1);
    private int playFirstAngerSoundIn;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @Nullable
    private UUID persistentAngerTarget;
    private static final int ALERT_RANGE_Y = 10;
    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private int ticksUntilNextAlert;

    public ZombifiedPiglin(EntityType<? extends ZombifiedPiglin> p_34427_, Level p_34428_) {
        super(p_34427_, p_34428_);
        this.setPathfindingMalus(PathType.LAVA, 8.0F);
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID p_34444_) {
        this.persistentAngerTarget = p_34444_;
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0).add(Attributes.MOVEMENT_SPEED, 0.23F).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose p_334497_) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(p_334497_);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel p_361922_) {
        AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (this.isAngry()) {
            if (!this.isBaby() && !attributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING_ID)) {
                attributeinstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }

            this.maybePlayFirstAngerSound();
        } else if (attributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING_ID)) {
            attributeinstance.removeModifier(SPEED_MODIFIER_ATTACKING_ID);
        }

        this.updatePersistentAnger(p_361922_, true);
        if (this.getTarget() != null) {
            this.maybeAlertOthers();
        }

        if (this.isAngry()) {
            this.lastHurtByPlayerTime = this.tickCount;
        }

        super.customServerAiStep(p_361922_);
    }

    private void maybePlayFirstAngerSound() {
        if (this.playFirstAngerSoundIn > 0) {
            this.playFirstAngerSoundIn--;
            if (this.playFirstAngerSoundIn == 0) {
                this.playAngerSound();
            }
        }
    }

    private void maybeAlertOthers() {
        if (this.ticksUntilNextAlert > 0) {
            this.ticksUntilNextAlert--;
        } else {
            if (this.getSensing().hasLineOfSight(this.getTarget())) {
                this.alertOthers();
            }

            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
    }

    private void alertOthers() {
        double d0 = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB aabb = AABB.unitCubeFromLowerCorner(this.position()).inflate(d0, 10.0, d0);
        this.level()
            .getEntitiesOfClass(ZombifiedPiglin.class, aabb, EntitySelector.NO_SPECTATORS)
            .stream()
            .filter(p_34463_ -> p_34463_ != this)
            .filter(p_359264_ -> p_359264_.getTarget() == null)
            .filter(p_359263_ -> !p_359263_.isAlliedTo(this.getTarget()))
            .forEach(p_359262_ -> p_359262_.setTarget(this.getTarget()));
    }

    private void playAngerSound() {
        this.playSound(SoundEvents.ZOMBIFIED_PIGLIN_ANGRY, this.getSoundVolume() * 2.0F, this.getVoicePitch() * 1.8F);
    }

    @Override
    public void setTarget(@Nullable LivingEntity p_34478_) {
        if (this.getTarget() == null && p_34478_ != null) {
            this.playFirstAngerSoundIn = FIRST_ANGER_SOUND_DELAY.sample(this.random);
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }

        if (p_34478_ instanceof Player) {
            this.setLastHurtByPlayer((Player)p_34478_);
        }

        super.setTarget(p_34478_);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public static boolean checkZombifiedPiglinSpawnRules(
        EntityType<ZombifiedPiglin> p_219174_, LevelAccessor p_219175_, EntitySpawnReason p_367680_, BlockPos p_219177_, RandomSource p_219178_
    ) {
        return p_219175_.getDifficulty() != Difficulty.PEACEFUL && !p_219175_.getBlockState(p_219177_.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader p_34442_) {
        return p_34442_.isUnobstructed(this) && !p_34442_.containsAnyLiquid(this.getBoundingBox());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_34458_) {
        super.addAdditionalSaveData(p_34458_);
        this.addPersistentAngerSaveData(p_34458_);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_34446_) {
        super.readAdditionalSaveData(p_34446_);
        this.readPersistentAngerSaveData(this.level(), p_34446_);
    }

    @Override
    public void setRemainingPersistentAngerTime(int p_34448_) {
        this.remainingPersistentAngerTime = p_34448_;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAngry() ? SoundEvents.ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_34466_) {
        return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource p_219171_, DifficultyInstance p_219172_) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void randomizeReinforcementsChance() {
        this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0);
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public boolean isPreventingPlayerRest(ServerLevel p_363237_, Player p_34475_) {
        return this.isAngryAt(p_34475_, p_363237_);
    }

    @Override
    public boolean wantsToPickUp(ServerLevel p_363685_, ItemStack p_182402_) {
        return this.canHoldItem(p_182402_);
    }
}