package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

public abstract class IntrinsicHolderTagsProvider<T> extends TagsProvider<T> {
    private final Function<T, ResourceKey<T>> keyExtractor;

    public IntrinsicHolderTagsProvider(
        PackOutput p_256164_,
        ResourceKey<? extends Registry<T>> p_256155_,
        CompletableFuture<HolderLookup.Provider> p_256488_,
        Function<T, ResourceKey<T>> p_256168_
    ) {
        super(p_256164_, p_256155_, p_256488_);
        this.keyExtractor = p_256168_;
    }

    public IntrinsicHolderTagsProvider(
        PackOutput p_275304_,
        ResourceKey<? extends Registry<T>> p_275709_,
        CompletableFuture<HolderLookup.Provider> p_275227_,
        CompletableFuture<TagsProvider.TagLookup<T>> p_275311_,
        Function<T, ResourceKey<T>> p_275566_
    ) {
        super(p_275304_, p_275709_, p_275227_, p_275311_);
        this.keyExtractor = p_275566_;
    }

    protected IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> tag(TagKey<T> p_255730_) {
        TagBuilder tagbuilder = this.getOrCreateRawBuilder(p_255730_);
        return new IntrinsicHolderTagsProvider.IntrinsicTagAppender<>(tagbuilder, this.keyExtractor);
    }

    protected static class IntrinsicTagAppender<T> extends TagsProvider.TagAppender<T> {
        private final Function<T, ResourceKey<T>> keyExtractor;

        IntrinsicTagAppender(TagBuilder p_256108_, Function<T, ResourceKey<T>> p_256433_) {
            super(p_256108_);
            this.keyExtractor = p_256433_;
        }

        public IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addTag(TagKey<T> p_256311_) {
            super.addTag(p_256311_);
            return this;
        }

        public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(T p_256557_) {
            this.add(this.keyExtractor.apply(p_256557_));
            return this;
        }

        @SafeVarargs
        public final IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(T... p_255868_) {
            Stream.<T>of(p_255868_).map(this.keyExtractor).forEach(this::add);
            return this;
        }
    }
}