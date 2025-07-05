package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

public abstract class Mob extends LivingEntity implements EquipmentUser, Leashable, Targeting {
    private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
    private static final int MOB_FLAG_NO_AI = 1;
    private static final int MOB_FLAG_LEFTHANDED = 2;
    private static final int MOB_FLAG_AGGRESSIVE = 4;
    protected static final int PICKUP_REACH = 1;
    private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 0, 1);
    private static final List<EquipmentSlot> EQUIPMENT_POPULATION_ORDER = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    public static final float MAX_WEARING_ARMOR_CHANCE = 0.15F;
    public static final float MAX_PICKUP_LOOT_CHANCE = 0.55F;
    public static final float MAX_ENCHANTED_ARMOR_CHANCE = 0.5F;
    public static final float MAX_ENCHANTED_WEAPON_CHANCE = 0.25F;
    public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085F;
    public static final float PRESERVE_ITEM_DROP_CHANCE_THRESHOLD = 1.0F;
    public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
    public static final int UPDATE_GOAL_SELECTOR_EVERY_N_TICKS = 2;
    private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04F) - 0.6F;
    protected static final ResourceLocation RANDOM_SPAWN_BONUS_ID = ResourceLocation.withDefaultNamespace("random_spawn_bonus");
    public int ambientSoundTime;
    protected int xpReward;
    protected LookControl lookControl;
    protected MoveControl moveControl;
    protected JumpControl jumpControl;
    private final BodyRotationControl bodyRotationControl;
    protected PathNavigation navigation;
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    @Nullable
    private LivingEntity target;
    private final Sensing sensing;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    protected final float[] handDropChances = new float[2];
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    protected final float[] armorDropChances = new float[4];
    private ItemStack bodyArmorItem = ItemStack.EMPTY;
    protected float bodyArmorDropChance;
    private boolean canPickUpLoot;
    private boolean persistenceRequired;
    private final Map<PathType, Float> pathfindingMalus = Maps.newEnumMap(PathType.class);
    private Optional<ResourceKey<LootTable>> lootTable = Optional.empty();
    private long lootTableSeed;
    @Nullable
    private Leashable.LeashData leashData;
    private BlockPos restrictCenter = BlockPos.ZERO;
    private float restrictRadius = -1.0F;

    protected Mob(EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        this.goalSelector = new GoalSelector();
        this.targetSelector = new GoalSelector();
        this.lookControl = new LookControl(this);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.bodyRotationControl = this.createBodyControl();
        this.navigation = this.createNavigation(p_21369_);
        this.sensing = new Sensing(this);
        Arrays.fill(this.armorDropChances, 0.085F);
        Arrays.fill(this.handDropChances, 0.085F);
        this.bodyArmorDropChance = 0.085F;
        if (p_21369_ instanceof ServerLevel) {
            this.registerGoals();
        }
    }

    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0);
    }

    protected PathNavigation createNavigation(Level p_21480_) {
        return new GroundPathNavigation(this, p_21480_);
    }

    protected boolean shouldPassengersInheritMalus() {
        return false;
    }

    public float getPathfindingMalus(PathType p_334857_) {
        Mob mob;
        label17: {
            if (this.getControlledVehicle() instanceof Mob mob1 && mob1.shouldPassengersInheritMalus()) {
                mob = mob1;
                break label17;
            }

            mob = this;
        }

        Float f = mob.pathfindingMalus.get(p_334857_);
        return f == null ? p_334857_.getMalus() : f;
    }

    public void setPathfindingMalus(PathType p_332507_, float p_21443_) {
        this.pathfindingMalus.put(p_332507_, p_21443_);
    }

    public void onPathfindingStart() {
    }

    public void onPathfindingDone() {
    }

    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

    public LookControl getLookControl() {
        return this.lookControl;
    }

    public MoveControl getMoveControl() {
        return this.getControlledVehicle() instanceof Mob mob ? mob.getMoveControl() : this.moveControl;
    }

    public JumpControl getJumpControl() {
        return this.jumpControl;
    }

    public PathNavigation getNavigation() {
        return this.getControlledVehicle() instanceof Mob mob ? mob.getNavigation() : this.navigation;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (!this.isNoAi() && entity instanceof Mob mob && entity.canControlVehicle()) {
            return mob;
        }

        return null;
    }

    public Sensing getSensing() {
        return this.sensing;
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return this.target;
    }

    @Nullable
    protected final LivingEntity getTargetFromBrain() {
        return this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    public void setTarget(@Nullable LivingEntity p_21544_) {
        this.target = p_21544_;
    }

    @Override
    public boolean canAttackType(EntityType<?> p_21399_) {
        return p_21399_ != EntityType.GHAST;
    }

    public boolean canFireProjectileWeapon(ProjectileWeaponItem p_21430_) {
        return false;
    }

    public void ate() {
        this.gameEvent(GameEvent.EAT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_335882_) {
        super.defineSynchedData(p_335882_);
        p_335882_.define(DATA_MOB_FLAGS_ID, (byte)0);
    }

    public int getAmbientSoundInterval() {
        return 80;
    }

    public void playAmbientSound() {
        this.makeSound(this.getAmbientSound());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        ProfilerFiller profilerfiller = Profiler.get();
        profilerfiller.push("mobBaseTick");
        if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundTime++) {
            this.resetAmbientSoundTime();
            this.playAmbientSound();
        }

        profilerfiller.pop();
    }

    @Override
    protected void playHurtSound(DamageSource p_21493_) {
        this.resetAmbientSoundTime();
        super.playHurtSound(p_21493_);
    }

    private void resetAmbientSoundTime() {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel p_369877_) {
        if (this.xpReward > 0) {
            int i = this.xpReward;

            for (int j = 0; j < this.armorItems.size(); j++) {
                if (!this.armorItems.get(j).isEmpty() && this.armorDropChances[j] <= 1.0F) {
                    i += 1 + this.random.nextInt(3);
                }
            }

            for (int k = 0; k < this.handItems.size(); k++) {
                if (!this.handItems.get(k).isEmpty() && this.handDropChances[k] <= 1.0F) {
                    i += 1 + this.random.nextInt(3);
                }
            }

            if (!this.bodyArmorItem.isEmpty() && this.bodyArmorDropChance <= 1.0F) {
                i += 1 + this.random.nextInt(3);
            }

            return i;
        } else {
            return this.xpReward;
        }
    }

    public void spawnAnim() {
        if (this.level().isClientSide) {
            this.makePoofParticles();
        } else {
            this.level().broadcastEntityEvent(this, (byte)20);
        }
    }

    @Override
    public void handleEntityEvent(byte p_21375_) {
        if (p_21375_ == 20) {
            this.spawnAnim();
        } else {
            super.handleEntityEvent(p_21375_);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount % 5 == 0) {
            this.updateControlFlags();
        }
    }

    protected void updateControlFlags() {
        boolean flag = !(this.getControllingPassenger() instanceof Mob);
        boolean flag1 = !(this.getVehicle() instanceof AbstractBoat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, flag);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag && flag1);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, flag);
    }

    @Override
    protected float tickHeadTurn(float p_21538_, float p_21539_) {
        this.bodyRotationControl.clientTick();
        return p_21539_;
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_21484_) {
        super.addAdditionalSaveData(p_21484_);
        p_21484_.putBoolean("CanPickUpLoot", this.canPickUpLoot());
        p_21484_.putBoolean("PersistenceRequired", this.persistenceRequired);
        ListTag listtag = new ListTag();

        for (ItemStack itemstack : this.armorItems) {
            if (!itemstack.isEmpty()) {
                listtag.add(itemstack.save(this.registryAccess()));
            } else {
                listtag.add(new CompoundTag());
            }
        }

        p_21484_.put("ArmorItems", listtag);
        ListTag listtag1 = new ListTag();

        for (float f : this.armorDropChances) {
            listtag1.add(FloatTag.valueOf(f));
        }

        p_21484_.put("ArmorDropChances", listtag1);
        ListTag listtag2 = new ListTag();

        for (ItemStack itemstack1 : this.handItems) {
            if (!itemstack1.isEmpty()) {
                listtag2.add(itemstack1.save(this.registryAccess()));
            } else {
                listtag2.add(new CompoundTag());
            }
        }

        p_21484_.put("HandItems", listtag2);
        ListTag listtag3 = new ListTag();

        for (float f1 : this.handDropChances) {
            listtag3.add(FloatTag.valueOf(f1));
        }

        p_21484_.put("HandDropChances", listtag3);
        if (!this.bodyArmorItem.isEmpty()) {
            p_21484_.put("body_armor_item", this.bodyArmorItem.save(this.registryAccess()));
            p_21484_.putFloat("body_armor_drop_chance", this.bodyArmorDropChance);
        }

        this.writeLeashData(p_21484_, this.leashData);
        p_21484_.putBoolean("LeftHanded", this.isLeftHanded());
        if (this.lootTable.isPresent()) {
            p_21484_.putString("DeathLootTable", this.lootTable.get().location().toString());
        }

        if (this.lootTableSeed != 0L) {
            p_21484_.putLong("DeathLootTableSeed", this.lootTableSeed);
        }

        if (this.isNoAi()) {
            p_21484_.putBoolean("NoAI", this.isNoAi());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_21450_) {
        super.readAdditionalSaveData(p_21450_);
        this.setCanPickUpLoot(p_21450_.getBoolean("CanPickUpLoot"));
        this.persistenceRequired = p_21450_.getBoolean("PersistenceRequired");
        if (p_21450_.contains("ArmorItems", 9)) {
            ListTag listtag = p_21450_.getList("ArmorItems", 10);

            for (int i = 0; i < this.armorItems.size(); i++) {
                CompoundTag compoundtag = listtag.getCompound(i);
                this.armorItems.set(i, ItemStack.parseOptional(this.registryAccess(), compoundtag));
            }
        } else {
            this.armorItems.replaceAll(p_374935_ -> ItemStack.EMPTY);
        }

        if (p_21450_.contains("ArmorDropChances", 9)) {
            ListTag listtag1 = p_21450_.getList("ArmorDropChances", 5);

            for (int j = 0; j < listtag1.size(); j++) {
                this.armorDropChances[j] = listtag1.getFloat(j);
            }
        } else {
            Arrays.fill(this.armorDropChances, 0.0F);
        }

        if (p_21450_.contains("HandItems", 9)) {
            ListTag listtag2 = p_21450_.getList("HandItems", 10);

            for (int k = 0; k < this.handItems.size(); k++) {
                CompoundTag compoundtag1 = listtag2.getCompound(k);
                this.handItems.set(k, ItemStack.parseOptional(this.registryAccess(), compoundtag1));
            }
        } else {
            this.handItems.replaceAll(p_374934_ -> ItemStack.EMPTY);
        }

        if (p_21450_.contains("HandDropChances", 9)) {
            ListTag listtag3 = p_21450_.getList("HandDropChances", 5);

            for (int l = 0; l < listtag3.size(); l++) {
                this.handDropChances[l] = listtag3.getFloat(l);
            }
        } else {
            Arrays.fill(this.handDropChances, 0.0F);
        }

        if (p_21450_.contains("body_armor_item", 10)) {
            this.bodyArmorItem = ItemStack.parse(this.registryAccess(), p_21450_.getCompound("body_armor_item")).orElse(ItemStack.EMPTY);
            this.bodyArmorDropChance = p_21450_.getFloat("body_armor_drop_chance");
        } else {
            this.bodyArmorItem = ItemStack.EMPTY;
        }

        this.readLeashData(p_21450_);
        this.setLeftHanded(p_21450_.getBoolean("LeftHanded"));
        if (p_21450_.contains("DeathLootTable", 8)) {
            this.lootTable = Optional.of(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(p_21450_.getString("DeathLootTable"))));
        } else {
            this.lootTable = Optional.empty();
        }

        this.lootTableSeed = p_21450_.getLong("DeathLootTableSeed");
        this.setNoAi(p_21450_.getBoolean("NoAI"));
    }

    @Override
    protected void dropFromLootTable(ServerLevel p_367479_, DamageSource p_21389_, boolean p_21390_) {
        super.dropFromLootTable(p_367479_, p_21389_, p_21390_);
        this.lootTable = Optional.empty();
    }

    @Override
    public final Optional<ResourceKey<LootTable>> getLootTable() {
        return this.lootTable.isPresent() ? this.lootTable : super.getLootTable();
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setZza(float p_21565_) {
        this.zza = p_21565_;
    }

    public void setYya(float p_21568_) {
        this.yya = p_21568_;
    }

    public void setXxa(float p_21571_) {
        this.xxa = p_21571_;
    }

    @Override
    public void setSpeed(float p_21556_) {
        super.setSpeed(p_21556_);
        this.setZza(p_21556_);
    }

    public void stopInPlace() {
        this.getNavigation().stop();
        this.setXxa(0.0F);
        this.setYya(0.0F);
        this.setSpeed(0.0F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        ProfilerFiller profilerfiller = Profiler.get();
        profilerfiller.push("looting");
        if (this.level() instanceof ServerLevel serverlevel
            && this.canPickUpLoot()
            && this.isAlive()
            && !this.dead
            && serverlevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            Vec3i vec3i = this.getPickupReach();

            for (ItemEntity itementity : this.level()
                .getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ()))) {
                if (!itementity.isRemoved() && !itementity.getItem().isEmpty() && !itementity.hasPickUpDelay() && this.wantsToPickUp(serverlevel, itementity.getItem())) {
                    this.pickUpItem(serverlevel, itementity);
                }
            }
        }

        profilerfiller.pop();
    }

    protected Vec3i getPickupReach() {
        return ITEM_PICKUP_REACH;
    }

    protected void pickUpItem(ServerLevel p_363972_, ItemEntity p_21471_) {
        ItemStack itemstack = p_21471_.getItem();
        ItemStack itemstack1 = this.equipItemIfPossible(p_363972_, itemstack.copy());
        if (!itemstack1.isEmpty()) {
            this.onItemPickup(p_21471_);
            this.take(p_21471_, itemstack1.getCount());
            itemstack.shrink(itemstack1.getCount());
            if (itemstack.isEmpty()) {
                p_21471_.discard();
            }
        }
    }

    public ItemStack equipItemIfPossible(ServerLevel p_362503_, ItemStack p_255842_) {
        EquipmentSlot equipmentslot = this.getEquipmentSlotForItem(p_255842_);
        ItemStack itemstack = this.getItemBySlot(equipmentslot);
        boolean flag = this.canReplaceCurrentItem(p_255842_, itemstack, equipmentslot);
        if (equipmentslot.isArmor() && !flag) {
            equipmentslot = EquipmentSlot.MAINHAND;
            itemstack = this.getItemBySlot(equipmentslot);
            flag = itemstack.isEmpty();
        }

        if (flag && this.canHoldItem(p_255842_)) {
            double d0 = (double)this.getEquipmentDropChance(equipmentslot);
            if (!itemstack.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d0) {
                this.spawnAtLocation(p_362503_, itemstack);
            }

            ItemStack itemstack1 = equipmentslot.limit(p_255842_);
            this.setItemSlotAndDropWhenKilled(equipmentslot, itemstack1);
            return itemstack1;
        } else {
            return ItemStack.EMPTY;
        }
    }

    protected void setItemSlotAndDropWhenKilled(EquipmentSlot p_21469_, ItemStack p_21470_) {
        this.setItemSlot(p_21469_, p_21470_);
        this.setGuaranteedDrop(p_21469_);
        this.persistenceRequired = true;
    }

    public void setGuaranteedDrop(EquipmentSlot p_21509_) {
        switch (p_21509_.getType()) {
            case HAND:
                this.handDropChances[p_21509_.getIndex()] = 2.0F;
                break;
            case HUMANOID_ARMOR:
                this.armorDropChances[p_21509_.getIndex()] = 2.0F;
                break;
            case ANIMAL_ARMOR:
                this.bodyArmorDropChance = 2.0F;
        }
    }

    protected boolean canReplaceCurrentItem(ItemStack p_21428_, ItemStack p_21429_, EquipmentSlot p_362798_) {
        if (p_21429_.isEmpty()) {
            return true;
        } else if (p_362798_.isArmor()) {
            return this.compareArmor(p_21428_, p_21429_, p_362798_);
        } else {
            return p_362798_ == EquipmentSlot.MAINHAND ? this.compareWeapons(p_21428_, p_21429_, p_362798_) : false;
        }
    }

    private boolean compareArmor(ItemStack p_377405_, ItemStack p_378380_, EquipmentSlot p_376197_) {
        if (EnchantmentHelper.has(p_378380_, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            return false;
        } else {
            double d0 = this.getApproximateAttributeWith(p_377405_, Attributes.ARMOR, p_376197_);
            double d1 = this.getApproximateAttributeWith(p_378380_, Attributes.ARMOR, p_376197_);
            double d2 = this.getApproximateAttributeWith(p_377405_, Attributes.ARMOR_TOUGHNESS, p_376197_);
            double d3 = this.getApproximateAttributeWith(p_378380_, Attributes.ARMOR_TOUGHNESS, p_376197_);
            if (d0 != d1) {
                return d0 > d1;
            } else {
                return d2 != d3 ? d2 > d3 : this.canReplaceEqualItem(p_377405_, p_378380_);
            }
        }
    }

    private boolean compareWeapons(ItemStack p_376507_, ItemStack p_378437_, EquipmentSlot p_378407_) {
        TagKey<Item> tagkey = this.getPreferredWeaponType();
        if (tagkey != null) {
            if (p_378437_.is(tagkey) && !p_376507_.is(tagkey)) {
                return false;
            }

            if (!p_378437_.is(tagkey) && p_376507_.is(tagkey)) {
                return true;
            }
        }

        double d0 = this.getApproximateAttributeWith(p_376507_, Attributes.ATTACK_DAMAGE, p_378407_);
        double d1 = this.getApproximateAttributeWith(p_378437_, Attributes.ATTACK_DAMAGE, p_378407_);
        return d0 != d1 ? d0 > d1 : this.canReplaceEqualItem(p_376507_, p_378437_);
    }

    private double getApproximateAttributeWith(ItemStack p_363720_, Holder<Attribute> p_366827_, EquipmentSlot p_366430_) {
        double d0 = this.getAttributes().hasAttribute(p_366827_) ? this.getAttributeBaseValue(p_366827_) : 0.0;
        ItemAttributeModifiers itemattributemodifiers = p_363720_.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return itemattributemodifiers.compute(d0, p_366430_);
    }

    public boolean canReplaceEqualItem(ItemStack p_21478_, ItemStack p_21479_) {
        Set<Entry<Holder<Enchantment>>> set = p_21479_.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet();
        Set<Entry<Holder<Enchantment>>> set1 = p_21478_.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet();
        if (set1.size() != set.size()) {
            return set1.size() > set.size();
        } else {
            int i = p_21478_.getDamageValue();
            int j = p_21479_.getDamageValue();
            return i != j ? i < j : p_21478_.has(DataComponents.CUSTOM_NAME) && !p_21479_.has(DataComponents.CUSTOM_NAME);
        }
    }

    public boolean canHoldItem(ItemStack p_21545_) {
        return true;
    }

    public boolean wantsToPickUp(ServerLevel p_367521_, ItemStack p_21546_) {
        return this.canHoldItem(p_21546_);
    }

    @Nullable
    public TagKey<Item> getPreferredWeaponType() {
        return null;
    }

    public boolean removeWhenFarAway(double p_21542_) {
        return true;
    }

    public boolean requiresCustomPersistence() {
        return this.isPassenger();
    }

    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
        } else if (!this.isPersistenceRequired() && !this.requiresCustomPersistence()) {
            Entity entity = this.level().getNearestPlayer(this, -1.0);
            if (entity != null) {
                double d0 = entity.distanceToSqr(this);
                int i = this.getType().getCategory().getDespawnDistance();
                int j = i * i;
                if (d0 > (double)j && this.removeWhenFarAway(d0)) {
                    this.discard();
                }

                int k = this.getType().getCategory().getNoDespawnDistance();
                int l = k * k;
                if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.removeWhenFarAway(d0)) {
                    this.discard();
                } else if (d0 < (double)l) {
                    this.noActionTime = 0;
                }
            }
        } else {
            this.noActionTime = 0;
        }
    }

    @Override
    protected final void serverAiStep() {
        this.noActionTime++;
        ProfilerFiller profilerfiller = Profiler.get();
        profilerfiller.push("sensing");
        this.sensing.tick();
        profilerfiller.pop();
        int i = this.tickCount + this.getId();
        if (i % 2 != 0 && this.tickCount > 1) {
            profilerfiller.push("targetSelector");
            this.targetSelector.tickRunningGoals(false);
            profilerfiller.pop();
            profilerfiller.push("goalSelector");
            this.goalSelector.tickRunningGoals(false);
            profilerfiller.pop();
        } else {
            profilerfiller.push("targetSelector");
            this.targetSelector.tick();
            profilerfiller.pop();
            profilerfiller.push("goalSelector");
            this.goalSelector.tick();
            profilerfiller.pop();
        }

        profilerfiller.push("navigation");
        this.navigation.tick();
        profilerfiller.pop();
        profilerfiller.push("mob tick");
        this.customServerAiStep((ServerLevel)this.level());
        profilerfiller.pop();
        profilerfiller.push("controls");
        profilerfiller.push("move");
        this.moveControl.tick();
        profilerfiller.popPush("look");
        this.lookControl.tick();
        profilerfiller.popPush("jump");
        this.jumpControl.tick();
        profilerfiller.pop();
        profilerfiller.pop();
        this.sendDebugPackets();
    }

    protected void sendDebugPackets() {
        DebugPackets.sendGoalSelector(this.level(), this, this.goalSelector);
    }

    protected void customServerAiStep(ServerLevel p_361697_) {
    }

    public int getMaxHeadXRot() {
        return 40;
    }

    public int getMaxHeadYRot() {
        return 75;
    }

    protected void clampHeadRotationToBody() {
        float f = (float)this.getMaxHeadYRot();
        float f1 = this.getYHeadRot();
        float f2 = Mth.wrapDegrees(this.yBodyRot - f1);
        float f3 = Mth.clamp(Mth.wrapDegrees(this.yBodyRot - f1), -f, f);
        float f4 = f1 + f2 - f3;
        this.setYHeadRot(f4);
    }

    public int getHeadRotSpeed() {
        return 10;
    }

    public void lookAt(Entity p_21392_, float p_21393_, float p_21394_) {
        double d0 = p_21392_.getX() - this.getX();
        double d2 = p_21392_.getZ() - this.getZ();
        double d1;
        if (p_21392_ instanceof LivingEntity livingentity) {
            d1 = livingentity.getEyeY() - this.getEyeY();
        } else {
            d1 = (p_21392_.getBoundingBox().minY + p_21392_.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }

        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float f = (float)(Mth.atan2(d2, d0) * 180.0F / (float)Math.PI) - 90.0F;
        float f1 = (float)(-(Mth.atan2(d1, d3) * 180.0F / (float)Math.PI));
        this.setXRot(this.rotlerp(this.getXRot(), f1, p_21394_));
        this.setYRot(this.rotlerp(this.getYRot(), f, p_21393_));
    }

    private float rotlerp(float p_21377_, float p_21378_, float p_21379_) {
        float f = Mth.wrapDegrees(p_21378_ - p_21377_);
        if (f > p_21379_) {
            f = p_21379_;
        }

        if (f < -p_21379_) {
            f = -p_21379_;
        }

        return p_21377_ + f;
    }

    public static boolean checkMobSpawnRules(
        EntityType<? extends Mob> p_217058_, LevelAccessor p_217059_, EntitySpawnReason p_362165_, BlockPos p_217061_, RandomSource p_217062_
    ) {
        BlockPos blockpos = p_217061_.below();
        return EntitySpawnReason.isSpawner(p_362165_) || p_217059_.getBlockState(blockpos).isValidSpawn(p_217059_, blockpos, p_217058_);
    }

    public boolean checkSpawnRules(LevelAccessor p_21431_, EntitySpawnReason p_366364_) {
        return true;
    }

    public boolean checkSpawnObstruction(LevelReader p_21433_) {
        return !p_21433_.containsAnyLiquid(this.getBoundingBox()) && p_21433_.isUnobstructed(this);
    }

    public int getMaxSpawnClusterSize() {
        return 4;
    }

    public boolean isMaxGroupSizeReached(int p_21489_) {
        return false;
    }

    @Override
    public int getMaxFallDistance() {
        if (this.getTarget() == null) {
            return this.getComfortableFallDistance(0.0F);
        } else {
            int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
            i -= (3 - this.level().getDifficulty().getId()) * 4;
            if (i < 0) {
                i = 0;
            }

            return this.getComfortableFallDistance((float)i);
        }
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return this.handItems;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    public ItemStack getBodyArmorItem() {
        return this.bodyArmorItem;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot p_334488_) {
        return p_334488_ != EquipmentSlot.BODY;
    }

    public boolean isWearingBodyArmor() {
        return !this.getItemBySlot(EquipmentSlot.BODY).isEmpty();
    }

    public void setBodyArmorItem(ItemStack p_333947_) {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.BODY, p_333947_);
    }

    @Override
    public Iterable<ItemStack> getArmorAndBodyArmorSlots() {
        return (Iterable<ItemStack>)(this.bodyArmorItem.isEmpty() ? this.armorItems : Iterables.concat(this.armorItems, List.of(this.bodyArmorItem)));
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot p_21467_) {
        return switch (p_21467_.getType()) {
            case HAND -> (ItemStack)this.handItems.get(p_21467_.getIndex());
            case HUMANOID_ARMOR -> (ItemStack)this.armorItems.get(p_21467_.getIndex());
            case ANIMAL_ARMOR -> this.bodyArmorItem;
        };
    }

    @Override
    public void setItemSlot(EquipmentSlot p_21416_, ItemStack p_21417_) {
        this.verifyEquippedItem(p_21417_);
        switch (p_21416_.getType()) {
            case HAND:
                this.onEquipItem(p_21416_, this.handItems.set(p_21416_.getIndex(), p_21417_), p_21417_);
                break;
            case HUMANOID_ARMOR:
                this.onEquipItem(p_21416_, this.armorItems.set(p_21416_.getIndex(), p_21417_), p_21417_);
                break;
            case ANIMAL_ARMOR:
                ItemStack itemstack = this.bodyArmorItem;
                this.bodyArmorItem = p_21417_;
                this.onEquipItem(p_21416_, itemstack, p_21417_);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel p_345102_, DamageSource p_21385_, boolean p_21387_) {
        super.dropCustomDeathLoot(p_345102_, p_21385_, p_21387_);

        for (EquipmentSlot equipmentslot : EquipmentSlot.VALUES) {
            ItemStack itemstack = this.getItemBySlot(equipmentslot);
            float f = this.getEquipmentDropChance(equipmentslot);
            if (f != 0.0F) {
                boolean flag = f > 1.0F;
                Entity entity = p_21385_.getEntity();
                if (entity instanceof LivingEntity) {
                    LivingEntity livingentity = (LivingEntity)entity;
                    if (this.level() instanceof ServerLevel serverlevel) {
                        f = EnchantmentHelper.processEquipmentDropChance(serverlevel, livingentity, p_21385_, f);
                    }
                }

                if (!itemstack.isEmpty()
                    && !EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
                    && (p_21387_ || flag)
                    && this.random.nextFloat() < f) {
                    if (!flag && itemstack.isDamageableItem()) {
                        itemstack.setDamageValue(itemstack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemstack.getMaxDamage() - 3, 1))));
                    }

                    this.spawnAtLocation(p_345102_, itemstack);
                    this.setItemSlot(equipmentslot, ItemStack.EMPTY);
                }
            }
        }
    }

    protected float getEquipmentDropChance(EquipmentSlot p_21520_) {
        return switch (p_21520_.getType()) {
            case HAND -> this.handDropChances[p_21520_.getIndex()];
            case HUMANOID_ARMOR -> this.armorDropChances[p_21520_.getIndex()];
            case ANIMAL_ARMOR -> this.bodyArmorDropChance;
        };
    }

    public void dropPreservedEquipment(ServerLevel p_364926_) {
        this.dropPreservedEquipment(p_364926_, p_343352_ -> true);
    }

    public Set<EquipmentSlot> dropPreservedEquipment(ServerLevel p_367808_, Predicate<ItemStack> p_361335_) {
        Set<EquipmentSlot> set = new HashSet<>();

        for (EquipmentSlot equipmentslot : EquipmentSlot.VALUES) {
            ItemStack itemstack = this.getItemBySlot(equipmentslot);
            if (!itemstack.isEmpty()) {
                if (!p_361335_.test(itemstack)) {
                    set.add(equipmentslot);
                } else {
                    double d0 = (double)this.getEquipmentDropChance(equipmentslot);
                    if (d0 > 1.0) {
                        this.setItemSlot(equipmentslot, ItemStack.EMPTY);
                        this.spawnAtLocation(p_367808_, itemstack);
                    }
                }
            }
        }

        return set;
    }

    private LootParams createEquipmentParams(ServerLevel p_331909_) {
        return new LootParams.Builder(p_331909_)
            .withParameter(LootContextParams.ORIGIN, this.position())
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .create(LootContextParamSets.EQUIPMENT);
    }

    public void equip(EquipmentTable p_332456_) {
        this.equip(p_332456_.lootTable(), p_332456_.slotDropChances());
    }

    public void equip(ResourceKey<LootTable> p_328521_, Map<EquipmentSlot, Float> p_335710_) {
        if (this.level() instanceof ServerLevel serverlevel) {
            this.equip(p_328521_, this.createEquipmentParams(serverlevel), p_335710_);
        }
    }

    protected void populateDefaultEquipmentSlots(RandomSource p_217055_, DifficultyInstance p_217056_) {
        if (p_217055_.nextFloat() < 0.15F * p_217056_.getSpecialMultiplier()) {
            int i = p_217055_.nextInt(2);
            float f = this.level().getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
            if (p_217055_.nextFloat() < 0.095F) {
                i++;
            }

            if (p_217055_.nextFloat() < 0.095F) {
                i++;
            }

            if (p_217055_.nextFloat() < 0.095F) {
                i++;
            }

            boolean flag = true;

            for (EquipmentSlot equipmentslot : EQUIPMENT_POPULATION_ORDER) {
                ItemStack itemstack = this.getItemBySlot(equipmentslot);
                if (!flag && p_217055_.nextFloat() < f) {
                    break;
                }

                flag = false;
                if (itemstack.isEmpty()) {
                    Item item = getEquipmentForSlot(equipmentslot, i);
                    if (item != null) {
                        this.setItemSlot(equipmentslot, new ItemStack(item));
                    }
                }
            }
        }
    }

    @Nullable
    public static Item getEquipmentForSlot(EquipmentSlot p_21413_, int p_21414_) {
        switch (p_21413_) {
            case HEAD:
                if (p_21414_ == 0) {
                    return Items.LEATHER_HELMET;
                } else if (p_21414_ == 1) {
                    return Items.GOLDEN_HELMET;
                } else if (p_21414_ == 2) {
                    return Items.CHAINMAIL_HELMET;
                } else if (p_21414_ == 3) {
                    return Items.IRON_HELMET;
                } else if (p_21414_ == 4) {
                    return Items.DIAMOND_HELMET;
                }
            case CHEST:
                if (p_21414_ == 0) {
                    return Items.LEATHER_CHESTPLATE;
                } else if (p_21414_ == 1) {
                    return Items.GOLDEN_CHESTPLATE;
                } else if (p_21414_ == 2) {
                    return Items.CHAINMAIL_CHESTPLATE;
                } else if (p_21414_ == 3) {
                    return Items.IRON_CHESTPLATE;
                } else if (p_21414_ == 4) {
                    return Items.DIAMOND_CHESTPLATE;
                }
            case LEGS:
                if (p_21414_ == 0) {
                    return Items.LEATHER_LEGGINGS;
                } else if (p_21414_ == 1) {
                    return Items.GOLDEN_LEGGINGS;
                } else if (p_21414_ == 2) {
                    return Items.CHAINMAIL_LEGGINGS;
                } else if (p_21414_ == 3) {
                    return Items.IRON_LEGGINGS;
                } else if (p_21414_ == 4) {
                    return Items.DIAMOND_LEGGINGS;
                }
            case FEET:
                if (p_21414_ == 0) {
                    return Items.LEATHER_BOOTS;
                } else if (p_21414_ == 1) {
                    return Items.GOLDEN_BOOTS;
                } else if (p_21414_ == 2) {
                    return Items.CHAINMAIL_BOOTS;
                } else if (p_21414_ == 3) {
                    return Items.IRON_BOOTS;
                } else if (p_21414_ == 4) {
                    return Items.DIAMOND_BOOTS;
                }
            default:
                return null;
        }
    }

    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor p_344674_, RandomSource p_217063_, DifficultyInstance p_217064_) {
        this.enchantSpawnedWeapon(p_344674_, p_217063_, p_217064_);

        for (EquipmentSlot equipmentslot : EquipmentSlot.VALUES) {
            if (equipmentslot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                this.enchantSpawnedArmor(p_344674_, p_217063_, equipmentslot, p_217064_);
            }
        }
    }

    protected void enchantSpawnedWeapon(ServerLevelAccessor p_344989_, RandomSource p_217049_, DifficultyInstance p_344491_) {
        this.enchantSpawnedEquipment(p_344989_, EquipmentSlot.MAINHAND, p_217049_, 0.25F, p_344491_);
    }

    protected void enchantSpawnedArmor(ServerLevelAccessor p_342770_, RandomSource p_217052_, EquipmentSlot p_217054_, DifficultyInstance p_342649_) {
        this.enchantSpawnedEquipment(p_342770_, p_217054_, p_217052_, 0.5F, p_342649_);
    }

    private void enchantSpawnedEquipment(ServerLevelAccessor p_342440_, EquipmentSlot p_344135_, RandomSource p_344290_, float p_343248_, DifficultyInstance p_345046_) {
        ItemStack itemstack = this.getItemBySlot(p_344135_);
        if (!itemstack.isEmpty() && p_344290_.nextFloat() < p_343248_ * p_345046_.getSpecialMultiplier()) {
            EnchantmentHelper.enchantItemFromProvider(itemstack, p_342440_.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, p_345046_, p_344290_);
            this.setItemSlot(p_344135_, itemstack);
        }
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_21434_, DifficultyInstance p_21435_, EntitySpawnReason p_369316_, @Nullable SpawnGroupData p_21437_) {
        RandomSource randomsource = p_21434_.getRandom();
        AttributeInstance attributeinstance = Objects.requireNonNull(this.getAttribute(Attributes.FOLLOW_RANGE));
        if (!attributeinstance.hasModifier(RANDOM_SPAWN_BONUS_ID)) {
            attributeinstance.addPermanentModifier(
                new AttributeModifier(RANDOM_SPAWN_BONUS_ID, randomsource.triangle(0.0, 0.11485000000000001), AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            );
        }

        this.setLeftHanded(randomsource.nextFloat() < 0.05F);
        return p_21437_;
    }

    public void setPersistenceRequired() {
        this.persistenceRequired = true;
    }

    @Override
    public void setDropChance(EquipmentSlot p_21410_, float p_21411_) {
        switch (p_21410_.getType()) {
            case HAND:
                this.handDropChances[p_21410_.getIndex()] = p_21411_;
                break;
            case HUMANOID_ARMOR:
                this.armorDropChances[p_21410_.getIndex()] = p_21411_;
                break;
            case ANIMAL_ARMOR:
                this.bodyArmorDropChance = p_21411_;
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return this.canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean p_21554_) {
        this.canPickUpLoot = p_21554_;
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot p_367943_) {
        return this.canPickUpLoot();
    }

    public boolean isPersistenceRequired() {
        return this.persistenceRequired;
    }

    @Override
    public final InteractionResult interact(Player p_21420_, InteractionHand p_21421_) {
        if (!this.isAlive()) {
            return InteractionResult.PASS;
        } else {
            InteractionResult interactionresult = this.checkAndHandleImportantInteractions(p_21420_, p_21421_);
            if (interactionresult.consumesAction()) {
                this.gameEvent(GameEvent.ENTITY_INTERACT, p_21420_);
                return interactionresult;
            } else {
                InteractionResult interactionresult1 = super.interact(p_21420_, p_21421_);
                if (interactionresult1 != InteractionResult.PASS) {
                    return interactionresult1;
                } else {
                    interactionresult = this.mobInteract(p_21420_, p_21421_);
                    if (interactionresult.consumesAction()) {
                        this.gameEvent(GameEvent.ENTITY_INTERACT, p_21420_);
                        return interactionresult;
                    } else {
                        return InteractionResult.PASS;
                    }
                }
            }
        }
    }

    private InteractionResult checkAndHandleImportantInteractions(Player p_21500_, InteractionHand p_21501_) {
        ItemStack itemstack = p_21500_.getItemInHand(p_21501_);
        if (itemstack.is(Items.NAME_TAG)) {
            InteractionResult interactionresult = itemstack.interactLivingEntity(p_21500_, this, p_21501_);
            if (interactionresult.consumesAction()) {
                return interactionresult;
            }
        }

        if (itemstack.getItem() instanceof SpawnEggItem) {
            if (this.level() instanceof ServerLevel) {
                SpawnEggItem spawneggitem = (SpawnEggItem)itemstack.getItem();
                Optional<Mob> optional = spawneggitem.spawnOffspringFromSpawnEgg(
                    p_21500_, this, (EntityType<? extends Mob>)this.getType(), (ServerLevel)this.level(), this.position(), itemstack
                );
                optional.ifPresent(p_21476_ -> this.onOffspringSpawnedFromEgg(p_21500_, p_21476_));
                if (optional.isEmpty()) {
                    return InteractionResult.PASS;
                }
            }

            return InteractionResult.SUCCESS_SERVER;
        } else {
            return InteractionResult.PASS;
        }
    }

    protected void onOffspringSpawnedFromEgg(Player p_21422_, Mob p_21423_) {
    }

    protected InteractionResult mobInteract(Player p_21472_, InteractionHand p_21473_) {
        return InteractionResult.PASS;
    }

    public boolean isWithinRestriction() {
        return this.isWithinRestriction(this.blockPosition());
    }

    public boolean isWithinRestriction(BlockPos p_21445_) {
        return this.restrictRadius == -1.0F ? true : this.restrictCenter.distSqr(p_21445_) < (double)(this.restrictRadius * this.restrictRadius);
    }

    public void restrictTo(BlockPos p_21447_, int p_21448_) {
        this.restrictCenter = p_21447_;
        this.restrictRadius = (float)p_21448_;
    }

    public BlockPos getRestrictCenter() {
        return this.restrictCenter;
    }

    public float getRestrictRadius() {
        return this.restrictRadius;
    }

    public void clearRestriction() {
        this.restrictRadius = -1.0F;
    }

    public boolean hasRestriction() {
        return this.restrictRadius != -1.0F;
    }

    @Nullable
    public <T extends Mob> T convertTo(
        EntityType<T> p_21407_, ConversionParams p_365288_, EntitySpawnReason p_367052_, ConversionParams.AfterConversion<T> p_368263_
    ) {
        if (this.isRemoved()) {
            return null;
        } else {
            T t = (T)p_21407_.create(this.level(), p_367052_);
            if (t == null) {
                return null;
            } else {
                p_365288_.type().convert(this, t, p_365288_);
                p_368263_.finalizeConversion(t);
                if (this.level() instanceof ServerLevel serverlevel) {
                    serverlevel.addFreshEntity(t);
                }

                if (p_365288_.type().shouldDiscardAfterConversion()) {
                    this.discard();
                }

                return t;
            }
        }
    }

    @Nullable
    public <T extends Mob> T convertTo(EntityType<T> p_364522_, ConversionParams p_368972_, ConversionParams.AfterConversion<T> p_362927_) {
        return this.convertTo(p_364522_, p_368972_, EntitySpawnReason.CONVERSION, p_362927_);
    }

    @Nullable
    @Override
    public Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData p_344337_) {
        this.leashData = p_344337_;
    }

    @Override
    public void onLeashRemoved() {
        if (this.getLeashData() == null) {
            this.clearRestriction();
        }
    }

    @Override
    public void leashTooFarBehaviour() {
        Leashable.super.leashTooFarBehaviour();
        this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
    }

    @Override
    public boolean canBeLeashed() {
        return !(this instanceof Enemy);
    }

    @Override
    public boolean startRiding(Entity p_21396_, boolean p_21397_) {
        boolean flag = super.startRiding(p_21396_, p_21397_);
        if (flag && this.isLeashed()) {
            this.dropLeash();
        }

        return flag;
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && !this.isNoAi();
    }

    public void setNoAi(boolean p_21558_) {
        byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, p_21558_ ? (byte)(b0 | 1) : (byte)(b0 & -2));
    }

    public void setLeftHanded(boolean p_21560_) {
        byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, p_21560_ ? (byte)(b0 | 2) : (byte)(b0 & -3));
    }

    public void setAggressive(boolean p_21562_) {
        byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, p_21562_ ? (byte)(b0 | 4) : (byte)(b0 & -5));
    }

    public boolean isNoAi() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 1) != 0;
    }

    public boolean isLeftHanded() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 2) != 0;
    }

    public boolean isAggressive() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 4) != 0;
    }

    public void setBaby(boolean p_21451_) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public boolean isWithinMeleeAttackRange(LivingEntity p_217067_) {
        return this.getAttackBoundingBox().intersects(p_217067_.getHitbox());
    }

    protected AABB getAttackBoundingBox() {
        Entity entity = this.getVehicle();
        AABB aabb;
        if (entity != null) {
            AABB aabb1 = entity.getBoundingBox();
            AABB aabb2 = this.getBoundingBox();
            aabb = new AABB(
                Math.min(aabb2.minX, aabb1.minX),
                aabb2.minY,
                Math.min(aabb2.minZ, aabb1.minZ),
                Math.max(aabb2.maxX, aabb1.maxX),
                aabb2.maxY,
                Math.max(aabb2.maxZ, aabb1.maxZ)
            );
        } else {
            aabb = this.getBoundingBox();
        }

        return aabb.inflate(DEFAULT_ATTACK_REACH, 0.0, DEFAULT_ATTACK_REACH);
    }

    @Override
    public boolean doHurtTarget(ServerLevel p_365421_, Entity p_21372_) {
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ItemStack itemstack = this.getWeaponItem();
        DamageSource damagesource = Optional.ofNullable(itemstack.getItem().getDamageSource(this)).orElse(this.damageSources().mobAttack(this));
        f = EnchantmentHelper.modifyDamage(p_365421_, itemstack, p_21372_, damagesource, f);
        f += itemstack.getItem().getAttackDamageBonus(p_21372_, f, damagesource);
        boolean flag = p_21372_.hurtServer(p_365421_, damagesource, f);
        if (flag) {
            float f1 = this.getKnockback(p_21372_, damagesource);
            if (f1 > 0.0F && p_21372_ instanceof LivingEntity livingentity) {
                livingentity.knockback(
                    (double)(f1 * 0.5F),
                    (double)Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)),
                    (double)(-Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)))
                );
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }

            if (p_21372_ instanceof LivingEntity livingentity1) {
                itemstack.hurtEnemy(livingentity1, this);
            }

            EnchantmentHelper.doPostAttackEffects(p_365421_, p_21372_, damagesource);
            this.setLastHurtMob(p_21372_);
            this.playAttackSound();
        }

        return flag;
    }

    protected void playAttackSound() {
    }

    protected boolean isSunBurnTick() {
        if (this.level().isDay() && !this.level().isClientSide) {
            float f = this.getLightLevelDependentMagicValue();
            BlockPos blockpos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
            boolean flag = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
            if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !flag && this.level().canSeeSky(blockpos)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> p_204045_) {
        if (this.getNavigation().canFloat()) {
            super.jumpInLiquid(p_204045_);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.3, 0.0));
        }
    }

    @VisibleForTesting
    public void removeFreeWill() {
        this.removeAllGoals(p_341273_ -> true);
        this.getBrain().removeAllBehaviors();
    }

    public void removeAllGoals(Predicate<Goal> p_262667_) {
        this.goalSelector.removeAllGoals(p_262667_);
    }

    @Override
    protected void removeAfterChangingDimensions() {
        super.removeAfterChangingDimensions();
        this.getAllSlots().forEach(p_278936_ -> {
            if (!p_278936_.isEmpty()) {
                p_278936_.setCount(0);
            }
        });
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        SpawnEggItem spawneggitem = SpawnEggItem.byId(this.getType());
        return spawneggitem == null ? null : new ItemStack(spawneggitem);
    }

    @Override
    protected void onAttributeUpdated(Holder<Attribute> p_365996_) {
        super.onAttributeUpdated(p_365996_);
        if (p_365996_.is(Attributes.FOLLOW_RANGE) || p_365996_.is(Attributes.TEMPT_RANGE)) {
            this.getNavigation().updatePathfinderMaxVisitedNodes();
        }
    }

    @VisibleForTesting
    public float[] getHandDropChances() {
        return this.handDropChances;
    }

    @VisibleForTesting
    public float[] getArmorDropChances() {
        return this.armorDropChances;
    }
}