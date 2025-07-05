package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public interface ResourceManagerReloadListener extends PreparableReloadListener {
    @Override
    default CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier p_10752_, ResourceManager p_10753_, Executor p_10756_, Executor p_10757_
    ) {
        return p_10752_.wait(Unit.INSTANCE).thenRunAsync(() -> {
            ProfilerFiller profilerfiller = Profiler.get();
            profilerfiller.push("listener");
            this.onResourceManagerReload(p_10753_);
            profilerfiller.pop();
        }, p_10757_);
    }

    void onResourceManagerReload(ResourceManager p_10758_);
}