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

package net.lax1dude.eaglercraft.v1_8.profile;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ScreenDefaultUsernameNote extends Screen {

	private final Screen back;
	private final Screen cont;

	public ScreenDefaultUsernameNote(Screen back, Screen cont) {
		this.back = back;
		this.cont = cont;
	}

	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 6 + 112, I18n.get("defaultUsernameDetected.changeUsername")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 6 + 142, I18n.get("defaultUsernameDetected.continueAnyway")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(2, this.width / 2 - 100, this.height / 6 + 172, I18n.get("defaultUsernameDetected.doNotShow")));
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(fontRendererObj, I18n.get("defaultUsernameDetected.title"), this.width / 2, 70, 11184810);
		this.drawCenteredString(fontRendererObj, I18n.get("defaultUsernameDetected.text0", EaglerProfile.getName()), this.width / 2, 90, 16777215);
		this.drawCenteredString(fontRendererObj, I18n.get("defaultUsernameDetected.text1"), this.width / 2, 105, 16777215);
		this.drawCenteredString(fontRendererObj, I18n.get("defaultUsernameDetected.text2"), this.width / 2, 120, 16777215);
		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(net.minecraft.client.gui.components.Button parButton) {
		if(parButton.id == 0) {
			this.minecraft.displayScreen(back);
		}else if(parButton.id == 1) {
			this.minecraft.displayScreen(cont);
		}else if(parButton.id == 2) {
			this.minecraft.options.hideDefaultUsernameWarning = true;
			this.minecraft.options.saveOptions();
			this.minecraft.displayScreen(cont);
		}
	}

}