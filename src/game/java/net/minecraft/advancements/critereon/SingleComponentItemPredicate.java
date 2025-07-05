package net.minecraft.advancements.critereon;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public interface SingleComponentItemPredicate<T> extends ItemSubPredicate {
    @Override
    default boolean matches(ItemStack p_332866_) {
        T t = p_332866_.get(this.componentType());
        return t != null && this.matches(p_332866_, t);
    }

    DataComponentType<T> componentType();

    boolean matches(ItemStack p_329983_, T p_333057_);
}