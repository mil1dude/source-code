package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public abstract class ItemEnchantmentsPredicate implements SingleComponentItemPredicate<ItemEnchantments> {
    private final List<EnchantmentPredicate> enchantments;

    protected ItemEnchantmentsPredicate(List<EnchantmentPredicate> p_327909_) {
        this.enchantments = p_327909_;
    }

    public static <T extends ItemEnchantmentsPredicate> Codec<T> codec(Function<List<EnchantmentPredicate>, T> p_330847_) {
        return EnchantmentPredicate.CODEC.listOf().xmap(p_330847_, ItemEnchantmentsPredicate::enchantments);
    }

    protected List<EnchantmentPredicate> enchantments() {
        return this.enchantments;
    }

    public boolean matches(ItemStack p_332137_, ItemEnchantments p_331461_) {
        for (EnchantmentPredicate enchantmentpredicate : this.enchantments) {
            if (!enchantmentpredicate.containedIn(p_331461_)) {
                return false;
            }
        }

        return true;
    }

    public static ItemEnchantmentsPredicate.Enchantments enchantments(List<EnchantmentPredicate> p_334509_) {
        return new ItemEnchantmentsPredicate.Enchantments(p_334509_);
    }

    public static ItemEnchantmentsPredicate.StoredEnchantments storedEnchantments(List<EnchantmentPredicate> p_331491_) {
        return new ItemEnchantmentsPredicate.StoredEnchantments(p_331491_);
    }

    public static class Enchantments extends ItemEnchantmentsPredicate {
        public static final Codec<ItemEnchantmentsPredicate.Enchantments> CODEC = codec(ItemEnchantmentsPredicate.Enchantments::new);

        protected Enchantments(List<EnchantmentPredicate> p_333770_) {
            super(p_333770_);
        }

        @Override
        public DataComponentType<ItemEnchantments> componentType() {
            return DataComponents.ENCHANTMENTS;
        }
    }

    public static class StoredEnchantments extends ItemEnchantmentsPredicate {
        public static final Codec<ItemEnchantmentsPredicate.StoredEnchantments> CODEC = codec(ItemEnchantmentsPredicate.StoredEnchantments::new);

        protected StoredEnchantments(List<EnchantmentPredicate> p_330178_) {
            super(p_330178_);
        }

        @Override
        public DataComponentType<ItemEnchantments> componentType() {
            return DataComponents.STORED_ENCHANTMENTS;
        }
    }
}