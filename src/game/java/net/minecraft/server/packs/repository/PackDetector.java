package net.minecraft.server.packs.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;

public abstract class PackDetector<T> {
    private final DirectoryValidator validator;

    protected PackDetector(DirectoryValidator p_300595_) {
        this.validator = p_300595_;
    }

    @Nullable
    public T detectPackResources(Path p_298083_, List<ForbiddenSymlinkInfo> p_297322_) throws IOException {
        Path path = p_298083_;

        BasicFileAttributes basicfileattributes;
        try {
            basicfileattributes = Files.readAttributes(p_298083_, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException nosuchfileexception) {
            return null;
        }

        if (basicfileattributes.isSymbolicLink()) {
            this.validator.validateSymlink(p_298083_, p_297322_);
            if (!p_297322_.isEmpty()) {
                return null;
            }

            path = Files.readSymbolicLink(p_298083_);
            basicfileattributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        }

        if (basicfileattributes.isDirectory()) {
            this.validator.validateKnownDirectory(path, p_297322_);
            if (!p_297322_.isEmpty()) {
                return null;
            } else {
                return !Files.isRegularFile(path.resolve("pack.mcmeta")) ? null : this.createDirectoryPack(path);
            }
        } else {
            return basicfileattributes.isRegularFile() && path.getFileName().toString().endsWith(".zip") ? this.createZipPack(path) : null;
        }
    }

    @Nullable
    protected abstract T createZipPack(Path p_297649_) throws IOException;

    @Nullable
    protected abstract T createDirectoryPack(Path p_298942_) throws IOException;
}