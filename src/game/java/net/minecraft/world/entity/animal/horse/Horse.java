package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;

public class Horse extends AbstractHorse implements VariantHolder<Variant> {
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.HORSE
        .getDimensions()
        .withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, EntityType.HORSE.getHeight() + 0.125F, 0.0F))
        .scale(0.5F);

    public Horse(EntityType<? extends Horse> p_30689_, Level p_30690_) {
        super(p_30689_, p_30690_);
    }

    @Override
    protected void randomizeAttributes(RandomSource p_218815_) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)generateMaxHealth(p_218815_::nextInt));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(generateSpeed(p_218815_::nextDouble));
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(generateJumpStrength(p_218815_::nextDouble));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_335516_) {
        super.defineSynchedData(p_335516_);
        p_335516_.define(DATA_ID_TYPE_VARIANT, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_30716_) {
        super.addAdditionalSaveData(p_30716_);
        p_30716_.putInt("Variant", this.getTypeVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_30711_) {
        super.readAdditionalSaveData(p_30711_);
        this.setTypeVariant(p_30711_.getInt("Variant"));
    }

    private void setTypeVariant(int p_30737_) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, p_30737_);
    }

    private int getTypeVariant() {
        return this.entityData.get(DATA_ID_TYPE_VARIANT);
    }

    private void setVariantAndMarkings(Variant p_30700_, Markings p_30701_) {
        this.setTypeVariant(p_30700_.getId() & 0xFF | p_30701_.getId() << 8 & 0xFF00);
    }

    public Variant getVariant() {
        return Variant.byId(this.getTypeVariant() & 0xFF);
    }

    public void setVariant(Variant p_262684_) {
        this.setTypeVariant(p_262684_.getId() & 0xFF | this.getTypeVariant() & -256);
    }

    public Markings getMarkings() {
        return Markings.byId((this.getTypeVariant() & 0xFF00) >> 8);
    }

    @Override
    protected void playGallopSound(SoundType p_30709_) {
        super.playGallopSound(p_30709_);
        if (this.random.nextInt(10) == 0) {
            this.playSound(SoundEvents.HORSE_BREATHE, p_30709_.getVolume() * 0.6F, p_30709_.getPitch());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HORSE_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.HORSE_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_30720_) {
        return SoundEvents.HORSE_HURT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.HORSE_ANGRY;
    }

    @Override
    public InteractionResult mobInteract(Player p_30713_, InteractionHand p_30714_) {
        boolean flag = !this.isBaby() && this.isTamed() && p_30713_.isSecondaryUseActive();
        if (!this.isVehicle() && !flag) {
            ItemStack itemstack = p_30713_.getItemInHand(p_30714_);
            if (!itemstack.isEmpty()) {
                if (this.isFood(itemstack)) {
                    return this.fedFood(p_30713_, itemstack);
                }

                if (!this.isTamed()) {
                    this.makeMad();
                    return InteractionResult.SUCCESS;
                }
            }

            return super.mobInteract(p_30713_, p_30714_);
        } else {
            return super.mobInteract(p_30713_, p_30714_);
        }
    }

    @Override
    public boolean canMate(Animal p_30698_) {
        if (p_30698_ == this) {
            return false;
        } else {
            return !(p_30698_ instanceof Donkey) && !(p_30698_ instanceof Horse) ? false : this.canParent() && ((AbstractHorse)p_30698_).canParent();
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_149533_, AgeableMob p_149534_) {
        if (p_149534_ instanceof Donkey) {
            Mule mule = EntityType.MULE.create(p_149533_, EntitySpawnReason.BREEDING);
            if (mule != null) {
                this.setOffspringAttributes(p_149534_, mule);
            }

            return mule;
        } else {
            Horse horse = (Horse)p_149534_;
            Horse horse1 = EntityType.HORSE.create(p_149533_, EntitySpawnReason.BREEDING);
            if (horse1 != null) {
                int i = this.random.nextInt(9);
                Variant variant;
                if (i < 4) {
                    variant = this.getVariant();
                } else if (i < 8) {
                    variant = horse.getVariant();
                } else {
                    variant = Util.getRandom(Variant.values(), this.random);
                }

                int j = this.random.nextInt(5);
                Markings markings;
                if (j < 2) {
                    markings = this.getMarkings();
                } else if (j < 4) {
                    markings = horse.getMarkings();
                } else {
                    markings = Util.getRandom(Markings.values(), this.random);
                }

                horse1.setVariantAndMarkings(variant, markings);
                this.setOffspringAttributes(p_149534_, horse1);
            }

            return horse1;
        }
    }

    @Override
    public boolean canUseSlot(EquipmentSlot p_345205_) {
        return true;
    }

    @Override
    protected void hurtArmor(DamageSource p_363728_, float p_365413_) {
        this.doHurtEquipment(p_363728_, p_365413_, new EquipmentSlot[]{EquipmentSlot.BODY});
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_30703_, DifficultyInstance p_30704_, EntitySpawnReason p_370066_, @Nullable SpawnGroupData p_30706_) {
        RandomSource randomsource = p_30703_.getRandom();
        Variant variant;
        if (p_30706_ instanceof Horse.HorseGroupData) {
            variant = ((Horse.HorseGroupData)p_30706_).variant;
        } else {
            variant = Util.getRandom(Variant.values(), randomsource);
            p_30706_ = new Horse.HorseGroupData(variant);
        }

        this.setVariantAndMarkings(variant, Util.getRandom(Markings.values(), randomsource));
        return super.finalizeSpawn(p_30703_, p_30704_, p_370066_, p_30706_);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose p_329389_) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(p_329389_);
    }

    public static class HorseGroupData extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        public HorseGroupData(Variant p_30740_) {
            super(true);
            this.variant = p_30740_;
        }
    }
}