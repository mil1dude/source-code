package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ItemAttributeModifiersPredicate(
    Optional<CollectionPredicate<ItemAttributeModifiers.Entry, ItemAttributeModifiersPredicate.EntryPredicate>> modifiers
) implements SingleComponentItemPredicate<ItemAttributeModifiers> {
    public static final Codec<ItemAttributeModifiersPredicate> CODEC = RecordCodecBuilder.create(
        p_334039_ -> p_334039_.group(
                    CollectionPredicate.<ItemAttributeModifiers.Entry, ItemAttributeModifiersPredicate.EntryPredicate>codec(
                            ItemAttributeModifiersPredicate.EntryPredicate.CODEC
                        )
                        .optionalFieldOf("modifiers")
                        .forGetter(ItemAttributeModifiersPredicate::modifiers)
                )
                .apply(p_334039_, ItemAttributeModifiersPredicate::new)
    );

    @Override
    public DataComponentType<ItemAttributeModifiers> componentType() {
        return DataComponents.ATTRIBUTE_MODIFIERS;
    }

    public boolean matches(ItemStack p_331791_, ItemAttributeModifiers p_328619_) {
        return !this.modifiers.isPresent() || this.modifiers.get().test(p_328619_.modifiers());
    }

    public static record EntryPredicate(
        Optional<HolderSet<Attribute>> attribute,
        Optional<ResourceLocation> id,
        MinMaxBounds.Doubles amount,
        Optional<AttributeModifier.Operation> operation,
        Optional<EquipmentSlotGroup> slot
    ) implements Predicate<ItemAttributeModifiers.Entry> {
        public static final Codec<ItemAttributeModifiersPredicate.EntryPredicate> CODEC = RecordCodecBuilder.create(
            p_340758_ -> p_340758_.group(
                        RegistryCodecs.homogeneousList(Registries.ATTRIBUTE)
                            .optionalFieldOf("attribute")
                            .forGetter(ItemAttributeModifiersPredicate.EntryPredicate::attribute),
                        ResourceLocation.CODEC.optionalFieldOf("id").forGetter(ItemAttributeModifiersPredicate.EntryPredicate::id),
                        MinMaxBounds.Doubles.CODEC
                            .optionalFieldOf("amount", MinMaxBounds.Doubles.ANY)
                            .forGetter(ItemAttributeModifiersPredicate.EntryPredicate::amount),
                        AttributeModifier.Operation.CODEC.optionalFieldOf("operation").forGetter(ItemAttributeModifiersPredicate.EntryPredicate::operation),
                        EquipmentSlotGroup.CODEC.optionalFieldOf("slot").forGetter(ItemAttributeModifiersPredicate.EntryPredicate::slot)
                    )
                    .apply(p_340758_, ItemAttributeModifiersPredicate.EntryPredicate::new)
        );

        public boolean test(ItemAttributeModifiers.Entry p_332302_) {
            if (this.attribute.isPresent() && !this.attribute.get().contains(p_332302_.attribute())) {
                return false;
            } else if (this.id.isPresent() && !this.id.get().equals(p_332302_.modifier().id())) {
                return false;
            } else if (!this.amount.matches(p_332302_.modifier().amount())) {
                return false;
            } else {
                return this.operation.isPresent() && this.operation.get() != p_332302_.modifier().operation()
                    ? false
                    : !this.slot.isPresent() || this.slot.get() == p_332302_.slot();
            }
        }
    }
}