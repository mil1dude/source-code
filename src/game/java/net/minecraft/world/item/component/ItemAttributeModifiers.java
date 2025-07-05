package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record ItemAttributeModifiers(List<ItemAttributeModifiers.Entry> modifiers, boolean showInTooltip) {
    public static final ItemAttributeModifiers EMPTY = new ItemAttributeModifiers(List.of(), true);
    private static final Codec<ItemAttributeModifiers> FULL_CODEC = RecordCodecBuilder.create(
        p_333950_ -> p_333950_.group(
                    ItemAttributeModifiers.Entry.CODEC.listOf().fieldOf("modifiers").forGetter(ItemAttributeModifiers::modifiers),
                    Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(ItemAttributeModifiers::showInTooltip)
                )
                .apply(p_333950_, ItemAttributeModifiers::new)
    );
    public static final Codec<ItemAttributeModifiers> CODEC = Codec.withAlternative(
        FULL_CODEC, ItemAttributeModifiers.Entry.CODEC.listOf(), p_331984_ -> new ItemAttributeModifiers(p_331984_, true)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers> STREAM_CODEC = StreamCodec.composite(
        ItemAttributeModifiers.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
        ItemAttributeModifiers::modifiers,
        ByteBufCodecs.BOOL,
        ItemAttributeModifiers::showInTooltip,
        ItemAttributeModifiers::new
    );
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(
        new DecimalFormat("#.##"), p_334862_ -> p_334862_.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
    );

    public ItemAttributeModifiers withTooltip(boolean p_332852_) {
        return new ItemAttributeModifiers(this.modifiers, p_332852_);
    }

    public static ItemAttributeModifiers.Builder builder() {
        return new ItemAttributeModifiers.Builder();
    }

    public ItemAttributeModifiers withModifierAdded(Holder<Attribute> p_335092_, AttributeModifier p_327974_, EquipmentSlotGroup p_328449_) {
        ImmutableList.Builder<ItemAttributeModifiers.Entry> builder = ImmutableList.builderWithExpectedSize(this.modifiers.size() + 1);

        for (ItemAttributeModifiers.Entry itemattributemodifiers$entry : this.modifiers) {
            if (!itemattributemodifiers$entry.matches(p_335092_, p_327974_.id())) {
                builder.add(itemattributemodifiers$entry);
            }
        }

        builder.add(new ItemAttributeModifiers.Entry(p_335092_, p_327974_, p_328449_));
        return new ItemAttributeModifiers(builder.build(), this.showInTooltip);
    }

    public void forEach(EquipmentSlotGroup p_343586_, BiConsumer<Holder<Attribute>, AttributeModifier> p_344914_) {
        for (ItemAttributeModifiers.Entry itemattributemodifiers$entry : this.modifiers) {
            if (itemattributemodifiers$entry.slot.equals(p_343586_)) {
                p_344914_.accept(itemattributemodifiers$entry.attribute, itemattributemodifiers$entry.modifier);
            }
        }
    }

    public void forEach(EquipmentSlot p_334753_, BiConsumer<Holder<Attribute>, AttributeModifier> p_331767_) {
        for (ItemAttributeModifiers.Entry itemattributemodifiers$entry : this.modifiers) {
            if (itemattributemodifiers$entry.slot.test(p_334753_)) {
                p_331767_.accept(itemattributemodifiers$entry.attribute, itemattributemodifiers$entry.modifier);
            }
        }
    }

    public double compute(double p_332865_, EquipmentSlot p_329615_) {
        double d0 = p_332865_;

        for (ItemAttributeModifiers.Entry itemattributemodifiers$entry : this.modifiers) {
            if (itemattributemodifiers$entry.slot.test(p_329615_)) {
                double d1 = itemattributemodifiers$entry.modifier.amount();

                d0 += switch (itemattributemodifiers$entry.modifier.operation()) {
                    case ADD_VALUE -> d1;
                    case ADD_MULTIPLIED_BASE -> d1 * p_332865_;
                    case ADD_MULTIPLIED_TOTAL -> d1 * d0;
                };
            }
        }

        return d0;
    }

    public static class Builder {
        private final ImmutableList.Builder<ItemAttributeModifiers.Entry> entries = ImmutableList.builder();

        Builder() {
        }

        public ItemAttributeModifiers.Builder add(Holder<Attribute> p_330104_, AttributeModifier p_333549_, EquipmentSlotGroup p_332621_) {
            this.entries.add(new ItemAttributeModifiers.Entry(p_330104_, p_333549_, p_332621_));
            return this;
        }

        public ItemAttributeModifiers build() {
            return new ItemAttributeModifiers(this.entries.build(), true);
        }
    }

    public static record Entry(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        public static final Codec<ItemAttributeModifiers.Entry> CODEC = RecordCodecBuilder.create(
            p_341578_ -> p_341578_.group(
                        Attribute.CODEC.fieldOf("type").forGetter(ItemAttributeModifiers.Entry::attribute),
                        AttributeModifier.MAP_CODEC.forGetter(ItemAttributeModifiers.Entry::modifier),
                        EquipmentSlotGroup.CODEC.optionalFieldOf("slot", EquipmentSlotGroup.ANY).forGetter(ItemAttributeModifiers.Entry::slot)
                    )
                    .apply(p_341578_, ItemAttributeModifiers.Entry::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeModifiers.Entry> STREAM_CODEC = StreamCodec.composite(
            Attribute.STREAM_CODEC,
            ItemAttributeModifiers.Entry::attribute,
            AttributeModifier.STREAM_CODEC,
            ItemAttributeModifiers.Entry::modifier,
            EquipmentSlotGroup.STREAM_CODEC,
            ItemAttributeModifiers.Entry::slot,
            ItemAttributeModifiers.Entry::new
        );

        public boolean matches(Holder<Attribute> p_344464_, ResourceLocation p_344217_) {
            return p_344464_.equals(this.attribute) && this.modifier.is(p_344217_);
        }
    }
}