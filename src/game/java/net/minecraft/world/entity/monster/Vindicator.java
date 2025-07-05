package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class Vindicator extends AbstractIllager {
    private static final String TAG_JOHNNY = "Johnny";
    static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = p_34082_ -> p_34082_ == Difficulty.NORMAL || p_34082_ == Difficulty.HARD;
    boolean isJohnny;

    public Vindicator(EntityType<? extends Vindicator> p_34074_, Level p_34075_) {
        super(p_34074_, p_34075_);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Creaking.class, 8.0F, 1.0, 1.2));
        this.goalSelector.addGoal(2, new Vindicator.VindicatorBreakDoorGoal(this));
        this.goalSelector.addGoal(3, new AbstractIllager.RaiderOpenDoorGoal(this));
        this.goalSelector.addGoal(4, new Raider.HoldGroundAttackGoal(this, 10.0F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new Vindicator.VindicatorJohnnyAttackGoal(this));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    protected void customServerAiStep(ServerLevel p_366004_) {
        if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this)) {
            boolean flag = p_366004_.isRaided(this.blockPosition());
            ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(flag);
        }

        super.customServerAiStep(p_366004_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.35F)
            .add(Attributes.FOLLOW_RANGE, 12.0)
            .add(Attributes.MAX_HEALTH, 24.0)
            .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_34100_) {
        super.addAdditionalSaveData(p_34100_);
        if (this.isJohnny) {
            p_34100_.putBoolean("Johnny", true);
        }
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isAggressive()) {
            return AbstractIllager.IllagerArmPose.ATTACKING;
        } else {
            return this.isCelebrating() ? AbstractIllager.IllagerArmPose.CELEBRATING : AbstractIllager.IllagerArmPose.CROSSED;
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_34094_) {
        super.readAdditionalSaveData(p_34094_);
        if (p_34094_.contains("Johnny", 99)) {
            this.isJohnny = p_34094_.getBoolean("Johnny");
        }
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.VINDICATOR_CELEBRATE;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_34088_, DifficultyInstance p_34089_, EntitySpawnReason p_361065_, @Nullable SpawnGroupData p_34091_) {
        SpawnGroupData spawngroupdata = super.finalizeSpawn(p_34088_, p_34089_, p_361065_, p_34091_);
        ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
        RandomSource randomsource = p_34088_.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, p_34089_);
        this.populateDefaultEquipmentEnchantments(p_34088_, randomsource, p_34089_);
        return spawngroupdata;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource p_219149_, DifficultyInstance p_219150_) {
        if (this.getCurrentRaid() == null) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        }
    }

    @Override
    public void setCustomName(@Nullable Component p_34096_) {
        super.setCustomName(p_34096_);
        if (!this.isJohnny && p_34096_ != null && p_34096_.getString().equals("Johnny")) {
            this.isJohnny = true;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VINDICATOR_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VINDICATOR_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_34103_) {
        return SoundEvents.VINDICATOR_HURT;
    }

    @Override
    public void applyRaidBuffs(ServerLevel p_343632_, int p_34079_, boolean p_34080_) {
        ItemStack itemstack = new ItemStack(Items.IRON_AXE);
        Raid raid = this.getCurrentRaid();
        boolean flag = this.random.nextFloat() <= raid.getEnchantOdds();
        if (flag) {
            ResourceKey<EnchantmentProvider> resourcekey = p_34079_ > raid.getNumGroups(Difficulty.NORMAL)
                ? VanillaEnchantmentProviders.RAID_VINDICATOR_POST_WAVE_5
                : VanillaEnchantmentProviders.RAID_VINDICATOR;
            EnchantmentHelper.enchantItemFromProvider(itemstack, p_343632_.registryAccess(), resourcekey, p_343632_.getCurrentDifficultyAt(this.blockPosition()), this.random);
        }

        this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
    }

    static class VindicatorBreakDoorGoal extends BreakDoorGoal {
        public VindicatorBreakDoorGoal(Mob p_34112_) {
            super(p_34112_, 6, Vindicator.DOOR_BREAKING_PREDICATE);
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canContinueToUse() {
            Vindicator vindicator = (Vindicator)this.mob;
            return vindicator.hasActiveRaid() && super.canContinueToUse();
        }

        @Override
        public boolean canUse() {
            Vindicator vindicator = (Vindicator)this.mob;
            return vindicator.hasActiveRaid() && vindicator.random.nextInt(reducedTickDelay(10)) == 0 && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            this.mob.setNoActionTime(0);
        }
    }

    static class VindicatorJohnnyAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
        public VindicatorJohnnyAttackGoal(Vindicator p_34117_) {
            super(p_34117_, LivingEntity.class, 0, true, true, (p_366054_, p_368223_) -> p_366054_.attackable());
        }

        @Override
        public boolean canUse() {
            return ((Vindicator)this.mob).isJohnny && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            this.mob.setNoActionTime(0);
        }
    }
}