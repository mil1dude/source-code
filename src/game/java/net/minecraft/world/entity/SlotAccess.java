package net.minecraft.world.entity;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface SlotAccess {
    SlotAccess NULL = new SlotAccess() {
        @Override
        public ItemStack get() {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean set(ItemStack p_147314_) {
            return false;
        }
    };

    static SlotAccess of(final Supplier<ItemStack> p_328960_, final Consumer<ItemStack> p_334295_) {
        return new SlotAccess() {
            @Override
            public ItemStack get() {
                return p_328960_.get();
            }

            @Override
            public boolean set(ItemStack p_147324_) {
                p_334295_.accept(p_147324_);
                return true;
            }
        };
    }

    static SlotAccess forContainer(final Container p_147296_, final int p_147297_, final Predicate<ItemStack> p_147298_) {
        return new SlotAccess() {
            @Override
            public ItemStack get() {
                return p_147296_.getItem(p_147297_);
            }

            @Override
            public boolean set(ItemStack p_147334_) {
                if (!p_147298_.test(p_147334_)) {
                    return false;
                } else {
                    p_147296_.setItem(p_147297_, p_147334_);
                    return true;
                }
            }
        };
    }

    static SlotAccess forContainer(Container p_147293_, int p_147294_) {
        return forContainer(p_147293_, p_147294_, p_147310_ -> true);
    }

    static SlotAccess forEquipmentSlot(final LivingEntity p_147303_, final EquipmentSlot p_147304_, final Predicate<ItemStack> p_147305_) {
        return new SlotAccess() {
            @Override
            public ItemStack get() {
                return p_147303_.getItemBySlot(p_147304_);
            }

            @Override
            public boolean set(ItemStack p_336326_) {
                if (!p_147305_.test(p_336326_)) {
                    return false;
                } else {
                    p_147303_.setItemSlot(p_147304_, p_336326_);
                    return true;
                }
            }
        };
    }

    static SlotAccess forEquipmentSlot(LivingEntity p_147300_, EquipmentSlot p_147301_) {
        return forEquipmentSlot(p_147300_, p_147301_, p_147308_ -> true);
    }

    ItemStack get();

    boolean set(ItemStack p_147306_);
}