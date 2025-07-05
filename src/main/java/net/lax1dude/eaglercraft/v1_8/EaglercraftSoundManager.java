/*
 * Copyright (c) 2022-2023 lax1dude, ayunami2000. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package net.lax1dude.eaglercraft.v1_8;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.internal.EnumPlatformType;
import net.lax1dude.eaglercraft.v1_8.internal.IAudioCacheLoader;
import net.lax1dude.eaglercraft.v1_8.internal.IAudioHandle;
import net.lax1dude.eaglercraft.v1_8.internal.IAudioResource;
import net.lax1dude.eaglercraft.v1_8.internal.PlatformAudio;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.minecraft.client.Minecraft;

import net.minecraft.client.resources.sounds.Sound; // MCP Reborn 1.21.4 package
import net.minecraft.sounds.SoundSource;
import net.minecraft.client.resources.sounds.SoundInstance; // MCP Reborn 1.21.4 package
import net.minecraft.client.resources.sounds.TickableSoundInstance; // MCP Reborn 1.21.4 package
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.resources.sounds.Sound; // Updated for 1.21.4
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

public class EaglercraftSoundManager {
	
	protected class ActiveSoundEvent {

		protected final EaglercraftSoundManager manager;
		
		protected final SoundInstance soundInstance;
		protected final SoundSource soundCategory;
		protected final Sound soundConfig;
		protected IAudioHandle soundHandle;
		
		protected float activeX;
		protected float activeY;
		protected float activeZ;
		
		protected float activePitch;
		protected float activeGain;
		
		protected int repeatCounter = 0;
		protected boolean paused = false;
		
		protected ActiveSoundEvent(EaglercraftSoundManager manager, SoundInstance soundInstance,
				SoundSource soundCategory, Sound soundConfig, IAudioHandle soundHandle) {
			this.manager = manager;
			this.soundInstance = soundInstance;
			this.soundCategory = soundCategory;
			this.soundConfig = soundConfig;
			this.soundHandle = soundHandle;
			this.activeX = soundInstance.getXPosF();
			this.activeY = soundInstance.getYPosF();
			this.activeZ = soundInstance.getZPosF();
			this.activePitch = soundInstance.getPitch();
			this.activeGain = soundInstance.getVolume();
		}
		
		protected void updateLocation() {
			float x = soundInstance.getXPosF();
			float y = soundInstance.getYPosF();
			float z = soundInstance.getZPosF();
			float pitch = soundInstance.getPitch();
			float gain = soundInstance.getVolume();
			if(x != activeX || y != activeY || z != activeZ) {
				soundHandle.move(x, y, z);
				activeX = x;
				activeY = y;
				activeZ = z;
			}
			if(pitch != activePitch) {
				soundHandle.pitch(EaglercraftSoundManager.this.getNormalizedPitch(soundInstance, soundConfig));
				activePitch = pitch;
			}
			if(gain != activeGain) {
				soundHandle.gain(EaglercraftSoundManager.this.getNormalizedVolume(soundInstance, soundConfig, soundCategory));
				activeGain = gain;
			}
		}
		
	}
	
	protected static class WaitingSoundEvent {
		
		protected final SoundInstance playSound;
		protected int playTicks;
		protected boolean paused = false;
		
		private WaitingSoundEvent(SoundInstance playSound, int playTicks) {
			this.playSound = playSound;
			this.playTicks = playTicks;
		}
		
	}
	
	private static final Logger logger = LogManager.getLogger("SoundManager");
	
	private final Options settings;
	private final SoundManager handler;
	private final float[] categoryVolumes;
	private final List<ActiveSoundEvent> activeSounds;
	private final List<WaitingSoundEvent> queuedSounds;

	public EaglercraftSoundManager(Options settings, SoundManager handler) {
		this.settings = settings;
		this.handler = handler;
		categoryVolumes = new float[] {
				settings.getSoundLevel(SoundSource.MASTER), settings.getSoundLevel(SoundSource.MUSIC),
				settings.getSoundLevel(SoundSource.RECORDS), settings.getSoundLevel(SoundSource.WEATHER),
				settings.getSoundLevel(SoundSource.BLOCKS), settings.getSoundLevel(SoundSource.MOBS),
				settings.getSoundLevel(SoundSource.ANIMALS), settings.getSoundLevel(SoundSource.PLAYERS),
				settings.getSoundLevel(SoundSource.AMBIENT)
		};
		activeSounds = new LinkedList<>();
		queuedSounds = new LinkedList<>();
	}

	public void unloadSoundSystem() {
		// handled by PlatformApplication
	}
	
	public void reloadSoundSystem() {
		PlatformAudio.flushAudioCache();
	}
	
	public void setSoundSourceVolume(SoundSource category, float volume) {
		categoryVolumes[category.getCategoryId()] = volume;
		Iterator<ActiveSoundEvent> soundItr = activeSounds.iterator();
		while(soundItr.hasNext()) {
			ActiveSoundEvent evt = soundItr.next();
			if((category == SoundSource.MASTER || evt.soundCategory == category)
					&& !evt.soundHandle.shouldFree()) {
				float newVolume = getNormalizedVolume(evt.soundInstance, evt.soundConfig, evt.soundCategory);
				if(newVolume > 0.0f) {
					evt.soundHandle.gain(newVolume);
				}else {
					evt.soundHandle.end();
					soundItr.remove();
				}
			}
		}
	}
	
	public void stopAllSounds() {
		Iterator<ActiveSoundEvent> soundItr = activeSounds.iterator();
		while(soundItr.hasNext()) {
			ActiveSoundEvent evt = soundItr.next();
			if(!evt.soundHandle.shouldFree()) {
				evt.soundHandle.end();
			}
		}
		activeSounds.clear();
	}
	
	public void pauseAllSounds() {
		Iterator<ActiveSoundEvent> soundItr = activeSounds.iterator();
		while(soundItr.hasNext()) {
			ActiveSoundEvent evt = soundItr.next();
			if(!evt.soundHandle.shouldFree()) {
				evt.soundHandle.pause(true);
				evt.paused = true;
			}
		}
		Iterator<WaitingSoundEvent> soundItr2 = queuedSounds.iterator();
		while(soundItr2.hasNext()) {
			soundItr2.next().paused = true;
		}
	}
	
	public void resumeAllSounds() {
		Iterator<ActiveSoundEvent> soundItr = activeSounds.iterator();
		while(soundItr.hasNext()) {
			ActiveSoundEvent evt = soundItr.next();
			if(!evt.soundHandle.shouldFree()) {
				evt.soundHandle.pause(false);
				evt.paused = false;
			}
		}
		Iterator<WaitingSoundEvent> soundItr2 = queuedSounds.iterator();
		while(soundItr2.hasNext()) {
			soundItr2.next().paused = false;
		}
	}
	
	public void updateAllSounds() {
		Iterator<ActiveSoundEvent> soundItr = activeSounds.iterator();
		while(soundItr.hasNext()) {
			ActiveSoundEvent evt = soundItr.next();
			boolean persist = false;
			if(!evt.paused && (evt.soundInstance instanceof TickableBlockEntitySound)) {
				boolean destroy = false;
				TickableBlockEntitySound snd = (TickableBlockEntitySound) evt.soundInstance;
				lbl : {
					try {
						snd.update();
						if(snd.isDonePlaying()) {
							destroy = true;
							break lbl;
						}
						persist = true;
					}catch(Throwable t) {
						logger.error("Error ticking sound: {}", t.toString());
						logger.error(t);
						destroy = true;
					}
				}
				if(destroy) {
					if(!evt.soundHandle.shouldFree()) {
						evt.soundHandle.end();
					}
					soundItr.remove();
					continue;
				}
			}
			if(evt.soundHandle.shouldFree()) {
				if(!persist) {
					soundItr.remove();
				}
			}else {
				evt.updateLocation();
			}
		}
		Iterator<WaitingSoundEvent> soundItr2 = queuedSounds.iterator();
		while(soundItr2.hasNext()) {
			WaitingSoundEvent evt = soundItr2.next();
			if(!evt.paused && --evt.playTicks <= 0) {
				soundItr2.remove();
				playSound(evt.playSound);
			}
		}
		PlatformAudio.clearAudioCache();
	}
	
	public boolean isSoundPlaying(SoundInstance sound) { // Keep same reference
		Iterator<ActiveSoundEvent> soundItr = activeSounds.iterator();
		while(soundItr.hasNext()) {
			ActiveSoundEvent evt = soundItr.next();
			if(evt.soundInstance == sound) {
				return !evt.soundHandle.shouldFree();
			}
		}
		return false;
	}
	
	public void stopSound(SoundInstance sound) { // Keep same reference
		Iterator<ActiveSoundEvent> soundItr = activeSounds.iterator();
		while(soundItr.hasNext()) {
			ActiveSoundEvent evt = soundItr.next();
			if(evt.soundInstance == sound) {
				if(!evt.soundHandle.shouldFree()) {
					evt.soundHandle.end();
					soundItr.remove();
					return;
				}
			}
		}
		Iterator<WaitingSoundEvent> soundItr2 = queuedSounds.iterator();
		while(soundItr2.hasNext()) {
			if(soundItr2.next().playSound == sound) {
				soundItr2.remove();
			}
		}
	}

	private final IAudioCacheLoader browserResourcePackLoader = filename -> {
		try {
			return EaglerInputStream.inputStreamToBytesQuiet(Minecraft.getMinecraft().getResourceManager()
					.getResource(new ResourceLocation(filename)).getInputStream());
		}catch(Throwable t) {
			return null;
		}
	};

	public void playSound(SoundInstance sound) { // Keep same reference
		if(!PlatformAudio.available()) {
			return;
		}
		if(sound != null && categoryVolumes[SoundSource.MASTER.getCategoryId()] > 0.0f) {
			SoundEventAccessorComposite accessor = handler.getSoundEvent(sound.getSoundLocation());
			if(accessor == null) {
				logger.warn("Unable to play unknown soundEvent(1): {}", sound.getSoundLocation().toString());
			}else {
				Sound etr = accessor.cloneEntry();
				if (etr == SoundManager.missing_sound) {
					logger.warn("Unable to play empty soundEvent(2): {}", etr.getSoundLocation().toString());
				}else {
					ResourceLocation lc = etr.getSoundLocation();
					IAudioResource trk;
					if(EagRuntime.getPlatformType() != EnumPlatformType.DESKTOP) {
						trk = PlatformAudio.loadAudioDataNew(lc.toString(), !etr.isStreamingSound(), browserResourcePackLoader);
					}else {
						trk = PlatformAudio.loadAudioData(
								"/assets/" + lc.getResourceDomain() + "/" + lc.getResourcePath(), !etr.isStreamingSound());
					}
					if(trk == null) {
						logger.warn("Unable to play unknown soundEvent(3): {}", sound.getSoundLocation().toString());
					}else {
						
						ActiveSoundEvent newSound = new ActiveSoundEvent(this, sound, accessor.getSoundSource(), etr, null);

						float pitch = getNormalizedPitch(sound, etr);
						float attenuatedGain = getNormalizedVolume(sound, etr, accessor.getSoundSource());
						boolean repeat = sound.canRepeat();
						
						SoundInstance.Attenuation tp = sound.getAttenuationType();
						if(tp == SoundInstance.Attenuation.LINEAR) {
							newSound.soundHandle = PlatformAudio.beginPlayback(trk, newSound.activeX, newSound.activeY,
									newSound.activeZ, attenuatedGain, pitch, repeat);
						}else {
							newSound.soundHandle = PlatformAudio.beginPlaybackStatic(trk, attenuatedGain, pitch, repeat);
						}
						
						if(newSound.soundHandle == null) {
							logger.error("Unable to play soundEvent(4): {}", sound.getSoundLocation().toString());
						}else {
							activeSounds.add(newSound);
						}
					}
				}
			}
		}
	}
	
	public void playDelayedSound(SoundInstance sound, int delay) { // Keep same reference
		queuedSounds.add(new WaitingSoundEvent(sound, delay));
	}
	
	private float getNormalizedVolume(SoundInstance sound, Sound entry, SoundSource category) { // Keep same reference
		return (float) Mth.clamp_double((double) sound.getVolume() * entry.getVolume(), 0.0D, 1.0D)
				* (category.getCategoryId() == SoundSource.MASTER.getCategoryId() ? 1.0f
						: categoryVolumes[category.getCategoryId()])
				* categoryVolumes[SoundSource.MASTER.getCategoryId()];
	}
	
	private float getNormalizedPitch(SoundInstance sound, Sound entry) { // Keep same reference
		return Mth.clamp_float(sound.getPitch() * (float)entry.getPitch(), 0.5f, 2.0f);
	}
	
	public void setListener(Player player, float partialTicks) {
		if(!PlatformAudio.available()) {
			return;
		}
		if(player != null) {
			try {
				float f = player.prevRotationPitch + (player.getXRot() - player.prevRotationPitch) * partialTicks;
				float f1 = player.prevRotationYaw + (player.getYRot() - player.prevRotationYaw) * partialTicks;
				double d0 = player.prevPosX + (player.getX() - player.prevPosX) * (double) partialTicks;
				double d1 = player.prevPosY + (player.getY() - player.prevPosY) * (double) partialTicks + (double) player.getEyeHeight();
				double d2 = player.prevPosZ + (player.getZ() - player.prevPosZ) * (double) partialTicks;
				PlatformAudio.setListener((float)d0, (float)d1, (float)d2, f, f1);
			}catch(Throwable t) {
				// eaglercraft 1.5.2 had Infinity/NaN crashes for this function which
				// couldn't be resolved via if statement checks in the above variables
			}
		}
	}
	
}