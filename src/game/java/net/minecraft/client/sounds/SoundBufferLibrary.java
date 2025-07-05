package net.minecraft.client.sounds;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.audio.SoundBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.Util;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundBufferLibrary {
    private final ResourceProvider resourceManager;
    private final Map<ResourceLocation, CompletableFuture<SoundBuffer>> cache = Maps.newHashMap();

    public SoundBufferLibrary(ResourceProvider p_248900_) {
        this.resourceManager = p_248900_;
    }

    public CompletableFuture<SoundBuffer> getCompleteBuffer(ResourceLocation p_120203_) {
        return this.cache.computeIfAbsent(p_120203_, p_358059_ -> CompletableFuture.supplyAsync(() -> {
                try {
                    SoundBuffer soundbuffer;
                    try (
                        InputStream inputstream = this.resourceManager.open(p_358059_);
                        FiniteAudioStream finiteaudiostream = new JOrbisAudioStream(inputstream);
                    ) {
                        ByteBuffer bytebuffer = finiteaudiostream.readAll();
                        soundbuffer = new SoundBuffer(bytebuffer, finiteaudiostream.getFormat());
                    }

                    return soundbuffer;
                } catch (IOException ioexception) {
                    throw new CompletionException(ioexception);
                }
            }, Util.nonCriticalIoPool()));
    }

    public CompletableFuture<AudioStream> getStream(ResourceLocation p_120205_, boolean p_120206_) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InputStream inputstream = this.resourceManager.open(p_120205_);
                return (AudioStream)(p_120206_ ? new LoopingAudioStream(JOrbisAudioStream::new, inputstream) : new JOrbisAudioStream(inputstream));
            } catch (IOException ioexception) {
                throw new CompletionException(ioexception);
            }
        }, Util.nonCriticalIoPool());
    }

    public void clear() {
        this.cache.values().forEach(p_120201_ -> p_120201_.thenAccept(SoundBuffer::discardAlBuffer));
        this.cache.clear();
    }

    public CompletableFuture<?> preload(Collection<Sound> p_120199_) {
        return CompletableFuture.allOf(p_120199_.stream().map(p_120197_ -> this.getCompleteBuffer(p_120197_.getPath())).toArray(CompletableFuture[]::new));
    }
}