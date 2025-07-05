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

import net.lax1dude.eaglercraft.v1_8.EaglercraftVersion;
import net.lax1dude.eaglercraft.v1_8.internal.PlatformWebRTC;
import net.lax1dude.eaglercraft.v1_8.profile.EaglerProfile;
import net.lax1dude.eaglercraft.v1_8.socket.ConnectionHandshake;
import net.lax1dude.eaglercraft.v1_8.sp.lan.LANClientNetworkManager;
import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayManager;
import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayServer;
import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayServerSocket;
import net.lax1dude.eaglercraft.v1_8.sp.socket.NetHandlerSingleplayerLogin;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public class ScreenLANConnecting extends Screen {

	private final Screen parent;
	private final String code;
	private final RelayServer relay;

	private boolean completed = false;

	private LANClientNetworkManager networkManager = null;

	private int renderCount = 0;

	public ScreenLANConnecting(Screen parent, String code) {
		this.parent = parent;
		this.code = code;
		this.relay = null;
	}

	public ScreenLANConnecting(Screen parent, String code, RelayServer relay) {
		this.parent = parent;
		this.code = code;
		this.relay = relay;
		Minecraft.getMinecraft().setServerData(new ServerData("Shared Level", "shared:" + relay.address, false));
	}

	public boolean doesGuiPauseGame() {
		return false;
	}

	public void updateScreen() {
		if(networkManager != null) {
			if (networkManager.isChannelOpen()) {
				try {
					networkManager.processReceivedPackets();
				} catch (IOException ex) {
				}
			} else {
				if (networkManager.checkDisconnected()) {
					this.minecraft.getSession().reset();
					if (mc.screen == this) {
						mc.loadLevel(null);
						mc.displayScreen(new DisconnectedScreen(parent, "connect.failed", new Component("LAN Connection Refused")));
					}
				}
			}
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		if(completed) {
			String message = I18n.get("connect.authorizing");
			this.drawString(fontRendererObj, message, (this.width - this.font.getStringWidth(message)) / 2, this.height / 3 + 10, 0xFFFFFF);
		}else {
			LoadingScreenRenderer ls = mc.loadingScreen;

			String message = I18n.get("lanServer.pleaseWait");
			this.drawString(fontRendererObj, message, (this.width - this.font.getStringWidth(message)) / 2, this.height / 3 + 10, 0xFFFFFF);

			PlatformWebRTC.startRTCLANClient();

			if(++renderCount > 1) {
				RelayServerSocket sock;
				if(relay == null) {
					sock = RelayManager.relayManager.getWorkingRelay((str) -> ls.resetProgressAndMessage("Connecting: " + str), 0x02, code);
				}else {
					sock = RelayManager.relayManager.connectHandshake(relay, 0x02, code);
				}
				if(sock == null) {
					this.minecraft.displayScreen(new ScreenNoRelays(parent, I18n.get("noRelay.worldNotFound1").replace("$code$", code),
							I18n.get("noRelay.worldNotFound2").replace("$code$", code), I18n.get("noRelay.worldNotFound3")));
					return;
				}

				networkManager = LANClientNetworkManager.connectToLevel(sock, code, sock.getURI());
				if(networkManager == null) {
					this.minecraft.displayScreen(new DisconnectedScreen(parent, "connect.failed", new Component(I18n.get("noRelay.worldFail").replace("$code$", code))));
					return;
				}

				completed = true;

				this.minecraft.getSession().setLAN();
				this.minecraft.clearTitles();
				networkManager.setConnectionState(ConnectionProtocol.LOGIN);
				networkManager.setNetHandler(new NetHandlerSingleplayerLogin(networkManager, mc, parent));
				networkManager.sendPacket(new ServerboundHelloPacket(this.minecraft.getSession().getProfile(),
						EaglerProfile.getSkinPacket(3), EaglerProfile.getCapePacket(),
						ConnectionHandshake.getSPHandshakeProtocolData(), EaglercraftVersion.clientBrandUUID));
			}
		}
	}

	public boolean canCloseGui() {
		return false;
	}

}