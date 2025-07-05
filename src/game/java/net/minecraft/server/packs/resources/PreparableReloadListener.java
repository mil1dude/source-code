package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface PreparableReloadListener {
    CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier p_10638_, ResourceManager p_10639_, Executor p_10642_, Executor p_10643_);

    default String getName() {
        return this.getClass().getSimpleName();
    }

    public interface PreparationBarrier {
        <T> CompletableFuture<T> wait(T p_10644_);
    }
}