package net.minecraft.world.entity.vehicle;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartFurnace extends AbstractMinecart {
    private static final EntityDataAccessor<Boolean> DATA_ID_FUEL = SynchedEntityData.defineId(MinecartFurnace.class, EntityDataSerializers.BOOLEAN);
    private static final int FUEL_TICKS_PER_ITEM = 3600;
    private static final int MAX_FUEL_TICKS = 32000;
    private int fuel;
    public Vec3 push = Vec3.ZERO;

    public MinecartFurnace(EntityType<? extends MinecartFurnace> p_38552_, Level p_38553_) {
        super(p_38552_, p_38553_);
    }

    @Override
    public boolean isFurnace() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_336383_) {
        super.defineSynchedData(p_336383_);
        p_336383_.define(DATA_ID_FUEL, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.fuel > 0) {
                this.fuel--;
            }

            if (this.fuel <= 0) {
                this.push = Vec3.ZERO;
            }

            this.setHasFuel(this.fuel > 0);
        }

        if (this.hasFuel() && this.random.nextInt(4) == 0) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected double getMaxSpeed(ServerLevel p_361846_) {
        return this.isInWater() ? super.getMaxSpeed(p_361846_) * 0.75 : super.getMaxSpeed(p_361846_) * 0.5;
    }

    @Override
    protected Item getDropItem() {
        return Items.FURNACE_MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.FURNACE_MINECART);
    }

    @Override
    protected Vec3 applyNaturalSlowdown(Vec3 p_361522_) {
        Vec3 vec3;
        if (this.push.lengthSqr() > 1.0E-7) {
            this.push = this.calculateNewPushAlong(p_361522_);
            vec3 = p_361522_.multiply(0.8, 0.0, 0.8).add(this.push);
            if (this.isInWater()) {
                vec3 = vec3.scale(0.1);
            }
        } else {
            vec3 = p_361522_.multiply(0.98, 0.0, 0.98);
        }

        return super.applyNaturalSlowdown(vec3);
    }

    private Vec3 calculateNewPushAlong(Vec3 p_362599_) {
        double d0 = 1.0E-4;
        double d1 = 0.001;
        return this.push.horizontalDistanceSqr() > 1.0E-4 && p_362599_.horizontalDistanceSqr() > 0.001
            ? this.push.projectedOn(p_362599_).normalize().scale(this.push.length())
            : this.push;
    }

    @Override
    public InteractionResult interact(Player p_38562_, InteractionHand p_38563_) {
        ItemStack itemstack = p_38562_.getItemInHand(p_38563_);
        if (itemstack.is(ItemTags.FURNACE_MINECART_FUEL) && this.fuel + 3600 <= 32000) {
            itemstack.consume(1, p_38562_);
            this.fuel += 3600;
        }

        if (this.fuel > 0) {
            this.push = this.position().subtract(p_38562_.position()).horizontal();
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_38567_) {
        super.addAdditionalSaveData(p_38567_);
        p_38567_.putDouble("PushX", this.push.x);
        p_38567_.putDouble("PushZ", this.push.z);
        p_38567_.putShort("Fuel", (short)this.fuel);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_38565_) {
        super.readAdditionalSaveData(p_38565_);
        double d0 = p_38565_.getDouble("PushX");
        double d1 = p_38565_.getDouble("PushZ");
        this.push = new Vec3(d0, 0.0, d1);
        this.fuel = p_38565_.getShort("Fuel");
    }

    protected boolean hasFuel() {
        return this.entityData.get(DATA_ID_FUEL);
    }

    protected void setHasFuel(boolean p_38577_) {
        this.entityData.set(DATA_ID_FUEL, p_38577_);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH).setValue(FurnaceBlock.LIT, Boolean.valueOf(this.hasFuel()));
    }
}