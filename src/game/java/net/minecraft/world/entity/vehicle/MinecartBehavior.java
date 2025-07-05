package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public abstract class MinecartBehavior {
    protected final AbstractMinecart minecart;

    protected MinecartBehavior(AbstractMinecart p_362322_) {
        this.minecart = p_362322_;
    }

    public void cancelLerp() {
    }

    public void lerpTo(double p_366069_, double p_369509_, double p_363209_, float p_363993_, float p_361648_, int p_360709_) {
        this.setPos(p_366069_, p_369509_, p_363209_);
        this.setYRot(p_363993_ % 360.0F);
        this.setXRot(p_361648_ % 360.0F);
    }

    public double lerpTargetX() {
        return this.getX();
    }

    public double lerpTargetY() {
        return this.getY();
    }

    public double lerpTargetZ() {
        return this.getZ();
    }

    public float lerpTargetXRot() {
        return this.getXRot();
    }

    public float lerpTargetYRot() {
        return this.getYRot();
    }

    public void lerpMotion(double p_363790_, double p_369322_, double p_361777_) {
        this.setDeltaMovement(p_363790_, p_369322_, p_361777_);
    }

    public abstract void tick();

    public Level level() {
        return this.minecart.level();
    }

    public abstract void moveAlongTrack(ServerLevel p_361282_);

    public abstract double stepAlongTrack(BlockPos p_367776_, RailShape p_365444_, double p_369487_);

    public abstract boolean pushAndPickupEntities();

    public Vec3 getDeltaMovement() {
        return this.minecart.getDeltaMovement();
    }

    public void setDeltaMovement(Vec3 p_368968_) {
        this.minecart.setDeltaMovement(p_368968_);
    }

    public void setDeltaMovement(double p_369756_, double p_368764_, double p_362872_) {
        this.minecart.setDeltaMovement(p_369756_, p_368764_, p_362872_);
    }

    public Vec3 position() {
        return this.minecart.position();
    }

    public double getX() {
        return this.minecart.getX();
    }

    public double getY() {
        return this.minecart.getY();
    }

    public double getZ() {
        return this.minecart.getZ();
    }

    public void setPos(Vec3 p_367928_) {
        this.minecart.setPos(p_367928_);
    }

    public void setPos(double p_366087_, double p_367426_, double p_364435_) {
        this.minecart.setPos(p_366087_, p_367426_, p_364435_);
    }

    public float getXRot() {
        return this.minecart.getXRot();
    }

    public void setXRot(float p_362033_) {
        this.minecart.setXRot(p_362033_);
    }

    public float getYRot() {
        return this.minecart.getYRot();
    }

    public void setYRot(float p_365528_) {
        this.minecart.setYRot(p_365528_);
    }

    public Direction getMotionDirection() {
        return this.minecart.getDirection();
    }

    public Vec3 getKnownMovement(Vec3 p_368351_) {
        return p_368351_;
    }

    public abstract double getMaxSpeed(ServerLevel p_361034_);

    public abstract double getSlowdownFactor();
}