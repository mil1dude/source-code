package net.minecraft.server.level.progress;

import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public interface ChunkProgressListener {
    void updateSpawnPos(ChunkPos p_9617_);

    void onStatusChange(ChunkPos p_9618_, @Nullable ChunkStatus p_328329_);

    void start();

    void stop();

    static int calculateDiameter(int p_329991_) {
        return 2 * p_329991_ + 1;
    }
}