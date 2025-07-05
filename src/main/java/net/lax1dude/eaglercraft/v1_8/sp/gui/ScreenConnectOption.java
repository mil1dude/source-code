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

import net.lax1dude.eaglercraft.v1_8.sp.lan.LANServerController;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.resources.language.I18n;

public class ScreenConnectOption extends Screen {

	private final JoinMultiplayerScreen guiScreen;
	private String title;
	private String prompt;

	private final GuiNetworkSettingsButton relaysButton;

	public ScreenConnectOption(JoinMultiplayerScreen guiScreen) {
		this.guiScreen = guiScreen;
		this.relaysButton = new GuiNetworkSettingsButton(this);
	}

	public void initGui() {
		title = I18n.get("selectServer.direct");
		prompt = I18n.get("directConnect.prompt");
		buttonList.clear();
		buttonList.add(new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 4 - 60 + 90, I18n.get("directConnect.serverJoin")));
		buttonList.add(new net.minecraft.client.gui.components.Button(2, this.width / 2 - 100, this.height / 4 - 60 + 115, I18n.get("directConnect.lanLevel")));
		buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 4 - 60 + 155, I18n.get("gui.cancel")));
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			guiScreen.cancelDirectConnect();
			mc.displayScreen(guiScreen);
		}else if(par1Button.id == 1) {
			mc.displayGuiScreen(new GuiMultiplayer(guiScreen));
		}else if(par1Button.id == 2) {
			if(LANServerController.supported()) {
				guiScreen.cancelDirectConnect();
				mc.displayScreen(ScreenLANInfo.showLANInfoScreen(new ScreenLANConnect(guiScreen)));
			}else {
				mc.displayScreen(new ScreenLANNotSupported(this));
			}
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.font, title, this.width / 2, this.height / 4 - 60 + 20, 16777215);
		this.drawCenteredString(this.font, prompt, this.width / 2, this.height / 4 - 60 + 55, 0x999999);
		super.drawScreen(par1, par2, par3);
		relaysButton.drawScreen(par1, par2);
	}

	protected void mouseClicked(int par1, int par2, int par3) {
		relaysButton.mouseClicked(par1, par2, par3);
		super.mouseClicked(par1, par2, par3);
	}

}