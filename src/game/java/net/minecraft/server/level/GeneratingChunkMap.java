package net.minecraft.server.level;

import java.util.concurrent.CompletableFuture;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;

public interface GeneratingChunkMap {
    GenerationChunkHolder acquireGeneration(long p_343344_);

    void releaseGeneration(GenerationChunkHolder p_344409_);

    CompletableFuture<ChunkAccess> applyStep(GenerationChunkHolder p_344300_, ChunkStep p_343131_, StaticCache2D<GenerationChunkHolder> p_343029_);

    ChunkGenerationTask scheduleGenerationTask(ChunkStatus p_345220_, ChunkPos p_342114_);

    void runGenerationTasks();
}