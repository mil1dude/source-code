package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

public class SmithingTrimRecipe implements SmithingRecipe {
    final Optional<Ingredient> template;
    final Optional<Ingredient> base;
    final Optional<Ingredient> addition;
    @Nullable
    private PlacementInfo placementInfo;

    public SmithingTrimRecipe(Optional<Ingredient> p_369733_, Optional<Ingredient> p_361422_, Optional<Ingredient> p_368814_) {
        this.template = p_369733_;
        this.base = p_361422_;
        this.addition = p_368814_;
    }

    public ItemStack assemble(SmithingRecipeInput p_344440_, HolderLookup.Provider p_330268_) {
        return applyTrim(p_330268_, p_344440_.base(), p_344440_.addition(), p_344440_.template());
    }

    public static ItemStack applyTrim(HolderLookup.Provider p_369231_, ItemStack p_368958_, ItemStack p_366218_, ItemStack p_368590_) {
        Optional<Holder.Reference<TrimMaterial>> optional = TrimMaterials.getFromIngredient(p_369231_, p_366218_);
        Optional<Holder.Reference<TrimPattern>> optional1 = TrimPatterns.getFromTemplate(p_369231_, p_368590_);
        if (optional.isPresent() && optional1.isPresent()) {
            ArmorTrim armortrim = p_368958_.get(DataComponents.TRIM);
            if (armortrim != null && armortrim.hasPatternAndMaterial(optional1.get(), optional.get())) {
                return ItemStack.EMPTY;
            } else {
                ItemStack itemstack = p_368958_.copyWithCount(1);
                itemstack.set(DataComponents.TRIM, new ArmorTrim(optional.get(), optional1.get()));
                return itemstack;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return this.template;
    }

    @Override
    public Optional<Ingredient> baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return this.addition;
    }

    @Override
    public RecipeSerializer<SmithingTrimRecipe> getSerializer() {
        return RecipeSerializer.SMITHING_TRIM;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(List.of(this.template, this.base, this.addition));
        }

        return this.placementInfo;
    }

    @Override
    public List<RecipeDisplay> display() {
        SlotDisplay slotdisplay = Ingredient.optionalIngredientToDisplay(this.base);
        SlotDisplay slotdisplay1 = Ingredient.optionalIngredientToDisplay(this.addition);
        SlotDisplay slotdisplay2 = Ingredient.optionalIngredientToDisplay(this.template);
        return List.of(
            new SmithingRecipeDisplay(
                slotdisplay2,
                slotdisplay,
                slotdisplay1,
                new SlotDisplay.SmithingTrimDemoSlotDisplay(slotdisplay, slotdisplay1, slotdisplay2),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
            )
        );
    }

    public static class Serializer implements RecipeSerializer<SmithingTrimRecipe> {
        private static final MapCodec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.mapCodec(
            p_359872_ -> p_359872_.group(
                        Ingredient.CODEC.optionalFieldOf("template").forGetter(p_359874_ -> p_359874_.template),
                        Ingredient.CODEC.optionalFieldOf("base").forGetter(p_359876_ -> p_359876_.base),
                        Ingredient.CODEC.optionalFieldOf("addition").forGetter(p_359877_ -> p_359877_.addition)
                    )
                    .apply(p_359872_, SmithingTrimRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
            p_359878_ -> p_359878_.template,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
            p_359873_ -> p_359873_.base,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
            p_359875_ -> p_359875_.addition,
            SmithingTrimRecipe::new
        );

        @Override
        public MapCodec<SmithingTrimRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}