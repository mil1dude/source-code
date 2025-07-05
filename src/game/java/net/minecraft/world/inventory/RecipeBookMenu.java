package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class RecipeBookMenu extends AbstractContainerMenu {
    public RecipeBookMenu(MenuType<?> p_40115_, int p_40116_) {
        super(p_40115_, p_40116_);
    }

    public abstract RecipeBookMenu.PostPlaceAction handlePlacement(
        boolean p_40119_, boolean p_363647_, RecipeHolder<?> p_297420_, ServerLevel p_367376_, Inventory p_364321_
    );

    public abstract void fillCraftSlotsStackedContents(StackedItemContents p_365715_);

    public abstract RecipeBookType getRecipeBookType();

    public static enum PostPlaceAction {
        NOTHING,
        PLACE_GHOST_RECIPE;
    }
}