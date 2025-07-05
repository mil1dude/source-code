package net.minecraft.world.entity.boss;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;

public class EnderDragonPart extends Entity {
    public final EnderDragon parentMob;
    public final String name;
    private final EntityDimensions size;

    public EnderDragonPart(EnderDragon p_31014_, String p_31015_, float p_31016_, float p_31017_) {
        super(p_31014_.getType(), p_31014_.level());
        this.size = EntityDimensions.scalable(p_31016_, p_31017_);
        this.refreshDimensions();
        this.parentMob = p_31014_;
        this.name = p_31015_;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_331865_) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_31025_) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_31028_) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return this.parentMob.getPickResult();
    }

    @Override
    public final boolean hurtServer(ServerLevel p_363349_, DamageSource p_366248_, float p_364296_) {
        return this.isInvulnerableToBase(p_366248_) ? false : this.parentMob.hurt(p_363349_, this, p_366248_, p_364296_);
    }

    @Override
    public boolean is(Entity p_31031_) {
        return this == p_31031_ || this.parentMob == p_31031_;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity p_342788_) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityDimensions getDimensions(Pose p_31023_) {
        return this.size;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}