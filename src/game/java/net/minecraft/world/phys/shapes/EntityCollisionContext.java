package net.minecraft.world.phys.shapes;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class EntityCollisionContext implements CollisionContext {
    protected static final CollisionContext EMPTY = new EntityCollisionContext(false, -Double.MAX_VALUE, ItemStack.EMPTY, p_205118_ -> false, null) {
        @Override
        public boolean isAbove(VoxelShape p_82898_, BlockPos p_82899_, boolean p_82900_) {
            return p_82900_;
        }
    };
    private final boolean descending;
    private final double entityBottom;
    private final ItemStack heldItem;
    private final Predicate<FluidState> canStandOnFluid;
    @Nullable
    private final Entity entity;

    protected EntityCollisionContext(boolean p_198916_, double p_198917_, ItemStack p_198918_, Predicate<FluidState> p_198919_, @Nullable Entity p_198920_) {
        this.descending = p_198916_;
        this.entityBottom = p_198917_;
        this.heldItem = p_198918_;
        this.canStandOnFluid = p_198919_;
        this.entity = p_198920_;
    }

    @Deprecated
    protected EntityCollisionContext(Entity p_82872_, boolean p_365888_) {
        this(
            p_82872_.isDescending(),
            p_82872_.getY(),
            p_82872_ instanceof LivingEntity ? ((LivingEntity)p_82872_).getMainHandItem() : ItemStack.EMPTY,
            p_365888_ ? p_360701_ -> true : (p_82872_ instanceof LivingEntity ? ((LivingEntity)p_82872_)::canStandOnFluid : p_205113_ -> false),
            p_82872_
        );
    }

    @Override
    public boolean isHoldingItem(Item p_82879_) {
        return this.heldItem.is(p_82879_);
    }

    @Override
    public boolean canStandOnFluid(FluidState p_205115_, FluidState p_205116_) {
        return this.canStandOnFluid.test(p_205116_) && !p_205115_.getType().isSame(p_205116_.getType());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState p_367344_, CollisionGetter p_362064_, BlockPos p_364238_) {
        return p_367344_.getCollisionShape(p_362064_, p_364238_, this);
    }

    @Override
    public boolean isDescending() {
        return this.descending;
    }

    @Override
    public boolean isAbove(VoxelShape p_82886_, BlockPos p_82887_, boolean p_82888_) {
        return this.entityBottom > (double)p_82887_.getY() + p_82886_.max(Direction.Axis.Y) - 1.0E-5F;
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }
}