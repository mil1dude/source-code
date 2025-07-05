package net.minecraft.client.resources.server;

import java.util.UUID;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface PackLoadFeedback {
    void reportUpdate(UUID p_312796_, PackLoadFeedback.Update p_311319_);

    void reportFinalResult(UUID p_309920_, PackLoadFeedback.FinalResult p_312819_);

    @OnlyIn(Dist.CLIENT)
    public static enum FinalResult {
        DECLINED,
        APPLIED,
        DISCARDED,
        DOWNLOAD_FAILED,
        ACTIVATION_FAILED;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Update {
        ACCEPTED,
        DOWNLOADED;
    }
}