package net.minecraft.world.level.levelgen.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class StructureFeatureIndexSavedData extends SavedData {
    private static final String TAG_REMAINING_INDEXES = "Remaining";
    private static final String TAG_All_INDEXES = "All";
    private final LongSet all;
    private final LongSet remaining;

    public static SavedData.Factory<StructureFeatureIndexSavedData> factory() {
        return new SavedData.Factory<>(
            StructureFeatureIndexSavedData::new, StructureFeatureIndexSavedData::load, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES
        );
    }

    private StructureFeatureIndexSavedData(LongSet p_163532_, LongSet p_163533_) {
        this.all = p_163532_;
        this.remaining = p_163533_;
    }

    public StructureFeatureIndexSavedData() {
        this(new LongOpenHashSet(), new LongOpenHashSet());
    }

    public static StructureFeatureIndexSavedData load(CompoundTag p_163535_, HolderLookup.Provider p_333516_) {
        return new StructureFeatureIndexSavedData(new LongOpenHashSet(p_163535_.getLongArray("All")), new LongOpenHashSet(p_163535_.getLongArray("Remaining")));
    }

    @Override
    public CompoundTag save(CompoundTag p_73372_, HolderLookup.Provider p_329815_) {
        p_73372_.putLongArray("All", this.all.toLongArray());
        p_73372_.putLongArray("Remaining", this.remaining.toLongArray());
        return p_73372_;
    }

    public void addIndex(long p_73366_) {
        this.all.add(p_73366_);
        this.remaining.add(p_73366_);
        this.setDirty();
    }

    public boolean hasStartIndex(long p_73370_) {
        return this.all.contains(p_73370_);
    }

    public boolean hasUnhandledIndex(long p_73374_) {
        return this.remaining.contains(p_73374_);
    }

    public void removeIndex(long p_73376_) {
        if (this.remaining.remove(p_73376_)) {
            this.setDirty();
        }
    }

    public LongSet getAll() {
        return this.all;
    }
}