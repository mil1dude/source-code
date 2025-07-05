package net.minecraft.advancements;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record AdvancementRewards(int experience, List<ResourceKey<LootTable>> loot, List<ResourceKey<Recipe<?>>> recipes, Optional<CacheableFunction> function) {
    public static final Codec<AdvancementRewards> CODEC = RecordCodecBuilder.create(
        p_357612_ -> p_357612_.group(
                    Codec.INT.optionalFieldOf("experience", Integer.valueOf(0)).forGetter(AdvancementRewards::experience),
                    ResourceKey.codec(Registries.LOOT_TABLE).listOf().optionalFieldOf("loot", List.of()).forGetter(AdvancementRewards::loot),
                    ResourceKey.codec(Registries.RECIPE).listOf().optionalFieldOf("recipes", List.of()).forGetter(AdvancementRewards::recipes),
                    CacheableFunction.CODEC.optionalFieldOf("function").forGetter(AdvancementRewards::function)
                )
                .apply(p_357612_, AdvancementRewards::new)
    );
    public static final AdvancementRewards EMPTY = new AdvancementRewards(0, List.of(), List.of(), Optional.empty());

    public void grant(ServerPlayer p_9990_) {
        p_9990_.giveExperiencePoints(this.experience);
        LootParams lootparams = new LootParams.Builder(p_9990_.serverLevel())
            .withParameter(LootContextParams.THIS_ENTITY, p_9990_)
            .withParameter(LootContextParams.ORIGIN, p_9990_.position())
            .create(LootContextParamSets.ADVANCEMENT_REWARD);
        boolean flag = false;

        for (ResourceKey<LootTable> resourcekey : this.loot) {
            for (ItemStack itemstack : p_9990_.server.reloadableRegistries().getLootTable(resourcekey).getRandomItems(lootparams)) {
                if (p_9990_.addItem(itemstack)) {
                    p_9990_.level()
                        .playSound(
                            null,
                            p_9990_.getX(),
                            p_9990_.getY(),
                            p_9990_.getZ(),
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.PLAYERS,
                            0.2F,
                            ((p_9990_.getRandom().nextFloat() - p_9990_.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                        );
                    flag = true;
                } else {
                    ItemEntity itementity = p_9990_.drop(itemstack, false);
                    if (itementity != null) {
                        itementity.setNoPickUpDelay();
                        itementity.setTarget(p_9990_.getUUID());
                    }
                }
            }
        }

        if (flag) {
            p_9990_.containerMenu.broadcastChanges();
        }

        if (!this.recipes.isEmpty()) {
            p_9990_.awardRecipesByKey(this.recipes);
        }

        MinecraftServer minecraftserver = p_9990_.server;
        this.function
            .flatMap(p_308107_ -> p_308107_.get(minecraftserver.getFunctions()))
            .ifPresent(
                p_357615_ -> minecraftserver.getFunctions().execute((CommandFunction<CommandSourceStack>)p_357615_, p_9990_.createCommandSourceStack().withSuppressedOutput().withPermission(2))
            );
    }

    public static class Builder {
        private int experience;
        private final ImmutableList.Builder<ResourceKey<LootTable>> loot = ImmutableList.builder();
        private final ImmutableList.Builder<ResourceKey<Recipe<?>>> recipes = ImmutableList.builder();
        private Optional<ResourceLocation> function = Optional.empty();

        public static AdvancementRewards.Builder experience(int p_10006_) {
            return new AdvancementRewards.Builder().addExperience(p_10006_);
        }

        public AdvancementRewards.Builder addExperience(int p_10008_) {
            this.experience += p_10008_;
            return this;
        }

        public static AdvancementRewards.Builder loot(ResourceKey<LootTable> p_332404_) {
            return new AdvancementRewards.Builder().addLootTable(p_332404_);
        }

        public AdvancementRewards.Builder addLootTable(ResourceKey<LootTable> p_330122_) {
            this.loot.add(p_330122_);
            return this;
        }

        public static AdvancementRewards.Builder recipe(ResourceKey<Recipe<?>> p_365956_) {
            return new AdvancementRewards.Builder().addRecipe(p_365956_);
        }

        public AdvancementRewards.Builder addRecipe(ResourceKey<Recipe<?>> p_362523_) {
            this.recipes.add(p_362523_);
            return this;
        }

        public static AdvancementRewards.Builder function(ResourceLocation p_144827_) {
            return new AdvancementRewards.Builder().runs(p_144827_);
        }

        public AdvancementRewards.Builder runs(ResourceLocation p_144829_) {
            this.function = Optional.of(p_144829_);
            return this;
        }

        public AdvancementRewards build() {
            return new AdvancementRewards(this.experience, this.loot.build(), this.recipes.build(), this.function.map(CacheableFunction::new));
        }
    }
}