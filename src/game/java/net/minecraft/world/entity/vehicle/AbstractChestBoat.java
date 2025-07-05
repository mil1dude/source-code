package net.minecraft.world.entity.vehicle;

import java.util.function.Supplier;
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
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTable;

public abstract class AbstractChestBoat extends AbstractBoat implements HasCustomInventoryScreen, ContainerEntity {
    private static final int CONTAINER_SIZE = 27;
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    public AbstractChestBoat(EntityType<? extends AbstractChestBoat> p_360747_, Level p_363408_, Supplier<Item> p_366159_) {
        super(p_360747_, p_363408_, p_366159_);
    }

    @Override
    protected float getSinglePassengerXOffset() {
        return 0.15F;
    }

    @Override
    protected int getMaxPassengers() {
        return 1;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_365153_) {
        super.addAdditionalSaveData(p_365153_);
        this.addChestVehicleSaveData(p_365153_, this.registryAccess());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_366418_) {
        super.readAdditionalSaveData(p_366418_);
        this.readChestVehicleSaveData(p_366418_, this.registryAccess());
    }

    @Override
    public void destroy(ServerLevel p_367890_, DamageSource p_366820_) {
        this.destroy(p_367890_, this.getDropItem());
        this.chestVehicleDestroyed(p_366820_, p_367890_, this);
    }

    @Override
    public void remove(Entity.RemovalReason p_365462_) {
        if (!this.level().isClientSide && p_365462_.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this);
        }

        super.remove(p_365462_);
    }

    @Override
    public InteractionResult interact(Player p_363142_, InteractionHand p_369395_) {
        if (!p_363142_.isSecondaryUseActive()) {
            InteractionResult interactionresult = super.interact(p_363142_, p_369395_);
            if (interactionresult != InteractionResult.PASS) {
                return interactionresult;
            }
        }

        if (this.canAddPassenger(p_363142_) && !p_363142_.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        } else {
            InteractionResult interactionresult1 = this.interactWithContainerVehicle(p_363142_);
            if (interactionresult1.consumesAction() && p_363142_.level() instanceof ServerLevel serverlevel) {
                this.gameEvent(GameEvent.CONTAINER_OPEN, p_363142_);
                PiglinAi.angerNearbyPiglins(serverlevel, p_363142_, true);
            }

            return interactionresult1;
        }
    }

    @Override
    public void openCustomInventoryScreen(Player p_366064_) {
        p_366064_.openMenu(this);
        if (p_366064_.level() instanceof ServerLevel serverlevel) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, p_366064_);
            PiglinAi.angerNearbyPiglins(serverlevel, p_366064_, true);
        }
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public ItemStack getItem(int p_368547_) {
        return this.getChestVehicleItem(p_368547_);
    }

    @Override
    public ItemStack removeItem(int p_370192_, int p_361799_) {
        return this.removeChestVehicleItem(p_370192_, p_361799_);
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_363538_) {
        return this.removeChestVehicleItemNoUpdate(p_363538_);
    }

    @Override
    public void setItem(int p_365825_, ItemStack p_361378_) {
        this.setChestVehicleItem(p_365825_, p_361378_);
    }

    @Override
    public SlotAccess getSlot(int p_367853_) {
        return this.getChestVehicleSlot(p_367853_);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player p_361359_) {
        return this.isChestVehicleStillValid(p_361359_);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_365606_, Inventory p_363969_, Player p_361417_) {
        if (this.lootTable != null && p_361417_.isSpectator()) {
            return null;
        } else {
            this.unpackLootTable(p_363969_.player);
            return ChestMenu.threeRows(p_365606_, p_363969_, this);
        }
    }

    public void unpackLootTable(@Nullable Player p_362478_) {
        this.unpackChestVehicleLootTable(p_362478_);
    }

    @Nullable
    @Override
    public ResourceKey<LootTable> getContainerLootTable() {
        return this.lootTable;
    }

    @Override
    public void setContainerLootTable(@Nullable ResourceKey<LootTable> p_362006_) {
        this.lootTable = p_362006_;
    }

    @Override
    public long getContainerLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setContainerLootTableSeed(long p_367535_) {
        this.lootTableSeed = p_367535_;
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    @Override
    public void clearItemStacks() {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public void stopOpen(Player p_365473_) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(p_365473_));
    }
}