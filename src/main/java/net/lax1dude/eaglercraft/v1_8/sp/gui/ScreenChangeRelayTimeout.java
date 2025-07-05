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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ScreenChangeRelayTimeout extends Screen {

	private Screen parent;
	private GuiSlider2 slider;
	private String title;

	public ScreenChangeRelayTimeout(Screen parent) {
		this.parent = parent;
	}

	public void initGui() {
		title = I18n.get("networkSettings.relayTimeoutTitle");
		buttonList.clear();
		buttonList.add(new net.minecraft.client.gui.components.Button(0, width / 2 - 100, height / 3 + 55, I18n.get("gui.done")));
		buttonList.add(new net.minecraft.client.gui.components.Button(1, width / 2 - 100, height / 3 + 85, I18n.get("gui.cancel")));
		slider = new GuiSlider2(0, width / 2 - 100, height / 3 + 10, 200, 20, (mc.options.relayTimeout - 1) / 14.0f, 1.0f) {
			public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
				if(super.mousePressed(par1Minecraft, par2, par3)) {
					this.displayString = "" + (int)((sliderValue * 14.0f) + 1.0f) + "s";
					return true;
				}else {
					return false;
				}
			}
			public void mouseDragged(Minecraft par1Minecraft, int par2, int par3) {
				super.mouseDragged(par1Minecraft, par2, par3);
				this.displayString = "" + (int)((sliderValue * 14.0f) + 1.0f) + "s";
			}
		};
		slider.displayString = "" + mc.options.relayTimeout + "s";
	}

	public void actionPerformed(net.minecraft.client.gui.components.Button btn) {
		if(btn.id == 0) {
			mc.options.relayTimeout = (int)((slider.sliderValue * 14.0f) + 1.0f);
			mc.options.saveOptions();
			mc.displayScreen(parent);
		}else if(btn.id == 1) {
			mc.displayScreen(parent);
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		drawBackground(0);
		drawCenteredString(fontRendererObj, title, width / 2, height / 3 - 20, 0xFFFFFF);
		slider.drawButton(mc, par1, par2);
		super.drawScreen(par1, par2, par3);
	}

	public void mouseClicked(int mx, int my, int button) {
		slider.mousePressed(mc, mx, my);
		super.mouseClicked(mx, my, button);
	}

	public void mouseReleased(int par1, int par2, int par3) {
		if(par3 == 0) {
			slider.mouseReleased(par1, par2);
		}
		super.mouseReleased(par1, par2, par3);
	}

}