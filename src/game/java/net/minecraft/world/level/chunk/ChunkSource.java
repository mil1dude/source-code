package net.minecraft.world.level.chunk;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;

public abstract class ChunkSource implements LightChunkGetter, AutoCloseable {
    @Nullable
    public LevelChunk getChunk(int p_62228_, int p_62229_, boolean p_62230_) {
        return (LevelChunk)this.getChunk(p_62228_, p_62229_, ChunkStatus.FULL, p_62230_);
    }

    @Nullable
    public LevelChunk getChunkNow(int p_62221_, int p_62222_) {
        return this.getChunk(p_62221_, p_62222_, false);
    }

    @Nullable
    @Override
    public LightChunk getChunkForLighting(int p_62241_, int p_62242_) {
        return this.getChunk(p_62241_, p_62242_, ChunkStatus.EMPTY, false);
    }

    public boolean hasChunk(int p_62238_, int p_62239_) {
        return this.getChunk(p_62238_, p_62239_, ChunkStatus.FULL, false) != null;
    }

    @Nullable
    public abstract ChunkAccess getChunk(int p_62223_, int p_62224_, ChunkStatus p_333812_, boolean p_62226_);

    public abstract void tick(BooleanSupplier p_202162_, boolean p_202163_);

    public void onSectionEmptinessChanged(int p_368741_, int p_361584_, int p_361455_, boolean p_62236_) {
    }

    public abstract String gatherStats();

    public abstract int getLoadedChunksCount();

    @Override
    public void close() throws IOException {
    }

    public abstract LevelLightEngine getLightEngine();

    public void setSpawnSettings(boolean p_363305_) {
    }

    public void updateChunkForced(ChunkPos p_62233_, boolean p_62234_) {
    }
}