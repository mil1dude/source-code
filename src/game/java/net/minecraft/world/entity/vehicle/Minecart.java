package net.minecraft.world.entity.vehicle;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Minecart extends AbstractMinecart {
    private float rotationOffset;
    private float playerRotationOffset;

    public Minecart(EntityType<?> p_38470_, Level p_38471_) {
        super(p_38470_, p_38471_);
    }

    @Override
    public InteractionResult interact(Player p_38483_, InteractionHand p_38484_) {
        if (!p_38483_.isSecondaryUseActive() && !this.isVehicle() && (this.level().isClientSide || p_38483_.startRiding(this))) {
            this.playerRotationOffset = this.rotationOffset;
            if (!this.level().isClientSide) {
                return (InteractionResult)(p_38483_.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS);
            } else {
                return InteractionResult.SUCCESS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    protected Item getDropItem() {
        return Items.MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.MINECART);
    }

    @Override
    public void activateMinecart(int p_38478_, int p_38479_, int p_38480_, boolean p_38481_) {
        if (p_38481_) {
            if (this.isVehicle()) {
                this.ejectPassengers();
            }

            if (this.getHurtTime() == 0) {
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.setDamage(50.0F);
                this.markHurt();
            }
        }
    }

    @Override
    public boolean isRideable() {
        return true;
    }

    @Override
    public void tick() {
        double d0 = (double)this.getYRot();
        Vec3 vec3 = this.position();
        super.tick();
        double d1 = ((double)this.getYRot() - d0) % 360.0;
        if (this.level().isClientSide && vec3.distanceTo(this.position()) > 0.01) {
            this.rotationOffset += (float)d1;
            this.rotationOffset %= 360.0F;
        }
    }

    @Override
    protected void positionRider(Entity p_365083_, Entity.MoveFunction p_368333_) {
        super.positionRider(p_365083_, p_368333_);
        if (this.level().isClientSide && p_365083_ instanceof Player player && player.shouldRotateWithMinecart() && useExperimentalMovement(this.level())) {
            float f = (float)Mth.rotLerp(0.5, (double)this.playerRotationOffset, (double)this.rotationOffset);
            player.setYRot(player.getYRot() - (f - this.playerRotationOffset));
            this.playerRotationOffset = f;
        }
    }
}