package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
    @Override
    public Codec<RecipeUnlockedTrigger.TriggerInstance> codec() {
        return RecipeUnlockedTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_63719_, RecipeHolder<?> p_300165_) {
        this.trigger(p_63719_, p_296143_ -> p_296143_.matches(p_300165_));
    }

    public static Criterion<RecipeUnlockedTrigger.TriggerInstance> unlocked(ResourceKey<Recipe<?>> p_362362_) {
        return CriteriaTriggers.RECIPE_UNLOCKED.createCriterion(new RecipeUnlockedTrigger.TriggerInstance(Optional.empty(), p_362362_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Recipe<?>> recipe)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<RecipeUnlockedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_357631_ -> p_357631_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RecipeUnlockedTrigger.TriggerInstance::player),
                        ResourceKey.codec(Registries.RECIPE).fieldOf("recipe").forGetter(RecipeUnlockedTrigger.TriggerInstance::recipe)
                    )
                    .apply(p_357631_, RecipeUnlockedTrigger.TriggerInstance::new)
        );

        public boolean matches(RecipeHolder<?> p_299959_) {
            return this.recipe == p_299959_.id();
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}