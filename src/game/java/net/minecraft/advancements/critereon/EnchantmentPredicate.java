package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentPredicate(Optional<HolderSet<Enchantment>> enchantments, MinMaxBounds.Ints level) {
    public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create(
        p_340753_ -> p_340753_.group(
                    RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(EnchantmentPredicate::enchantments),
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("levels", MinMaxBounds.Ints.ANY).forGetter(EnchantmentPredicate::level)
                )
                .apply(p_340753_, EnchantmentPredicate::new)
    );

    public EnchantmentPredicate(Holder<Enchantment> p_342794_, MinMaxBounds.Ints p_343999_) {
        this(Optional.of(HolderSet.direct(p_342794_)), p_343999_);
    }

    public EnchantmentPredicate(HolderSet<Enchantment> p_343165_, MinMaxBounds.Ints p_30472_) {
        this(Optional.of(p_343165_), p_30472_);
    }

    public boolean containedIn(ItemEnchantments p_334667_) {
        if (this.enchantments.isPresent()) {
            for (Holder<Enchantment> holder : this.enchantments.get()) {
                if (this.matchesEnchantment(p_334667_, holder)) {
                    return true;
                }
            }

            return false;
        } else if (this.level != MinMaxBounds.Ints.ANY) {
            for (Entry<Holder<Enchantment>> entry : p_334667_.entrySet()) {
                if (this.level.matches(entry.getIntValue())) {
                    return true;
                }
            }

            return false;
        } else {
            return !p_334667_.isEmpty();
        }
    }

    private boolean matchesEnchantment(ItemEnchantments p_342239_, Holder<Enchantment> p_342249_) {
        int i = p_342239_.getLevel(p_342249_);
        if (i == 0) {
            return false;
        } else {
            return this.level == MinMaxBounds.Ints.ANY ? true : this.level.matches(i);
        }
    }
}