package net.minecraft.core;

import javax.annotation.Nullable;

public interface IdMap<T> extends Iterable<T> {
    int DEFAULT = -1;

    int getId(T p_122652_);

    @Nullable
    T byId(int p_122651_);

    default T byIdOrThrow(int p_200958_) {
        T t = this.byId(p_200958_);
        if (t == null) {
            throw new IllegalArgumentException("No value with id " + p_200958_);
        } else {
            return t;
        }
    }

    default int getIdOrThrow(T p_329088_) {
        int i = this.getId(p_329088_);
        if (i == -1) {
            throw new IllegalArgumentException("Can't find id for '" + p_329088_ + "' in map " + this);
        } else {
            return i;
        }
    }

    int size();
}