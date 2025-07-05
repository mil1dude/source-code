package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

public class WitherSkeleton extends AbstractSkeleton {
    public WitherSkeleton(EntityType<? extends WitherSkeleton> p_34166_, Level p_34167_) {
        super(p_34166_, p_34167_);
        this.setPathfindingMalus(PathType.LAVA, 8.0F);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractPiglin.class, true));
        super.registerGoals();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_34195_) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    SoundEvent getStepSound() {
        return SoundEvents.WITHER_SKELETON_STEP;
    }

    @Override
    public TagKey<Item> getPreferredWeaponType() {
        return null;
    }

    @Override
    public boolean canHoldItem(ItemStack p_378340_) {
        return !p_378340_.is(ItemTags.WITHER_SKELETON_DISLIKED_WEAPONS) && super.canHoldItem(p_378340_);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel p_343020_, DamageSource p_34174_, boolean p_34176_) {
        super.dropCustomDeathLoot(p_343020_, p_34174_, p_34176_);
        if (p_34174_.getEntity() instanceof Creeper creeper && creeper.canDropMobsSkull()) {
            creeper.increaseDroppedSkulls();
            this.spawnAtLocation(p_343020_, Items.WITHER_SKELETON_SKULL);
        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource p_219154_, DifficultyInstance p_219155_) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor p_343231_, RandomSource p_219157_, DifficultyInstance p_219158_) {
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_34178_, DifficultyInstance p_34179_, EntitySpawnReason p_368639_, @Nullable SpawnGroupData p_34181_) {
        SpawnGroupData spawngroupdata = super.finalizeSpawn(p_34178_, p_34179_, p_368639_, p_34181_);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0);
        this.reassessWeaponGoal();
        return spawngroupdata;
    }

    @Override
    public boolean doHurtTarget(ServerLevel p_369658_, Entity p_34169_) {
        if (!super.doHurtTarget(p_369658_, p_34169_)) {
            return false;
        } else {
            if (p_34169_ instanceof LivingEntity) {
                ((LivingEntity)p_34169_).addEffect(new MobEffectInstance(MobEffects.WITHER, 200), this);
            }

            return true;
        }
    }

    @Override
    protected AbstractArrow getArrow(ItemStack p_34189_, float p_34190_, @Nullable ItemStack p_342697_) {
        AbstractArrow abstractarrow = super.getArrow(p_34189_, p_34190_, p_342697_);
        abstractarrow.igniteForSeconds(100.0F);
        return abstractarrow;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance p_34192_) {
        return p_34192_.is(MobEffects.WITHER) ? false : super.canBeAffected(p_34192_);
    }
}