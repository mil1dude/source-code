package net.minecraft.world.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HorseInventoryMenu extends AbstractContainerMenu {
    static final ResourceLocation SADDLE_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/saddle");
    private static final ResourceLocation LLAMA_ARMOR_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/llama_armor");
    private static final ResourceLocation ARMOR_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/horse_armor");
    private final Container horseContainer;
    private final Container armorContainer;
    private final AbstractHorse horse;
    private static final int SLOT_BODY_ARMOR = 1;
    private static final int SLOT_HORSE_INVENTORY_START = 2;

    public HorseInventoryMenu(int p_39656_, Inventory p_39657_, Container p_39658_, final AbstractHorse p_39659_, int p_342974_) {
        super(null, p_39656_);
        this.horseContainer = p_39658_;
        this.armorContainer = p_39659_.getBodyArmorAccess();
        this.horse = p_39659_;
        p_39658_.startOpen(p_39657_.player);
        this.addSlot(new Slot(p_39658_, 0, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack p_39677_) {
                return p_39677_.is(Items.SADDLE) && !this.hasItem() && p_39659_.isSaddleable();
            }

            @Override
            public boolean isActive() {
                return p_39659_.isSaddleable();
            }

            @Override
            public ResourceLocation getNoItemIcon() {
                return HorseInventoryMenu.SADDLE_SLOT_SPRITE;
            }
        });
        ResourceLocation resourcelocation = p_39659_ instanceof Llama ? LLAMA_ARMOR_SLOT_SPRITE : ARMOR_SLOT_SPRITE;
        this.addSlot(new ArmorSlot(this.armorContainer, p_39659_, EquipmentSlot.BODY, 0, 8, 36, resourcelocation) {
            @Override
            public boolean mayPlace(ItemStack p_39690_) {
                return p_39659_.isEquippableInSlot(p_39690_, EquipmentSlot.BODY);
            }

            @Override
            public boolean isActive() {
                return p_39659_.canUseSlot(EquipmentSlot.BODY);
            }
        });
        if (p_342974_ > 0) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < p_342974_; j++) {
                    this.addSlot(new Slot(p_39658_, 1 + j + i * p_342974_, 80 + j * 18, 18 + i * 18));
                }
            }
        }

        this.addStandardInventorySlots(p_39657_, 8, 84);
    }

    @Override
    public boolean stillValid(Player p_39661_) {
        return !this.horse.hasInventoryChanged(this.horseContainer)
            && this.horseContainer.stillValid(p_39661_)
            && this.armorContainer.stillValid(p_39661_)
            && this.horse.isAlive()
            && p_39661_.canInteractWithEntity(this.horse, 4.0);
    }

    @Override
    public ItemStack quickMoveStack(Player p_39665_, int p_39666_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_39666_);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            int i = this.horseContainer.getContainerSize() + 1;
            if (p_39666_ < i) {
                if (!this.moveItemStackTo(itemstack1, i, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(itemstack1) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(itemstack1)) {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (i <= 1 || !this.moveItemStackTo(itemstack1, 2, i, false)) {
                int j = i + 27;
                int k = j + 9;
                if (p_39666_ >= j && p_39666_ < k) {
                    if (!this.moveItemStackTo(itemstack1, i, j, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (p_39666_ >= i && p_39666_ < j) {
                    if (!this.moveItemStackTo(itemstack1, j, k, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, j, j, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(Player p_39663_) {
        super.removed(p_39663_);
        this.horseContainer.stopOpen(p_39663_);
    }
}