package net.minecraft;

import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public record TracingExecutor(ExecutorService service) implements Executor {
    public Executor forName(String p_364709_) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return p_369604_ -> this.service.execute(() -> {
                    Thread thread = Thread.currentThread();
                    String s = thread.getName();
                    thread.setName(p_364709_);

                    try (Zone zone = TracyClient.beginZone(p_364709_, SharedConstants.IS_RUNNING_IN_IDE)) {
                        p_369604_.run();
                    } finally {
                        thread.setName(s);
                    }
                });
        } else {
            return (TracyClient.isAvailable() ? p_366279_ -> this.service.execute(() -> {
                    try (Zone zone = TracyClient.beginZone(p_364709_, SharedConstants.IS_RUNNING_IN_IDE)) {
                        p_366279_.run();
                    }
                }) : this.service);
        }
    }

    @Override
    public void execute(Runnable p_362236_) {
        this.service.execute(wrapUnnamed(p_362236_));
    }

    public void shutdownAndAwait(long p_367055_, TimeUnit p_369186_) {
        this.service.shutdown();

        boolean flag;
        try {
            flag = this.service.awaitTermination(p_367055_, p_369186_);
        } catch (InterruptedException interruptedexception) {
            flag = false;
        }

        if (!flag) {
            this.service.shutdownNow();
        }
    }

    private static Runnable wrapUnnamed(Runnable p_362176_) {
        return !TracyClient.isAvailable() ? p_362176_ : () -> {
            try (Zone zone = TracyClient.beginZone("task", SharedConstants.IS_RUNNING_IN_IDE)) {
                p_362176_.run();
            }
        };
    }
}
