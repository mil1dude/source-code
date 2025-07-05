package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.function.Predicate;

public interface CollectionCountsPredicate<T, P extends Predicate<T>> extends Predicate<Iterable<T>> {
    List<CollectionCountsPredicate.Entry<T, P>> unpack();

    static <T, P extends Predicate<T>> Codec<CollectionCountsPredicate<T, P>> codec(Codec<P> p_335836_) {
        return CollectionCountsPredicate.Entry.<T, P>codec(p_335836_)
            .listOf()
            .xmap(CollectionCountsPredicate::of, CollectionCountsPredicate::unpack);
    }

    @SafeVarargs
    static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> of(CollectionCountsPredicate.Entry<T, P>... p_332496_) {
        return of(List.of(p_332496_));
    }

    static <T, P extends Predicate<T>> CollectionCountsPredicate<T, P> of(List<CollectionCountsPredicate.Entry<T, P>> p_334665_) {
        return (CollectionCountsPredicate<T, P>)(switch (p_334665_.size()) {
            case 0 -> new CollectionCountsPredicate.Zero();
            case 1 -> new CollectionCountsPredicate.Single(p_334665_.getFirst());
            default -> new CollectionCountsPredicate.Multiple(p_334665_);
        });
    }

    public static record Entry<T, P extends Predicate<T>>(P test, MinMaxBounds.Ints count) {
        public static <T, P extends Predicate<T>> Codec<CollectionCountsPredicate.Entry<T, P>> codec(Codec<P> p_334145_) {
            return RecordCodecBuilder.create(
                p_334567_ -> p_334567_.group(
                            p_334145_.fieldOf("test").forGetter(CollectionCountsPredicate.Entry::test),
                            MinMaxBounds.Ints.CODEC.fieldOf("count").forGetter(CollectionCountsPredicate.Entry::count)
                        )
                        .apply(p_334567_, CollectionCountsPredicate.Entry::new)
            );
        }

        public boolean test(Iterable<T> p_329726_) {
            int i = 0;

            for (T t : p_329726_) {
                if (this.test.test(t)) {
                    i++;
                }
            }

            return this.count.matches(i);
        }
    }

    public static record Multiple<T, P extends Predicate<T>>(List<CollectionCountsPredicate.Entry<T, P>> entries) implements CollectionCountsPredicate<T, P> {
        public boolean test(Iterable<T> p_329412_) {
            for (CollectionCountsPredicate.Entry<T, P> entry : this.entries) {
                if (!entry.test(p_329412_)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public List<CollectionCountsPredicate.Entry<T, P>> unpack() {
            return this.entries;
        }
    }

    public static record Single<T, P extends Predicate<T>>(CollectionCountsPredicate.Entry<T, P> entry) implements CollectionCountsPredicate<T, P> {
        public boolean test(Iterable<T> p_333879_) {
            return this.entry.test(p_333879_);
        }

        @Override
        public List<CollectionCountsPredicate.Entry<T, P>> unpack() {
            return List.of(this.entry);
        }
    }

    public static class Zero<T, P extends Predicate<T>> implements CollectionCountsPredicate<T, P> {
        public boolean test(Iterable<T> p_329157_) {
            return true;
        }

        @Override
        public List<CollectionCountsPredicate.Entry<T, P>> unpack() {
            return List.of();
        }
    }
}