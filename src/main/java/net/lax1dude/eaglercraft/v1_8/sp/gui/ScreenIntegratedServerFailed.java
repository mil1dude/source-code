/*
 * Copyright (c) 2023-2024 lax1dude. All Rights Reserved.
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

import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.sp.internal.ClientPlatformSingleplayer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ScreenIntegratedServerFailed extends Screen {

	private String str1;
	private String str2;
	private Screen cont;

	public ScreenIntegratedServerFailed(String str1, String str2, Screen cont) {
		this.str1 = I18n.get(str1);
		this.str2 = I18n.get(str2);
		this.cont = cont;
	}

	public ScreenIntegratedServerFailed(String str2, Screen cont) {
		this.str1 = I18n.get("singleplayer.failed.title");
		this.str2 = I18n.get(str2);
		this.cont = cont;
	}

	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 6 + 96, I18n.get("singleplayer.crashed.continue")));
		if(!ClientPlatformSingleplayer.isRunningSingleThreadMode() && ClientPlatformSingleplayer.isSingleThreadModeSupported()) {
			this.buttonList.add(new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 6 + 126, I18n.get("singleplayer.crashed.singleThreadCont")));
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(fontRendererObj, str1, this.width / 2, 70, 11184810);
		this.drawCenteredString(fontRendererObj, str2, this.width / 2, 90, 16777215);
		super.drawScreen(par1, par2, par3);
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			this.minecraft.displayScreen(cont);
		}else if(par1Button.id == 1) {
			if(SingleplayerServerController.canKillWorker()) {
				SingleplayerServerController.killWorker();
			}
			this.minecraft.displayScreen(new ScreenIntegratedServerStartup(new TitleScreen(), true));
		}
	}

}