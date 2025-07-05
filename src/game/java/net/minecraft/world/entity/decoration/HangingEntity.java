package net.minecraft.world.entity.decoration;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;

public abstract class HangingEntity extends BlockAttachedEntity {
    protected static final Predicate<Entity> HANGING_ENTITY = p_31734_ -> p_31734_ instanceof HangingEntity;
    protected Direction direction = Direction.SOUTH;

    protected HangingEntity(EntityType<? extends HangingEntity> p_31703_, Level p_31704_) {
        super(p_31703_, p_31704_);
    }

    protected HangingEntity(EntityType<? extends HangingEntity> p_31706_, Level p_31707_, BlockPos p_31708_) {
        this(p_31706_, p_31707_);
        this.pos = p_31708_;
    }

    protected void setDirection(Direction p_31728_) {
        Objects.requireNonNull(p_31728_);
        Validate.isTrue(p_31728_.getAxis().isHorizontal());
        this.direction = p_31728_;
        this.setYRot((float)(this.direction.get2DDataValue() * 90));
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected final void recalculateBoundingBox() {
        if (this.direction != null) {
            AABB aabb = this.calculateBoundingBox(this.pos, this.direction);
            Vec3 vec3 = aabb.getCenter();
            this.setPosRaw(vec3.x, vec3.y, vec3.z);
            this.setBoundingBox(aabb);
        }
    }

    protected abstract AABB calculateBoundingBox(BlockPos p_342672_, Direction p_343089_);

    @Override
    public boolean survives() {
        if (!this.level().noCollision(this)) {
            return false;
        } else {
            boolean flag = BlockPos.betweenClosedStream(this.calculateSupportBox()).allMatch(p_375130_ -> {
                BlockState blockstate = this.level().getBlockState(p_375130_);
                return blockstate.isSolid() || DiodeBlock.isDiode(blockstate);
            });
            return !flag ? false : this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
        }
    }

    protected AABB calculateSupportBox() {
        return this.getBoundingBox().move(this.direction.step().mul(-0.5F)).deflate(1.0E-7);
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    public abstract void playPlacementSound();

    @Override
    public ItemEntity spawnAtLocation(ServerLevel p_366091_, ItemStack p_31722_, float p_31723_) {
        ItemEntity itementity = new ItemEntity(
            this.level(),
            this.getX() + (double)((float)this.direction.getStepX() * 0.15F),
            this.getY() + (double)p_31723_,
            this.getZ() + (double)((float)this.direction.getStepZ() * 0.15F),
            p_31722_
        );
        itementity.setDefaultPickUpDelay();
        this.level().addFreshEntity(itementity);
        return itementity;
    }

    @Override
    public float rotate(Rotation p_31727_) {
        if (this.direction.getAxis() != Direction.Axis.Y) {
            switch (p_31727_) {
                case CLOCKWISE_180:
                    this.direction = this.direction.getOpposite();
                    break;
                case COUNTERCLOCKWISE_90:
                    this.direction = this.direction.getCounterClockWise();
                    break;
                case CLOCKWISE_90:
                    this.direction = this.direction.getClockWise();
            }
        }

        float f = Mth.wrapDegrees(this.getYRot());

        return switch (p_31727_) {
            case CLOCKWISE_180 -> f + 180.0F;
            case COUNTERCLOCKWISE_90 -> f + 90.0F;
            case CLOCKWISE_90 -> f + 270.0F;
            default -> f;
        };
    }

    @Override
    public float mirror(Mirror p_31725_) {
        return this.rotate(p_31725_.getRotation(this.direction));
    }
}