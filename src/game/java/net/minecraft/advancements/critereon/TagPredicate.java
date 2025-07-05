package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public record TagPredicate<T>(TagKey<T> tag, boolean expected) {
    public static <T> Codec<TagPredicate<T>> codec(ResourceKey<? extends Registry<T>> p_301303_) {
        return RecordCodecBuilder.create(
            p_299938_ -> p_299938_.group(
                        TagKey.codec(p_301303_).fieldOf("id").forGetter(TagPredicate::tag),
                        Codec.BOOL.fieldOf("expected").forGetter(TagPredicate::expected)
                    )
                    .apply(p_299938_, TagPredicate::new)
        );
    }

    public static <T> TagPredicate<T> is(TagKey<T> p_270668_) {
        return new TagPredicate<>(p_270668_, true);
    }

    public static <T> TagPredicate<T> isNot(TagKey<T> p_270264_) {
        return new TagPredicate<>(p_270264_, false);
    }

    public boolean matches(Holder<T> p_270125_) {
        return p_270125_.is(this.tag) == this.expected;
    }
}