package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public record ItemDamagePredicate(MinMaxBounds.Ints durability, MinMaxBounds.Ints damage) implements SingleComponentItemPredicate<Integer> {
    public static final Codec<ItemDamagePredicate> CODEC = RecordCodecBuilder.create(
        p_331200_ -> p_331200_.group(
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("durability", MinMaxBounds.Ints.ANY).forGetter(ItemDamagePredicate::durability),
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("damage", MinMaxBounds.Ints.ANY).forGetter(ItemDamagePredicate::damage)
                )
                .apply(p_331200_, ItemDamagePredicate::new)
    );

    @Override
    public DataComponentType<Integer> componentType() {
        return DataComponents.DAMAGE;
    }

    public boolean matches(ItemStack p_335150_, Integer p_329961_) {
        return !this.durability.matches(p_335150_.getMaxDamage() - p_329961_) ? false : this.damage.matches(p_329961_);
    }

    public static ItemDamagePredicate durability(MinMaxBounds.Ints p_335837_) {
        return new ItemDamagePredicate(p_335837_, MinMaxBounds.Ints.ANY);
    }
}