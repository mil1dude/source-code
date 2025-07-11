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

package net.lax1dude.eaglercraft.v1_8.webview;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public class ScreenPhishingWarning extends Screen {

	public static boolean hasShownMessage = false;

	private static final ResourceLocation beaconGuiTexture = new ResourceLocation("textures/gui/container/beacon.png");

	private Screen cont;
	private boolean mouseOverCheck;
	private boolean hasCheckedBox;

	public ScreenPhishingWarning(Screen cont) {
		this.cont = cont;
	}

	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 6 + 134, I18n.get("webviewPhishingWaring.continue")));
	}

	public void drawScreen(int mx, int my, float pt) {
		this.drawDefaultBackground();
		this.drawCenteredString(fontRendererObj, ChatFormatting.BOLD + I18n.get("webviewPhishingWaring.title"), this.width / 2, 70, 0xFF4444);
		this.drawCenteredString(fontRendererObj, I18n.get("webviewPhishingWaring.text0"), this.width / 2, 90, 16777215);
		this.drawCenteredString(fontRendererObj, I18n.get("webviewPhishingWaring.text1"), this.width / 2, 102, 16777215);
		this.drawCenteredString(fontRendererObj, I18n.get("webviewPhishingWaring.text2"), this.width / 2, 114, 16777215);
		
		String dontShowAgain = I18n.get("webviewPhishingWaring.dontShowAgain");
		int w = fontRendererObj.getStringWidth(dontShowAgain) + 20;
		int ww = (this.width - w) / 2;
		this.drawString(fontRendererObj, dontShowAgain, ww + 20, 137, 0xCCCCCC);
		
		mouseOverCheck = ww < mx && ww + 17 > mx && 133 < my && 150 > my;
		
		if(mouseOverCheck) {
			GlStateManager.color(0.7f, 0.7f, 1.0f, 1.0f);
		}else {
			GlStateManager.color(0.6f, 0.6f, 0.6f, 1.0f);
		}
		
		mc.getTextureManager().bindTexture(beaconGuiTexture);
		
		GlStateManager.pushMatrix();
		GlStateManager.scale(0.75f, 0.75f, 0.75f);
		drawTexturedModalRect(ww * 4 / 3, 133 * 4 / 3, 22, 219, 22, 22);
		GlStateManager.popMatrix();
		
		if(hasCheckedBox) {
			GlStateManager.pushMatrix();
			GlStateManager.color(1.1f, 1.1f, 1.1f, 1.0f);
			GlStateManager.translate(0.5f, 0.5f, 0.0f);
			drawTexturedModalRect(ww, 133, 90, 222, 16, 16);
			GlStateManager.popMatrix();
		}
		
		super.drawScreen(mx, my, pt);
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			if(hasCheckedBox && !mc.options.hasHiddenPhishWarning) {
				mc.options.hasHiddenPhishWarning = true;
				mc.options.saveOptions();
			}
			hasShownMessage = true;
			mc.displayScreen(cont);
		}
	}

	@Override
	protected void mouseClicked(int mx, int my, int btn) {
		if(btn == 0 && mouseOverCheck) {
			hasCheckedBox = !hasCheckedBox;
			mc.getSoundManager().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
			return;
		}
		super.mouseClicked(mx, my, btn);
	}

}