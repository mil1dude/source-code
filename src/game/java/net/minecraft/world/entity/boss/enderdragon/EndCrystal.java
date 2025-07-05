package net.minecraft.world.entity.boss.enderdragon;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.dimension.end.EndDragonFight;

public class EndCrystal extends Entity {
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.BOOLEAN);
    public int time;

    public EndCrystal(EntityType<? extends EndCrystal> p_31037_, Level p_31038_) {
        super(p_31037_, p_31038_);
        this.blocksBuilding = true;
        this.time = this.random.nextInt(100000);
    }

    public EndCrystal(Level p_31040_, double p_31041_, double p_31042_, double p_31043_) {
        this(EntityType.END_CRYSTAL, p_31040_);
        this.setPos(p_31041_, p_31042_, p_31043_);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_331044_) {
        p_331044_.define(DATA_BEAM_TARGET, Optional.empty());
        p_331044_.define(DATA_SHOW_BOTTOM, true);
    }

    @Override
    public void tick() {
        this.time++;
        this.applyEffectsFromBlocks();
        this.handlePortal();
        if (this.level() instanceof ServerLevel) {
            BlockPos blockpos = this.blockPosition();
            if (((ServerLevel)this.level()).getDragonFight() != null && this.level().getBlockState(blockpos).isAir()) {
                this.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level(), blockpos));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_31062_) {
        if (this.getBeamTarget() != null) {
            p_31062_.put("beam_target", NbtUtils.writeBlockPos(this.getBeamTarget()));
        }

        p_31062_.putBoolean("ShowBottom", this.showsBottom());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_31055_) {
        NbtUtils.readBlockPos(p_31055_, "beam_target").ifPresent(this::setBeamTarget);
        if (p_31055_.contains("ShowBottom", 1)) {
            this.setShowBottom(p_31055_.getBoolean("ShowBottom"));
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public final boolean hurtClient(DamageSource p_368846_) {
        return this.isInvulnerableToBase(p_368846_) ? false : !(p_368846_.getEntity() instanceof EnderDragon);
    }

    @Override
    public final boolean hurtServer(ServerLevel p_363851_, DamageSource p_362125_, float p_364288_) {
        if (this.isInvulnerableToBase(p_362125_)) {
            return false;
        } else if (p_362125_.getEntity() instanceof EnderDragon) {
            return false;
        } else {
            if (!this.isRemoved()) {
                this.remove(Entity.RemovalReason.KILLED);
                if (!p_362125_.is(DamageTypeTags.IS_EXPLOSION)) {
                    DamageSource damagesource = p_362125_.getEntity() != null ? this.damageSources().explosion(this, p_362125_.getEntity()) : null;
                    p_363851_.explode(
                        this, damagesource, null, this.getX(), this.getY(), this.getZ(), 6.0F, false, Level.ExplosionInteraction.BLOCK
                    );
                }

                this.onDestroyedBy(p_363851_, p_362125_);
            }

            return true;
        }
    }

    @Override
    public void kill(ServerLevel p_366543_) {
        this.onDestroyedBy(p_366543_, this.damageSources().generic());
        super.kill(p_366543_);
    }

    private void onDestroyedBy(ServerLevel p_366714_, DamageSource p_31048_) {
        EndDragonFight enddragonfight = p_366714_.getDragonFight();
        if (enddragonfight != null) {
            enddragonfight.onCrystalDestroyed(this, p_31048_);
        }
    }

    public void setBeamTarget(@Nullable BlockPos p_31053_) {
        this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(p_31053_));
    }

    @Nullable
    public BlockPos getBeamTarget() {
        return this.getEntityData().get(DATA_BEAM_TARGET).orElse(null);
    }

    public void setShowBottom(boolean p_31057_) {
        this.getEntityData().set(DATA_SHOW_BOTTOM, p_31057_);
    }

    public boolean showsBottom() {
        return this.getEntityData().get(DATA_SHOW_BOTTOM);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_31046_) {
        return super.shouldRenderAtSqrDistance(p_31046_) || this.getBeamTarget() != null;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.END_CRYSTAL);
    }
}