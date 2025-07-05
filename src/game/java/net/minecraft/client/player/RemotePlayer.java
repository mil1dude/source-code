package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RemotePlayer extends AbstractClientPlayer {
    private Vec3 lerpDeltaMovement = Vec3.ZERO;
    private int lerpDeltaMovementSteps;

    public RemotePlayer(ClientLevel p_252213_, GameProfile p_250471_) {
        super(p_252213_, p_250471_);
        this.noPhysics = true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_108770_) {
        double d0 = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(d0)) {
            d0 = 1.0;
        }

        d0 *= 64.0 * getViewScale();
        return p_108770_ < d0 * d0;
    }

    @Override
    public boolean hurtClient(DamageSource p_108772_) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.calculateEntityAnimation(false);
    }

    @Override
    public void aiStep() {
        if (this.lerpSteps > 0) {
            this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
            this.lerpSteps--;
        }

        if (this.lerpHeadSteps > 0) {
            this.lerpHeadRotationStep(this.lerpHeadSteps, this.lerpYHeadRot);
            this.lerpHeadSteps--;
        }

        if (this.lerpDeltaMovementSteps > 0) {
            this.addDeltaMovement(
                new Vec3(
                    (this.lerpDeltaMovement.x - this.getDeltaMovement().x) / (double)this.lerpDeltaMovementSteps,
                    (this.lerpDeltaMovement.y - this.getDeltaMovement().y) / (double)this.lerpDeltaMovementSteps,
                    (this.lerpDeltaMovement.z - this.getDeltaMovement().z) / (double)this.lerpDeltaMovementSteps
                )
            );
            this.lerpDeltaMovementSteps--;
        }

        this.oBob = this.bob;
        this.updateSwingTime();
        float f;
        if (this.onGround() && !this.isDeadOrDying()) {
            f = (float)Math.min(0.1, this.getDeltaMovement().horizontalDistance());
        } else {
            f = 0.0F;
        }

        this.bob = this.bob + (f - this.bob) * 0.4F;

        try (Zone zone = Profiler.get().zone("push")) {
            this.pushEntities();
        }
    }

    @Override
    public void lerpMotion(double p_273090_, double p_272647_, double p_273555_) {
        this.lerpDeltaMovement = new Vec3(p_273090_, p_272647_, p_273555_);
        this.lerpDeltaMovementSteps = this.getType().updateInterval() + 1;
    }

    @Override
    protected void updatePlayerPose() {
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket p_301606_) {
        super.recreateFromPacket(p_301606_);
        this.setOldPosAndRot();
    }
}