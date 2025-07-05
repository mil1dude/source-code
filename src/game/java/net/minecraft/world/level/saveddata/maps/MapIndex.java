package net.minecraft.world.level.saveddata.maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class MapIndex extends SavedData {
    public static final String FILE_NAME = "idcounts";
    private final Object2IntMap<String> usedAuxIds = new Object2IntOpenHashMap<>();

    public static SavedData.Factory<MapIndex> factory() {
        return new SavedData.Factory<>(MapIndex::new, MapIndex::load, DataFixTypes.SAVED_DATA_MAP_INDEX);
    }

    public MapIndex() {
        this.usedAuxIds.defaultReturnValue(-1);
    }

    public static MapIndex load(CompoundTag p_164763_, HolderLookup.Provider p_334612_) {
        MapIndex mapindex = new MapIndex();

        for (String s : p_164763_.getAllKeys()) {
            if (p_164763_.contains(s, 99)) {
                mapindex.usedAuxIds.put(s, p_164763_.getInt(s));
            }
        }

        return mapindex;
    }

    @Override
    public CompoundTag save(CompoundTag p_77884_, HolderLookup.Provider p_334737_) {
        for (Entry<String> entry : this.usedAuxIds.object2IntEntrySet()) {
            p_77884_.putInt(entry.getKey(), entry.getIntValue());
        }

        return p_77884_;
    }

    public MapId getFreeAuxValueForMap() {
        int i = this.usedAuxIds.getInt("map") + 1;
        this.usedAuxIds.put("map", i);
        this.setDirty();
        return new MapId(i);
    }
}