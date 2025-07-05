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

import java.io.IOException;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.EaglercraftVersion;
import net.lax1dude.eaglercraft.v1_8.profile.EaglerProfile;
import net.lax1dude.eaglercraft.v1_8.socket.ConnectionHandshake;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.sp.socket.ClientIntegratedServerNetworkManager;
import net.lax1dude.eaglercraft.v1_8.sp.socket.NetHandlerSingleplayerLogin;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.chat.Component;

public class ScreenSingleplayerConnecting extends Screen {

	private Screen menu;
	private String message;
	private net.minecraft.client.gui.components.Button killTask;
	private ClientIntegratedServerNetworkManager networkManager = null;
	private int timer = 0;
	
	private long startStartTime;
	private boolean hasOpened = false;
	
	public ScreenSingleplayerConnecting(Screen menu, String message) {
		this.menu = menu;
		this.message = message;
	}
	
	public void initGui() {
		if(startStartTime == 0) this.startStartTime = EagRuntime.steadyTimeMillis();
		this.buttonList.add(killTask = new net.minecraft.client.gui.components.Button(0, this.width / 2 - 100, this.height / 3 + 50, I18n.get("singleplayer.busy.killTask")));
		killTask.enabled = false;
	}
	
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		float f = 2.0f;
		int top = this.height / 3;
		
		long millis = EagRuntime.steadyTimeMillis();
		
		long dots = (millis / 500l) % 4l;
		this.drawString(fontRendererObj, message + (dots > 0 ? "." : "") + (dots > 1 ? "." : "") + (dots > 2 ? "." : ""), (this.width - this.font.getStringWidth(message)) / 2, top + 10, 0xFFFFFF);
		
		long elapsed = (millis - startStartTime) / 1000l;
		if(elapsed > 3) {
			this.drawCenteredString(fontRendererObj, "(" + elapsed + "s)", this.width / 2, top + 25, 0xFFFFFF);
		}
		
		super.drawScreen(par1, par2, par3);
	}

	public boolean doesGuiPauseGame() {
		return false;
	}
	
	public void updateScreen() {
		++timer;
		if (timer > 1) {
			if (this.networkManager == null) {
				this.networkManager = SingleplayerServerController.localPlayerNetworkManager;
				this.networkManager.connect();
			} else {
				if (this.networkManager.isChannelOpen()) {
					if (!hasOpened) {
						hasOpened = true;
						this.minecraft.getSession().setLAN();
						this.minecraft.clearTitles();
						this.networkManager.setConnectionState(ConnectionProtocol.LOGIN);
						this.networkManager.setNetHandler(new NetHandlerSingleplayerLogin(this.networkManager, this.minecraft, this.menu));
						this.networkManager.sendPacket(new ServerboundHelloPacket(this.minecraft.getSession().getProfile(),
								EaglerProfile.getSkinPacket(3), EaglerProfile.getCapePacket(),
								ConnectionHandshake.getSPHandshakeProtocolData(), EaglercraftVersion.clientBrandUUID));
					}
					try {
						this.networkManager.processReceivedPackets();
					} catch (IOException ex) {
					}
				} else {
					if (this.networkManager.checkDisconnected()) {
						this.minecraft.getSession().reset();
						if (mc.screen == this) {
							mc.loadLevel(null);
							mc.displayScreen(new DisconnectedScreen(menu, "connect.failed", new Component("Worker Connection Refused")));
						}
					}
				}
			}
		}
		
		long millis = EagRuntime.steadyTimeMillis();
		if(millis - startStartTime > 6000l && SingleplayerServerController.canKillWorker()) {
			killTask.enabled = true;
		}
	}

	protected void actionPerformed(net.minecraft.client.gui.components.Button par1Button) {
		if(par1Button.id == 0) {
			SingleplayerServerController.killWorker();
			this.minecraft.loadLevel((ClientLevel)null);
			this.minecraft.getSession().reset();
			this.minecraft.displayScreen(menu);
		}
	}

	public boolean shouldHangupIntegratedServer() {
		return false;
	}

	public boolean canCloseGui() {
		return false;
	}

}