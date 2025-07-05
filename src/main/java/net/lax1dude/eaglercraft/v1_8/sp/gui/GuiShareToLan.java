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

import net.lax1dude.eaglercraft.v1_8.internal.PlatformWebRTC;
import net.lax1dude.eaglercraft.v1_8.minecraft.EnumInputEvent;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.sp.lan.LANServerController;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelSettings;

public class GuiShareToLan extends Screen {
	/**
	 * A reference to the screen object that created this. Used for navigating
	 * between screens.
	 */
	private final Screen parentScreen;
	private net.minecraft.client.gui.components.Button buttonAllowCommandsToggle;
	private net.minecraft.client.gui.components.Button buttonGameMode;
	private net.minecraft.client.gui.components.Button buttonHiddenToggle;

	/**
	 * The currently selected game mode. One of 'survival', 'creative', or
	 * 'adventure'
	 */
	private String gameMode;

	/**
	 * True if 'Allow Cheats' is currently enabled
	 */
	private boolean allowCommands = false;

	private final GuiNetworkSettingsButton relaysButton;

	private boolean hiddenToggle = false;

	private EditBox codeTextField;

	public GuiShareToLan(Screen par1Screen, String gameMode) {
		this.parentScreen = par1Screen;
		this.relaysButton = new GuiNetworkSettingsButton(this);
		this.gameMode = gameMode;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(new net.minecraft.client.gui.components.Button(101, this.width / 2 - 155, this.height - 28, 140, 20,
				I18n.get("lanServer.start")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(102, this.width / 2 + 5, this.height - 28, 140, 20,
				I18n.get("gui.cancel")));
		this.buttonList.add(this.buttonGameMode = new net.minecraft.client.gui.components.Button(104, this.width / 2 - 155, 135, 140, 20,
				I18n.get("selectLevel.gameMode")));
		this.buttonList.add(this.buttonAllowCommandsToggle = new net.minecraft.client.gui.components.Button(103, this.width / 2 + 5, 135, 140, 20,
				I18n.get("selectLevel.allowCommands")));
		this.buttonGameMode.enabled = this.buttonAllowCommandsToggle.enabled = !mc.isDemo();
		this.buttonList.add(this.buttonHiddenToggle = new net.minecraft.client.gui.components.Button(105, this.width / 2 - 75, 165, 140, 20,
				I18n.get("lanServer.hidden")));
		this.codeTextField = new EditBox(0, this.font, this.width / 2 - 100, 80, 200, 20);
		this.codeTextField.setText(mc.player.getName() + "'s Level");
		this.codeTextField.setFocused(true);
		this.codeTextField.setMaxStringLength(252);
		this.func_74088_g();
	}

	private void func_74088_g() {
		this.buttonGameMode.displayString = I18n.get("selectLevel.gameMode") + ": "
				+ I18n.get("selectLevel.gameMode." + this.gameMode);
		this.buttonAllowCommandsToggle.displayString = I18n.get("selectLevel.allowCommands")
				+ " ";
		this.buttonHiddenToggle.displayString = I18n.get("lanServer.hidden")
				+ " ";

		if (this.allowCommands) {
			this.buttonAllowCommandsToggle.displayString = this.buttonAllowCommandsToggle.displayString
					+ I18n.get("options.on");
		} else {
			this.buttonAllowCommandsToggle.displayString = this.buttonAllowCommandsToggle.displayString
					+ I18n.get("options.off");
		}

		if (this.hiddenToggle) {
			this.buttonHiddenToggle.displayString = this.buttonHiddenToggle.displayString
					+ I18n.get("options.on");
		} else {
			this.buttonHiddenToggle.displayString = this.buttonHiddenToggle.displayString
					+ I18n.get("options.off");
		}
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if (par1Button.id == 102) {
			this.minecraft.displayScreen(this.parentScreen);
		} else if (par1Button.id == 104) {
			if(!mc.isDemo()) {
				if (this.gameMode.equals("survival")) {
					this.gameMode = "creative";
				} else if (this.gameMode.equals("creative")) {
					this.gameMode = "adventure";
				} else if (this.gameMode.equals("adventure")) {
					this.gameMode = "spectator";
				} else {
					this.gameMode = "survival";
				}
	
				this.func_74088_g();
			}
		} else if (par1Button.id == 103) {
			if(!mc.isDemo()) {
				this.allowCommands = !this.allowCommands;
				this.func_74088_g();
			}
		} else if (par1Button.id == 105) {
			this.hiddenToggle = !this.hiddenToggle;
			this.func_74088_g();
		} else if (par1Button.id == 101) {
			if (LANServerController.isLANOpen()) {
				return;
			}
			PlatformWebRTC.startRTCLANServer();
			String worldName = this.codeTextField.getText().trim();
			if (worldName.isEmpty()) {
				worldName = mc.player.getName() + "'s Level";
			}
			if (worldName.length() >= 252) {
				worldName = worldName.substring(0, 252);
			}
			this.minecraft.displayScreen(null);
			LoadingScreenRenderer ls = mc.loadingScreen;
			String code = LANServerController.shareToLAN((msg) -> ls.eaglerShow(msg, null), worldName, hiddenToggle);
			if (code != null) {
				SingleplayerServerController.configureLAN(LevelSettings.GameType.getByName(this.gameMode), this.allowCommands);
				this.minecraft.ingameGUI.getChatGUI().printChatMessage(new Component(I18n.get("lanServer.opened")
						.replace("$relay$", LANServerController.getCurrentURI()).replace("$code$", code)));
			} else {
				this.minecraft.displayScreen(new ScreenNoRelays(this, "noRelay.titleFail"));
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.font, I18n.get("lanServer.title"), this.width / 2,
				35, 16777215);
		this.drawCenteredString(this.font, I18n.get("lanServer.worldName"), this.width / 2,
				62, 16777215);
		this.drawCenteredString(this.font, I18n.get("lanServer.otherPlayers"),
				this.width / 2, 112, 16777215);
		this.drawCenteredString(this.font, I18n.get("lanServer.ipGrabNote"),
				this.width / 2, 195, 16777215);
		super.drawScreen(par1, par2, par3);
		this.relaysButton.drawScreen(par1, par2);
		this.codeTextField.drawTextBox();
	}

	public void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		this.relaysButton.mouseClicked(par1, par2, par3);
		this.codeTextField.mouseClicked(par1, par2, par3);
	}

	protected void keyTyped(char c, int k) {
		super.keyTyped(c, k);
		this.codeTextField.textboxKeyTyped(c, k);
	}

	public void updateScreen() {
		super.updateScreen();
		this.codeTextField.updateCursorCounter();
	}

	public boolean blockPTTKey() {
		return this.codeTextField.isFocused();
	}

	@Override
	public boolean showCopyPasteButtons() {
		return this.codeTextField.isFocused();
	}

	@Override
	public void fireInputEvent(EnumInputEvent event, String param) {
		this.codeTextField.fireInputEvent(event, param);
	}

}