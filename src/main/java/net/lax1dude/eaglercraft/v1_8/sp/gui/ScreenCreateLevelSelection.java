/*
 * Copyright (c) 2022-2024 lax1dude. All Rights Reserved.
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
import net.lax1dude.eaglercraft.v1_8.internal.FileChooserResult;
import net.minecraft.client.gui.components.Button; // MCP Reborn 1.21.4 package
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ScreenCreateLevelSelection extends Screen {

	private Screen mainmenu;
	private net.minecraft.client.gui.components.Button worldCreate = null;
	private net.minecraft.client.gui.components.Button worldImport = null;
	private net.minecraft.client.gui.components.Button worldVanilla = null;
	private boolean isImportingEPK = false;
	private boolean isImportingMCA = false;
	
	public ScreenCreateLevelSelection(Screen mainmenu) {
		this.mainmenu = mainmenu;
	}
	
	public void initGui() {
		this.buttonList.add(worldCreate = new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 4 + 40, I18n.get("singleplayer.create.create")));
		this.buttonList.add(worldImport = new net.minecraft.client.gui.components.Button(2, this.width / 2 - 100, this.height / 4 + 65, I18n.get("singleplayer.create.import")));
		this.buttonList.add(worldVanilla = new net.minecraft.client.gui.components.Button(3, this.width / 2 - 100, this.height / 4 + 90, I18n.get("singleplayer.create.vanilla")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 4 + 130, I18n.get("gui.cancel")));
	}
	
	public void updateScreen() {
		if(EagRuntime.fileChooserHasResult() && (isImportingEPK || isImportingMCA)) {
			FileChooserResult fr = EagRuntime.getFileChooserResult();
			if(fr != null) {
				this.minecraft.displayScreen(new ScreenNameLevelImport(mainmenu, fr, isImportingEPK ? 0 : (isImportingMCA ? 1 : -1)));
			}
			isImportingEPK = isImportingMCA = false;
		}
	}
	
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		
		this.drawCenteredString(this.font, I18n.get("singleplayer.create.title"), this.width / 2, this.height / 4, 16777215);
		
		int toolTipColor = 0xDDDDAA;
		if(worldCreate.isMouseOver()) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.create.create.tooltip"), this.width / 2, this.height / 4 + 20, toolTipColor);
		}else if(worldImport.isMouseOver()) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.create.import.tooltip"), this.width / 2, this.height / 4 + 20, toolTipColor);
		}else if(worldVanilla.isMouseOver()) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.create.vanilla.tooltip"), this.width / 2, this.height / 4 + 20, toolTipColor);
		}
		
		super.drawScreen(par1, par2, par3);
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			this.minecraft.displayScreen(mainmenu);
		}else if(par1Button.id == 1) {
			this.minecraft.displayScreen(new CreateWorldScreen(mainmenu));
		}else if(par1Button.id == 2) {
			isImportingEPK = true;
			EagRuntime.displayFileChooser(null, "epk");
		}else if(par1Button.id == 3) {
			isImportingMCA = true;
			EagRuntime.displayFileChooser(null, "zip");
		}
	}

}