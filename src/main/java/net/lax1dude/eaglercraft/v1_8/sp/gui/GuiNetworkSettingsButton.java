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

import net.lax1dude.eaglercraft.v1_8.Mouse;
import net.lax1dude.eaglercraft.v1_8.internal.EnumCursorType;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.sp.lan.LANServerController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public class GuiNetworkSettingsButton extends Gui {

	private final Screen screen;
	private final String text;
	private final Minecraft mc;

	public GuiNetworkSettingsButton(Screen screen) {
		this.screen = screen;
		this.text = I18n.get("directConnect.lanLevelRelay");
		this.minecraft = Minecraft.getMinecraft();
	}

	public void drawScreen(int xx, int yy) {
		GlStateManager.pushMatrix();
		GlStateManager.scale(0.75f, 0.75f, 0.75f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		int w = mc.font.getStringWidth(text);
		boolean hover = xx > 1 && yy > 1 && xx < (w * 3 / 4) + 7 && yy < 12;
		if(hover) {
			Mouse.showCursor(EnumCursorType.HAND);
		}

		drawString(mc.font, ChatFormatting.UNDERLINE + text, 5, 5, hover ? 0xFFEEEE22 : 0xFFCCCCCC);

		GlStateManager.popMatrix();
	}

	public void mouseClicked(int xx, int yy, int btn) {
		int w = mc.font.getStringWidth(text);
		if(xx > 2 && yy > 2 && xx < (w * 3 / 4) + 5 && yy < 12) {
			if(LANServerController.supported()) {
				mc.displayScreen(ScreenLANInfo.showLANInfoScreen(new ScreenRelay(screen)));
			}else {
				mc.displayScreen(new ScreenLANNotSupported(screen));
			}
			this.minecraft.getSoundManager().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
		}
	}

}