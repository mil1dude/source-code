package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

public class Pillager extends AbstractIllager implements CrossbowAttackMob, InventoryCarrier {
    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Pillager.class, EntityDataSerializers.BOOLEAN);
    private static final int INVENTORY_SIZE = 5;
    private static final int SLOT_OFFSET = 300;
    private final SimpleContainer inventory = new SimpleContainer(5);

    public Pillager(EntityType<? extends Pillager> p_33262_, Level p_33263_) {
        super(p_33262_, p_33263_);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Creaking.class, 8.0F, 1.0, 1.2));
        this.goalSelector.addGoal(2, new Raider.HoldGroundAttackGoal(this, 10.0F));
        this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal<>(this, 1.0, 8.0F));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.35F)
            .add(Attributes.MAX_HEALTH, 24.0)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_332488_) {
        super.defineSynchedData(p_332488_);
        p_332488_.define(IS_CHARGING_CROSSBOW, false);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem p_33280_) {
        return p_33280_ == Items.CROSSBOW;
    }

    public boolean isChargingCrossbow() {
        return this.entityData.get(IS_CHARGING_CROSSBOW);
    }

    @Override
    public void setChargingCrossbow(boolean p_33302_) {
        this.entityData.set(IS_CHARGING_CROSSBOW, p_33302_);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public TagKey<Item> getPreferredWeaponType() {
        return ItemTags.PILLAGER_PREFERRED_WEAPONS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_33300_) {
        super.addAdditionalSaveData(p_33300_);
        this.writeInventoryToTag(p_33300_, this.registryAccess());
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isChargingCrossbow()) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
        } else if (this.isHolding(Items.CROSSBOW)) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
        } else {
            return this.isAggressive() ? AbstractIllager.IllagerArmPose.ATTACKING : AbstractIllager.IllagerArmPose.NEUTRAL;
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_33291_) {
        super.readAdditionalSaveData(p_33291_);
        this.readInventoryFromTag(p_33291_, this.registryAccess());
        this.setCanPickUpLoot(true);
    }

    @Override
    public float getWalkTargetValue(BlockPos p_33288_, LevelReader p_33289_) {
        return 0.0F;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_33282_, DifficultyInstance p_33283_, EntitySpawnReason p_362767_, @Nullable SpawnGroupData p_33285_) {
        RandomSource randomsource = p_33282_.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, p_33283_);
        this.populateDefaultEquipmentEnchantments(p_33282_, randomsource, p_33283_);
        return super.finalizeSpawn(p_33282_, p_33283_, p_362767_, p_33285_);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource p_219059_, DifficultyInstance p_219060_) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
    }

    @Override
    protected void enchantSpawnedWeapon(ServerLevelAccessor p_343786_, RandomSource p_219056_, DifficultyInstance p_344265_) {
        super.enchantSpawnedWeapon(p_343786_, p_219056_, p_344265_);
        if (p_219056_.nextInt(300) == 0) {
            ItemStack itemstack = this.getMainHandItem();
            if (itemstack.is(Items.CROSSBOW)) {
                EnchantmentHelper.enchantItemFromProvider(itemstack, p_343786_.registryAccess(), VanillaEnchantmentProviders.PILLAGER_SPAWN_CROSSBOW, p_344265_, p_219056_);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_33306_) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    public void performRangedAttack(LivingEntity p_33272_, float p_33273_) {
        this.performCrossbowAttack(this, 1.6F);
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected void pickUpItem(ServerLevel p_363457_, ItemEntity p_33296_) {
        ItemStack itemstack = p_33296_.getItem();
        if (itemstack.getItem() instanceof BannerItem) {
            super.pickUpItem(p_363457_, p_33296_);
        } else if (this.wantsItem(itemstack)) {
            this.onItemPickup(p_33296_);
            ItemStack itemstack1 = this.inventory.addItem(itemstack);
            if (itemstack1.isEmpty()) {
                p_33296_.discard();
            } else {
                itemstack.setCount(itemstack1.getCount());
            }
        }
    }

    private boolean wantsItem(ItemStack p_149745_) {
        return this.hasActiveRaid() && p_149745_.is(Items.WHITE_BANNER);
    }

    @Override
    public SlotAccess getSlot(int p_149743_) {
        int i = p_149743_ - 300;
        return i >= 0 && i < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, i) : super.getSlot(p_149743_);
    }

    @Override
    public void applyRaidBuffs(ServerLevel p_345516_, int p_33267_, boolean p_33268_) {
        Raid raid = this.getCurrentRaid();
        boolean flag = this.random.nextFloat() <= raid.getEnchantOdds();
        if (flag) {
            ItemStack itemstack = new ItemStack(Items.CROSSBOW);
            ResourceKey<EnchantmentProvider> resourcekey;
            if (p_33267_ > raid.getNumGroups(Difficulty.NORMAL)) {
                resourcekey = VanillaEnchantmentProviders.RAID_PILLAGER_POST_WAVE_5;
            } else if (p_33267_ > raid.getNumGroups(Difficulty.EASY)) {
                resourcekey = VanillaEnchantmentProviders.RAID_PILLAGER_POST_WAVE_3;
            } else {
                resourcekey = null;
            }

            if (resourcekey != null) {
                EnchantmentHelper.enchantItemFromProvider(itemstack, p_345516_.registryAccess(), resourcekey, p_345516_.getCurrentDifficultyAt(this.blockPosition()), this.getRandom());
                this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
            }
        }
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }
}