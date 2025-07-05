package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;

public class PacketUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> p_131360_, T p_131361_, ServerLevel p_131362_) throws RunningOnDifferentThreadException {
        ensureRunningOnSameThread(p_131360_, p_131361_, p_131362_.getServer());
    }

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> p_131364_, T p_131365_, BlockableEventLoop<?> p_131366_) throws RunningOnDifferentThreadException {
        if (!p_131366_.isSameThread()) {
            p_131366_.executeIfPossible(() -> {
                if (p_131365_.shouldHandleMessage(p_131364_)) {
                    try {
                        p_131364_.handle(p_131365_);
                    } catch (Exception exception) {
                        if (exception instanceof ReportedException reportedexception && reportedexception.getCause() instanceof OutOfMemoryError) {
                            throw makeReportedException(exception, p_131364_, p_131365_);
                        }

                        p_131365_.onPacketError(p_131364_, exception);
                    }
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: {}", p_131364_);
                }
            });
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }

    public static <T extends PacketListener> ReportedException makeReportedException(Exception p_331079_, Packet<T> p_335356_, T p_332020_) {
        if (p_331079_ instanceof ReportedException reportedexception) {
            fillCrashReport(reportedexception.getReport(), p_332020_, p_335356_);
            return reportedexception;
        } else {
            CrashReport crashreport = CrashReport.forThrowable(p_331079_, "Main thread packet handler");
            fillCrashReport(crashreport, p_332020_, p_335356_);
            return new ReportedException(crashreport);
        }
    }

    public static <T extends PacketListener> void fillCrashReport(CrashReport p_330590_, T p_333816_, @Nullable Packet<T> p_330069_) {
        if (p_330069_ != null) {
            CrashReportCategory crashreportcategory = p_330590_.addCategory("Incoming Packet");
            crashreportcategory.setDetail("Type", () -> p_330069_.type().toString());
            crashreportcategory.setDetail("Is Terminal", () -> Boolean.toString(p_330069_.isTerminal()));
            crashreportcategory.setDetail("Is Skippable", () -> Boolean.toString(p_330069_.isSkippable()));
        }

        p_333816_.fillCrashReport(p_330590_);
    }
}