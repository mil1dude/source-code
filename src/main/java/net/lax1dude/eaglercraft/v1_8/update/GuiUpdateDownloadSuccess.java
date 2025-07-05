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

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class GuiUpdateDownloadSuccess extends Screen {

	protected final Screen parent;
	protected final UpdateDataObj updateData;

	public GuiUpdateDownloadSuccess(Screen parent, UpdateDataObj updateData) {
		this.parent = parent;
		this.updateData = updateData;
	}

	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 6 + 56, I18n.get("updateSuccess.downloadOffline")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 6 + 86, I18n.get("updateSuccess.installToBootMenu")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(2, this.width / 2 - 100, this.height / 6 + 130, I18n.get("gui.cancel")));
	}

	public void actionPerformed(net.minecraft.client.gui.components.Button btn) {
		if(btn.id == 0) {
			this.minecraft.loadingScreen.eaglerShow(I18n.get("updateSuccess.downloading"), null);
			UpdateService.quine(updateData.clientSignature, updateData.clientBundle);
			this.minecraft.displayScreen(parent);
		}else if(btn.id == 1) {
			this.minecraft.displayScreen(new GuiUpdateInstallOptions(this, parent, updateData));
		}else if(btn.id == 2) {
			this.minecraft.displayScreen(parent);
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(fontRendererObj, I18n.get("updateSuccess.title"), this.width / 2, 50, 11184810);
		this.drawCenteredString(fontRendererObj,
				updateData.clientSignature.bundleDisplayName + " " + updateData.clientSignature.bundleDisplayVersion,
				this.width / 2, 70, 0xFFFFAA);
		super.drawScreen(par1, par2, par3);
	}

}