package net.minecraft.core;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public interface Registry<T> extends Keyable, HolderLookup.RegistryLookup<T>, IdMap<T> {
    @Override
    ResourceKey<? extends Registry<T>> key();

    default Codec<T> byNameCodec() {
        return this.referenceHolderWithLifecycle().flatComapMap(Holder.Reference::value, p_325680_ -> this.safeCastToReference(this.wrapAsHolder((T)p_325680_)));
    }

    default Codec<Holder<T>> holderByNameCodec() {
        return this.referenceHolderWithLifecycle().flatComapMap(p_325683_ -> (Holder<T>)p_325683_, this::safeCastToReference);
    }

    private Codec<Holder.Reference<T>> referenceHolderWithLifecycle() {
        Codec<Holder.Reference<T>> codec = ResourceLocation.CODEC
            .comapFlatMap(
                p_358093_ -> this.get(p_358093_)
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + this.key() + ": " + p_358093_)),
                p_325675_ -> p_325675_.key().location()
            );
        return ExtraCodecs.overrideLifecycle(codec, p_325682_ -> this.registrationInfo(p_325682_.key()).map(RegistrationInfo::lifecycle).orElse(Lifecycle.experimental()));
    }

    private DataResult<Holder.Reference<T>> safeCastToReference(Holder<T> p_329506_) {
        return p_329506_ instanceof Holder.Reference<T> reference
            ? DataResult.success(reference)
            : DataResult.error(() -> "Unregistered holder in " + this.key() + ": " + p_329506_);
    }

    @Override
    default <U> Stream<U> keys(DynamicOps<U> p_123030_) {
        return this.keySet().stream().map(p_235784_ -> p_123030_.createString(p_235784_.toString()));
    }

    @Nullable
    ResourceLocation getKey(T p_123006_);

    Optional<ResourceKey<T>> getResourceKey(T p_123008_);

    @Override
    int getId(@Nullable T p_122977_);

    @Nullable
    T getValue(@Nullable ResourceKey<T> p_362147_);

    @Nullable
    T getValue(@Nullable ResourceLocation p_364352_);

    Optional<RegistrationInfo> registrationInfo(ResourceKey<T> p_333179_);

    default Optional<T> getOptional(@Nullable ResourceLocation p_123007_) {
        return Optional.ofNullable(this.getValue(p_123007_));
    }

    default Optional<T> getOptional(@Nullable ResourceKey<T> p_123010_) {
        return Optional.ofNullable(this.getValue(p_123010_));
    }

    Optional<Holder.Reference<T>> getAny();

    default T getValueOrThrow(ResourceKey<T> p_367641_) {
        T t = this.getValue(p_367641_);
        if (t == null) {
            throw new IllegalStateException("Missing key in " + this.key() + ": " + p_367641_);
        } else {
            return t;
        }
    }

    Set<ResourceLocation> keySet();

    Set<Entry<ResourceKey<T>, T>> entrySet();

    Set<ResourceKey<T>> registryKeySet();

    Optional<Holder.Reference<T>> getRandom(RandomSource p_235781_);

    default Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    boolean containsKey(ResourceLocation p_123011_);

    boolean containsKey(ResourceKey<T> p_175475_);

    static <T> T register(Registry<? super T> p_122962_, String p_122963_, T p_122964_) {
        return register(p_122962_, ResourceLocation.parse(p_122963_), p_122964_);
    }

    static <V, T extends V> T register(Registry<V> p_122966_, ResourceLocation p_122967_, T p_122968_) {
        return register(p_122966_, ResourceKey.create(p_122966_.key(), p_122967_), p_122968_);
    }

    static <V, T extends V> T register(Registry<V> p_194580_, ResourceKey<V> p_194581_, T p_194582_) {
        ((WritableRegistry)p_194580_).register(p_194581_, (V)p_194582_, RegistrationInfo.BUILT_IN);
        return p_194582_;
    }

    static <T> Holder.Reference<T> registerForHolder(Registry<T> p_263347_, ResourceKey<T> p_263355_, T p_263428_) {
        return ((WritableRegistry)p_263347_).register(p_263355_, p_263428_, RegistrationInfo.BUILT_IN);
    }

    static <T> Holder.Reference<T> registerForHolder(Registry<T> p_263351_, ResourceLocation p_263363_, T p_263423_) {
        return registerForHolder(p_263351_, ResourceKey.create(p_263351_.key(), p_263363_), p_263423_);
    }

    Registry<T> freeze();

    Holder.Reference<T> createIntrusiveHolder(T p_206068_);

    Optional<Holder.Reference<T>> get(int p_367150_);

    Optional<Holder.Reference<T>> get(ResourceLocation p_370108_);

    Holder<T> wrapAsHolder(T p_263382_);

    default Iterable<Holder<T>> getTagOrEmpty(TagKey<T> p_206059_) {
        return DataFixUtils.orElse((Optional<Iterable>)(Optional)this.get(p_206059_), List.<T>of());
    }

    default Optional<Holder<T>> getRandomElementOf(TagKey<T> p_332242_, RandomSource p_335036_) {
        return this.get(p_332242_).flatMap(p_325677_ -> p_325677_.getRandomElement(p_335036_));
    }

    Stream<HolderSet.Named<T>> getTags();

    default IdMap<Holder<T>> asHolderIdMap() {
        return new IdMap<Holder<T>>() {
            public int getId(Holder<T> p_259992_) {
                return Registry.this.getId(p_259992_.value());
            }

            @Nullable
            public Holder<T> byId(int p_259972_) {
                return (Holder<T>)Registry.this.get(p_259972_).orElse(null);
            }

            @Override
            public int size() {
                return Registry.this.size();
            }

            @Override
            public Iterator<Holder<T>> iterator() {
                return Registry.this.listElements().map(p_260061_ -> (Holder<T>)p_260061_).iterator();
            }
        };
    }

    Registry.PendingTags<T> prepareTagReload(TagLoader.LoadResult<T> p_364537_);

    public interface PendingTags<T> {
        ResourceKey<? extends Registry<? extends T>> key();

        HolderLookup.RegistryLookup<T> lookup();

        void apply();

        int size();
    }
}
