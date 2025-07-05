package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
    @Override
    public Codec<RecipeCraftedTrigger.TriggerInstance> codec() {
        return RecipeCraftedTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_281468_, ResourceKey<Recipe<?>> p_366112_, List<ItemStack> p_282070_) {
        this.trigger(p_281468_, p_357629_ -> p_357629_.matches(p_366112_, p_282070_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipeId, List<ItemPredicate> ingredients)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<RecipeCraftedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_357630_ -> p_357630_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RecipeCraftedTrigger.TriggerInstance::player),
                        ResourceKey.codec(Registries.RECIPE).fieldOf("recipe_id").forGetter(RecipeCraftedTrigger.TriggerInstance::recipeId),
                        ItemPredicate.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(RecipeCraftedTrigger.TriggerInstance::ingredients)
                    )
                    .apply(p_357630_, RecipeCraftedTrigger.TriggerInstance::new)
        );

        public static Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceKey<Recipe<?>> p_364431_, List<ItemPredicate.Builder> p_299678_) {
            return CriteriaTriggers.RECIPE_CRAFTED
                .createCriterion(
                    new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), p_364431_, p_299678_.stream().map(ItemPredicate.Builder::build).toList())
                );
        }

        public static Criterion<RecipeCraftedTrigger.TriggerInstance> craftedItem(ResourceKey<Recipe<?>> p_368041_) {
            return CriteriaTriggers.RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), p_368041_, List.of()));
        }

        public static Criterion<RecipeCraftedTrigger.TriggerInstance> crafterCraftedItem(ResourceKey<Recipe<?>> p_369526_) {
            return CriteriaTriggers.CRAFTER_RECIPE_CRAFTED.createCriterion(new RecipeCraftedTrigger.TriggerInstance(Optional.empty(), p_369526_, List.of()));
        }

        boolean matches(ResourceKey<Recipe<?>> p_361469_, List<ItemStack> p_283698_) {
            if (p_361469_ != this.recipeId) {
                return false;
            } else {
                List<ItemStack> list = new ArrayList<>(p_283698_);

                for (ItemPredicate itempredicate : this.ingredients) {
                    boolean flag = false;
                    Iterator<ItemStack> iterator = list.iterator();

                    while (iterator.hasNext()) {
                        if (itempredicate.test(iterator.next())) {
                            iterator.remove();
                            flag = true;
                            break;
                        }
                    }

                    if (!flag) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}