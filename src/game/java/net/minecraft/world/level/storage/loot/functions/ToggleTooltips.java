package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ToggleTooltips extends LootItemConditionalFunction {
    private static final Map<DataComponentType<?>, ToggleTooltips.ComponentToggle<?>> TOGGLES = Stream.of(
            new ToggleTooltips.ComponentToggle<>(DataComponents.TRIM, ArmorTrim::withTooltip),
            new ToggleTooltips.ComponentToggle<>(DataComponents.DYED_COLOR, DyedItemColor::withTooltip),
            new ToggleTooltips.ComponentToggle<>(DataComponents.ENCHANTMENTS, ItemEnchantments::withTooltip),
            new ToggleTooltips.ComponentToggle<>(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments::withTooltip),
            new ToggleTooltips.ComponentToggle<>(DataComponents.UNBREAKABLE, Unbreakable::withTooltip),
            new ToggleTooltips.ComponentToggle<>(DataComponents.CAN_BREAK, AdventureModePredicate::withTooltip),
            new ToggleTooltips.ComponentToggle<>(DataComponents.CAN_PLACE_ON, AdventureModePredicate::withTooltip),
            new ToggleTooltips.ComponentToggle<>(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers::withTooltip),
            new ToggleTooltips.ComponentToggle<>(DataComponents.JUKEBOX_PLAYABLE, JukeboxPlayable::withTooltip)
        )
        .collect(Collectors.toMap(ToggleTooltips.ComponentToggle::type, p_331423_ -> (ToggleTooltips.ComponentToggle<?>)p_331423_));
    private static final Codec<ToggleTooltips.ComponentToggle<?>> TOGGLE_CODEC = BuiltInRegistries.DATA_COMPONENT_TYPE
        .byNameCodec()
        .comapFlatMap(
            p_332617_ -> {
                ToggleTooltips.ComponentToggle<?> componenttoggle = TOGGLES.get(p_332617_);
                return componenttoggle != null
                    ? DataResult.success(componenttoggle)
                    : DataResult.error(() -> "Can't toggle tooltip visiblity for " + BuiltInRegistries.DATA_COMPONENT_TYPE.getKey((DataComponentType<?>)p_332617_));
            },
            ToggleTooltips.ComponentToggle::type
        );
    public static final MapCodec<ToggleTooltips> CODEC = RecordCodecBuilder.mapCodec(
        p_330512_ -> commonFields(p_330512_)
                .and(Codec.unboundedMap(TOGGLE_CODEC, Codec.BOOL).fieldOf("toggles").forGetter(p_331447_ -> p_331447_.values))
                .apply(p_330512_, ToggleTooltips::new)
    );
    private final Map<ToggleTooltips.ComponentToggle<?>, Boolean> values;

    private ToggleTooltips(List<LootItemCondition> p_330048_, Map<ToggleTooltips.ComponentToggle<?>, Boolean> p_332012_) {
        super(p_330048_);
        this.values = p_332012_;
    }

    @Override
    protected ItemStack run(ItemStack p_334443_, LootContext p_331872_) {
        this.values.forEach((p_330543_, p_329622_) -> p_330543_.applyIfPresent(p_334443_, p_329622_));
        return p_334443_;
    }

    @Override
    public LootItemFunctionType<ToggleTooltips> getType() {
        return LootItemFunctions.TOGGLE_TOOLTIPS;
    }

    static record ComponentToggle<T>(DataComponentType<T> type, ToggleTooltips.TooltipWither<T> setter) {
        public void applyIfPresent(ItemStack p_332822_, boolean p_333699_) {
            T t = p_332822_.get(this.type);
            if (t != null) {
                p_332822_.set(this.type, this.setter.withTooltip(t, p_333699_));
            }
        }
    }

    @FunctionalInterface
    interface TooltipWither<T> {
        T withTooltip(T p_328719_, boolean p_327686_);
    }
}