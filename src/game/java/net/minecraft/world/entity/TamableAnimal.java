package net.minecraft.world.entity;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.scores.PlayerTeam;

public abstract class TamableAnimal extends Animal implements OwnableEntity {
    public static final int TELEPORT_WHEN_DISTANCE_IS_SQ = 144;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 1;
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.OPTIONAL_UUID);
    private boolean orderedToSit;

    protected TamableAnimal(EntityType<? extends TamableAnimal> p_21803_, Level p_21804_) {
        super(p_21803_, p_21804_);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_329630_) {
        super.defineSynchedData(p_329630_);
        p_329630_.define(DATA_FLAGS_ID, (byte)0);
        p_329630_.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_21819_) {
        super.addAdditionalSaveData(p_21819_);
        if (this.getOwnerUUID() != null) {
            p_21819_.putUUID("Owner", this.getOwnerUUID());
        }

        p_21819_.putBoolean("Sitting", this.orderedToSit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_21815_) {
        super.readAdditionalSaveData(p_21815_);
        UUID uuid;
        if (p_21815_.hasUUID("Owner")) {
            uuid = p_21815_.getUUID("Owner");
        } else {
            String s = p_21815_.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
                this.setTame(true, false);
            } catch (Throwable throwable) {
                this.setTame(false, true);
            }
        } else {
            this.setOwnerUUID(null);
            this.setTame(false, true);
        }

        this.orderedToSit = p_21815_.getBoolean("Sitting");
        this.setInSittingPose(this.orderedToSit);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    public boolean handleLeashAtDistance(Entity p_344718_, float p_344621_) {
        if (this.isInSittingPose()) {
            if (p_344621_ > 10.0F) {
                this.dropLeash();
            }

            return false;
        } else {
            return super.handleLeashAtDistance(p_344718_, p_344621_);
        }
    }

    protected void spawnTamingParticles(boolean p_21835_) {
        ParticleOptions particleoptions = ParticleTypes.HEART;
        if (!p_21835_) {
            particleoptions = ParticleTypes.SMOKE;
        }

        for (int i = 0; i < 7; i++) {
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particleoptions, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d0, d1, d2);
        }
    }

    @Override
    public void handleEntityEvent(byte p_21807_) {
        if (p_21807_ == 7) {
            this.spawnTamingParticles(true);
        } else if (p_21807_ == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(p_21807_);
        }
    }

    public boolean isTame() {
        return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
    }

    public void setTame(boolean p_21836_, boolean p_332364_) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (p_21836_) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 4));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -5));
        }

        if (p_332364_) {
            this.applyTamingSideEffects();
        }
    }

    protected void applyTamingSideEffects() {
    }

    public boolean isInSittingPose() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setInSittingPose(boolean p_21838_) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (p_21838_) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -2));
        }
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID p_21817_) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(p_21817_));
    }

    public void tame(Player p_21829_) {
        this.setTame(true, true);
        this.setOwnerUUID(p_21829_.getUUID());
        if (p_21829_ instanceof ServerPlayer serverplayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger(serverplayer, this);
        }
    }

    @Override
    public boolean canAttack(LivingEntity p_21822_) {
        return this.isOwnedBy(p_21822_) ? false : super.canAttack(p_21822_);
    }

    public boolean isOwnedBy(LivingEntity p_21831_) {
        return p_21831_ == this.getOwner();
    }

    public boolean wantsToAttack(LivingEntity p_21810_, LivingEntity p_21811_) {
        return true;
    }

    @Override
    public PlayerTeam getTeam() {
        if (this.isTame()) {
            LivingEntity livingentity = this.getOwner();
            if (livingentity != null) {
                return livingentity.getTeam();
            }
        }

        return super.getTeam();
    }

    @Override
    protected boolean considersEntityAsAlly(Entity p_361057_) {
        if (this.isTame()) {
            LivingEntity livingentity = this.getOwner();
            if (p_361057_ == livingentity) {
                return true;
            }

            if (livingentity != null) {
                return livingentity.considersEntityAsAlly(p_361057_);
            }
        }

        return super.considersEntityAsAlly(p_361057_);
    }

    @Override
    public void die(DamageSource p_21809_) {
        if (this.level() instanceof ServerLevel serverlevel
            && serverlevel.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)
            && this.getOwner() instanceof ServerPlayer serverplayer) {
            serverplayer.sendSystemMessage(this.getCombatTracker().getDeathMessage());
        }

        super.die(p_21809_);
    }

    public boolean isOrderedToSit() {
        return this.orderedToSit;
    }

    public void setOrderedToSit(boolean p_21840_) {
        this.orderedToSit = p_21840_;
    }

    public void tryToTeleportToOwner() {
        LivingEntity livingentity = this.getOwner();
        if (livingentity != null) {
            this.teleportToAroundBlockPos(livingentity.blockPosition());
        }
    }

    public boolean shouldTryTeleportToOwner() {
        LivingEntity livingentity = this.getOwner();
        return livingentity != null && this.distanceToSqr(this.getOwner()) >= 144.0;
    }

    private void teleportToAroundBlockPos(BlockPos p_342611_) {
        for (int i = 0; i < 10; i++) {
            int j = this.random.nextIntBetweenInclusive(-3, 3);
            int k = this.random.nextIntBetweenInclusive(-3, 3);
            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = this.random.nextIntBetweenInclusive(-1, 1);
                if (this.maybeTeleportTo(p_342611_.getX() + j, p_342611_.getY() + l, p_342611_.getZ() + k)) {
                    return;
                }
            }
        }
    }

    private boolean maybeTeleportTo(int p_344380_, int p_344602_, int p_344979_) {
        if (!this.canTeleportTo(new BlockPos(p_344380_, p_344602_, p_344979_))) {
            return false;
        } else {
            this.moveTo((double)p_344380_ + 0.5, (double)p_344602_, (double)p_344979_ + 0.5, this.getYRot(), this.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos p_342572_) {
        PathType pathtype = WalkNodeEvaluator.getPathTypeStatic(this, p_342572_);
        if (pathtype != PathType.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = this.level().getBlockState(p_342572_.below());
            if (!this.canFlyToOwner() && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = p_342572_.subtract(this.blockPosition());
                return this.level().noCollision(this, this.getBoundingBox().move(blockpos));
            }
        }
    }

    public final boolean unableToMoveToOwner() {
        return this.isOrderedToSit() || this.isPassenger() || this.mayBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
    }

    protected boolean canFlyToOwner() {
        return false;
    }

    public class TamableAnimalPanicGoal extends PanicGoal {
        public TamableAnimalPanicGoal(final double p_344198_, final TagKey<DamageType> p_343270_) {
            super(TamableAnimal.this, p_344198_, p_343270_);
        }

        public TamableAnimalPanicGoal(final double p_344164_) {
            super(TamableAnimal.this, p_344164_);
        }

        @Override
        public void tick() {
            if (!TamableAnimal.this.unableToMoveToOwner() && TamableAnimal.this.shouldTryTeleportToOwner()) {
                TamableAnimal.this.tryToTeleportToOwner();
            }

            super.tick();
        }
    }
}
