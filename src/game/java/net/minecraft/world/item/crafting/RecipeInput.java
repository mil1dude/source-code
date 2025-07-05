package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;

public interface RecipeInput {
    ItemStack getItem(int p_344797_);

    int size();

    default boolean isEmpty() {
        for (int i = 0; i < this.size(); i++) {
            if (!this.getItem(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}