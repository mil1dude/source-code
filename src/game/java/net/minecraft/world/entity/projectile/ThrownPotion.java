package net.minecraft.world.entity.projectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownPotion extends ThrowableItemProjectile {
    public static final double SPLASH_RANGE = 4.0;
    private static final double SPLASH_RANGE_SQ = 16.0;
    public static final Predicate<LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = p_359357_ -> p_359357_.isSensitiveToWater() || p_359357_.isOnFire();

    public ThrownPotion(EntityType<? extends ThrownPotion> p_37527_, Level p_37528_) {
        super(p_37527_, p_37528_);
    }

    public ThrownPotion(Level p_37535_, LivingEntity p_37536_, ItemStack p_366952_) {
        super(EntityType.POTION, p_37536_, p_37535_, p_366952_);
    }

    public ThrownPotion(Level p_37530_, double p_37531_, double p_37532_, double p_37533_, ItemStack p_368100_) {
        super(EntityType.POTION, p_37531_, p_37532_, p_37533_, p_37530_, p_368100_);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHitBlock(BlockHitResult p_37541_) {
        super.onHitBlock(p_37541_);
        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            Direction direction = p_37541_.getDirection();
            BlockPos blockpos = p_37541_.getBlockPos();
            BlockPos blockpos1 = blockpos.relative(direction);
            PotionContents potioncontents = itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (potioncontents.is(Potions.WATER)) {
                this.dowseFire(blockpos1);
                this.dowseFire(blockpos1.relative(direction.getOpposite()));

                for (Direction direction1 : Direction.Plane.HORIZONTAL) {
                    this.dowseFire(blockpos1.relative(direction1));
                }
            }
        }
    }

    @Override
    protected void onHit(HitResult p_37543_) {
        super.onHit(p_37543_);
        if (this.level() instanceof ServerLevel serverlevel) {
            ItemStack itemstack = this.getItem();
            PotionContents potioncontents = itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (potioncontents.is(Potions.WATER)) {
                this.applyWater(serverlevel);
            } else if (potioncontents.hasEffects()) {
                if (this.isLingering()) {
                    this.makeAreaOfEffectCloud(potioncontents);
                } else {
                    this.applySplash(
                        serverlevel, potioncontents.getAllEffects(), p_37543_.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)p_37543_).getEntity() : null
                    );
                }
            }

            int i = potioncontents.potion().isPresent() && potioncontents.potion().get().value().hasInstantEffects() ? 2007 : 2002;
            serverlevel.levelEvent(i, this.blockPosition(), potioncontents.getColor());
            this.discard();
        }
    }

    private void applyWater(ServerLevel p_362613_) {
        AABB aabb = this.getBoundingBox().inflate(4.0, 2.0, 4.0);

        for (LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, aabb, WATER_SENSITIVE_OR_ON_FIRE)) {
            double d0 = this.distanceToSqr(livingentity);
            if (d0 < 16.0) {
                if (livingentity.isSensitiveToWater()) {
                    livingentity.hurtServer(p_362613_, this.damageSources().indirectMagic(this, this.getOwner()), 1.0F);
                }

                if (livingentity.isOnFire() && livingentity.isAlive()) {
                    livingentity.extinguishFire();
                }
            }
        }

        for (Axolotl axolotl : this.level().getEntitiesOfClass(Axolotl.class, aabb)) {
            axolotl.rehydrate();
        }
    }

    private void applySplash(ServerLevel p_366171_, Iterable<MobEffectInstance> p_329810_, @Nullable Entity p_37549_) {
        AABB aabb = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list = p_366171_.getEntitiesOfClass(LivingEntity.class, aabb);
        if (!list.isEmpty()) {
            Entity entity = this.getEffectSource();

            for (LivingEntity livingentity : list) {
                if (livingentity.isAffectedByPotions()) {
                    double d0 = this.distanceToSqr(livingentity);
                    if (d0 < 16.0) {
                        double d1;
                        if (livingentity == p_37549_) {
                            d1 = 1.0;
                        } else {
                            d1 = 1.0 - Math.sqrt(d0) / 4.0;
                        }

                        for (MobEffectInstance mobeffectinstance : p_329810_) {
                            Holder<MobEffect> holder = mobeffectinstance.getEffect();
                            if (holder.value().isInstantenous()) {
                                holder.value().applyInstantenousEffect(p_366171_, this, this.getOwner(), livingentity, mobeffectinstance.getAmplifier(), d1);
                            } else {
                                int i = mobeffectinstance.mapDuration(p_267930_ -> (int)(d1 * (double)p_267930_ + 0.5));
                                MobEffectInstance mobeffectinstance1 = new MobEffectInstance(
                                    holder, i, mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible()
                                );
                                if (!mobeffectinstance1.endsWithin(20)) {
                                    livingentity.addEffect(mobeffectinstance1, entity);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void makeAreaOfEffectCloud(PotionContents p_331360_) {
        AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        if (this.getOwner() instanceof LivingEntity livingentity) {
            areaeffectcloud.setOwner(livingentity);
        }

        areaeffectcloud.setRadius(3.0F);
        areaeffectcloud.setRadiusOnUse(-0.5F);
        areaeffectcloud.setWaitTime(10);
        areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());
        areaeffectcloud.setPotionContents(p_331360_);
        this.level().addFreshEntity(areaeffectcloud);
    }

    private boolean isLingering() {
        return this.getItem().is(Items.LINGERING_POTION);
    }

    private void dowseFire(BlockPos p_150193_) {
        BlockState blockstate = this.level().getBlockState(p_150193_);
        if (blockstate.is(BlockTags.FIRE)) {
            this.level().destroyBlock(p_150193_, false, this);
        } else if (AbstractCandleBlock.isLit(blockstate)) {
            AbstractCandleBlock.extinguish(null, blockstate, this.level(), p_150193_);
        } else if (CampfireBlock.isLitCampfire(blockstate)) {
            this.level().levelEvent(null, 1009, p_150193_, 0);
            CampfireBlock.dowse(this.getOwner(), this.level(), p_150193_, blockstate);
            this.level().setBlockAndUpdate(p_150193_, blockstate.setValue(CampfireBlock.LIT, Boolean.valueOf(false)));
        }
    }

    @Override
    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity p_344174_, DamageSource p_343470_) {
        double d0 = p_344174_.position().x - this.position().x;
        double d1 = p_344174_.position().z - this.position().z;
        return DoubleDoubleImmutablePair.of(d0, d1);
    }
}