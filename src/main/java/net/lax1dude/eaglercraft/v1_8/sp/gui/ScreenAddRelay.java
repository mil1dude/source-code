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
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.minecraft.EnumInputEvent;
import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayManager;
import net.minecraft.client.gui.components.Button; // MCP Reborn 1.21.4 package
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;

public class ScreenAddRelay extends Screen {

	/** This GUI's parent GUI. */
	private ScreenRelay parentGui;
	private EditBox serverAddress;
	private EditBox serverName;

	public ScreenAddRelay(ScreenRelay par1Screen) {
		this.parentGui = par1Screen;
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		this.serverName.updateCursorCounter();
		this.serverAddress.updateCursorCounter();
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.parentGui.addNewName = RelayManager.relayManager.makeNewRelayName();
		this.parentGui.addNewAddr = "";
		this.parentGui.addNewPrimary = RelayManager.relayManager.count() == 0;
		int sslOff = EagRuntime.requireSSL() ? 36 : 0;
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 4 + 96 + 12 + sslOff, I18n.get("addRelay.add")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 4 + 120 + 12 + sslOff, I18n.get("gui.cancel")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(2, this.width / 2 - 100, 142, I18n.get("addRelay.primary") + ": " + (this.parentGui.addNewPrimary ? I18n.get("gui.yes") : I18n.get("gui.no"))));
		this.serverName = new EditBox(3, this.font, this.width / 2 - 100, 106, 200, 20);
		this.serverAddress = new EditBox(4, this.font, this.width / 2 - 100, 66, 200, 20);
		this.serverAddress.setMaxStringLength(128);
		this.serverAddress.setFocused(true);
		((net.minecraft.client.gui.components.Button) this.buttonList.get(0)).enabled = this.serverAddress.getText().length() > 0 && this.serverAddress.getText().split(":").length > 0 && this.serverName.getText().length() > 0;
		this.serverName.setText(this.parentGui.addNewName);
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
				this.parentGui.confirmClicked(false, 0);
			} else if (par1Button.id == 0) {
				this.parentGui.addNewName = this.serverName.getText();
				this.parentGui.addNewAddr = this.serverAddress.getText();
				this.parentGui.confirmClicked(true, 0);
			} else if (par1Button.id == 2) {
				this.parentGui.addNewPrimary = !this.parentGui.addNewPrimary;
				((net.minecraft.client.gui.components.Button) this.buttonList.get(2)).displayString = I18n.get("addRelay.primary") + ": " + (this.parentGui.addNewPrimary ? I18n.get("gui.yes") : I18n.get("gui.no"));
			}
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	protected void keyTyped(char par1, int par2) {
		this.serverName.textboxKeyTyped(par1, par2);
		this.serverAddress.textboxKeyTyped(par1, par2);

		if (par1 == 9) {
			if (this.serverName.isFocused()) {
				this.serverName.setFocused(false);
				this.serverAddress.setFocused(true);
			} else {
				this.serverName.setFocused(true);
				this.serverAddress.setFocused(false);
			}
		}

		if (par1 == 13) {
			this.actionPerformed((net.minecraft.client.gui.components.Button) this.buttonList.get(0));
		}

		((net.minecraft.client.gui.components.Button) this.buttonList.get(0)).enabled = this.serverAddress.getText().length() > 0 && this.serverAddress.getText().split(":").length > 0 && this.serverName.getText().length() > 0;
	}

	/**
	 * Called when the mouse is clicked.
	 */
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		this.serverAddress.mouseClicked(par1, par2, par3);
		this.serverName.mouseClicked(par1, par2, par3);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3) {
		this.drawBackground(0);
		this.drawCenteredString(this.font, I18n.get("addRelay.title"), this.width / 2, 17, 16777215);
		this.drawString(this.font, I18n.get("addRelay.address"), this.width / 2 - 100, 53, 10526880);
		this.drawString(this.font, I18n.get("addRelay.name"), this.width / 2 - 100, 94, 10526880);
		if(EagRuntime.requireSSL()) {
			this.drawCenteredString(this.font, I18n.get("addServer.SSLWarn1"), this.width / 2, 169, 0xccccff);
			this.drawCenteredString(this.font, I18n.get("addServer.SSLWarn2"), this.width / 2, 181, 0xccccff);
		}
		this.serverName.drawTextBox();
		this.serverAddress.drawTextBox();
		super.drawScreen(par1, par2, par3);
	}

	public boolean blockPTTKey() {
		return this.serverName.isFocused() || this.serverAddress.isFocused();
	}

	@Override
	public boolean showCopyPasteButtons() {
		return this.serverName.isFocused() || this.serverAddress.isFocused();
	}

	@Override
	public void fireInputEvent(EnumInputEvent event, String param) {
		this.serverName.fireInputEvent(event, param);
		this.serverAddress.fireInputEvent(event, param);
	}

}