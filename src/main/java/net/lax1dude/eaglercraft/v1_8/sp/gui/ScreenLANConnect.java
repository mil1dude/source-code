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

import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.minecraft.EnumInputEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;

public class ScreenLANConnect extends Screen {

	private final Screen parent;
	private EditBox codeTextField;
	private final GuiNetworkSettingsButton relaysButton;

	private static String lastCode = "";

	public ScreenLANConnect(Screen parent) {
		this.parent = parent;
		this.relaysButton = new GuiNetworkSettingsButton(this);
	}

	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 4 + 96 + 12, I18n.get("directConnect.lanLevelJoin")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.get("gui.cancel")));
		this.codeTextField = new EditBox(2, this.font, this.width / 2 - 100, this.height / 4 + 27, 200, 20);
		this.codeTextField.setMaxStringLength(48);
		this.codeTextField.setFocused(true);
		this.codeTextField.setText(lastCode);
		this.buttonList.get(0).enabled = this.codeTextField.getText().trim().length() > 0;
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		lastCode = this.codeTextField.getText().trim();
	}

	protected void keyTyped(char par1, int par2) {
		if (this.codeTextField.textboxKeyTyped(par1, par2)) {
			((net.minecraft.client.gui.components.Button) this.buttonList.get(0)).enabled = this.codeTextField.getText().trim().length() > 0;
		} else if (par2 == 28) {
			this.actionPerformed(this.buttonList.get(0));
		}
	}

	public void updateScreen() {
		this.codeTextField.updateCursorCounter();
	}

	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		this.codeTextField.mouseClicked(par1, par2, par3);
		this.relaysButton.mouseClicked(par1, par2, par3);
	}

	public void drawScreen(int xx, int yy, float pt) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.font, I18n.get("selectServer.direct"), this.width / 2, this.height / 4 - 60 + 20, 16777215);
		this.drawString(this.font, I18n.get("directConnect.lanLevelCode"), this.width / 2 - 100, this.height / 4 + 12, 10526880);
		this.drawCenteredString(this.font, I18n.get("directConnect.networkSettingsNote"), this.width / 2, this.height / 4 + 63, 10526880);
		this.drawCenteredString(this.font, I18n.get("directConnect.ipGrabNote"), this.width / 2, this.height / 4 + 77, 10526880);
		this.codeTextField.drawTextBox();
		super.drawScreen(xx, yy, pt);
		this.relaysButton.drawScreen(xx, yy);
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 1) {
			mc.displayScreen(parent);
		}else if(par1Button.id == 0) {
			mc.displayScreen(new ScreenLANConnecting(parent, this.codeTextField.getText().trim()));
		}
	}

	@Override
	public boolean showCopyPasteButtons() {
		return codeTextField.isFocused();
	}

	@Override
	public void fireInputEvent(EnumInputEvent event, String param) {
		codeTextField.fireInputEvent(event, param);
	}

}