package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class SnowGolem extends AbstractGolem implements Shearable, RangedAttackMob {
    private static final EntityDataAccessor<Byte> DATA_PUMPKIN_ID = SynchedEntityData.defineId(SnowGolem.class, EntityDataSerializers.BYTE);
    private static final byte PUMPKIN_FLAG = 16;

    public SnowGolem(EntityType<? extends SnowGolem> p_29902_, Level p_29903_) {
        super(p_29902_, p_29903_);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25, 20, 10.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0, 1.0000001E-5F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, (p_29932_, p_364036_) -> p_29932_ instanceof Enemy));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_331373_) {
        super.defineSynchedData(p_331373_);
        p_331373_.define(DATA_PUMPKIN_ID, (byte)16);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_29923_) {
        super.addAdditionalSaveData(p_29923_);
        p_29923_.putBoolean("Pumpkin", this.hasPumpkin());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_29915_) {
        super.readAdditionalSaveData(p_29915_);
        if (p_29915_.contains("Pumpkin")) {
            this.setPumpkin(p_29915_.getBoolean("Pumpkin"));
        }
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel serverlevel) {
            if (this.level().getBiome(this.blockPosition()).is(BiomeTags.SNOW_GOLEM_MELTS)) {
                this.hurtServer(serverlevel, this.damageSources().onFire(), 1.0F);
            }

            if (!serverlevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return;
            }

            BlockState blockstate = Blocks.SNOW.defaultBlockState();

            for (int i = 0; i < 4; i++) {
                int j = Mth.floor(this.getX() + (double)((float)(i % 2 * 2 - 1) * 0.25F));
                int k = Mth.floor(this.getY());
                int l = Mth.floor(this.getZ() + (double)((float)(i / 2 % 2 * 2 - 1) * 0.25F));
                BlockPos blockpos = new BlockPos(j, k, l);
                if (this.level().getBlockState(blockpos).isAir() && blockstate.canSurvive(this.level(), blockpos)) {
                    this.level().setBlockAndUpdate(blockpos, blockstate);
                    this.level().gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(this, blockstate));
                }
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity p_29912_, float p_29913_) {
        double d0 = p_29912_.getX() - this.getX();
        double d1 = p_29912_.getEyeY() - 1.1F;
        double d2 = p_29912_.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2) * 0.2F;
        if (this.level() instanceof ServerLevel serverlevel) {
            ItemStack itemstack = new ItemStack(Items.SNOWBALL);
            Projectile.spawnProjectile(
                new Snowball(serverlevel, this, itemstack),
                serverlevel,
                itemstack,
                p_375116_ -> p_375116_.shoot(d0, d1 + d3 - p_375116_.getY(), d2, 1.6F, 12.0F)
            );
        }

        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Override
    protected InteractionResult mobInteract(Player p_29920_, InteractionHand p_29921_) {
        ItemStack itemstack = p_29920_.getItemInHand(p_29921_);
        if (itemstack.is(Items.SHEARS) && this.readyForShearing()) {
            if (this.level() instanceof ServerLevel serverlevel) {
                this.shear(serverlevel, SoundSource.PLAYERS, itemstack);
                this.gameEvent(GameEvent.SHEAR, p_29920_);
                itemstack.hurtAndBreak(1, p_29920_, getSlotForHand(p_29921_));
            }

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void shear(ServerLevel p_365069_, SoundSource p_29907_, ItemStack p_369140_) {
        p_365069_.playSound(null, this, SoundEvents.SNOW_GOLEM_SHEAR, p_29907_, 1.0F, 1.0F);
        this.setPumpkin(false);
        this.dropFromShearingLootTable(p_365069_, BuiltInLootTables.SHEAR_SNOW_GOLEM, p_369140_, (p_359192_, p_359193_) -> this.spawnAtLocation(p_359192_, p_359193_, this.getEyeHeight()));
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && this.hasPumpkin();
    }

    public boolean hasPumpkin() {
        return (this.entityData.get(DATA_PUMPKIN_ID) & 16) != 0;
    }

    public void setPumpkin(boolean p_29937_) {
        byte b0 = this.entityData.get(DATA_PUMPKIN_ID);
        if (p_29937_) {
            this.entityData.set(DATA_PUMPKIN_ID, (byte)(b0 | 16));
        } else {
            this.entityData.set(DATA_PUMPKIN_ID, (byte)(b0 & -17));
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNOW_GOLEM_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_29929_) {
        return SoundEvents.SNOW_GOLEM_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SNOW_GOLEM_DEATH;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.75F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }
}