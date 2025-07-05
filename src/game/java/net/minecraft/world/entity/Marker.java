package net.minecraft.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;

public class Marker extends Entity {
    private static final String DATA_TAG = "data";
    private CompoundTag data = new CompoundTag();

    public Marker(EntityType<?> p_147250_, Level p_147251_) {
        super(p_147250_, p_147251_);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_334647_) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_147254_) {
        this.data = p_147254_.getCompound("data");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_147257_) {
        p_147257_.put("data", this.data.copy());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity p_345137_) {
        throw new IllegalStateException("Markers should never be sent");
    }

    @Override
    protected boolean canAddPassenger(Entity p_265289_) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity p_270306_) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public final boolean hurtServer(ServerLevel p_366204_, DamageSource p_366965_, float p_363917_) {
        return false;
    }
}