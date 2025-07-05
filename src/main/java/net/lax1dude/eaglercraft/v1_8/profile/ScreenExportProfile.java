/*
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.profile;

import java.io.IOException;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.minecraft.EaglerFolderResourcePack;
import net.lax1dude.eaglercraft.v1_8.minecraft.ScreenGenericErrorMessage;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ScreenExportProfile extends Screen {

	private Screen back;

	private net.minecraft.client.gui.components.Button exportProfile;
	private boolean doExportProfile = true;
	private net.minecraft.client.gui.components.Button exportSettings;
	private boolean doExportSettings = true;
	private net.minecraft.client.gui.components.Button exportServers;
	private boolean doExportServers = true;
	private net.minecraft.client.gui.components.Button exportResourcePacks;
	private boolean doExportResourcePacks = false;

	public ScreenExportProfile(Screen back) {
		this.back = back;
	}

	public void initGui() {
		this.buttonList.add(exportProfile = new net.minecraft.client.gui.components.Button(2, this.width / 2 - 100, this.height / 4, I18n.get("settingsBackup.export.option.profile") + " " + I18n.get(doExportProfile ? "gui.yes" : "gui.no")));
		this.buttonList.add(exportSettings = new net.minecraft.client.gui.components.Button(3, this.width / 2 - 100, this.height / 4 + 25, I18n.get("settingsBackup.export.option.settings") + " " + I18n.get(doExportSettings ? "gui.yes" : "gui.no")));
		this.buttonList.add(exportServers = new net.minecraft.client.gui.components.Button(4, this.width / 2 - 100, this.height / 4 + 50, I18n.get("settingsBackup.export.option.servers") + " " + I18n.get(doExportServers ? "gui.yes" : "gui.no")));
		this.buttonList.add(exportResourcePacks = new net.minecraft.client.gui.components.Button(5, this.width / 2 - 100, this.height / 4 + 75, I18n.get("settingsBackup.export.option.resourcePacks") + " " + I18n.get(doExportResourcePacks ? "gui.yes" : "gui.no")));
		exportResourcePacks.enabled = EaglerFolderResourcePack.isSupported();
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 4 + 115, I18n.get("settingsBackup.export.option.export")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 4 + 140, I18n.get("gui.cancel")));
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			if(!doExportProfile && !doExportSettings && !doExportServers && !doExportResourcePacks) {
				mc.displayScreen(back);
			}else {
				mc.loadingScreen.eaglerShow(I18n.get("settingsBackup.exporting.1"), I18n.get("settingsBackup.exporting.2"));
				try {
					ProfileExporter.exportProfileAndSettings(doExportProfile, doExportSettings, doExportServers, doExportResourcePacks);
					mc.displayScreen(back);
				} catch (IOException e) {
					EagRuntime.debugPrintStackTrace(e);
					mc.displayScreen(new ScreenGenericErrorMessage("settingsBackup.exporting.failed.1", "settingsBackup.exporting.failed.2", back));
				}
			}
		}else if(par1Button.id == 1) {
			mc.displayScreen(back);
		}else if(par1Button.id == 2) {
			doExportProfile = !doExportProfile;
			exportProfile.displayString = I18n.get("settingsBackup.export.option.profile") + " " + I18n.get(doExportProfile ? "gui.yes" : "gui.no");
		}else if(par1Button.id == 3) {
			doExportSettings = !doExportSettings;
			exportSettings.displayString = I18n.get("settingsBackup.export.option.settings") + " " + I18n.get(doExportSettings ? "gui.yes" : "gui.no");
		}else if(par1Button.id == 4) {
			doExportServers = !doExportServers;
			exportServers.displayString = I18n.get("settingsBackup.export.option.servers") + " " + I18n.get(doExportServers ? "gui.yes" : "gui.no");
		}else if(par1Button.id == 5) {
			doExportResourcePacks = !doExportResourcePacks;
			exportResourcePacks.displayString = I18n.get("settingsBackup.export.option.resourcePacks") + " " + I18n.get(doExportResourcePacks ? "gui.yes" : "gui.no");
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.font, I18n.get("settingsBackup.export.title"), this.width / 2, this.height / 4 - 25, 16777215);
		super.drawScreen(par1, par2, par3);
	}
}