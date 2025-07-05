package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

public record ItemWrittenBookPredicate(
    Optional<CollectionPredicate<Filterable<Component>, ItemWrittenBookPredicate.PagePredicate>> pages,
    Optional<String> author,
    Optional<String> title,
    MinMaxBounds.Ints generation,
    Optional<Boolean> resolved
) implements SingleComponentItemPredicate<WrittenBookContent> {
    public static final Codec<ItemWrittenBookPredicate> CODEC = RecordCodecBuilder.create(
        p_330132_ -> p_330132_.group(
                    CollectionPredicate.<Filterable<Component>, ItemWrittenBookPredicate.PagePredicate>codec(
                            ItemWrittenBookPredicate.PagePredicate.CODEC
                        )
                        .optionalFieldOf("pages")
                        .forGetter(ItemWrittenBookPredicate::pages),
                    Codec.STRING.optionalFieldOf("author").forGetter(ItemWrittenBookPredicate::author),
                    Codec.STRING.optionalFieldOf("title").forGetter(ItemWrittenBookPredicate::title),
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("generation", MinMaxBounds.Ints.ANY).forGetter(ItemWrittenBookPredicate::generation),
                    Codec.BOOL.optionalFieldOf("resolved").forGetter(ItemWrittenBookPredicate::resolved)
                )
                .apply(p_330132_, ItemWrittenBookPredicate::new)
    );

    @Override
    public DataComponentType<WrittenBookContent> componentType() {
        return DataComponents.WRITTEN_BOOK_CONTENT;
    }

    public boolean matches(ItemStack p_336266_, WrittenBookContent p_336372_) {
        if (this.author.isPresent() && !this.author.get().equals(p_336372_.author())) {
            return false;
        } else if (this.title.isPresent() && !this.title.get().equals(p_336372_.title().raw())) {
            return false;
        } else if (!this.generation.matches(p_336372_.generation())) {
            return false;
        } else {
            return this.resolved.isPresent() && this.resolved.get() != p_336372_.resolved()
                ? false
                : !this.pages.isPresent() || this.pages.get().test(p_336372_.pages());
        }
    }

    public static record PagePredicate(Component contents) implements Predicate<Filterable<Component>> {
        public static final Codec<ItemWrittenBookPredicate.PagePredicate> CODEC = ComponentSerialization.CODEC
            .xmap(ItemWrittenBookPredicate.PagePredicate::new, ItemWrittenBookPredicate.PagePredicate::contents);

        public boolean test(Filterable<Component> p_327692_) {
            return p_327692_.raw().equals(this.contents);
        }
    }
}