package net.minecraft.client.resources.server;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface PackReloadConfig {
    void scheduleReload(PackReloadConfig.Callbacks p_312695_);

    @OnlyIn(Dist.CLIENT)
    public interface Callbacks {
        void onSuccess();

        void onFailure(boolean p_313000_);

        List<PackReloadConfig.IdAndPath> packsToLoad();
    }

    @OnlyIn(Dist.CLIENT)
    public static record IdAndPath(UUID id, Path path) {
    }
}