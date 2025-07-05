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
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.internal.FileChooserResult;
import net.lax1dude.eaglercraft.v1_8.minecraft.EnumInputEvent;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;

public class ScreenNameLevelImport extends Screen {
	private Screen parentScreen;
	private EditBox theEditBox;
	private net.minecraft.client.gui.components.Button loadSpawnChunksBtn;
	private net.minecraft.client.gui.components.Button enhancedGameRulesBtn;
	private int importFormat;
	private FileChooserResult world;
	private String name;
	private boolean timeToImport = false;
	private boolean definetlyTimeToImport = false;
	private boolean isImporting = false;
	private boolean loadSpawnChunks = false;
	private boolean enhancedGameRules = true;

	public ScreenNameLevelImport(Screen menu, FileChooserResult world, int format) {
		this.parentScreen = menu;
		this.importFormat = format;
		this.world = world;
		this.name = world.fileName;
		if(name.length() > 4 && (name.endsWith(".epk") || name.endsWith(".zip"))) {
			name = name.substring(0, name.length() - 4);
		}
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		if(!timeToImport) {
			this.theEditBox.updateCursorCounter();
		}
		if(definetlyTimeToImport && !isImporting) {
			isImporting = true;
			SingleplayerServerController.importLevel(CreateWorldScreen.func_146317_a(mc.getSaveLoader(), this.theEditBox.getText().trim()), world.fileData, importFormat, (byte) ((loadSpawnChunks ? 2 : 0) | (enhancedGameRules ? 1 : 0)));
			mc.displayScreen(new ScreenIntegratedServerBusy(parentScreen, "singleplayer.busy.importing." + (importFormat + 1), "singleplayer.failed.importing." + (importFormat + 1), SingleplayerServerController::isReady));
		}
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui() {
		if(!timeToImport) {
			Keyboard.enableRepeatEvents(true);
			this.buttonList.clear();
			this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 4 + 96 + 12, I18n.get("singleplayer.import.continue")));
			this.buttonList.add(new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.get("gui.cancel")));
			this.theEditBox = new EditBox(2, this.font, this.width / 2 - 100, this.height / 4 + 3, 200, 20);
			this.theEditBox.setFocused(true);
			this.theEditBox.setText(name);
			this.buttonList.add(loadSpawnChunksBtn = new net.minecraft.client.gui.components.Button(2, this.width / 2 - 100, this.height / 4 + 24 + 12, I18n.get("singleplayer.import.loadSpawnChunks", loadSpawnChunks ? I18n.get("gui.yes") : I18n.get("gui.no"))));
			this.buttonList.add(enhancedGameRulesBtn = new net.minecraft.client.gui.components.Button(3, this.width / 2 - 100, this.height / 4 + 48 + 12, I18n.get("singleplayer.import.enhancedGameRules", enhancedGameRules ? I18n.get("gui.yes") : I18n.get("gui.no"))));
		}
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if (par1Button.enabled) {
			if (par1Button.id == 1) {
				EagRuntime.clearFileChooserResult();
				this.minecraft.displayScreen(this.parentScreen);
			} else if (par1Button.id == 0) {
				this.buttonList.clear();
				timeToImport = true;
			} else if (par1Button.id == 2) {
				loadSpawnChunks = !loadSpawnChunks;
				loadSpawnChunksBtn.displayString = I18n.get("singleplayer.import.loadSpawnChunks", loadSpawnChunks ? I18n.get("gui.yes") : I18n.get("gui.no"));
			} else if (par1Button.id == 3) {
				enhancedGameRules = !enhancedGameRules;
				enhancedGameRulesBtn.displayString = I18n.get("singleplayer.import.enhancedGameRules", enhancedGameRules ? I18n.get("gui.yes") : I18n.get("gui.no"));
			}
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	protected void keyTyped(char par1, int par2) {
		this.theEditBox.textboxKeyTyped(par1, par2);
		((net.minecraft.client.gui.components.Button) this.buttonList.get(0)).enabled = this.theEditBox.getText().trim().length() > 0;

		if (par1 == 13) {
			this.actionPerformed((net.minecraft.client.gui.components.Button) this.buttonList.get(0));
		}
	}

	/**
	 * Called when the mouse is clicked.
	 */
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		if(!timeToImport) {
			this.theEditBox.mouseClicked(par1, par2, par3);
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		if(!timeToImport) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.import.title"), this.width / 2, this.height / 4 - 60 + 20, 16777215);
			this.drawString(this.font, I18n.get("singleplayer.import.enterName"), this.width / 2 - 100, this.height / 4 - 60 + 50, 10526880);
			this.drawCenteredString(this.font, I18n.get("createLevel.seedNote"), this.width / 2, this.height / 4 + 90, -6250336);
			this.theEditBox.drawTextBox();
		}else {
			definetlyTimeToImport = true;
			long dots = (EagRuntime.steadyTimeMillis() / 500l) % 4l;
			String str = I18n.get("singleplayer.import.reading", world.fileName);
			this.drawString(fontRendererObj, str + (dots > 0 ? "." : "") + (dots > 1 ? "." : "") + (dots > 2 ? "." : ""), (this.width - this.font.getStringWidth(str)) / 2, this.height / 3 + 10, 0xFFFFFF);
		}
		super.drawScreen(par1, par2, par3);
	}

	@Override
	public boolean showCopyPasteButtons() {
		return theEditBox.isFocused();
	}

	@Override
	public void fireInputEvent(EnumInputEvent event, String param) {
		theEditBox.fireInputEvent(event, param);
	}

}