/*
 * Copyright (c) 2022-2024 lax1dude, ayunami2000. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.sp.gui;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.profile.EaglerProfile;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.sp.ipc.IPCPacket05RequestData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.storage.LevelData;

public class ScreenBackupLevelSelection extends Screen {

	private Screen selectLevel;

	private net.minecraft.client.gui.components.Button worldRecreate = null;
	private net.minecraft.client.gui.components.Button worldDuplicate = null;
	private net.minecraft.client.gui.components.Button worldExport = null;
	private net.minecraft.client.gui.components.Button worldConvert = null;
	private net.minecraft.client.gui.components.Button worldBackup = null;
	private long worldSeed;
	private boolean oldRNG;
	private CompoundTag levelDat;
	
	private String worldName;
	
	public ScreenBackupLevelSelection(Screen selectLevel, String worldName, CompoundTag levelDat) {
		this.selectLevel = selectLevel;
		this.worldName = worldName;
		this.levelDat = levelDat;
		this.worldSeed = levelDat.getCompoundTag("Data").getLong("RandomSeed");
		this.oldRNG = levelDat.getCompoundTag("Data").getInteger("eaglerVersionSerial") == 0;
	}
	
	public void initGui() {
		this.buttonList.add(worldRecreate = new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 5 + 5, I18n.get("singleplayer.backup.recreate")));
		this.buttonList.add(worldDuplicate = new net.minecraft.client.gui.components.Button(2, this.width / 2 - 100, this.height / 5 + 30, I18n.get("singleplayer.backup.duplicate")));
		this.buttonList.add(worldExport = new net.minecraft.client.gui.components.Button(3, this.width / 2 - 100, this.height / 5 + 80, I18n.get("singleplayer.backup.export")));
		this.buttonList.add(worldConvert = new net.minecraft.client.gui.components.Button(4, this.width / 2 - 100, this.height / 5 + 105, I18n.get("singleplayer.backup.vanilla")));
		this.buttonList.add(worldBackup = new net.minecraft.client.gui.components.Button(5, this.width / 2 - 100, this.height / 5 + 136, I18n.get("singleplayer.backup.clearPlayerData")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 4 + 155, I18n.get("gui.cancel")));
	}
	
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();

		this.drawCenteredString(this.font, I18n.get("singleplayer.backup.title", worldName), this.width / 2, this.height / 5 - 35, 16777215);
		if(oldRNG) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.backup.seed") + " " + worldSeed + " " + ChatFormatting.RED + "(pre-u34)", this.width / 2, this.height / 5 + 62, 0xAAAAFF);
		}else {
			this.drawCenteredString(this.font, I18n.get("singleplayer.backup.seed") + " " + worldSeed, this.width / 2, this.height / 5 + 62, 0xAAAAFF);
		}
		
		int toolTipColor = 0xDDDDAA;
		if(worldRecreate.isMouseOver()) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.backup.recreate.tooltip"), this.width / 2, this.height / 5 - 12, toolTipColor);
		}else if(worldDuplicate.isMouseOver()) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.backup.duplicate.tooltip"), this.width / 2, this.height / 5 - 12, toolTipColor);
		}else if(worldExport.isMouseOver()) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.backup.export.tooltip"), this.width / 2, this.height / 5 - 12, toolTipColor);
		}else if(worldConvert.isMouseOver()) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.backup.vanilla.tooltip"), this.width / 2, this.height / 5 - 12, toolTipColor);
		}else if(worldBackup.isMouseOver()) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.backup.clearPlayerData.tooltip"), this.width / 2, this.height / 5 - 12, toolTipColor);
		}
		
		super.drawScreen(par1, par2, par3);
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			this.minecraft.displayScreen(selectLevel);
		}else if(par1Button.id == 1) {
			CreateWorldScreen cw = new CreateWorldScreen(selectLevel);
			LevelData inf = new LevelData(this.levelDat.getCompoundTag("Data"));
			cw.func_146318_a(inf);
			if(inf.isOldEaglercraftRandom()) {
				this.minecraft.displayScreen(new ScreenOldSeedWarning(cw));
			}else {
				this.minecraft.displayScreen(cw);
			}
		}else if(par1Button.id == 2) {
			this.minecraft.displayScreen(new EditWorldScreen(this.selectLevel, this.worldName, true));
		}else if(par1Button.id == 3) {
			SingleplayerServerController.exportLevel(worldName, IPCPacket05RequestData.REQUEST_LEVEL_EAG);
			this.minecraft.displayScreen(new ScreenIntegratedServerBusy(selectLevel, "singleplayer.busy.exporting.1", "singleplayer.failed.exporting.1", () -> {
				byte[] b = SingleplayerServerController.getExportResponse();
				if(b != null) {
					EagRuntime.downloadFileWithName(worldName + ".epk", b);
					return true;
				}
				return false;
			}));
		}else if(par1Button.id == 4) {
			SingleplayerServerController.exportLevel(worldName, IPCPacket05RequestData.REQUEST_LEVEL_MCA);
			this.minecraft.displayScreen(new ScreenIntegratedServerBusy(selectLevel, "singleplayer.busy.exporting.2", "singleplayer.failed.exporting.2", () -> {
				byte[] b = SingleplayerServerController.getExportResponse();
				if(b != null) {
					EagRuntime.downloadFileWithName(worldName + ".zip", b);
					return true;
				}
				return false;
			}));
		}else if(par1Button.id == 5) {
			this.minecraft.displayScreen(new ConfirmScreen(this, I18n.get("singleplayer.backup.clearPlayerData.warning1"),
					I18n.get("singleplayer.backup.clearPlayerData.warning2", worldName, EaglerProfile.getName()), 0));
		}
	}
	
	public void confirmClicked(boolean par1, int par2) {
		if(par1) {
			SingleplayerServerController.clearPlayerData(worldName);
			this.minecraft.displayScreen(new ScreenIntegratedServerBusy(this, "singleplayer.busy.clearplayers", "singleplayer.failed.clearplayers", SingleplayerServerController::isReady));
		}else {
			mc.displayScreen(this);
		}
	}

}