package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class PlayerDataStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final File playerDir;
    protected final DataFixer fixerUpper;
    private static final DateTimeFormatter FORMATTER = FileNameDateFormatter.create();

    public PlayerDataStorage(LevelStorageSource.LevelStorageAccess p_78430_, DataFixer p_78431_) {
        this.fixerUpper = p_78431_;
        this.playerDir = p_78430_.getLevelPath(LevelResource.PLAYER_DATA_DIR).toFile();
        this.playerDir.mkdirs();
    }

    public void save(Player p_78434_) {
        try {
            CompoundTag compoundtag = p_78434_.saveWithoutId(new CompoundTag());
            Path path = this.playerDir.toPath();
            Path path1 = Files.createTempFile(path, p_78434_.getStringUUID() + "-", ".dat");
            NbtIo.writeCompressed(compoundtag, path1);
            Path path2 = path.resolve(p_78434_.getStringUUID() + ".dat");
            Path path3 = path.resolve(p_78434_.getStringUUID() + ".dat_old");
            Util.safeReplaceFile(path2, path1, path3);
        } catch (Exception exception) {
            LOGGER.warn("Failed to save player data for {}", p_78434_.getName().getString());
        }
    }

    private void backup(Player p_331737_, String p_336359_) {
        Path path = this.playerDir.toPath();
        Path path1 = path.resolve(p_331737_.getStringUUID() + p_336359_);
        Path path2 = path.resolve(p_331737_.getStringUUID() + "_corrupted_" + LocalDateTime.now().format(FORMATTER) + p_336359_);
        if (Files.isRegularFile(path1)) {
            try {
                Files.copy(path1, path2, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } catch (Exception exception) {
                LOGGER.warn("Failed to copy the player.dat file for {}", p_331737_.getName().getString(), exception);
            }
        }
    }

    private Optional<CompoundTag> load(Player p_329651_, String p_330353_) {
        File file1 = new File(this.playerDir, p_329651_.getStringUUID() + p_330353_);
        if (file1.exists() && file1.isFile()) {
            try {
                return Optional.of(NbtIo.readCompressed(file1.toPath(), NbtAccounter.unlimitedHeap()));
            } catch (Exception exception) {
                LOGGER.warn("Failed to load player data for {}", p_329651_.getName().getString());
            }
        }

        return Optional.empty();
    }

    public Optional<CompoundTag> load(Player p_78436_) {
        Optional<CompoundTag> optional = this.load(p_78436_, ".dat");
        if (optional.isEmpty()) {
            this.backup(p_78436_, ".dat");
        }

        return optional.or(() -> this.load(p_78436_, ".dat_old")).map(p_328937_ -> {
            int i = NbtUtils.getDataVersion(p_328937_, -1);
            p_328937_ = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, p_328937_, i);
            p_78436_.load(p_328937_);
            return p_328937_;
        });
    }
}