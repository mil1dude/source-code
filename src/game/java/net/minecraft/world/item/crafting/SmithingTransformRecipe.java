package net.minecraft.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;

public class SmithingTransformRecipe implements SmithingRecipe {
    final Optional<Ingredient> template;
    final Optional<Ingredient> base;
    final Optional<Ingredient> addition;
    final ItemStack result;
    @Nullable
    private PlacementInfo placementInfo;

    public SmithingTransformRecipe(Optional<Ingredient> p_366010_, Optional<Ingredient> p_367568_, Optional<Ingredient> p_365900_, ItemStack p_267031_) {
        this.template = p_366010_;
        this.base = p_367568_;
        this.addition = p_365900_;
        this.result = p_267031_;
    }

    public ItemStack assemble(SmithingRecipeInput p_343590_, HolderLookup.Provider p_331030_) {
        ItemStack itemstack = p_343590_.base().transmuteCopy(this.result.getItem(), this.result.getCount());
        itemstack.applyComponents(this.result.getComponentsPatch());
        return itemstack;
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
    public RecipeSerializer<SmithingTransformRecipe> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
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
        return List.of(
            new SmithingRecipeDisplay(
                Ingredient.optionalIngredientToDisplay(this.template),
                Ingredient.optionalIngredientToDisplay(this.base),
                Ingredient.optionalIngredientToDisplay(this.addition),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
            )
        );
    }

    public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
        private static final MapCodec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.mapCodec(
            p_359868_ -> p_359868_.group(
                        Ingredient.CODEC.optionalFieldOf("template").forGetter(p_359869_ -> p_359869_.template),
                        Ingredient.CODEC.optionalFieldOf("base").forGetter(p_359871_ -> p_359871_.base),
                        Ingredient.CODEC.optionalFieldOf("addition").forGetter(p_359867_ -> p_359867_.addition),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_297480_ -> p_297480_.result)
                    )
                    .apply(p_359868_, SmithingTransformRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
            p_359864_ -> p_359864_.template,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
            p_359866_ -> p_359866_.base,
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
            p_359870_ -> p_359870_.addition,
            ItemStack.STREAM_CODEC,
            p_359865_ -> p_359865_.result,
            SmithingTransformRecipe::new
        );

        @Override
        public MapCodec<SmithingTransformRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTransformRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}