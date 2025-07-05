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

package net.lax1dude.eaglercraft.v1_8.profanity_filter;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.ChatFormatting;

public class ScreenContentWarning extends Screen {

	private final Screen cont;
	private boolean enableState;
	private net.minecraft.client.gui.components.Button optButton;

	public ScreenContentWarning(Screen cont) {
		this.cont = cont;
	}

	public void initGui() {
		this.buttonList.clear();
		enableState = mc.options.enableProfanityFilter;
		this.buttonList.add(optButton = new net.minecraft.client.gui.components.Button(1, this.width / 2 - 100, this.height / 6 + 108, I18n.get("options.profanityFilterButton") + ": " + I18n.get(enableState ? "gui.yes" : "gui.no")));
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 6 + 138, I18n.get("gui.done")));
	}

	@Override
	protected void actionPerformed(net.minecraft.client.gui.components.Button parButton) {
		if(parButton.id == 0) {
			mc.options.enableProfanityFilter = enableState;
			mc.options.hasShownProfanityFilter = true;
			mc.options.saveOptions();
			mc.displayScreen(cont);
		}else if(parButton.id == 1) {
			enableState = !enableState;
			optButton.displayString = I18n.get("options.profanityFilterButton") + ": " + I18n.get(enableState ? "gui.yes" : "gui.no");
		}
	}

	public void drawScreen(int mx, int my, float pt) {
		this.drawDefaultBackground();
		this.drawCenteredString(fontRendererObj, ChatFormatting.BOLD + I18n.get("profanityFilterWarning.title"), this.width / 2, 50, 0xFF4444);
		this.drawCenteredString(fontRendererObj, I18n.get("profanityFilterWarning.text0"), this.width / 2, 70, 16777215);
		this.drawCenteredString(fontRendererObj, I18n.get("profanityFilterWarning.text1"), this.width / 2, 82, 16777215);
		this.drawCenteredString(fontRendererObj, I18n.get("profanityFilterWarning.text2"), this.width / 2, 94, 16777215);
		this.drawCenteredString(fontRendererObj, I18n.get("profanityFilterWarning.text4"), this.width / 2, 116, 0xCCCCCC);
		super.drawScreen(mx, my, pt);
	}

}