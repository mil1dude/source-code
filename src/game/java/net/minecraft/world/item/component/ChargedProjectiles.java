package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ChargedProjectiles {
    public static final ChargedProjectiles EMPTY = new ChargedProjectiles(List.of());
    public static final Codec<ChargedProjectiles> CODEC = ItemStack.CODEC.listOf().xmap(ChargedProjectiles::new, p_333238_ -> p_333238_.items);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargedProjectiles> STREAM_CODEC = ItemStack.STREAM_CODEC
        .apply(ByteBufCodecs.list())
        .map(ChargedProjectiles::new, p_330449_ -> p_330449_.items);
    private final List<ItemStack> items;

    private ChargedProjectiles(List<ItemStack> p_328441_) {
        this.items = p_328441_;
    }

    public static ChargedProjectiles of(ItemStack p_330424_) {
        return new ChargedProjectiles(List.of(p_330424_.copy()));
    }

    public static ChargedProjectiles of(List<ItemStack> p_334351_) {
        return new ChargedProjectiles(List.copyOf(Lists.transform(p_334351_, ItemStack::copy)));
    }

    public boolean contains(Item p_329513_) {
        for (ItemStack itemstack : this.items) {
            if (itemstack.is(p_329513_)) {
                return true;
            }
        }

        return false;
    }

    public List<ItemStack> getItems() {
        return Lists.transform(this.items, ItemStack::copy);
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public boolean equals(Object p_332122_) {
        if (this == p_332122_) {
            return true;
        } else {
            if (p_332122_ instanceof ChargedProjectiles chargedprojectiles && ItemStack.listMatches(this.items, chargedprojectiles.items)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(this.items);
    }

    @Override
    public String toString() {
        return "ChargedProjectiles[items=" + this.items + "]";
    }
}