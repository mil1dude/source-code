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
import net.lax1dude.eaglercraft.v1_8.internal.FileChooserResult;
import net.lax1dude.eaglercraft.v1_8.minecraft.ScreenGenericErrorMessage;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ScreenImportExportProfile extends Screen {

	private Screen back;
	private boolean waitingForFile = false;

	public ScreenImportExportProfile(Screen back) {
		this.back = back;
	}

	public void initGui() {
		this.buttonList.add(new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 4 + 40, I18n.get("settingsBackup.importExport.import")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(2, this.width / 2 - 100, this.height / 4 + 65, I18n.get("settingsBackup.importExport.export")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 4 + 130, I18n.get("gui.cancel")));
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			mc.displayScreen(back);
		}else if(par1Button.id == 1) {
			waitingForFile = true;
			EagRuntime.displayFileChooser(null, "epk");
		}else if(par1Button.id == 2) {
			mc.displayScreen(new ScreenExportProfile(back));
		}
	}

	public void updateScreen() {
		if(waitingForFile && EagRuntime.fileChooserHasResult()) {
			waitingForFile = false;
			FileChooserResult result = EagRuntime.getFileChooserResult();
			if(result != null) {
				mc.loadingScreen.eaglerShow(I18n.get("settingsBackup.importing.1"), I18n.get("settingsBackup.importing.2"));
				ProfileImporter importer = new ProfileImporter(result.fileData);
				try {
					importer.readHeader();
					mc.displayScreen(new ScreenImportProfile(importer, back));
				}catch(IOException ex) {
					try {
						importer.close();
					} catch (IOException e) {
					}
					EagRuntime.debugPrintStackTrace(ex);
					mc.displayScreen(new ScreenGenericErrorMessage("settingsBackup.importing.failed.1", "settingsBackup.importing.failed.2", back));
				}
			}
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		
		this.drawCenteredString(this.font, I18n.get("settingsBackup.importExport.title"), this.width / 2, this.height / 4, 16777215);
		
		super.drawScreen(par1, par2, par3);
	}
}