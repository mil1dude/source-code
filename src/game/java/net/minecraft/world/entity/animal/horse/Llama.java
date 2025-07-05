package net.minecraft.world.entity.animal.horse;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Llama extends AbstractChestedHorse implements VariantHolder<Llama.Variant>, RangedAttackMob {
    private static final int MAX_STRENGTH = 5;
    private static final EntityDataAccessor<Integer> DATA_STRENGTH_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.LLAMA
        .getDimensions()
        .withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, EntityType.LLAMA.getHeight() - 0.8125F, -0.3F))
        .scale(0.5F);
    boolean didSpit;
    @Nullable
    private Llama caravanHead;
    @Nullable
    private Llama caravanTail;

    public Llama(EntityType<? extends Llama> p_30750_, Level p_30751_) {
        super(p_30750_, p_30751_);
        this.getNavigation().setRequiredPathLength(40.0F);
    }

    public boolean isTraderLlama() {
        return false;
    }

    private void setStrength(int p_30841_) {
        this.entityData.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, p_30841_)));
    }

    private void setRandomStrength(RandomSource p_218818_) {
        int i = p_218818_.nextFloat() < 0.04F ? 5 : 3;
        this.setStrength(1 + p_218818_.nextInt(i));
    }

    public int getStrength() {
        return this.entityData.get(DATA_STRENGTH_ID);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_30793_) {
        super.addAdditionalSaveData(p_30793_);
        p_30793_.putInt("Variant", this.getVariant().id);
        p_30793_.putInt("Strength", this.getStrength());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_30780_) {
        this.setStrength(p_30780_.getInt("Strength"));
        super.readAdditionalSaveData(p_30780_);
        this.setVariant(Llama.Variant.byId(p_30780_.getInt("Variant")));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
        this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, 2.1F));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25, 40, 20.0F));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.25, p_331564_ -> p_331564_.is(ItemTags.LLAMA_TEMPT_ITEMS), false));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new Llama.LlamaHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new Llama.LlamaAttackWolfGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseChestedHorseAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_327895_) {
        super.defineSynchedData(p_327895_);
        p_327895_.define(DATA_STRENGTH_ID, 0);
        p_327895_.define(DATA_VARIANT_ID, 0);
    }

    public Llama.Variant getVariant() {
        return Llama.Variant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    public void setVariant(Llama.Variant p_262628_) {
        this.entityData.set(DATA_VARIANT_ID, p_262628_.id);
    }

    @Override
    public boolean isFood(ItemStack p_30832_) {
        return p_30832_.is(ItemTags.LLAMA_FOOD);
    }

    @Override
    protected boolean handleEating(Player p_30796_, ItemStack p_30797_) {
        int i = 0;
        int j = 0;
        float f = 0.0F;
        boolean flag = false;
        if (p_30797_.is(Items.WHEAT)) {
            i = 10;
            j = 3;
            f = 2.0F;
        } else if (p_30797_.is(Blocks.HAY_BLOCK.asItem())) {
            i = 90;
            j = 6;
            f = 10.0F;
            if (this.isTamed() && this.getAge() == 0 && this.canFallInLove()) {
                flag = true;
                this.setInLove(p_30796_);
            }
        }

        if (this.getHealth() < this.getMaxHealth() && f > 0.0F) {
            this.heal(f);
            flag = true;
        }

        if (this.isBaby() && i > 0) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level().isClientSide) {
                this.ageUp(i);
            }

            flag = true;
        }

        if (j > 0 && (flag || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
            flag = true;
            if (!this.level().isClientSide) {
                this.modifyTemper(j);
            }
        }

        if (flag && !this.isSilent()) {
            SoundEvent soundevent = this.getEatingSound();
            if (soundevent != null) {
                this.level()
                    .playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        this.getEatingSound(),
                        this.getSoundSource(),
                        1.0F,
                        1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                    );
            }
        }

        return flag;
    }

    @Override
    public boolean isImmobile() {
        return this.isDeadOrDying() || this.isEating();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_30774_, DifficultyInstance p_30775_, EntitySpawnReason p_365213_, @Nullable SpawnGroupData p_30777_) {
        RandomSource randomsource = p_30774_.getRandom();
        this.setRandomStrength(randomsource);
        Llama.Variant llama$variant;
        if (p_30777_ instanceof Llama.LlamaGroupData) {
            llama$variant = ((Llama.LlamaGroupData)p_30777_).variant;
        } else {
            llama$variant = Util.getRandom(Llama.Variant.values(), randomsource);
            p_30777_ = new Llama.LlamaGroupData(llama$variant);
        }

        this.setVariant(llama$variant);
        return super.finalizeSpawn(p_30774_, p_30775_, p_365213_, p_30777_);
    }

    @Override
    protected boolean canPerformRearing() {
        return false;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.LLAMA_ANGRY;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_30803_) {
        return SoundEvents.LLAMA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.LLAMA_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.LLAMA_EAT;
    }

    @Override
    protected void playStepSound(BlockPos p_30790_, BlockState p_30791_) {
        this.playSound(SoundEvents.LLAMA_STEP, 0.15F, 1.0F);
    }

    @Override
    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.LLAMA_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? this.getStrength() : 0;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot p_344756_) {
        return true;
    }

    @Override
    public boolean isSaddleable() {
        return false;
    }

    @Override
    public int getMaxTemper() {
        return 30;
    }

    @Override
    public boolean canMate(Animal p_30765_) {
        return p_30765_ != this && p_30765_ instanceof Llama && this.canParent() && ((Llama)p_30765_).canParent();
    }

    @Nullable
    public Llama getBreedOffspring(ServerLevel p_149545_, AgeableMob p_149546_) {
        Llama llama = this.makeNewLlama();
        if (llama != null) {
            this.setOffspringAttributes(p_149546_, llama);
            Llama llama1 = (Llama)p_149546_;
            int i = this.random.nextInt(Math.max(this.getStrength(), llama1.getStrength())) + 1;
            if (this.random.nextFloat() < 0.03F) {
                i++;
            }

            llama.setStrength(i);
            llama.setVariant(this.random.nextBoolean() ? this.getVariant() : llama1.getVariant());
        }

        return llama;
    }

    @Nullable
    protected Llama makeNewLlama() {
        return EntityType.LLAMA.create(this.level(), EntitySpawnReason.BREEDING);
    }

    private void spit(LivingEntity p_30828_) {
        LlamaSpit llamaspit = new LlamaSpit(this.level(), this);
        double d0 = p_30828_.getX() - this.getX();
        double d1 = p_30828_.getY(0.3333333333333333) - llamaspit.getY();
        double d2 = p_30828_.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2) * 0.2F;
        if (this.level() instanceof ServerLevel serverlevel) {
            Projectile.spawnProjectileUsingShoot(llamaspit, serverlevel, ItemStack.EMPTY, d0, d1 + d3, d2, 1.5F, 10.0F);
        }

        if (!this.isSilent()) {
            this.level()
                .playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.LLAMA_SPIT,
                    this.getSoundSource(),
                    1.0F,
                    1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                );
        }

        this.didSpit = true;
    }

    void setDidSpit(boolean p_30753_) {
        this.didSpit = p_30753_;
    }

    @Override
    public boolean causeFallDamage(float p_149538_, float p_149539_, DamageSource p_149540_) {
        int i = this.calculateFallDamage(p_149538_, p_149539_);
        if (i <= 0) {
            return false;
        } else {
            if (p_149538_ >= 6.0F) {
                this.hurt(p_149540_, (float)i);
                if (this.isVehicle()) {
                    for (Entity entity : this.getIndirectPassengers()) {
                        entity.hurt(p_149540_, (float)i);
                    }
                }
            }

            this.playBlockFallSound();
            return true;
        }
    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
        }

        this.caravanHead = null;
    }

    public void joinCaravan(Llama p_30767_) {
        this.caravanHead = p_30767_;
        this.caravanHead.caravanTail = this;
    }

    public boolean hasCaravanTail() {
        return this.caravanTail != null;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    @Nullable
    public Llama getCaravanHead() {
        return this.caravanHead;
    }

    @Override
    protected double followLeashSpeed() {
        return 2.0;
    }

    @Override
    protected void followMommy(ServerLevel p_364063_) {
        if (!this.inCaravan() && this.isBaby()) {
            super.followMommy(p_364063_);
        }
    }

    @Override
    public boolean canEatGrass() {
        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity p_30762_, float p_30763_) {
        this.spit(p_30762_);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.75 * (double)this.getEyeHeight(), (double)this.getBbWidth() * 0.5);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose p_332334_) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(p_332334_);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity p_298288_, EntityDimensions p_300390_, float p_300878_) {
        return getDefaultPassengerAttachmentPoint(this, p_298288_, p_300390_.attachments());
    }

    static class LlamaAttackWolfGoal extends NearestAttackableTargetGoal<Wolf> {
        public LlamaAttackWolfGoal(Llama p_30843_) {
            super(p_30843_, Wolf.class, 16, false, true, (p_359217_, p_359218_) -> !((Wolf)p_359217_).isTame());
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.25;
        }
    }

    static class LlamaGroupData extends AgeableMob.AgeableMobGroupData {
        public final Llama.Variant variant;

        LlamaGroupData(Llama.Variant p_262658_) {
            super(true);
            this.variant = p_262658_;
        }
    }

    static class LlamaHurtByTargetGoal extends HurtByTargetGoal {
        public LlamaHurtByTargetGoal(Llama p_30854_) {
            super(p_30854_);
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mob instanceof Llama llama && llama.didSpit) {
                llama.setDidSpit(false);
                return false;
            }

            return super.canContinueToUse();
        }
    }

    public static enum Variant implements StringRepresentable {
        CREAMY(0, "creamy"),
        WHITE(1, "white"),
        BROWN(2, "brown"),
        GRAY(3, "gray");

        public static final Codec<Llama.Variant> CODEC = StringRepresentable.fromEnum(Llama.Variant::values);
        private static final IntFunction<Llama.Variant> BY_ID = ByIdMap.continuous(Llama.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        final int id;
        private final String name;

        private Variant(final int p_262677_, final String p_262641_) {
            this.id = p_262677_;
            this.name = p_262641_;
        }

        public int getId() {
            return this.id;
        }

        public static Llama.Variant byId(int p_262608_) {
            return BY_ID.apply(p_262608_);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}