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

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ScreenNoRelays extends Screen {

	private Screen parent;
	private String title1;
	private String title2;
	private String title3;

	public ScreenNoRelays(Screen parent, String title) {
		this.parent = parent;
		this.title1 = title;
		this.title2 = null;
		this.title3 = null;
	}

	public ScreenNoRelays(Screen parent, String title1, String title2, String title3) {
		this.parent = parent;
		this.title1 = title1;
		this.title2 = title2;
		this.title3 = title3;
	}

	public void initGui() {
		buttonList.clear();
		buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 4 - 60 + 145, I18n.get("gui.cancel")));
		buttonList.add(new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 4 - 60 + 115, I18n.get("directConnect.lanLevelRelay")));
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.font, I18n.get(title1), this.width / 2, this.height / 4 - 60 + 70, 16777215);
		if(title2 != null) {
			this.drawCenteredString(this.font, I18n.get(title2), this.width / 2, this.height / 4 - 60 + 80, 0xCCCCCC);
		}
		if(title3 != null) {
			this.drawCenteredString(this.font, I18n.get(title3), this.width / 2, this.height / 4 - 60 + 90, 0xCCCCCC);
		}
		super.drawScreen(par1, par2, par3);
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			mc.displayScreen(parent);
		}else if(par1Button.id == 1) {
			mc.displayScreen(ScreenLANInfo.showLANInfoScreen(new ScreenRelay(parent)));
		}
	}

}