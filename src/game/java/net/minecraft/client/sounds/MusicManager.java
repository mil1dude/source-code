package net.minecraft.client.sounds;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MusicManager {
    private static final int STARTING_DELAY = 100;
    private final RandomSource random = RandomSource.create();
    private final Minecraft minecraft;
    @Nullable
    private SoundInstance currentMusic;
    private float currentGain = 1.0F;
    private int nextSongDelay = 100;

    public MusicManager(Minecraft p_120182_) {
        this.minecraft = p_120182_;
    }

    public void tick() {
        MusicInfo musicinfo = this.minecraft.getSituationalMusic();
        float f = musicinfo.volume();
        if (this.currentMusic != null && this.currentGain != f) {
            boolean flag = this.fadePlaying(f);
            if (!flag) {
                return;
            }
        }

        Music music = musicinfo.music();
        if (music == null) {
            this.nextSongDelay = Math.max(this.nextSongDelay, 100);
        } else {
            if (this.currentMusic != null) {
                if (musicinfo.canReplace(this.currentMusic)) {
                    this.minecraft.getSoundManager().stop(this.currentMusic);
                    this.nextSongDelay = Mth.nextInt(this.random, 0, music.getMinDelay() / 2);
                }

                if (!this.minecraft.getSoundManager().isActive(this.currentMusic)) {
                    this.currentMusic = null;
                    this.nextSongDelay = Math.min(this.nextSongDelay, Mth.nextInt(this.random, music.getMinDelay(), music.getMaxDelay()));
                }
            }

            this.nextSongDelay = Math.min(this.nextSongDelay, music.getMaxDelay());
            if (this.currentMusic == null && this.nextSongDelay-- <= 0) {
                this.startPlaying(musicinfo);
            }
        }
    }

    public void startPlaying(MusicInfo p_377601_) {
        this.currentMusic = SimpleSoundInstance.forMusic(p_377601_.music().getEvent().value());
        if (this.currentMusic.getSound() != SoundManager.EMPTY_SOUND) {
            this.minecraft.getSoundManager().play(this.currentMusic);
            this.minecraft.getSoundManager().setVolume(this.currentMusic, p_377601_.volume());
        }

        this.nextSongDelay = Integer.MAX_VALUE;
        this.currentGain = p_377601_.volume();
    }

    public void stopPlaying(Music p_278295_) {
        if (this.isPlayingMusic(p_278295_)) {
            this.stopPlaying();
        }
    }

    public void stopPlaying() {
        if (this.currentMusic != null) {
            this.minecraft.getSoundManager().stop(this.currentMusic);
            this.currentMusic = null;
        }

        this.nextSongDelay += 100;
    }

    private boolean fadePlaying(float p_375585_) {
        if (this.currentMusic == null) {
            return false;
        } else if (this.currentGain == p_375585_) {
            return true;
        } else {
            if (this.currentGain < p_375585_) {
                this.currentGain = this.currentGain + Mth.clamp(this.currentGain, 5.0E-4F, 0.005F);
                if (this.currentGain > p_375585_) {
                    this.currentGain = p_375585_;
                }
            } else {
                this.currentGain = 0.03F * p_375585_ + 0.97F * this.currentGain;
                if (Math.abs(this.currentGain - p_375585_) < 1.0E-4F || this.currentGain < p_375585_) {
                    this.currentGain = p_375585_;
                }
            }

            this.currentGain = Mth.clamp(this.currentGain, 0.0F, 1.0F);
            if (this.currentGain <= 1.0E-4F) {
                this.stopPlaying();
                return false;
            } else {
                this.minecraft.getSoundManager().setVolume(this.currentMusic, this.currentGain);
                return true;
            }
        }
    }

    public boolean isPlayingMusic(Music p_120188_) {
        return this.currentMusic == null ? false : p_120188_.getEvent().value().location().equals(this.currentMusic.getLocation());
    }
}