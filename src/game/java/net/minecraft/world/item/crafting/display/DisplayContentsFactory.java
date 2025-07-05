package net.minecraft.world.item.crafting.display;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface DisplayContentsFactory<T> {
    public interface ForRemainders<T> extends DisplayContentsFactory<T> {
        T addRemainder(T p_362972_, List<T> p_363372_);
    }

    public interface ForStacks<T> extends DisplayContentsFactory<T> {
        default T forStack(Holder<Item> p_364562_) {
            return this.forStack(new ItemStack(p_364562_));
        }

        default T forStack(Item p_361017_) {
            return this.forStack(new ItemStack(p_361017_));
        }

        T forStack(ItemStack p_361037_);
    }
}