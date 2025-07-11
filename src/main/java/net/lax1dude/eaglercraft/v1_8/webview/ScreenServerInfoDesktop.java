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

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.PauseMenuCustomizeState;
import net.lax1dude.eaglercraft.v1_8.internal.WebViewOptions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ScreenServerInfoDesktop extends Screen {

	private final Screen parent;
	private final WebViewOptions opts;

	private int timer = 0;
	private boolean hasStarted = false;

	private net.minecraft.client.gui.components.Button btnOpen;

	public ScreenServerInfoDesktop(Screen parent, WebViewOptions opts) {
		this.parent = parent;
		this.opts = opts;
	}

	public void initGui() {
		buttonList.clear();
		buttonList.add(btnOpen = new net.minecraft.client.gui.components.Button(0, (width - 200) / 2, height / 6 + 110, I18n.get("fallbackWebViewScreen.openButton")));
		btnOpen.enabled = false;
		buttonList.add(new net.minecraft.client.gui.components.Button(1, (width - 200) / 2, height / 6 + 140, I18n.get("fallbackWebViewScreen.exitButton")));
	}

	public void updateScreen() {
		++timer;
		if(timer == 2) {
			WebViewOverlayController.endFallbackServer();
			WebViewOverlayController.launchFallback(opts);
		}else if(timer > 2) {
			if(WebViewOverlayController.fallbackRunning()) {
				btnOpen.enabled = WebViewOverlayController.getFallbackURL() != null;
				hasStarted = true;
			}else {
				btnOpen.enabled = false;
			}
		}
	}

	public void actionPerformed(net.minecraft.client.gui.components.Button button) {
		if(button.id == 0) {
			String link = WebViewOverlayController.getFallbackURL();
			if(link != null) {
				EagRuntime.openLink(link);
			}
		}else if(button.id == 1) {
			mc.displayScreen(parent);
		}
	}

	public void onGuiClosed() {
		WebViewOverlayController.endFallbackServer();
	}

	public void drawScreen(int mx, int my, float pt) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, PauseMenuCustomizeState.serverInfoEmbedTitle, this.width / 2, 70, 16777215);
		drawCenteredString(fontRendererObj, I18n.get("fallbackWebViewScreen.text0"), this.width / 2, 90, 11184810);
		String link = WebViewOverlayController.fallbackRunning() ? WebViewOverlayController.getFallbackURL()
				: I18n.get(hasStarted ? "fallbackWebViewScreen.exited" : "fallbackWebViewScreen.startingUp");
		drawCenteredString(fontRendererObj, link != null ? link : I18n.get("fallbackWebViewScreen.pleaseWait"),
				width / 2, 110, 16777215);
		super.drawScreen(mx, my, pt);
	}

	protected boolean isPartOfPauseMenu() {
		return true;
	}

}