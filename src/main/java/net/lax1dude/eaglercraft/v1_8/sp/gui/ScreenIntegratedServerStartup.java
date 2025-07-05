/*
 * Copyright (c) 2023-2024 lax1dude. All Rights Reserved.
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

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.sp.WorkerStartupFailedException;
import net.lax1dude.eaglercraft.v1_8.sp.ipc.IPCPacket15Crashed;
import net.lax1dude.eaglercraft.v1_8.sp.ipc.IPCPacket1CIssueDetected;
import net.minecraft.client.gui.components.Button; // MCP Reborn 1.21.4 package
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.resources.language.I18n;

public class ScreenIntegratedServerStartup extends Screen {

	private final Screen backScreen;
	private final boolean singleThread;
	private static final String[] dotDotDot = new String[] { "", ".", "..", "..." };

	private int counter = 0;

	private net.minecraft.client.gui.components.Button cancelButton;

	public ScreenIntegratedServerStartup(Screen backScreen) {
		this.backScreen = backScreen;
		this.singleThread = false;
	}

	public ScreenIntegratedServerStartup(Screen backScreen, boolean singleThread) {
		this.backScreen = backScreen;
		this.singleThread = singleThread;
	}

	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(cancelButton = new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 3 + 50, I18n.get("singleplayer.busy.killTask")));
		cancelButton.visible = false;
	}

	public void updateScreen() {
		++counter;
		if(counter == 2) {
			try {
				SingleplayerServerController.startIntegratedServerWorker(singleThread);
			}catch(WorkerStartupFailedException ex) {
				mc.displayScreen(new ScreenIntegratedServerFailed(ex.getMessage(), new TitleScreen()));
				return;
			}
		}else if(counter > 2) {
			if(counter > 100 && SingleplayerServerController.canKillWorker() && !singleThread) {
				cancelButton.visible = true;
			}
			IPCPacket15Crashed[] crashReport = SingleplayerServerController.worldStatusErrors();
			if(crashReport != null) {
				mc.displayScreen(ScreenIntegratedServerBusy.createException(new TitleScreen(), "singleplayer.failed.notStarted", crashReport));
			}else if(SingleplayerServerController.isIntegratedServerWorkerStarted()) {
				Screen cont = new SelectWorldScreen(backScreen);
				if(SingleplayerServerController.isRunningSingleThreadMode()) {
					cont = new ScreenIntegratedServerFailed("singleplayer.failed.singleThreadWarning.1", "singleplayer.failed.singleThreadWarning.2", cont);
				} else if (!EagRuntime.getConfiguration().isRamdiskMode()
						&& SingleplayerServerController.isIssueDetected(IPCPacket1CIssueDetected.ISSUE_RAMDISK_MODE)
						&& SingleplayerServerController.canKillWorker()) {
					cont = new ScreenRAMDiskModeDetected(cont);
				}
				mc.displayScreen(cont);
			}
		}
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button parButton) {
		if(parButton.id == 0) {
			SingleplayerServerController.killWorker();
			mc.displayScreen(new ScreenIntegratedServerStartup(new TitleScreen(), true));
		}
	}

	public void drawScreen(int i, int j, float f) {
		this.drawBackground(0);
		String txt = I18n.get("singleplayer.integratedStartup");
		int w = this.font.getStringWidth(txt);
		this.drawString(this.font, txt + dotDotDot[(int)((EagRuntime.steadyTimeMillis() / 300L) % 4L)], (this.width - w) / 2, this.height / 2 - 50, 16777215);
		super.drawScreen(i, j, f);
	}

	public boolean canCloseGui() {
		return false;
	}

}