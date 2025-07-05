package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;

public class SimpleRegionStorage implements AutoCloseable {
    private final IOWorker worker;
    private final DataFixer fixerUpper;
    private final DataFixTypes dataFixType;

    public SimpleRegionStorage(RegionStorageInfo p_327836_, Path p_328804_, DataFixer p_332309_, boolean p_335456_, DataFixTypes p_331426_) {
        this.fixerUpper = p_332309_;
        this.dataFixType = p_331426_;
        this.worker = new IOWorker(p_327836_, p_328804_, p_335456_);
    }

    public CompletableFuture<Optional<CompoundTag>> read(ChunkPos p_328805_) {
        return this.worker.loadAsync(p_328805_);
    }

    public CompletableFuture<Void> write(ChunkPos p_328507_, @Nullable CompoundTag p_328699_) {
        return this.worker.store(p_328507_, p_328699_);
    }

    public CompoundTag upgradeChunkTag(CompoundTag p_330988_, int p_328203_) {
        int i = NbtUtils.getDataVersion(p_330988_, p_328203_);
        return this.dataFixType.updateToCurrentVersion(this.fixerUpper, p_330988_, i);
    }

    public Dynamic<Tag> upgradeChunkTag(Dynamic<Tag> p_329521_, int p_334930_) {
        return this.dataFixType.updateToCurrentVersion(this.fixerUpper, p_329521_, p_334930_);
    }

    public CompletableFuture<Void> synchronize(boolean p_334675_) {
        return this.worker.synchronize(p_334675_);
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }

    public RegionStorageInfo storageInfo() {
        return this.worker.storageInfo();
    }
}