package net.minecraft.world.level.chunk.status;

import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;

@FunctionalInterface
public interface ChunkStatusTask {
    CompletableFuture<ChunkAccess> doWork(
        WorldGenContext p_342387_, ChunkStep p_344134_, StaticCache2D<GenerationChunkHolder> p_345304_, ChunkAccess p_344075_
    );
}