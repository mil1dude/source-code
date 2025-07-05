package com.mojang.realmsclient.client.worldupload;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.FileUpload;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.UploadResult;
import com.mojang.realmsclient.util.UploadTokenCache;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.User;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldUpload {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int UPLOAD_RETRIES = 20;
    private final RealmsClient client = RealmsClient.create();
    private final Path worldFolder;
    private final RealmsWorldOptions worldOptions;
    private final User user;
    private final long realmId;
    private final int slotId;
    private final RealmsWorldUploadStatusTracker statusCallback;
    private volatile boolean cancelled;
    @Nullable
    private FileUpload uploadTask;

    public RealmsWorldUpload(
        Path p_365252_, RealmsWorldOptions p_367690_, User p_366652_, long p_368429_, int p_361659_, RealmsWorldUploadStatusTracker p_361427_
    ) {
        this.worldFolder = p_365252_;
        this.worldOptions = p_367690_;
        this.user = p_366652_;
        this.realmId = p_368429_;
        this.slotId = p_361659_;
        this.statusCallback = p_361427_;
    }

    public CompletableFuture<?> packAndUpload() {
        return CompletableFuture.runAsync(
            () -> {
                File file1 = null;

                try {
                    UploadInfo uploadinfo = this.requestUploadInfoWithRetries();
                    file1 = RealmsUploadWorldPacker.pack(this.worldFolder, () -> this.cancelled);
                    this.statusCallback.setUploading();
                    FileUpload fileupload = new FileUpload(
                        file1,
                        this.realmId,
                        this.slotId,
                        uploadinfo,
                        this.user,
                        SharedConstants.getCurrentVersion().getName(),
                        this.worldOptions.version,
                        this.statusCallback.getUploadStatus()
                    );
                    this.uploadTask = fileupload;
                    UploadResult uploadresult = fileupload.upload();
                    String s = uploadresult.getSimplifiedErrorMessage();
                    if (s != null) {
                        throw new RealmsUploadFailedException(s);
                    }

                    UploadTokenCache.invalidate(this.realmId);
                    this.client.updateSlot(this.realmId, this.slotId, this.worldOptions);
                } catch (IOException ioexception) {
                    throw new RealmsUploadFailedException(ioexception.getMessage());
                } catch (RealmsServiceException realmsserviceexception) {
                    throw new RealmsUploadFailedException(realmsserviceexception.realmsError.errorMessage());
                } catch (CancellationException | InterruptedException interruptedexception) {
                    throw new RealmsUploadCanceledException();
                } finally {
                    if (file1 != null) {
                        LOGGER.debug("Deleting file {}", file1.getAbsolutePath());
                        file1.delete();
                    }
                }
            },
            Util.backgroundExecutor()
        );
    }

    public void cancel() {
        this.cancelled = true;
        if (this.uploadTask != null) {
            this.uploadTask.cancel();
            this.uploadTask = null;
        }
    }

    private UploadInfo requestUploadInfoWithRetries() throws RealmsServiceException, InterruptedException {
        for (int i = 0; i < 20; i++) {
            try {
                UploadInfo uploadinfo = this.client.requestUploadInfo(this.realmId);
                if (this.cancelled) {
                    throw new RealmsUploadCanceledException();
                }

                if (uploadinfo != null) {
                    if (!uploadinfo.isWorldClosed()) {
                        throw new RealmsUploadWorldNotClosedException();
                    }

                    return uploadinfo;
                }
            } catch (RetryCallException retrycallexception) {
                Thread.sleep((long)retrycallexception.delaySeconds * 1000L);
            }
        }

        throw new RealmsUploadWorldNotClosedException();
    }
}