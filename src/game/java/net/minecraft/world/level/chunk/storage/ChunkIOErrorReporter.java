package net.minecraft.world.level.chunk.storage;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.level.ChunkPos;

public interface ChunkIOErrorReporter {
    void reportChunkLoadFailure(Throwable p_344152_, RegionStorageInfo p_342086_, ChunkPos p_342081_);

    void reportChunkSaveFailure(Throwable p_342260_, RegionStorageInfo p_344817_, ChunkPos p_345030_);

    static ReportedException createMisplacedChunkReport(ChunkPos p_343859_, ChunkPos p_343919_) {
        CrashReport crashreport = CrashReport.forThrowable(
            new IllegalStateException("Retrieved chunk position " + p_343859_ + " does not match requested " + p_343919_), "Chunk found in invalid location"
        );
        CrashReportCategory crashreportcategory = crashreport.addCategory("Misplaced Chunk");
        crashreportcategory.setDetail("Stored Position", p_343859_::toString);
        return new ReportedException(crashreport);
    }

    default void reportMisplacedChunk(ChunkPos p_344532_, ChunkPos p_343492_, RegionStorageInfo p_342478_) {
        this.reportChunkLoadFailure(createMisplacedChunkReport(p_344532_, p_343492_), p_342478_, p_343492_);
    }
}