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

import net.lax1dude.eaglercraft.v1_8.sp.lan.LANServerController;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.level.DemoMode;

public class ScreenDemoPlayLevelSelection extends Screen {

	private Screen mainmenu;
	private net.minecraft.client.gui.components.Button playLevel = null;
	private net.minecraft.client.gui.components.Button joinLevel = null;
	
	public ScreenDemoPlayLevelSelection(Screen mainmenu) {
		this.mainmenu = mainmenu;
	}
	
	public void initGui() {
		this.buttonList.add(playLevel = new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 4 + 40, I18n.get("singleplayer.demo.create.create")));
		this.buttonList.add(joinLevel = new net.minecraft.client.gui.components.Button(2, this.width / 2 - 100, this.height / 4 + 65, I18n.get("singleplayer.demo.create.join")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 4 + 130, I18n.get("gui.cancel")));
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		
		this.drawCenteredString(this.font, I18n.get("singleplayer.demo.create.title"), this.width / 2, this.height / 4, 16777215);
		
		int toolTipColor = 0xDDDDAA;
		if(playLevel.isMouseOver()) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.demo.create.create.tooltip"), this.width / 2, this.height / 4 + 20, toolTipColor);
		}else if(joinLevel.isMouseOver()) {
			this.drawCenteredString(this.font, I18n.get("singleplayer.demo.create.join.tooltip"), this.width / 2, this.height / 4 + 20, toolTipColor);
		}
		
		super.drawScreen(par1, par2, par3);
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			this.minecraft.displayScreen(mainmenu);
		}else if(par1Button.id == 1) {
			this.minecraft.options.hasCreatedDemoLevel = true;
			this.minecraft.options.saveOptions();
			this.minecraft.launchIntegratedServer("Demo Level", "Demo Level", DemoLevel.demoLevelSettings);
		}else if(par1Button.id == 2) {
			if(LANServerController.supported()) {
				this.minecraft.displayScreen(ScreenLANInfo.showLANInfoScreen(new ScreenLANConnect(mainmenu)));
			}else {
				this.minecraft.displayScreen(new ScreenLANNotSupported(mainmenu));
			}
		}
	}

}