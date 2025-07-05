package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class Inventory implements Container, Nameable {
    public static final int POP_TIME_DURATION = 5;
    public static final int INVENTORY_SIZE = 36;
    public static final int SELECTION_SIZE = 9;
    public static final int SLOT_OFFHAND = 40;
    public static final int NOT_FOUND_INDEX = -1;
    public final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
    public final NonNullList<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
    public final NonNullList<ItemStack> offhand = NonNullList.withSize(1, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> compartments = ImmutableList.of(this.items, this.armor, this.offhand);
    public int selected;
    public final Player player;
    private int timesChanged;

    public Inventory(Player p_35983_) {
        this.player = p_35983_;
    }

    public ItemStack getSelected() {
        return isHotbarSlot(this.selected) ? this.items.get(this.selected) : ItemStack.EMPTY;
    }

    public static int getSelectionSize() {
        return 9;
    }

    private boolean hasRemainingSpaceForItem(ItemStack p_36015_, ItemStack p_36016_) {
        return !p_36015_.isEmpty() && ItemStack.isSameItemSameComponents(p_36015_, p_36016_) && p_36015_.isStackable() && p_36015_.getCount() < this.getMaxStackSize(p_36015_);
    }

    public int getFreeSlot() {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    public void addAndPickItem(ItemStack p_378587_) {
        this.selected = this.getSuitableHotbarSlot();
        if (!this.items.get(this.selected).isEmpty()) {
            int i = this.getFreeSlot();
            if (i != -1) {
                this.items.set(i, this.items.get(this.selected));
            }
        }

        this.items.set(this.selected, p_378587_);
    }

    public void pickSlot(int p_36039_) {
        this.selected = this.getSuitableHotbarSlot();
        ItemStack itemstack = this.items.get(this.selected);
        this.items.set(this.selected, this.items.get(p_36039_));
        this.items.set(p_36039_, itemstack);
    }

    public static boolean isHotbarSlot(int p_36046_) {
        return p_36046_ >= 0 && p_36046_ < 9;
    }

    public int findSlotMatchingItem(ItemStack p_36031_) {
        for (int i = 0; i < this.items.size(); i++) {
            if (!this.items.get(i).isEmpty() && ItemStack.isSameItemSameComponents(p_36031_, this.items.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isUsableForCrafting(ItemStack p_362871_) {
        return !p_362871_.isDamaged() && !p_362871_.isEnchanted() && !p_362871_.has(DataComponents.CUSTOM_NAME);
    }

    public int findSlotMatchingCraftingIngredient(Holder<Item> p_363996_, ItemStack p_376934_) {
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemstack = this.items.get(i);
            if (!itemstack.isEmpty()
                && itemstack.is(p_363996_)
                && isUsableForCrafting(itemstack)
                && (p_376934_.isEmpty() || ItemStack.isSameItemSameComponents(p_376934_, itemstack))) {
                return i;
            }
        }

        return -1;
    }

    public int getSuitableHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            int j = (this.selected + i) % 9;
            if (this.items.get(j).isEmpty()) {
                return j;
            }
        }

        for (int k = 0; k < 9; k++) {
            int l = (this.selected + k) % 9;
            if (!this.items.get(l).isEnchanted()) {
                return l;
            }
        }

        return this.selected;
    }

    public void setSelectedHotbarSlot(int p_363017_) {
        this.selected = p_363017_;
    }

    public int clearOrCountMatchingItems(Predicate<ItemStack> p_36023_, int p_36024_, Container p_36025_) {
        int i = 0;
        boolean flag = p_36024_ == 0;
        i += ContainerHelper.clearOrCountMatchingItems(this, p_36023_, p_36024_ - i, flag);
        i += ContainerHelper.clearOrCountMatchingItems(p_36025_, p_36023_, p_36024_ - i, flag);
        ItemStack itemstack = this.player.containerMenu.getCarried();
        i += ContainerHelper.clearOrCountMatchingItems(itemstack, p_36023_, p_36024_ - i, flag);
        if (itemstack.isEmpty()) {
            this.player.containerMenu.setCarried(ItemStack.EMPTY);
        }

        return i;
    }

    private int addResource(ItemStack p_36067_) {
        int i = this.getSlotWithRemainingSpace(p_36067_);
        if (i == -1) {
            i = this.getFreeSlot();
        }

        return i == -1 ? p_36067_.getCount() : this.addResource(i, p_36067_);
    }

    private int addResource(int p_36048_, ItemStack p_36049_) {
        int i = p_36049_.getCount();
        ItemStack itemstack = this.getItem(p_36048_);
        if (itemstack.isEmpty()) {
            itemstack = p_36049_.copyWithCount(0);
            this.setItem(p_36048_, itemstack);
        }

        int j = this.getMaxStackSize(itemstack) - itemstack.getCount();
        int k = Math.min(i, j);
        if (k == 0) {
            return i;
        } else {
            i -= k;
            itemstack.grow(k);
            itemstack.setPopTime(5);
            return i;
        }
    }

    public int getSlotWithRemainingSpace(ItemStack p_36051_) {
        if (this.hasRemainingSpaceForItem(this.getItem(this.selected), p_36051_)) {
            return this.selected;
        } else if (this.hasRemainingSpaceForItem(this.getItem(40), p_36051_)) {
            return 40;
        } else {
            for (int i = 0; i < this.items.size(); i++) {
                if (this.hasRemainingSpaceForItem(this.items.get(i), p_36051_)) {
                    return i;
                }
            }

            return -1;
        }
    }

    public void tick() {
        for (NonNullList<ItemStack> nonnulllist : this.compartments) {
            for (int i = 0; i < nonnulllist.size(); i++) {
                if (!nonnulllist.get(i).isEmpty()) {
                    nonnulllist.get(i).inventoryTick(this.player.level(), this.player, i, this.selected == i);
                }
            }
        }
    }

    public boolean add(ItemStack p_36055_) {
        return this.add(-1, p_36055_);
    }

    public boolean add(int p_36041_, ItemStack p_36042_) {
        if (p_36042_.isEmpty()) {
            return false;
        } else {
            try {
                if (p_36042_.isDamaged()) {
                    if (p_36041_ == -1) {
                        p_36041_ = this.getFreeSlot();
                    }

                    if (p_36041_ >= 0) {
                        this.items.set(p_36041_, p_36042_.copyAndClear());
                        this.items.get(p_36041_).setPopTime(5);
                        return true;
                    } else if (this.player.hasInfiniteMaterials()) {
                        p_36042_.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;
                    do {
                        i = p_36042_.getCount();
                        if (p_36041_ == -1) {
                            p_36042_.setCount(this.addResource(p_36042_));
                        } else {
                            p_36042_.setCount(this.addResource(p_36041_, p_36042_));
                        }
                    } while (!p_36042_.isEmpty() && p_36042_.getCount() < i);

                    if (p_36042_.getCount() == i && this.player.hasInfiniteMaterials()) {
                        p_36042_.setCount(0);
                        return true;
                    } else {
                        return p_36042_.getCount() < i;
                    }
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being added");
                crashreportcategory.setDetail("Item ID", Item.getId(p_36042_.getItem()));
                crashreportcategory.setDetail("Item data", p_36042_.getDamageValue());
                crashreportcategory.setDetail("Item name", () -> p_36042_.getHoverName().getString());
                throw new ReportedException(crashreport);
            }
        }
    }

    public void placeItemBackInInventory(ItemStack p_150080_) {
        this.placeItemBackInInventory(p_150080_, true);
    }

    public void placeItemBackInInventory(ItemStack p_150077_, boolean p_150078_) {
        while (!p_150077_.isEmpty()) {
            int i = this.getSlotWithRemainingSpace(p_150077_);
            if (i == -1) {
                i = this.getFreeSlot();
            }

            if (i == -1) {
                this.player.drop(p_150077_, false);
                break;
            }

            int j = p_150077_.getMaxStackSize() - this.getItem(i).getCount();
            if (this.add(i, p_150077_.split(j)) && p_150078_ && this.player instanceof ServerPlayer serverplayer) {
                serverplayer.connection.send(this.createInventoryUpdatePacket(i));
            }
        }
    }

    public ClientboundSetPlayerInventoryPacket createInventoryUpdatePacket(int p_362278_) {
        return new ClientboundSetPlayerInventoryPacket(p_362278_, this.getItem(p_362278_).copy());
    }

    @Override
    public ItemStack removeItem(int p_35993_, int p_35994_) {
        List<ItemStack> list = null;

        for (NonNullList<ItemStack> nonnulllist : this.compartments) {
            if (p_35993_ < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }

            p_35993_ -= nonnulllist.size();
        }

        return list != null && !list.get(p_35993_).isEmpty() ? ContainerHelper.removeItem(list, p_35993_, p_35994_) : ItemStack.EMPTY;
    }

    public void removeItem(ItemStack p_36058_) {
        for (NonNullList<ItemStack> nonnulllist : this.compartments) {
            for (int i = 0; i < nonnulllist.size(); i++) {
                if (nonnulllist.get(i) == p_36058_) {
                    nonnulllist.set(i, ItemStack.EMPTY);
                    break;
                }
            }
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_36029_) {
        NonNullList<ItemStack> nonnulllist = null;

        for (NonNullList<ItemStack> nonnulllist1 : this.compartments) {
            if (p_36029_ < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }

            p_36029_ -= nonnulllist1.size();
        }

        if (nonnulllist != null && !nonnulllist.get(p_36029_).isEmpty()) {
            ItemStack itemstack = nonnulllist.get(p_36029_);
            nonnulllist.set(p_36029_, ItemStack.EMPTY);
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItem(int p_35999_, ItemStack p_36000_) {
        NonNullList<ItemStack> nonnulllist = null;

        for (NonNullList<ItemStack> nonnulllist1 : this.compartments) {
            if (p_35999_ < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }

            p_35999_ -= nonnulllist1.size();
        }

        if (nonnulllist != null) {
            nonnulllist.set(p_35999_, p_36000_);
        }
    }

    public float getDestroySpeed(BlockState p_36021_) {
        return this.items.get(this.selected).getDestroySpeed(p_36021_);
    }

    public ListTag save(ListTag p_36027_) {
        for (int i = 0; i < this.items.size(); i++) {
            if (!this.items.get(i).isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte)i);
                p_36027_.add(this.items.get(i).save(this.player.registryAccess(), compoundtag));
            }
        }

        for (int j = 0; j < this.armor.size(); j++) {
            if (!this.armor.get(j).isEmpty()) {
                CompoundTag compoundtag1 = new CompoundTag();
                compoundtag1.putByte("Slot", (byte)(j + 100));
                p_36027_.add(this.armor.get(j).save(this.player.registryAccess(), compoundtag1));
            }
        }

        for (int k = 0; k < this.offhand.size(); k++) {
            if (!this.offhand.get(k).isEmpty()) {
                CompoundTag compoundtag2 = new CompoundTag();
                compoundtag2.putByte("Slot", (byte)(k + 150));
                p_36027_.add(this.offhand.get(k).save(this.player.registryAccess(), compoundtag2));
            }
        }

        return p_36027_;
    }

    public void load(ListTag p_36036_) {
        this.items.clear();
        this.armor.clear();
        this.offhand.clear();

        for (int i = 0; i < p_36036_.size(); i++) {
            CompoundTag compoundtag = p_36036_.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.parse(this.player.registryAccess(), compoundtag).orElse(ItemStack.EMPTY);
            if (j >= 0 && j < this.items.size()) {
                this.items.set(j, itemstack);
            } else if (j >= 100 && j < this.armor.size() + 100) {
                this.armor.set(j - 100, itemstack);
            } else if (j >= 150 && j < this.offhand.size() + 150) {
                this.offhand.set(j - 150, itemstack);
            }
        }
    }

    @Override
    public int getContainerSize() {
        return this.items.size() + this.armor.size() + this.offhand.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        for (ItemStack itemstack1 : this.armor) {
            if (!itemstack1.isEmpty()) {
                return false;
            }
        }

        for (ItemStack itemstack2 : this.offhand) {
            if (!itemstack2.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int p_35991_) {
        List<ItemStack> list = null;

        for (NonNullList<ItemStack> nonnulllist : this.compartments) {
            if (p_35991_ < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }

            p_35991_ -= nonnulllist.size();
        }

        return list == null ? ItemStack.EMPTY : list.get(p_35991_);
    }

    @Override
    public Component getName() {
        return Component.translatable("container.inventory");
    }

    public ItemStack getArmor(int p_36053_) {
        return this.armor.get(p_36053_);
    }

    public void dropAll() {
        for (List<ItemStack> list : this.compartments) {
            for (int i = 0; i < list.size(); i++) {
                ItemStack itemstack = list.get(i);
                if (!itemstack.isEmpty()) {
                    this.player.drop(itemstack, true, false);
                    list.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void setChanged() {
        this.timesChanged++;
    }

    public int getTimesChanged() {
        return this.timesChanged;
    }

    @Override
    public boolean stillValid(Player p_36009_) {
        return p_36009_.canInteractWithEntity(this.player, 4.0);
    }

    public boolean contains(ItemStack p_36064_) {
        for (List<ItemStack> list : this.compartments) {
            for (ItemStack itemstack : list) {
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, p_36064_)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean contains(TagKey<Item> p_204076_) {
        for (List<ItemStack> list : this.compartments) {
            for (ItemStack itemstack : list) {
                if (!itemstack.isEmpty() && itemstack.is(p_204076_)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean contains(Predicate<ItemStack> p_332183_) {
        for (List<ItemStack> list : this.compartments) {
            for (ItemStack itemstack : list) {
                if (p_332183_.test(itemstack)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void replaceWith(Inventory p_36007_) {
        for (int i = 0; i < this.getContainerSize(); i++) {
            this.setItem(i, p_36007_.getItem(i));
        }

        this.selected = p_36007_.selected;
    }

    @Override
    public void clearContent() {
        for (List<ItemStack> list : this.compartments) {
            list.clear();
        }
    }

    public void fillStackedContents(StackedItemContents p_364670_) {
        for (ItemStack itemstack : this.items) {
            p_364670_.accountSimpleStack(itemstack);
        }
    }

    public ItemStack removeFromSelected(boolean p_182404_) {
        ItemStack itemstack = this.getSelected();
        return itemstack.isEmpty() ? ItemStack.EMPTY : this.removeItem(this.selected, p_182404_ ? itemstack.getCount() : 1);
    }
}