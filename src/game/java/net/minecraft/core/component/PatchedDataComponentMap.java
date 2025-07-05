package net.minecraft.core.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public final class PatchedDataComponentMap implements DataComponentMap {
    private final DataComponentMap prototype;
    private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;
    private boolean copyOnWrite;

    public PatchedDataComponentMap(DataComponentMap p_331141_) {
        this(p_331141_, Reference2ObjectMaps.emptyMap(), true);
    }

    private PatchedDataComponentMap(DataComponentMap p_335089_, Reference2ObjectMap<DataComponentType<?>, Optional<?>> p_333211_, boolean p_334948_) {
        this.prototype = p_335089_;
        this.patch = p_333211_;
        this.copyOnWrite = p_334948_;
    }

    public static PatchedDataComponentMap fromPatch(DataComponentMap p_334311_, DataComponentPatch p_332061_) {
        if (isPatchSanitized(p_334311_, p_332061_.map)) {
            return new PatchedDataComponentMap(p_334311_, p_332061_.map, true);
        } else {
            PatchedDataComponentMap patcheddatacomponentmap = new PatchedDataComponentMap(p_334311_);
            patcheddatacomponentmap.applyPatch(p_332061_);
            return patcheddatacomponentmap;
        }
    }

    private static boolean isPatchSanitized(DataComponentMap p_331971_, Reference2ObjectMap<DataComponentType<?>, Optional<?>> p_332857_) {
        for (Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(p_332857_)) {
            Object object = p_331971_.get(entry.getKey());
            Optional<?> optional = entry.getValue();
            if (optional.isPresent() && optional.get().equals(object)) {
                return false;
            }

            if (optional.isEmpty() && object == null) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    @Override
    public <T> T get(DataComponentType<? extends T> p_331525_) {
        Optional<? extends T> optional = (Optional<? extends T>)this.patch.get(p_331525_);
        return (T)(optional != null ? optional.orElse(null) : this.prototype.get(p_331525_));
    }

    public boolean hasNonDefault(DataComponentType<?> p_376646_) {
        return this.patch.containsKey(p_376646_);
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> p_334181_, @Nullable T p_328828_) {
        this.ensureMapOwnership();
        T t = this.prototype.get((DataComponentType<? extends T>)p_334181_);
        Optional<T> optional;
        if (Objects.equals(p_328828_, t)) {
            optional = (Optional<T>)this.patch.remove(p_334181_);
        } else {
            optional = (Optional<T>)this.patch.put(p_334181_, Optional.ofNullable(p_328828_));
        }

        return optional != null ? optional.orElse(t) : t;
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> p_331496_) {
        this.ensureMapOwnership();
        T t = this.prototype.get(p_331496_);
        Optional<? extends T> optional;
        if (t != null) {
            optional = (Optional<? extends T>)this.patch.put(p_331496_, Optional.empty());
        } else {
            optional = (Optional<? extends T>)this.patch.remove(p_331496_);
        }

        return (T)(optional != null ? optional.orElse(null) : t);
    }

    public void applyPatch(DataComponentPatch p_329626_) {
        this.ensureMapOwnership();

        for (Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(p_329626_.map)) {
            this.applyPatch(entry.getKey(), entry.getValue());
        }
    }

    private void applyPatch(DataComponentType<?> p_327856_, Optional<?> p_331456_) {
        Object object = this.prototype.get(p_327856_);
        if (p_331456_.isPresent()) {
            if (p_331456_.get().equals(object)) {
                this.patch.remove(p_327856_);
            } else {
                this.patch.put(p_327856_, p_331456_);
            }
        } else if (object != null) {
            this.patch.put(p_327856_, Optional.empty());
        } else {
            this.patch.remove(p_327856_);
        }
    }

    public void restorePatch(DataComponentPatch p_331119_) {
        this.ensureMapOwnership();
        this.patch.clear();
        this.patch.putAll(p_331119_.map);
    }

    public void clearPatch() {
        this.ensureMapOwnership();
        this.patch.clear();
    }

    public void setAll(DataComponentMap p_336067_) {
        for (TypedDataComponent<?> typeddatacomponent : p_336067_) {
            typeddatacomponent.applyTo(this);
        }
    }

    private void ensureMapOwnership() {
        if (this.copyOnWrite) {
            this.patch = new Reference2ObjectArrayMap<>(this.patch);
            this.copyOnWrite = false;
        }
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        if (this.patch.isEmpty()) {
            return this.prototype.keySet();
        } else {
            Set<DataComponentType<?>> set = new ReferenceArraySet<>(this.prototype.keySet());

            for (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(
                this.patch
            )) {
                Optional<?> optional = entry.getValue();
                if (optional.isPresent()) {
                    set.add(entry.getKey());
                } else {
                    set.remove(entry.getKey());
                }
            }

            return set;
        }
    }

    @Override
    public Iterator<TypedDataComponent<?>> iterator() {
        if (this.patch.isEmpty()) {
            return this.prototype.iterator();
        } else {
            List<TypedDataComponent<?>> list = new ArrayList<>(this.patch.size() + this.prototype.size());

            for (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(
                this.patch
            )) {
                if (entry.getValue().isPresent()) {
                    list.add(TypedDataComponent.createUnchecked(entry.getKey(), entry.getValue().get()));
                }
            }

            for (TypedDataComponent<?> typeddatacomponent : this.prototype) {
                if (!this.patch.containsKey(typeddatacomponent.type())) {
                    list.add(typeddatacomponent);
                }
            }

            return list.iterator();
        }
    }

    @Override
    public int size() {
        int i = this.prototype.size();

        for (it.unimi.dsi.fastutil.objects.Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(
            this.patch
        )) {
            boolean flag = entry.getValue().isPresent();
            boolean flag1 = this.prototype.has(entry.getKey());
            if (flag != flag1) {
                i += flag ? 1 : -1;
            }
        }

        return i;
    }

    public DataComponentPatch asPatch() {
        if (this.patch.isEmpty()) {
            return DataComponentPatch.EMPTY;
        } else {
            this.copyOnWrite = true;
            return new DataComponentPatch(this.patch);
        }
    }

    public PatchedDataComponentMap copy() {
        this.copyOnWrite = true;
        return new PatchedDataComponentMap(this.prototype, this.patch, true);
    }

    public DataComponentMap toImmutableMap() {
        return (DataComponentMap)(this.patch.isEmpty() ? this.prototype : this.copy());
    }

    @Override
    public boolean equals(Object p_335823_) {
        if (this == p_335823_) {
            return true;
        } else {
            if (p_335823_ instanceof PatchedDataComponentMap patcheddatacomponentmap
                && this.prototype.equals(patcheddatacomponentmap.prototype)
                && this.patch.equals(patcheddatacomponentmap.patch)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.prototype.hashCode() + this.patch.hashCode() * 31;
    }

    @Override
    public String toString() {
        return "{" + this.stream().map(TypedDataComponent::toString).collect(Collectors.joining(", ")) + "}";
    }
}