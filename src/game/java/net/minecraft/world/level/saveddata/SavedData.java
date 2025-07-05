package net.minecraft.world.level.saveddata;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;

public abstract class SavedData {
    private boolean dirty;

    public abstract CompoundTag save(CompoundTag p_77763_, HolderLookup.Provider p_334349_);

    public void setDirty() {
        this.setDirty(true);
    }

    public void setDirty(boolean p_77761_) {
        this.dirty = p_77761_;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public CompoundTag save(HolderLookup.Provider p_336018_) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.put("data", this.save(new CompoundTag(), p_336018_));
        NbtUtils.addCurrentDataVersion(compoundtag);
        this.setDirty(false);
        return compoundtag;
    }

    public static record Factory<T extends SavedData>(
        Supplier<T> constructor, BiFunction<CompoundTag, HolderLookup.Provider, T> deserializer, DataFixTypes type
    ) {
    }
}