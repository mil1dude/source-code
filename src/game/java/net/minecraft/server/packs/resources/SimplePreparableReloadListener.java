package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class SimplePreparableReloadListener<T> implements PreparableReloadListener {
    @Override
    public final CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier p_10780_, ResourceManager p_10781_, Executor p_10784_, Executor p_10785_
    ) {
        return CompletableFuture.<T>supplyAsync(() -> this.prepare(p_10781_, Profiler.get()), p_10784_)
            .thenCompose(p_10780_::wait)
            .thenAcceptAsync(p_358748_ -> this.apply((T)p_358748_, p_10781_, Profiler.get()), p_10785_);
    }

    protected abstract T prepare(ResourceManager p_10796_, ProfilerFiller p_10797_);

    protected abstract void apply(T p_10793_, ResourceManager p_10794_, ProfilerFiller p_10795_);
}