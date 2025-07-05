package net.minecraft.data.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class ItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
    private final CompletableFuture<TagsProvider.TagLookup<Block>> blockTags;
    private final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new HashMap<>();

    public ItemTagsProvider(
        PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagsProvider.TagLookup<Block>> p_275322_
    ) {
        super(p_275343_, Registries.ITEM, p_275729_, p_255790_ -> p_255790_.builtInRegistryHolder().key());
        this.blockTags = p_275322_;
    }

    public ItemTagsProvider(
        PackOutput p_275204_,
        CompletableFuture<HolderLookup.Provider> p_275194_,
        CompletableFuture<TagsProvider.TagLookup<Item>> p_275207_,
        CompletableFuture<TagsProvider.TagLookup<Block>> p_275634_
    ) {
        super(p_275204_, Registries.ITEM, p_275194_, p_275207_, p_274765_ -> p_274765_.builtInRegistryHolder().key());
        this.blockTags = p_275634_;
    }

    protected void copy(TagKey<Block> p_206422_, TagKey<Item> p_206423_) {
        this.tagsToCopy.put(p_206422_, p_206423_);
    }

    @Override
    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return super.createContentsProvider().thenCombine(this.blockTags, (p_274766_, p_274767_) -> {
            this.tagsToCopy.forEach((p_274763_, p_274764_) -> {
                TagBuilder tagbuilder = this.getOrCreateRawBuilder((TagKey<Item>)p_274764_);
                Optional<TagBuilder> optional = p_274767_.apply((TagKey)p_274763_);
                optional.orElseThrow(() -> new IllegalStateException("Missing block tag " + p_274764_.location())).build().forEach(tagbuilder::add);
            });
            return (HolderLookup.Provider)p_274766_;
        });
    }
}
