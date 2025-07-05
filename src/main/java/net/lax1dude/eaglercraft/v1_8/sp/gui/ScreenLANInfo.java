/*
 * Copyright (c) 2024 lax1dude, ayunami2000. All Rights Reserved.
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

public class ScreenLANInfo extends Screen {
	private Screen parent;

	public ScreenLANInfo(Screen parent) {
		this.parent = parent;
	}

	public void initGui() {
		buttonList.clear();
		buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, height / 6 + 168, I18n.get("gui.continue")));
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.font, I18n.get("lanInfo.title"), this.width / 2, this.height / 4 - 60 + 20, 16777215);
		this.font.drawSplitString(I18n.get("lanInfo.desc.0") + "\n\n\n" + I18n.get("lanInfo.desc.1", I18n.get("menu.multiplayer"), I18n.get("menu.openToLan")), this.width / 2 - 100, this.height / 4 - 60 + 60, 200, -6250336);
		super.drawScreen(par1, par2, par3);
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			mc.displayScreen(parent);
		}
	}

	private static boolean hasShown = false;

	public static Screen showLANInfoScreen(Screen cont) {
		if(!hasShown) {
			hasShown = true;
			return new ScreenLANInfo(cont);
		}else {
			return cont;
		}
	}

}