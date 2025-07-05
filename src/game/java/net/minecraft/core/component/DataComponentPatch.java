package net.minecraft.core.component;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

public final class DataComponentPatch {
    public static final DataComponentPatch EMPTY = new DataComponentPatch(Reference2ObjectMaps.emptyMap());
    public static final Codec<DataComponentPatch> CODEC = Codec.dispatchedMap(DataComponentPatch.PatchKey.CODEC, DataComponentPatch.PatchKey::valueCodec)
        .xmap(p_330428_ -> {
            if (p_330428_.isEmpty()) {
                return EMPTY;
            } else {
                Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2objectmap = new Reference2ObjectArrayMap<>(p_330428_.size());

                for (Entry<DataComponentPatch.PatchKey, ?> entry : p_330428_.entrySet()) {
                    DataComponentPatch.PatchKey datacomponentpatch$patchkey = entry.getKey();
                    if (datacomponentpatch$patchkey.removed()) {
                        reference2objectmap.put(datacomponentpatch$patchkey.type(), Optional.empty());
                    } else {
                        reference2objectmap.put(datacomponentpatch$patchkey.type(), Optional.of(entry.getValue()));
                    }
                }

                return new DataComponentPatch(reference2objectmap);
            }
        }, p_335950_ -> {
            Reference2ObjectMap<DataComponentPatch.PatchKey, Object> reference2objectmap = new Reference2ObjectArrayMap<>(p_335950_.map.size());

            for (Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(p_335950_.map)) {
                DataComponentType<?> datacomponenttype = entry.getKey();
                if (!datacomponenttype.isTransient()) {
                    Optional<?> optional = entry.getValue();
                    if (optional.isPresent()) {
                        reference2objectmap.put(new DataComponentPatch.PatchKey(datacomponenttype, false), optional.get());
                    } else {
                        reference2objectmap.put(new DataComponentPatch.PatchKey(datacomponenttype, true), Unit.INSTANCE);
                    }
                }
            }

            return (Reference2ObjectMap)reference2objectmap;
        });
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch>() {
        public DataComponentPatch decode(RegistryFriendlyByteBuf p_330298_) {
            int i = p_330298_.readVarInt();
            int j = p_330298_.readVarInt();
            if (i == 0 && j == 0) {
                return DataComponentPatch.EMPTY;
            } else {
                int k = i + j;
                Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2objectmap = new Reference2ObjectArrayMap<>(Math.min(k, 65536));

                for (int l = 0; l < i; l++) {
                    DataComponentType<?> datacomponenttype = DataComponentType.STREAM_CODEC.decode(p_330298_);
                    Object object = datacomponenttype.streamCodec().decode(p_330298_);
                    reference2objectmap.put(datacomponenttype, Optional.of(object));
                }

                for (int i1 = 0; i1 < j; i1++) {
                    DataComponentType<?> datacomponenttype1 = DataComponentType.STREAM_CODEC.decode(p_330298_);
                    reference2objectmap.put(datacomponenttype1, Optional.empty());
                }

                return new DataComponentPatch(reference2objectmap);
            }
        }

        public void encode(RegistryFriendlyByteBuf p_334360_, DataComponentPatch p_336144_) {
            if (p_336144_.isEmpty()) {
                p_334360_.writeVarInt(0);
                p_334360_.writeVarInt(0);
            } else {
                int i = 0;
                int j = 0;

                for (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(
                    p_336144_.map
                )) {
                    if (entry.getValue().isPresent()) {
                        i++;
                    } else {
                        j++;
                    }
                }

                p_334360_.writeVarInt(i);
                p_334360_.writeVarInt(j);

                for (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> entry1 : Reference2ObjectMaps.fastIterable(
                    p_336144_.map
                )) {
                    Optional<?> optional = entry1.getValue();
                    if (optional.isPresent()) {
                        DataComponentType<?> datacomponenttype = entry1.getKey();
                        DataComponentType.STREAM_CODEC.encode(p_334360_, datacomponenttype);
                        encodeComponent(p_334360_, datacomponenttype, optional.get());
                    }
                }

                for (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> entry2 : Reference2ObjectMaps.fastIterable(
                    p_336144_.map
                )) {
                    if (entry2.getValue().isEmpty()) {
                        DataComponentType<?> datacomponenttype1 = entry2.getKey();
                        DataComponentType.STREAM_CODEC.encode(p_334360_, datacomponenttype1);
                    }
                }
            }
        }

        private static <T> void encodeComponent(RegistryFriendlyByteBuf p_331641_, DataComponentType<T> p_335861_, Object p_335951_) {
            p_335861_.streamCodec().encode(p_331641_, (T)p_335951_);
        }
    };
    private static final String REMOVED_PREFIX = "!";
    final Reference2ObjectMap<DataComponentType<?>, Optional<?>> map;

    DataComponentPatch(Reference2ObjectMap<DataComponentType<?>, Optional<?>> p_329783_) {
        this.map = p_329783_;
    }

    public static DataComponentPatch.Builder builder() {
        return new DataComponentPatch.Builder();
    }

    @Nullable
    public <T> Optional<? extends T> get(DataComponentType<? extends T> p_330742_) {
        return (Optional<? extends T>)this.map.get(p_330742_);
    }

    public Set<Entry<DataComponentType<?>, Optional<?>>> entrySet() {
        return this.map.entrySet();
    }

    public int size() {
        return this.map.size();
    }

    public DataComponentPatch forget(Predicate<DataComponentType<?>> p_333810_) {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2objectmap = new Reference2ObjectArrayMap<>(this.map);
            reference2objectmap.keySet().removeIf(p_333810_);
            return reference2objectmap.isEmpty() ? EMPTY : new DataComponentPatch(reference2objectmap);
        }
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public DataComponentPatch.SplitResult split() {
        if (this.isEmpty()) {
            return DataComponentPatch.SplitResult.EMPTY;
        } else {
            DataComponentMap.Builder datacomponentmap$builder = DataComponentMap.builder();
            Set<DataComponentType<?>> set = Sets.newIdentityHashSet();
            this.map.forEach((p_336136_, p_328765_) -> {
                if (p_328765_.isPresent()) {
                    datacomponentmap$builder.setUnchecked((DataComponentType<?>)p_336136_, p_328765_.get());
                } else {
                    set.add((DataComponentType<?>)p_336136_);
                }
            });
            return new DataComponentPatch.SplitResult(datacomponentmap$builder.build(), set);
        }
    }

    @Override
    public boolean equals(Object p_334345_) {
        if (this == p_334345_) {
            return true;
        } else {
            if (p_334345_ instanceof DataComponentPatch datacomponentpatch && this.map.equals(datacomponentpatch.map)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public String toString() {
        return toString(this.map);
    }

    static String toString(Reference2ObjectMap<DataComponentType<?>, Optional<?>> p_335670_) {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append('{');
        boolean flag = true;

        for (Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(p_335670_)) {
            if (flag) {
                flag = false;
            } else {
                stringbuilder.append(", ");
            }

            Optional<?> optional = entry.getValue();
            if (optional.isPresent()) {
                stringbuilder.append(entry.getKey());
                stringbuilder.append("=>");
                stringbuilder.append(optional.get());
            } else {
                stringbuilder.append("!");
                stringbuilder.append(entry.getKey());
            }
        }

        stringbuilder.append('}');
        return stringbuilder.toString();
    }

    public static class Builder {
        private final Reference2ObjectMap<DataComponentType<?>, Optional<?>> map = new Reference2ObjectArrayMap<>();

        Builder() {
        }

        public <T> DataComponentPatch.Builder set(DataComponentType<T> p_329935_, T p_331578_) {
            this.map.put(p_329935_, Optional.of(p_331578_));
            return this;
        }

        public <T> DataComponentPatch.Builder remove(DataComponentType<T> p_329018_) {
            this.map.put(p_329018_, Optional.empty());
            return this;
        }

        public <T> DataComponentPatch.Builder set(TypedDataComponent<T> p_331095_) {
            return this.set(p_331095_.type(), p_331095_.value());
        }

        public DataComponentPatch build() {
            return this.map.isEmpty() ? DataComponentPatch.EMPTY : new DataComponentPatch(this.map);
        }
    }

    static record PatchKey(DataComponentType<?> type, boolean removed) {
        public static final Codec<DataComponentPatch.PatchKey> CODEC = Codec.STRING
            .flatXmap(
                p_330758_ -> {
                    boolean flag = p_330758_.startsWith("!");
                    if (flag) {
                        p_330758_ = p_330758_.substring("!".length());
                    }

                    ResourceLocation resourcelocation = ResourceLocation.tryParse(p_330758_);
                    DataComponentType<?> datacomponenttype = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(resourcelocation);
                    if (datacomponenttype == null) {
                        return DataResult.error(() -> "No component with type: '" + resourcelocation + "'");
                    } else {
                        return datacomponenttype.isTransient()
                            ? DataResult.error(() -> "'" + resourcelocation + "' is not a persistent component")
                            : DataResult.success(new DataComponentPatch.PatchKey(datacomponenttype, flag));
                    }
                },
                p_329482_ -> {
                    DataComponentType<?> datacomponenttype = p_329482_.type();
                    ResourceLocation resourcelocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(datacomponenttype);
                    return resourcelocation == null
                        ? DataResult.error(() -> "Unregistered component: " + datacomponenttype)
                        : DataResult.success(p_329482_.removed() ? "!" + resourcelocation : resourcelocation.toString());
                }
            );

        public Codec<?> valueCodec() {
            return this.removed ? Codec.EMPTY.codec() : this.type.codecOrThrow();
        }
    }

    public static record SplitResult(DataComponentMap added, Set<DataComponentType<?>> removed) {
        public static final DataComponentPatch.SplitResult EMPTY = new DataComponentPatch.SplitResult(DataComponentMap.EMPTY, Set.of());
    }
}
