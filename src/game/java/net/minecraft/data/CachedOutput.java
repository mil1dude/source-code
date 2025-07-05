package net.minecraft.data;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.FileUtil;

public interface CachedOutput {
    CachedOutput NO_CACHE = (p_308470_, p_308471_, p_308472_) -> {
        FileUtil.createDirectoriesSafe(p_308470_.getParent());
        Files.write(p_308470_, p_308471_);
    };

    void writeIfNeeded(Path p_236022_, byte[] p_236023_, HashCode p_236024_) throws IOException;
}