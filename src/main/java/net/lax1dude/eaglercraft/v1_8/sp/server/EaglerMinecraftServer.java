/*
 * Copyright (c) 2023-2024 lax1dude, ayunami2000. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.sp.server;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.GameType; // MCP Reborn 1.21.4 package
import net.lax1dude.eaglercraft.v1_8.sp.server.skins.IntegratedCapeService;
import net.lax1dude.eaglercraft.v1_8.sp.server.skins.IntegratedSkinService;
import net.lax1dude.eaglercraft.v1_8.sp.server.voice.IntegratedVoiceService;

public class EaglerMinecraftServer extends MinecraftServer {

	public static final Logger logger = EaglerIntegratedServerWorker.logger;

	public static final VFile2 savesDir = LevelsDB.newVFile("worlds");

	protected Difficulty difficulty;
	protected net.minecraft.world.level.GameType gamemode; // MCP Reborn 1.21.4 class
	protected LevelSettings newLevelSettings;
	protected boolean paused;
	protected EaglerSaveHandler saveHandler;
	protected IntegratedSkinService skinService;
	protected IntegratedCapeService capeService;
	protected IntegratedVoiceService voiceService;

	private long lastTPSUpdate = 0l;

	public static int counterTicksPerSecond = 0;
	public static int counterChunkRead = 0;
	public static int counterChunkGenerate = 0;
	public static int counterChunkWrite = 0;
	public static int counterTileUpdate = 0;
	public static int counterLightUpdate = 0;

	private final List<Runnable> scheduledTasks = new LinkedList<>();

	public EaglerMinecraftServer(String world, String owner, int viewDistance, LevelSettings currentLevelSettings, boolean demo) {
		super(world);
		Bootstrap.register();
		this.saveHandler = new EaglerSaveHandler(savesDir, world);
		this.skinService = new IntegratedSkinService(LevelsDB.newVFile(saveHandler.getLevelDirectory(), "eagler/skulls"));
		this.capeService = new IntegratedCapeService();
		this.voiceService = null;
		this.setServerOwner(owner);
		logger.info("server owner: " + owner);
		this.setDemo(demo);
		this.canCreateBonusChest(currentLevelSettings != null && currentLevelSettings.isBonusChestEnabled());
		this.setBuildLimit(256);
		this.setConfigManager(new EaglerPlayerList(this, viewDistance));
		this.newLevelSettings = currentLevelSettings;
		this.paused = false;
	}

	public IntegratedSkinService getSkinService() {
		return skinService;
	}

	public IntegratedCapeService getCapeService() {
		return capeService;
	}

	public IntegratedVoiceService getVoiceService() {
		return voiceService;
	}

	public void enableVoice(String[] iceServers) {
		if(iceServers != null) {
			if(voiceService != null) {
				voiceService.changeICEServers(iceServers);
			}else {
				voiceService = new IntegratedVoiceService(iceServers);
				for(ServerPlayer player : getConfigurationManager().func_181057_v()) {
					voiceService.handlePlayerLoggedIn(player);
				}
			}
		}
	}

	public void setBaseServerProperties(Difficulty difficulty, net.minecraft.world.level.GameType gamemode) { // MCP Reborn 1.21.4 class
		this.difficulty = difficulty;
		this.gamemode = gamemode;
		this.setCanSpawnAnimals(true);
		this.setCanSpawnNPCs(true);
		this.setAllowPvp(true);
		this.setAllowFlight(true);
	}

	@Override
	public void addScheduledTask(Runnable var1) {
		scheduledTasks.add(var1);
	}

	@Override
	protected boolean startServer() throws IOException {
		logger.info("Starting integrated eaglercraft server version 1.8.8");
		this.loadAllLevels(saveHandler, this.getLevelName(), newLevelSettings);
		serverRunning = true;
		return true;
	}

	public void deleteLevelAndStopServer() {
		super.deleteLevelAndStopServer();
		logger.info("Deleting world...");
		EaglerIntegratedServerWorker.saveFormat.deleteLevelDirectory(getFolderName());
	}

	public void mainLoop(boolean singleThreadMode) {
		long k = getCurrentTimeMillis();
		this.sendTPSToClient(k);
		if(paused && this.playersOnline.size() <= 1) {
			currentTime = k;
			return;
		}

		long j = k - this.currentTime;
		if (j > (singleThreadMode ? 500L : 2000L) && this.currentTime - this.timeOfLastWarning >= (singleThreadMode ? 5000L : 15000L)) {
			logger.warn(
					"Can\'t keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)",
					new Object[] { Long.valueOf(j), Long.valueOf(j / 50L) });
			j = 100L;
			this.currentTime = k - 100l;
			this.timeOfLastWarning = this.currentTime;
		}

		if (j < 0L) {
			logger.warn("Time ran backwards! Did the system time change?");
			j = 0L;
			this.currentTime = k;
		}

		if (this.worldServers[0].areAllPlayersAsleep()) {
			this.currentTime = k;
			this.tick();
			++counterTicksPerSecond;
		} else {
			if (j > 50L) {
				this.currentTime += 50l;
				this.tick();
				++counterTicksPerSecond;
			}
		}
	}

	public void updateTimeLightAndEntities() {
		this.skinService.flushCache();
		super.updateTimeLightAndEntities();
	}

	protected void sendTPSToClient(long millis) {
		if(millis - lastTPSUpdate > 1000l) {
			lastTPSUpdate = millis;
			if(serverRunning && this.worldServers != null) {
				List<String> lst = Lists.newArrayList(
						"TPS: " + counterTicksPerSecond + "/20",
						"Chunks: " + countChunksLoaded(this.worldServers) + "/" + countChunksTotal(this.worldServers),
						"Entities: " + countEntities(this.worldServers) + "+" + countTileEntities(this.worldServers),
						"R: " + counterChunkRead + ", G: " + counterChunkGenerate + ", W: " + counterChunkWrite,
						"TU: " + counterTileUpdate + ", LU: " + counterLightUpdate
				);
				int players = countPlayerEntities(this.worldServers);
				if(players > 1) {
					lst.add("Players: " + players);
				}
				counterTicksPerSecond = counterChunkRead = counterChunkGenerate = 0;
				counterChunkWrite = counterTileUpdate = counterLightUpdate = 0;
				EaglerIntegratedServerWorker.reportTPS(lst);
			}
		}
	}

	private static int countChunksLoaded(ServerLevel[] worlds) {
		int i = 0;
		for(int j = 0; j < worlds.length; ++j) {
			if(worlds[j] != null) {
				i += worlds[j].theChunkProviderServer.getLoadedChunkCount();
			}
		}
		return i;
	}

	private static int countChunksTotal(ServerLevel[] worlds) {
		int i = 0;
		for(int j = 0; j < worlds.length; ++j) {
			if(worlds[j] != null) {
				List<Player> players = worlds[j].playerEntities;
				for(int l = 0, n = players.size(); l < n; ++l) {
					i += ((ServerPlayer)players.get(l)).loadedChunks.size();
				}
				i += worlds[j].theChunkProviderServer.getLoadedChunkCount();
			}
		}
		return i;
	}

	private static int countEntities(ServerLevel[] worlds) {
		int i = 0;
		for(int j = 0; j < worlds.length; ++j) {
			if(worlds[j] != null) {
				i += worlds[j].loadedEntityList.size();
			}
		}
		return i;
	}

	private static int countTileEntities(ServerLevel[] worlds) {
		int i = 0;
		for(int j = 0; j < worlds.length; ++j) {
			if(worlds[j] != null) {
				i += worlds[j].loadedTileEntityList.size();
			}
		}
		return i;
	}

	private static int countPlayerEntities(ServerLevel[] worlds) {
		int i = 0;
		for(int j = 0; j < worlds.length; ++j) {
			if(worlds[j] != null) {
				i += worlds[j].playerEntities.size();
			}
		}
		return i;
	}

	public void setPaused(boolean p) {
		paused = p;
		if(!p) {
			currentTime = EagRuntime.steadyTimeMillis();
		}
	}
	
	public boolean getPaused() {
		return paused;
	}

	@Override
	public boolean canStructuresSpawn() {
		return worldServers != null ? worldServers[0].getLevelData().isMapFeaturesEnabled() : newLevelSettings.isMapFeaturesEnabled();
	}

	@Override
	public net.minecraft.world.level.GameType getGameType() { // MCP Reborn 1.21.4 class
		return worldServers != null ? worldServers[0].getLevelData().getGameType() : newLevelSettings.getGameType();
	}

	@Override
	public Difficulty getDifficulty() {
		return difficulty;
	}

	@Override
	public boolean isHardcore() {
		return worldServers != null ? worldServers[0].getLevelData().isHardcoreModeEnabled() : newLevelSettings.getHardcoreEnabled();
	}

	@Override
	public int getOpPermissionLevel() {
		return 4;
	}

	@Override
	public boolean func_181034_q() {
		return true;
	}

	@Override
	public boolean func_183002_r() {
		return true;
	}

	@Override
	public boolean isDedicatedServer() {
		return false;
	}

	@Override
	public boolean func_181035_ah() {
		return false;
	}

	@Override
	public boolean isCommandBlockEnabled() {
		return true;
	}

	@Override
	public String shareToLAN(net.minecraft.world.level.GameType var1, boolean var2) { // MCP Reborn 1.21.4 class
		return null;
	}

}