package net.minecraft.util.parsing.packrat;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Objects;
import javax.annotation.Nullable;

public final class Scope {
    private final Object2ObjectMap<Atom<?>, Object> values = new Object2ObjectArrayMap<>();

    public <T> void put(Atom<T> p_329036_, @Nullable T p_328259_) {
        this.values.put(p_329036_, p_328259_);
    }

    @Nullable
    public <T> T get(Atom<T> p_331470_) {
        return (T)this.values.get(p_331470_);
    }

    public <T> T getOrThrow(Atom<T> p_332933_) {
        return Objects.requireNonNull(this.get(p_332933_));
    }

    public <T> T getOrDefault(Atom<T> p_335515_, T p_333340_) {
        return Objects.requireNonNullElse(this.get(p_335515_), p_333340_);
    }

    @Nullable
    @SafeVarargs
    public final <T> T getAny(Atom<T>... p_331175_) {
        for (Atom<T> atom : p_331175_) {
            T t = this.get(atom);
            if (t != null) {
                return t;
            }
        }

        return null;
    }

    @SafeVarargs
    public final <T> T getAnyOrThrow(Atom<T>... p_330748_) {
        return Objects.requireNonNull(this.getAny(p_330748_));
    }

    @Override
    public String toString() {
        return this.values.toString();
    }

    public void putAll(Scope p_334073_) {
        this.values.putAll(p_334073_.values);
    }

    @Override
    public boolean equals(Object p_331272_) {
        if (this == p_331272_) {
            return true;
        } else {
            return p_331272_ instanceof Scope scope ? this.values.equals(scope.values) : false;
        }
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }
}