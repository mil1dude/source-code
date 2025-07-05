/*
 * Copyright (c) 2022-2024 lax1dude. All Rights Reserved.
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

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.sp.ipc.IPCPacket15Crashed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button; // MCP Reborn 1.21.4 package
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ScreenIntegratedServerBusy extends Screen {

	public final Screen menu;
	private net.minecraft.client.gui.components.Button killTask;
	public final String failMessage;
	private BooleanSupplier checkTaskComplete;
	private Runnable taskKill;
	private String lastStatus;
	private String currentStatus;
	private BiConsumer<Screen, IPCPacket15Crashed[]> onException;
	private int areYouSure;
	
	private long startStartTime;
	
	private static final Runnable defaultTerminateAction = () -> {
		if(SingleplayerServerController.canKillWorker()) {
			SingleplayerServerController.killWorker();
			Minecraft.getMinecraft().displayScreen(new ScreenIntegratedServerFailed("singleplayer.failed.killed", new TitleScreen()));
		}else {
			EagRuntime.showPopup("Cannot kill worker tasks on desktop runtime!");
		}
	};
	
	public static Screen createException(Screen ok, String msg, IPCPacket15Crashed[] exceptions) {
		ok = new ScreenIntegratedServerFailed(msg, ok);
		if(exceptions != null) {
			for(int i = exceptions.length - 1; i >= 0; --i) {
				ok = new ScreenIntegratedServerCrashed(ok, exceptions[i].crashReport);
			}
		}
		return ok;
	}
	
	private static final BiConsumer<Screen, IPCPacket15Crashed[]> defaultExceptionAction = (t, u) -> {
		ScreenIntegratedServerBusy tt = (ScreenIntegratedServerBusy) t;
		Minecraft.getMinecraft().displayScreen(createException(tt.menu, tt.failMessage, u));
	};
	
	public ScreenIntegratedServerBusy(Screen menu, String progressMessage, String failMessage, BooleanSupplier checkTaskComplete) {
		this(menu, progressMessage, failMessage, checkTaskComplete, defaultExceptionAction, defaultTerminateAction);
	}
	
	public ScreenIntegratedServerBusy(Screen menu, String progressMessage, String failMessage, BooleanSupplier checkTaskComplete, BiConsumer<Screen, IPCPacket15Crashed[]> exceptionAction) {
		this(menu, progressMessage, failMessage, checkTaskComplete, exceptionAction, defaultTerminateAction);
	}
	
	public ScreenIntegratedServerBusy(Screen menu, String progressMessage, String failMessage, BooleanSupplier checkTaskComplete, Runnable onTerminate) {
		this(menu, progressMessage, failMessage, checkTaskComplete, defaultExceptionAction, onTerminate);
	}
	
	public ScreenIntegratedServerBusy(Screen menu, String progressMessage, String failMessage, BooleanSupplier checkTaskComplete, BiConsumer<Screen, IPCPacket15Crashed[]> onException, Runnable onTerminate) {
		this.menu = menu;
		this.failMessage = failMessage;
		this.checkTaskComplete = checkTaskComplete;
		this.onException = onException;
		this.taskKill = onTerminate;
		this.lastStatus = SingleplayerServerController.worldStatusString();
		this.currentStatus = progressMessage;
	}
	
	public void initGui() {
		if(startStartTime == 0) this.startStartTime = EagRuntime.steadyTimeMillis();
		areYouSure = 0;
		this.buttonList.add(killTask = new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 3 + 50, I18n.get("singleplayer.busy.killTask")));
		killTask.enabled = false;
	}
	
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		int top = this.height / 3;
		
		long millis = EagRuntime.steadyTimeMillis();
		
		String str = I18n.get(currentStatus);
		
		long dots = (millis / 500l) % 4l;
		this.drawString(fontRendererObj, str + (dots > 0 ? "." : "") + (dots > 1 ? "." : "") + (dots > 2 ? "." : ""), (this.width - this.font.getStringWidth(str)) / 2, top + 10, 0xFFFFFF);
		
		if(areYouSure > 0) {
			this.drawCenteredString(fontRendererObj, I18n.get("singleplayer.busy.cancelWarning"), this.width / 2, top + 25, 0xFF8888);
		}else {
			float prog = SingleplayerServerController.worldStatusProgress();
			if(this.currentStatus.equals(this.lastStatus) && prog > 0.01f) {
				this.drawCenteredString(fontRendererObj, (prog > 1.0f ? ("(" + (prog > 1000000.0f ? "" + (int)(prog / 1000000.0f) + "MB" :
					(prog > 1000.0f ? "" + (int)(prog / 1000.0f) + "kB" : "" + (int)prog + "B")) + ")") : "" + (int)(prog * 100.0f) + "%"), this.width / 2, top + 25, 0xFFFFFF);
			}else {
				long elapsed = (millis - startStartTime) / 1000l;
				if(elapsed > 3) {
					this.drawCenteredString(fontRendererObj, "(" + elapsed + "s)", this.width / 2, top + 25, 0xFFFFFF);
				}
			}
		}
		
		super.drawScreen(par1, par2, par3);
	}
	
	public void updateScreen() {
		long millis = EagRuntime.steadyTimeMillis();
		if(millis - startStartTime > 6000l && SingleplayerServerController.canKillWorker()) {
			killTask.enabled = true;
		}
		if(SingleplayerServerController.didLastCallFail() || !SingleplayerServerController.isIntegratedServerWorkerAlive()) {
			onException.accept(this, SingleplayerServerController.worldStatusErrors());
			return;
		}
		if(checkTaskComplete.getAsBoolean()) {
			this.minecraft.displayScreen(menu);
		}
		String str = SingleplayerServerController.worldStatusString();
		if(!lastStatus.equals(str)) {
			lastStatus = str;
			currentStatus = str;
		}
		killTask.displayString = I18n.get(areYouSure > 0 ? "singleplayer.busy.confirmCancel" : "singleplayer.busy.killTask");
		if(areYouSure > 0) {
			--areYouSure;
		}
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			if(areYouSure <= 0) {
				areYouSure = 80;
			}else if(areYouSure <= 65) {
				taskKill.run();
			}
		}
	}

	public boolean shouldHangupIntegratedServer() {
		return false;
	}

	public boolean canCloseGui() {
		return false;
	}

}