package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class DispenserBlockEntity extends RandomizableContainerBlockEntity {
    public static final int CONTAINER_SIZE = 9;
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    protected DispenserBlockEntity(BlockEntityType<?> p_155489_, BlockPos p_155490_, BlockState p_155491_) {
        super(p_155489_, p_155490_, p_155491_);
    }

    public DispenserBlockEntity(BlockPos p_155493_, BlockState p_155494_) {
        this(BlockEntityType.DISPENSER, p_155493_, p_155494_);
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    public int getRandomSlot(RandomSource p_222762_) {
        this.unpackLootTable(null);
        int i = -1;
        int j = 1;

        for (int k = 0; k < this.items.size(); k++) {
            if (!this.items.get(k).isEmpty() && p_222762_.nextInt(j++) == 0) {
                i = k;
            }
        }

        return i;
    }

    public ItemStack insertItem(ItemStack p_345441_) {
        int i = this.getMaxStackSize(p_345441_);

        for (int j = 0; j < this.items.size(); j++) {
            ItemStack itemstack = this.items.get(j);
            if (itemstack.isEmpty() || ItemStack.isSameItemSameComponents(p_345441_, itemstack)) {
                int k = Math.min(p_345441_.getCount(), i - itemstack.getCount());
                if (k > 0) {
                    if (itemstack.isEmpty()) {
                        this.setItem(j, p_345441_.split(k));
                    } else {
                        p_345441_.shrink(k);
                        itemstack.grow(k);
                    }
                }

                if (p_345441_.isEmpty()) {
                    break;
                }
            }
        }

        return p_345441_;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.dispenser");
    }

    @Override
    protected void loadAdditional(CompoundTag p_332674_, HolderLookup.Provider p_335532_) {
        super.loadAdditional(p_332674_, p_335532_);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(p_332674_)) {
            ContainerHelper.loadAllItems(p_332674_, this.items, p_335532_);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag p_187498_, HolderLookup.Provider p_328492_) {
        super.saveAdditional(p_187498_, p_328492_);
        if (!this.trySaveLootTable(p_187498_)) {
            ContainerHelper.saveAllItems(p_187498_, this.items, p_328492_);
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> p_59243_) {
        this.items = p_59243_;
    }

    @Override
    protected AbstractContainerMenu createMenu(int p_59235_, Inventory p_59236_) {
        return new DispenserMenu(p_59235_, p_59236_, this);
    }
}