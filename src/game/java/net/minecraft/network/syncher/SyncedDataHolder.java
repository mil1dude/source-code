package net.minecraft.network.syncher;

import java.util.List;

public interface SyncedDataHolder {
    void onSyncedDataUpdated(EntityDataAccessor<?> p_332504_);

    void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> p_334582_);
}