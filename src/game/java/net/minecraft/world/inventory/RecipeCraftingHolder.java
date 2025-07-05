package net.minecraft.world.inventory;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameRules;

public interface RecipeCraftingHolder {
    void setRecipeUsed(@Nullable RecipeHolder<?> p_297397_);

    @Nullable
    RecipeHolder<?> getRecipeUsed();

    default void awardUsedRecipes(Player p_297343_, List<ItemStack> p_297535_) {
        RecipeHolder<?> recipeholder = this.getRecipeUsed();
        if (recipeholder != null) {
            p_297343_.triggerRecipeCrafted(recipeholder, p_297535_);
            if (!recipeholder.value().isSpecial()) {
                p_297343_.awardRecipes(Collections.singleton(recipeholder));
                this.setRecipeUsed(null);
            }
        }
    }

    default boolean setRecipeUsed(ServerPlayer p_301009_, RecipeHolder<?> p_301264_) {
        if (!p_301264_.value().isSpecial()
            && p_301009_.serverLevel().getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING)
            && !p_301009_.getRecipeBook().contains(p_301264_.id())) {
            return false;
        } else {
            this.setRecipeUsed(p_301264_);
            return true;
        }
    }
}