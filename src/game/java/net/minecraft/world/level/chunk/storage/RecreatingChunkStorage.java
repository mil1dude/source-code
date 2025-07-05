package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.io.FileUtils;

public class RecreatingChunkStorage extends ChunkStorage {
    private final IOWorker writeWorker;
    private final Path writeFolder;

    public RecreatingChunkStorage(
        RegionStorageInfo p_335619_, Path p_328786_, RegionStorageInfo p_335564_, Path p_329291_, DataFixer p_334361_, boolean p_329095_
    ) {
        super(p_335619_, p_328786_, p_334361_, p_329095_);
        this.writeFolder = p_329291_;
        this.writeWorker = new IOWorker(p_335564_, p_329291_, p_329095_);
    }

    @Override
    public CompletableFuture<Void> write(ChunkPos p_330240_, Supplier<CompoundTag> p_366280_) {
        this.handleLegacyStructureIndex(p_330240_);
        return this.writeWorker.store(p_330240_, p_366280_);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.writeWorker.close();
        if (this.writeFolder.toFile().exists()) {
            FileUtils.deleteDirectory(this.writeFolder.toFile());
        }
    }
}