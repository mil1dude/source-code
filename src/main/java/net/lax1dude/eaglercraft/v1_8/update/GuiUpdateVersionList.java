/*
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
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

package net.lax1dude.eaglercraft.v1_8.update;

import java.io.IOException;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class GuiUpdateVersionList extends Screen {

	final Screen back;
	GuiUpdateVersionSlot slots;
	int selected;
	net.minecraft.client.gui.components.Button downloadButton;
	int mx = 0;
	int my = 0;
	String tooltip = null;

	public GuiUpdateVersionList(Screen back) {
		this.back = back;
	}

	public void initGui() {
		selected = -1;
		buttonList.clear();
		buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 + 54, this.height - 28, 100, 20, I18n.get("gui.done")));
		buttonList.add(downloadButton = new net.minecraft.client.gui.components.Button(1, this.width / 2 - 50, this.height - 28, 100, 20, I18n.get("updateList.download")));
		buttonList.add(new net.minecraft.client.gui.components.Button(2, this.width / 2 - 154, this.height - 28, 100, 20, I18n.get("updateList.refresh")));
		slots = new GuiUpdateVersionSlot(this);
		updateButtons();
	}

	void updateButtons() {
		downloadButton.enabled = selected != -1;
	}

	static Minecraft getMinecraft(GuiUpdateVersionList screen) {
		return screen.minecraft;
	}

	public void actionPerformed(net.minecraft.client.gui.components.Button btn) {
		switch(btn.id) {
		case 1:
			if(selected != -1) {
				UpdateService.startClientUpdateFrom(slots.certList.get(selected));
			}
		case 0:
			mc.displayScreen(back);
			break;
		case 2:
			this.initGui();
			break;
		default:
			break;
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		mx = par1;
		my = par2;
		slots.drawScreen(par1, par2, par3);
		this.drawCenteredString(fontRendererObj, I18n.get("updateList.title"), this.width / 2, 16, 16777215);
		this.drawCenteredString(fontRendererObj, I18n.get("updateList.note.0"), this.width / 2, this.height - 55, 0x888888);
		this.drawCenteredString(fontRendererObj, I18n.get("updateList.note.1"), this.width / 2, this.height - 45, 0x888888);
		super.drawScreen(par1, par2, par3);
		if(tooltip != null) {
			drawHoveringText(mc.font.listFormattedStringToWidth(tooltip, 180), par1, par2);
			GlStateManager.disableLighting();
			tooltip = null;
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		slots.handleMouseInput();
	}

	@Override
	public void handleTouchInput() throws IOException {
		super.handleTouchInput();
		slots.handleTouchInput();
	}

}