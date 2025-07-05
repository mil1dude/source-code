package net.minecraft.stats;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.slf4j.Logger;

public class ServerRecipeBook extends RecipeBook {
    public static final String RECIPE_BOOK_TAG = "recipeBook";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerRecipeBook.DisplayResolver displayResolver;
    @VisibleForTesting
    protected final Set<ResourceKey<Recipe<?>>> known = Sets.newIdentityHashSet();
    @VisibleForTesting
    protected final Set<ResourceKey<Recipe<?>>> highlight = Sets.newIdentityHashSet();

    public ServerRecipeBook(ServerRecipeBook.DisplayResolver p_361467_) {
        this.displayResolver = p_361467_;
    }

    public void add(ResourceKey<Recipe<?>> p_369732_) {
        this.known.add(p_369732_);
    }

    public boolean contains(ResourceKey<Recipe<?>> p_360909_) {
        return this.known.contains(p_360909_);
    }

    public void remove(ResourceKey<Recipe<?>> p_366423_) {
        this.known.remove(p_366423_);
        this.highlight.remove(p_366423_);
    }

    public void removeHighlight(ResourceKey<Recipe<?>> p_366458_) {
        this.highlight.remove(p_366458_);
    }

    private void addHighlight(ResourceKey<Recipe<?>> p_365655_) {
        this.highlight.add(p_365655_);
    }

    public int addRecipes(Collection<RecipeHolder<?>> p_12792_, ServerPlayer p_12793_) {
        List<ClientboundRecipeBookAddPacket.Entry> list = new ArrayList<>();

        for (RecipeHolder<?> recipeholder : p_12792_) {
            ResourceKey<Recipe<?>> resourcekey = recipeholder.id();
            if (!this.known.contains(resourcekey) && !recipeholder.value().isSpecial()) {
                this.add(resourcekey);
                this.addHighlight(resourcekey);
                this.displayResolver
                    .displaysForRecipe(
                        resourcekey, p_363687_ -> list.add(new ClientboundRecipeBookAddPacket.Entry(p_363687_, recipeholder.value().showNotification(), true))
                    );
                CriteriaTriggers.RECIPE_UNLOCKED.trigger(p_12793_, recipeholder);
            }
        }

        if (!list.isEmpty()) {
            p_12793_.connection.send(new ClientboundRecipeBookAddPacket(list, false));
        }

        return list.size();
    }

    public int removeRecipes(Collection<RecipeHolder<?>> p_12807_, ServerPlayer p_12808_) {
        List<RecipeDisplayId> list = Lists.newArrayList();

        for (RecipeHolder<?> recipeholder : p_12807_) {
            ResourceKey<Recipe<?>> resourcekey = recipeholder.id();
            if (this.known.contains(resourcekey)) {
                this.remove(resourcekey);
                this.displayResolver.displaysForRecipe(resourcekey, p_364401_ -> list.add(p_364401_.id()));
            }
        }

        if (!list.isEmpty()) {
            p_12808_.connection.send(new ClientboundRecipeBookRemovePacket(list));
        }

        return list.size();
    }

    public CompoundTag toNbt() {
        CompoundTag compoundtag = new CompoundTag();
        this.getBookSettings().write(compoundtag);
        ListTag listtag = new ListTag();

        for (ResourceKey<Recipe<?>> resourcekey : this.known) {
            listtag.add(StringTag.valueOf(resourcekey.location().toString()));
        }

        compoundtag.put("recipes", listtag);
        ListTag listtag1 = new ListTag();

        for (ResourceKey<Recipe<?>> resourcekey1 : this.highlight) {
            listtag1.add(StringTag.valueOf(resourcekey1.location().toString()));
        }

        compoundtag.put("toBeDisplayed", listtag1);
        return compoundtag;
    }

    public void fromNbt(CompoundTag p_12795_, Predicate<ResourceKey<Recipe<?>>> p_362297_) {
        this.setBookSettings(RecipeBookSettings.read(p_12795_));
        ListTag listtag = p_12795_.getList("recipes", 8);
        this.loadRecipes(listtag, this::add, p_362297_);
        ListTag listtag1 = p_12795_.getList("toBeDisplayed", 8);
        this.loadRecipes(listtag1, this::addHighlight, p_362297_);
    }

    private void loadRecipes(ListTag p_12798_, Consumer<ResourceKey<Recipe<?>>> p_12799_, Predicate<ResourceKey<Recipe<?>>> p_367349_) {
        for (int i = 0; i < p_12798_.size(); i++) {
            String s = p_12798_.getString(i);

            try {
                ResourceKey<Recipe<?>> resourcekey = ResourceKey.create(Registries.RECIPE, ResourceLocation.parse(s));
                if (!p_367349_.test(resourcekey)) {
                    LOGGER.error("Tried to load unrecognized recipe: {} removed now.", resourcekey);
                } else {
                    p_12799_.accept(resourcekey);
                }
            } catch (ResourceLocationException resourcelocationexception) {
                LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", s);
            }
        }
    }

    public void sendInitialRecipeBook(ServerPlayer p_12790_) {
        p_12790_.connection.send(new ClientboundRecipeBookSettingsPacket(this.getBookSettings()));
        List<ClientboundRecipeBookAddPacket.Entry> list = new ArrayList<>(this.known.size());

        for (ResourceKey<Recipe<?>> resourcekey : this.known) {
            this.displayResolver
                .displaysForRecipe(resourcekey, p_369028_ -> list.add(new ClientboundRecipeBookAddPacket.Entry(p_369028_, false, this.highlight.contains(resourcekey))));
        }

        p_12790_.connection.send(new ClientboundRecipeBookAddPacket(list, true));
    }

    public void copyOverData(ServerRecipeBook p_369276_) {
        this.known.clear();
        this.highlight.clear();
        this.bookSettings.replaceFrom(p_369276_.bookSettings);
        this.known.addAll(p_369276_.known);
        this.highlight.addAll(p_369276_.highlight);
    }

    @FunctionalInterface
    public interface DisplayResolver {
        void displaysForRecipe(ResourceKey<Recipe<?>> p_367891_, Consumer<RecipeDisplayEntry> p_363395_);
    }
}