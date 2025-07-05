package net.minecraft.world.item.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.util.random.WeightedEntry;

public class EnchantmentInstance extends WeightedEntry.IntrusiveBase {
    public final Holder<Enchantment> enchantment;
    public final int level;

    public EnchantmentInstance(Holder<Enchantment> p_343221_, int p_44951_) {
        super(p_343221_.value().getWeight());
        this.enchantment = p_343221_;
        this.level = p_44951_;
    }
}