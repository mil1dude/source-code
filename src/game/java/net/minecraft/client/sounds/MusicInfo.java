package net.minecraft.client.sounds;

import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.Music;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record MusicInfo(@Nullable Music music, float volume) {
    public MusicInfo(Music p_376760_) {
        this(p_376760_, 1.0F);
    }

    public boolean canReplace(SoundInstance p_376744_) {
        return this.music == null ? false : this.music.replaceCurrentMusic() && !this.music.getEvent().value().location().equals(p_376744_.getLocation());
    }
}