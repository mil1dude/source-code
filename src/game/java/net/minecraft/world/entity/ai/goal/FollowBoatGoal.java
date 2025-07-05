package net.minecraft.world.entity.ai.goal;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.phys.Vec3;

public class FollowBoatGoal extends Goal {
    private int timeToRecalcPath;
    private final PathfinderMob mob;
    @Nullable
    private Player following;
    private BoatGoals currentGoal;

    public FollowBoatGoal(PathfinderMob p_25238_) {
        this.mob = p_25238_;
    }

    @Override
    public boolean canUse() {
        List<AbstractBoat> list = this.mob.level().getEntitiesOfClass(AbstractBoat.class, this.mob.getBoundingBox().inflate(5.0));
        boolean flag = false;

        for (AbstractBoat abstractboat : list) {
            Entity entity = abstractboat.getControllingPassenger();
            if (entity instanceof Player && (Mth.abs(((Player)entity).xxa) > 0.0F || Mth.abs(((Player)entity).zza) > 0.0F)) {
                flag = true;
                break;
            }
        }

        return this.following != null && (Mth.abs(this.following.xxa) > 0.0F || Mth.abs(this.following.zza) > 0.0F) || flag;
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.following != null
            && this.following.isPassenger()
            && (Mth.abs(this.following.xxa) > 0.0F || Mth.abs(this.following.zza) > 0.0F);
    }

    @Override
    public void start() {
        for (AbstractBoat abstractboat : this.mob.level().getEntitiesOfClass(AbstractBoat.class, this.mob.getBoundingBox().inflate(5.0))) {
            if (abstractboat.getControllingPassenger() instanceof Player player) {
                this.following = player;
                break;
            }
        }

        this.timeToRecalcPath = 0;
        this.currentGoal = BoatGoals.GO_TO_BOAT;
    }

    @Override
    public void stop() {
        this.following = null;
    }

    @Override
    public void tick() {
        boolean flag = Mth.abs(this.following.xxa) > 0.0F || Mth.abs(this.following.zza) > 0.0F;
        float f = this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION ? (flag ? 0.01F : 0.0F) : 0.015F;
        this.mob.moveRelative(f, new Vec3((double)this.mob.xxa, (double)this.mob.yya, (double)this.mob.zza));
        this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (this.currentGoal == BoatGoals.GO_TO_BOAT) {
                BlockPos blockpos = this.following.blockPosition().relative(this.following.getDirection().getOpposite());
                blockpos = blockpos.offset(0, -1, 0);
                this.mob.getNavigation().moveTo((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 1.0);
                if (this.mob.distanceTo(this.following) < 4.0F) {
                    this.timeToRecalcPath = 0;
                    this.currentGoal = BoatGoals.GO_IN_BOAT_DIRECTION;
                }
            } else if (this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION) {
                Direction direction = this.following.getMotionDirection();
                BlockPos blockpos1 = this.following.blockPosition().relative(direction, 10);
                this.mob.getNavigation().moveTo((double)blockpos1.getX(), (double)(blockpos1.getY() - 1), (double)blockpos1.getZ(), 1.0);
                if (this.mob.distanceTo(this.following) > 12.0F) {
                    this.timeToRecalcPath = 0;
                    this.currentGoal = BoatGoals.GO_TO_BOAT;
                }
            }
        }
    }
}