package net.minecraft.server.level.progress;

import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.util.thread.ConsecutiveExecutor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class ProcessorChunkProgressListener implements ChunkProgressListener {
    private final ChunkProgressListener delegate;
    private final ConsecutiveExecutor consecutiveExecutor;
    private boolean started;

    private ProcessorChunkProgressListener(ChunkProgressListener p_9640_, Executor p_9641_) {
        this.delegate = p_9640_;
        this.consecutiveExecutor = new ConsecutiveExecutor(p_9641_, "progressListener");
    }

    public static ProcessorChunkProgressListener createStarted(ChunkProgressListener p_143584_, Executor p_143585_) {
        ProcessorChunkProgressListener processorchunkprogresslistener = new ProcessorChunkProgressListener(p_143584_, p_143585_);
        processorchunkprogresslistener.start();
        return processorchunkprogresslistener;
    }

    @Override
    public void updateSpawnPos(ChunkPos p_9643_) {
        this.consecutiveExecutor.schedule(() -> this.delegate.updateSpawnPos(p_9643_));
    }

    @Override
    public void onStatusChange(ChunkPos p_9645_, @Nullable ChunkStatus p_330099_) {
        if (this.started) {
            this.consecutiveExecutor.schedule(() -> this.delegate.onStatusChange(p_9645_, p_330099_));
        }
    }

    @Override
    public void start() {
        this.started = true;
        this.consecutiveExecutor.schedule(this.delegate::start);
    }

    @Override
    public void stop() {
        this.started = false;
        this.consecutiveExecutor.schedule(this.delegate::stop);
    }
}