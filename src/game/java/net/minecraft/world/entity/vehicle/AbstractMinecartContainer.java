package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractMinecartContainer extends AbstractMinecart implements ContainerEntity {
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    protected AbstractMinecartContainer(EntityType<?> p_38213_, Level p_38214_) {
        super(p_38213_, p_38214_);
    }

    @Override
    public void destroy(ServerLevel p_363845_, DamageSource p_38228_) {
        super.destroy(p_363845_, p_38228_);
        this.chestVehicleDestroyed(p_38228_, p_363845_, this);
    }

    @Override
    public ItemStack getItem(int p_38218_) {
        return this.getChestVehicleItem(p_38218_);
    }

    @Override
    public ItemStack removeItem(int p_38220_, int p_38221_) {
        return this.removeChestVehicleItem(p_38220_, p_38221_);
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_38244_) {
        return this.removeChestVehicleItemNoUpdate(p_38244_);
    }

    @Override
    public void setItem(int p_38225_, ItemStack p_38226_) {
        this.setChestVehicleItem(p_38225_, p_38226_);
    }

    @Override
    public SlotAccess getSlot(int p_150257_) {
        return this.getChestVehicleSlot(p_150257_);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player p_38230_) {
        return this.isChestVehicleStillValid(p_38230_);
    }

    @Override
    public void remove(Entity.RemovalReason p_150255_) {
        if (!this.level().isClientSide && p_150255_.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this);
        }

        super.remove(p_150255_);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_38248_) {
        super.addAdditionalSaveData(p_38248_);
        this.addChestVehicleSaveData(p_38248_, this.registryAccess());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_38235_) {
        super.readAdditionalSaveData(p_38235_);
        this.readChestVehicleSaveData(p_38235_, this.registryAccess());
    }

    @Override
    public InteractionResult interact(Player p_38232_, InteractionHand p_38233_) {
        return this.interactWithContainerVehicle(p_38232_);
    }

    @Override
    protected Vec3 applyNaturalSlowdown(Vec3 p_365311_) {
        float f = 0.98F;
        if (this.lootTable == null) {
            int i = 15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this);
            f += (float)i * 0.001F;
        }

        if (this.isInWater()) {
            f *= 0.95F;
        }

        return p_365311_.multiply((double)f, 0.0, (double)f);
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    public void setLootTable(ResourceKey<LootTable> p_331998_, long p_329252_) {
        this.lootTable = p_331998_;
        this.lootTableSeed = p_329252_;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_38251_, Inventory p_38252_, Player p_38253_) {
        if (this.lootTable != null && p_38253_.isSpectator()) {
            return null;
        } else {
            this.unpackChestVehicleLootTable(p_38252_.player);
            return this.createMenu(p_38251_, p_38252_);
        }
    }

    protected abstract AbstractContainerMenu createMenu(int p_38222_, Inventory p_38223_);

    @Nullable
    @Override
    public ResourceKey<LootTable> getContainerLootTable() {
        return this.lootTable;
    }

    @Override
    public void setContainerLootTable(@Nullable ResourceKey<LootTable> p_331410_) {
        this.lootTable = p_331410_;
    }

    @Override
    public long getContainerLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setContainerLootTableSeed(long p_219857_) {
        this.lootTableSeed = p_219857_;
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    @Override
    public void clearItemStacks() {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }
}