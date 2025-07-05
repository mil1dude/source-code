package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class MinecartHopper extends AbstractMinecartContainer implements Hopper {
    private boolean enabled = true;
    private boolean consumedItemThisFrame = false;

    public MinecartHopper(EntityType<? extends MinecartHopper> p_38584_, Level p_38585_) {
        super(p_38584_, p_38585_);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.HOPPER.defaultBlockState();
    }

    @Override
    public int getDefaultDisplayOffset() {
        return 1;
    }

    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public void activateMinecart(int p_38596_, int p_38597_, int p_38598_, boolean p_38599_) {
        boolean flag = !p_38599_;
        if (flag != this.isEnabled()) {
            this.setEnabled(flag);
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean p_38614_) {
        this.enabled = p_38614_;
    }

    @Override
    public double getLevelX() {
        return this.getX();
    }

    @Override
    public double getLevelY() {
        return this.getY() + 0.5;
    }

    @Override
    public double getLevelZ() {
        return this.getZ();
    }

    @Override
    public boolean isGridAligned() {
        return false;
    }

    @Override
    public void tick() {
        this.consumedItemThisFrame = false;
        super.tick();
        this.tryConsumeItems();
    }

    @Override
    protected double makeStepAlongTrack(BlockPos p_361796_, RailShape p_362892_, double p_366793_) {
        double d0 = super.makeStepAlongTrack(p_361796_, p_362892_, p_366793_);
        this.tryConsumeItems();
        return d0;
    }

    private void tryConsumeItems() {
        if (!this.level().isClientSide && this.isAlive() && this.isEnabled() && !this.consumedItemThisFrame && this.suckInItems()) {
            this.consumedItemThisFrame = true;
            this.setChanged();
        }
    }

    public boolean suckInItems() {
        if (HopperBlockEntity.suckInItems(this.level(), this)) {
            return true;
        } else {
            for (ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.25, 0.0, 0.25), EntitySelector.ENTITY_STILL_ALIVE)) {
                if (HopperBlockEntity.addItem(this, itementity)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    protected Item getDropItem() {
        return Items.HOPPER_MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.HOPPER_MINECART);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_38608_) {
        super.addAdditionalSaveData(p_38608_);
        p_38608_.putBoolean("Enabled", this.enabled);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_38606_) {
        super.readAdditionalSaveData(p_38606_);
        this.enabled = p_38606_.contains("Enabled") ? p_38606_.getBoolean("Enabled") : true;
    }

    @Override
    public AbstractContainerMenu createMenu(int p_38601_, Inventory p_38602_) {
        return new HopperMenu(p_38601_, p_38602_, this);
    }
}