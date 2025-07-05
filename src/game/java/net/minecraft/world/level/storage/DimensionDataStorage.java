package net.minecraft.world.level.storage;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class DimensionDataStorage implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, Optional<SavedData>> cache = new HashMap<>();
    private final DataFixer fixerUpper;
    private final HolderLookup.Provider registries;
    private final Path dataFolder;
    private CompletableFuture<?> pendingWriteFuture = CompletableFuture.completedFuture(null);

    public DimensionDataStorage(Path p_364133_, DataFixer p_78150_, HolderLookup.Provider p_336063_) {
        this.fixerUpper = p_78150_;
        this.dataFolder = p_364133_;
        this.registries = p_336063_;
    }

    private Path getDataFile(String p_78157_) {
        return this.dataFolder.resolve(p_78157_ + ".dat");
    }

    public <T extends SavedData> T computeIfAbsent(SavedData.Factory<T> p_297495_, String p_164864_) {
        T t = this.get(p_297495_, p_164864_);
        if (t != null) {
            return t;
        } else {
            T t1 = (T)p_297495_.constructor().get();
            this.set(p_164864_, t1);
            return t1;
        }
    }

    @Nullable
    public <T extends SavedData> T get(SavedData.Factory<T> p_297465_, String p_164860_) {
        Optional<SavedData> optional = this.cache.get(p_164860_);
        if (optional == null) {
            optional = Optional.ofNullable(this.readSavedData(p_297465_.deserializer(), p_297465_.type(), p_164860_));
            this.cache.put(p_164860_, optional);
        }

        return (T)optional.orElse(null);
    }

    @Nullable
    private <T extends SavedData> T readSavedData(BiFunction<CompoundTag, HolderLookup.Provider, T> p_335409_, DataFixTypes p_300231_, String p_164870_) {
        try {
            Path path = this.getDataFile(p_164870_);
            if (Files.exists(path)) {
                CompoundTag compoundtag = this.readTagFromDisk(p_164870_, p_300231_, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
                return p_335409_.apply(compoundtag.getCompound("data"), this.registries);
            }
        } catch (Exception exception) {
            LOGGER.error("Error loading saved data: {}", p_164870_, exception);
        }

        return null;
    }

    public void set(String p_164856_, SavedData p_164857_) {
        this.cache.put(p_164856_, Optional.of(p_164857_));
        p_164857_.setDirty();
    }

    public CompoundTag readTagFromDisk(String p_78159_, DataFixTypes p_301060_, int p_78160_) throws IOException {
        CompoundTag compoundtag1;
        try (
            InputStream inputstream = Files.newInputStream(this.getDataFile(p_78159_));
            PushbackInputStream pushbackinputstream = new PushbackInputStream(new FastBufferedInputStream(inputstream), 2);
        ) {
            CompoundTag compoundtag;
            if (this.isGzip(pushbackinputstream)) {
                compoundtag = NbtIo.readCompressed(pushbackinputstream, NbtAccounter.unlimitedHeap());
            } else {
                try (DataInputStream datainputstream = new DataInputStream(pushbackinputstream)) {
                    compoundtag = NbtIo.read(datainputstream);
                }
            }

            int i = NbtUtils.getDataVersion(compoundtag, 1343);
            compoundtag1 = p_301060_.update(this.fixerUpper, compoundtag, i, p_78160_);
        }

        return compoundtag1;
    }

    private boolean isGzip(PushbackInputStream p_78155_) throws IOException {
        byte[] abyte = new byte[2];
        boolean flag = false;
        int i = p_78155_.read(abyte, 0, 2);
        if (i == 2) {
            int j = (abyte[1] & 255) << 8 | abyte[0] & 255;
            if (j == 35615) {
                flag = true;
            }
        }

        if (i != 0) {
            p_78155_.unread(abyte, 0, i);
        }

        return flag;
    }

    public CompletableFuture<?> scheduleSave() {
        Map<Path, CompoundTag> map = this.collectDirtyTagsToSave();
        if (map.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        } else {
            int i = Util.maxAllowedExecutorThreads();
            int j = map.size();
            if (j > i) {
                this.pendingWriteFuture = this.pendingWriteFuture.thenCompose(p_375371_ -> {
                    List<CompletableFuture<?>> list = new ArrayList<>(i);
                    int k = Mth.positiveCeilDiv(j, i);

                    for (List<Entry<Path, CompoundTag>> list1 : Iterables.partition(map.entrySet(), k)) {
                        list.add(CompletableFuture.runAsync(() -> {
                            for (Entry<Path, CompoundTag> entry : list1) {
                                tryWrite(entry.getKey(), entry.getValue());
                            }
                        }, Util.ioPool()));
                    }

                    return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
                });
            } else {
                this.pendingWriteFuture = this.pendingWriteFuture
                    .thenCompose(
                        p_360653_ -> CompletableFuture.allOf(
                                map.entrySet()
                                    .stream()
                                    .map(p_375366_ -> CompletableFuture.runAsync(() -> tryWrite(p_375366_.getKey(), p_375366_.getValue()), Util.ioPool()))
                                    .toArray(CompletableFuture[]::new)
                            )
                    );
            }

            return this.pendingWriteFuture;
        }
    }

    private Map<Path, CompoundTag> collectDirtyTagsToSave() {
        Map<Path, CompoundTag> map = new Object2ObjectArrayMap<>();
        this.cache
            .forEach(
                (p_360648_, p_360649_) -> p_360649_.filter(SavedData::isDirty)
                        .ifPresent(p_360658_ -> map.put(this.getDataFile(p_360648_), p_360658_.save(this.registries)))
            );
        return map;
    }

    private static void tryWrite(Path p_377750_, CompoundTag p_378799_) {
        try {
            NbtIo.writeCompressed(p_378799_, p_377750_);
        } catch (IOException ioexception) {
            LOGGER.error("Could not save data to {}", p_377750_.getFileName(), ioexception);
        }
    }

    public void saveAndJoin() {
        this.scheduleSave().join();
    }

    @Override
    public void close() {
        this.saveAndJoin();
    }
}