package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BrewingStandBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int INGREDIENT_SLOT = 3;
    private static final int FUEL_SLOT = 4;
    private static final int[] SLOTS_FOR_UP = new int[]{3};
    private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
    private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};
    public static final int FUEL_USES = 20;
    public static final int DATA_BREW_TIME = 0;
    public static final int DATA_FUEL_USES = 1;
    public static final int NUM_DATA_VALUES = 2;
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    int brewTime;
    private boolean[] lastPotionCount;
    private Item ingredient;
    int fuel;
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int p_59038_) {
            return switch (p_59038_) {
                case 0 -> BrewingStandBlockEntity.this.brewTime;
                case 1 -> BrewingStandBlockEntity.this.fuel;
                default -> 0;
            };
        }

        @Override
        public void set(int p_59040_, int p_59041_) {
            switch (p_59040_) {
                case 0:
                    BrewingStandBlockEntity.this.brewTime = p_59041_;
                    break;
                case 1:
                    BrewingStandBlockEntity.this.fuel = p_59041_;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public BrewingStandBlockEntity(BlockPos p_155283_, BlockState p_155284_) {
        super(BlockEntityType.BREWING_STAND, p_155283_, p_155284_);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.brewing");
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> p_332629_) {
        this.items = p_332629_;
    }

    public static void serverTick(Level p_155286_, BlockPos p_155287_, BlockState p_155288_, BrewingStandBlockEntity p_155289_) {
        ItemStack itemstack = p_155289_.items.get(4);
        if (p_155289_.fuel <= 0 && itemstack.is(ItemTags.BREWING_FUEL)) {
            p_155289_.fuel = 20;
            itemstack.shrink(1);
            setChanged(p_155286_, p_155287_, p_155288_);
        }

        boolean flag = isBrewable(p_155286_.potionBrewing(), p_155289_.items);
        boolean flag1 = p_155289_.brewTime > 0;
        ItemStack itemstack1 = p_155289_.items.get(3);
        if (flag1) {
            p_155289_.brewTime--;
            boolean flag2 = p_155289_.brewTime == 0;
            if (flag2 && flag) {
                doBrew(p_155286_, p_155287_, p_155289_.items);
            } else if (!flag || !itemstack1.is(p_155289_.ingredient)) {
                p_155289_.brewTime = 0;
            }

            setChanged(p_155286_, p_155287_, p_155288_);
        } else if (flag && p_155289_.fuel > 0) {
            p_155289_.fuel--;
            p_155289_.brewTime = 400;
            p_155289_.ingredient = itemstack1.getItem();
            setChanged(p_155286_, p_155287_, p_155288_);
        }

        boolean[] aboolean = p_155289_.getPotionBits();
        if (!Arrays.equals(aboolean, p_155289_.lastPotionCount)) {
            p_155289_.lastPotionCount = aboolean;
            BlockState blockstate = p_155288_;
            if (!(p_155288_.getBlock() instanceof BrewingStandBlock)) {
                return;
            }

            for (int i = 0; i < BrewingStandBlock.HAS_BOTTLE.length; i++) {
                blockstate = blockstate.setValue(BrewingStandBlock.HAS_BOTTLE[i], Boolean.valueOf(aboolean[i]));
            }

            p_155286_.setBlock(p_155287_, blockstate, 2);
        }
    }

    private boolean[] getPotionBits() {
        boolean[] aboolean = new boolean[3];

        for (int i = 0; i < 3; i++) {
            if (!this.items.get(i).isEmpty()) {
                aboolean[i] = true;
            }
        }

        return aboolean;
    }

    private static boolean isBrewable(PotionBrewing p_336227_, NonNullList<ItemStack> p_155295_) {
        ItemStack itemstack = p_155295_.get(3);
        if (itemstack.isEmpty()) {
            return false;
        } else if (!p_336227_.isIngredient(itemstack)) {
            return false;
        } else {
            for (int i = 0; i < 3; i++) {
                ItemStack itemstack1 = p_155295_.get(i);
                if (!itemstack1.isEmpty() && p_336227_.hasMix(itemstack1, itemstack)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static void doBrew(Level p_155291_, BlockPos p_155292_, NonNullList<ItemStack> p_155293_) {
        ItemStack itemstack = p_155293_.get(3);
        PotionBrewing potionbrewing = p_155291_.potionBrewing();

        for (int i = 0; i < 3; i++) {
            p_155293_.set(i, potionbrewing.mix(itemstack, p_155293_.get(i)));
        }

        itemstack.shrink(1);
        ItemStack itemstack1 = itemstack.getItem().getCraftingRemainder();
        if (!itemstack1.isEmpty()) {
            if (itemstack.isEmpty()) {
                itemstack = itemstack1;
            } else {
                Containers.dropItemStack(p_155291_, (double)p_155292_.getX(), (double)p_155292_.getY(), (double)p_155292_.getZ(), itemstack1);
            }
        }

        p_155293_.set(3, itemstack);
        p_155291_.levelEvent(1035, p_155292_, 0);
    }

    @Override
    protected void loadAdditional(CompoundTag p_335279_, HolderLookup.Provider p_330361_) {
        super.loadAdditional(p_335279_, p_330361_);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(p_335279_, this.items, p_330361_);
        this.brewTime = p_335279_.getShort("BrewTime");
        if (this.brewTime > 0) {
            this.ingredient = this.items.get(3).getItem();
        }

        this.fuel = p_335279_.getByte("Fuel");
    }

    @Override
    protected void saveAdditional(CompoundTag p_187484_, HolderLookup.Provider p_336147_) {
        super.saveAdditional(p_187484_, p_336147_);
        p_187484_.putShort("BrewTime", (short)this.brewTime);
        ContainerHelper.saveAllItems(p_187484_, this.items, p_336147_);
        p_187484_.putByte("Fuel", (byte)this.fuel);
    }

    @Override
    public boolean canPlaceItem(int p_59017_, ItemStack p_59018_) {
        if (p_59017_ == 3) {
            PotionBrewing potionbrewing = this.level != null ? this.level.potionBrewing() : PotionBrewing.EMPTY;
            return potionbrewing.isIngredient(p_59018_);
        } else {
            return p_59017_ == 4
                ? p_59018_.is(ItemTags.BREWING_FUEL)
                : (
                        p_59018_.is(Items.POTION)
                            || p_59018_.is(Items.SPLASH_POTION)
                            || p_59018_.is(Items.LINGERING_POTION)
                            || p_59018_.is(Items.GLASS_BOTTLE)
                    )
                    && this.getItem(p_59017_).isEmpty();
        }
    }

    @Override
    public int[] getSlotsForFace(Direction p_59010_) {
        if (p_59010_ == Direction.UP) {
            return SLOTS_FOR_UP;
        } else {
            return p_59010_ == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int p_58996_, ItemStack p_58997_, @Nullable Direction p_58998_) {
        return this.canPlaceItem(p_58996_, p_58997_);
    }

    @Override
    public boolean canTakeItemThroughFace(int p_59020_, ItemStack p_59021_, Direction p_59022_) {
        return p_59020_ == 3 ? p_59021_.is(Items.GLASS_BOTTLE) : true;
    }

    @Override
    protected AbstractContainerMenu createMenu(int p_58990_, Inventory p_58991_) {
        return new BrewingStandMenu(p_58990_, p_58991_, this, this.dataAccess);
    }
}