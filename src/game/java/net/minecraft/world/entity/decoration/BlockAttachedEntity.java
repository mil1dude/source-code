package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class BlockAttachedEntity extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int checkInterval;
    protected BlockPos pos;

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> p_342082_, Level p_342394_) {
        super(p_342082_, p_342394_);
    }

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> p_343768_, Level p_343896_, BlockPos p_344928_) {
        this(p_343768_, p_343896_);
        this.pos = p_344928_;
    }

    protected abstract void recalculateBoundingBox();

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel serverlevel) {
            this.checkBelowWorld();
            if (this.checkInterval++ == 100) {
                this.checkInterval = 0;
                if (!this.isRemoved() && !this.survives()) {
                    this.discard();
                    this.dropItem(serverlevel, null);
                }
            }
        }
    }

    public abstract boolean survives();

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity p_342897_) {
        if (p_342897_ instanceof Player player) {
            return !this.level().mayInteract(player, this.pos) ? true : this.hurtOrSimulate(this.damageSources().playerAttack(player), 0.0F);
        } else {
            return false;
        }
    }

    @Override
    public boolean hurtClient(DamageSource p_364173_) {
        return !this.isInvulnerableToBase(p_364173_);
    }

    @Override
    public boolean hurtServer(ServerLevel p_365107_, DamageSource p_362614_, float p_362901_) {
        if (this.isInvulnerableToBase(p_362614_)) {
            return false;
        } else if (!p_365107_.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && p_362614_.getEntity() instanceof Mob) {
            return false;
        } else {
            if (!this.isRemoved()) {
                this.kill(p_365107_);
                this.markHurt();
                this.dropItem(p_365107_, p_362614_.getEntity());
            }

            return true;
        }
    }

    @Override
    public boolean ignoreExplosion(Explosion p_363332_) {
        return p_363332_.shouldAffectBlocklikeEntities() ? super.ignoreExplosion(p_363332_) : true;
    }

    @Override
    public void move(MoverType p_344908_, Vec3 p_344746_) {
        if (this.level() instanceof ServerLevel serverlevel && !this.isRemoved() && p_344746_.lengthSqr() > 0.0) {
            this.kill(serverlevel);
            this.dropItem(serverlevel, null);
        }
    }

    @Override
    public void push(double p_342878_, double p_342443_, double p_343763_) {
        if (this.level() instanceof ServerLevel serverlevel
            && !this.isRemoved()
            && p_342878_ * p_342878_ + p_342443_ * p_342443_ + p_343763_ * p_343763_ > 0.0) {
            this.kill(serverlevel);
            this.dropItem(serverlevel, null);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_342533_) {
        BlockPos blockpos = this.getPos();
        p_342533_.putInt("TileX", blockpos.getX());
        p_342533_.putInt("TileY", blockpos.getY());
        p_342533_.putInt("TileZ", blockpos.getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_342903_) {
        BlockPos blockpos = new BlockPos(p_342903_.getInt("TileX"), p_342903_.getInt("TileY"), p_342903_.getInt("TileZ"));
        if (!blockpos.closerThan(this.blockPosition(), 16.0)) {
            LOGGER.error("Block-attached entity at invalid position: {}", blockpos);
        } else {
            this.pos = blockpos;
        }
    }

    public abstract void dropItem(ServerLevel p_361705_, @Nullable Entity p_342668_);

    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public void setPos(double p_342922_, double p_342992_, double p_343897_) {
        this.pos = BlockPos.containing(p_342922_, p_342992_, p_343897_);
        this.recalculateBoundingBox();
        this.hasImpulse = true;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    public void thunderHit(ServerLevel p_343731_, LightningBolt p_343666_) {
    }

    @Override
    public void refreshDimensions() {
    }
}