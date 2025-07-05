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

package net.lax1dude.eaglercraft.v1_8.sp.socket;

import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.netty.Unpooled;
import net.lax1dude.eaglercraft.v1_8.socket.EaglercraftNetworkManager;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.GamePluginMessageConstants;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.GamePluginMessageProtocol;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.client.GameProtocolMessageController;
import net.lax1dude.eaglercraft.v1_8.update.UpdateService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
// Using the correct packet class for Eaglercraft
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Handles the login process for singleplayer connections.
 */
public class NetHandlerSingleplayerLogin implements ClientLoginPacketListener {

	private final Minecraft minecraft;
	private final Screen previousScreen;
	private final EaglercraftNetworkManager networkManager;

	private static final Logger logger = LogManager.getLogger("NetHandlerSingleplayerLogin");

	public NetHandlerSingleplayerLogin(EaglercraftNetworkManager parNetworkManager, Minecraft minecraft, Screen previousScreen) {
		this.networkManager = parNetworkManager;
		this.minecraft = minecraft;
		this.previousScreen = previousScreen;
	}

	/**
	 * Called when the connection is disconnected.
	 *
	 * @param reason The reason for disconnection
	 */
	@Override
	public void onDisconnect(Component reason) {
		if (this.minecraft == null) {
			logger.error("Minecraft instance is null during disconnection");
			return;
		}
		
		// Log the disconnection reason
		if (reason != null) {
			logger.warn("Disconnected from server: {}", reason.getString());
		}
		
		// Handle the disconnection screen
		if (this.previousScreen != null) {
			this.minecraft.setScreen(new DisconnectedScreen(
				this.previousScreen, 
				"disconnect.lost", 
				reason != null ? reason : Component.literal("Disconnected from server")
			));
		} else {
			this.minecraft.setScreen(new net.minecraft.client.gui.screens.DisconnectedScreen(
				new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(
					new net.minecraft.client.gui.screens.TitleScreen()
				),
				"disconnect.lost",
				reason != null ? reason : Component.literal("Disconnected from server")
			));
		}
	}

	/**
	 * Handles the login hello packet from the server.
	 * This is called when the server sends its hello packet.
	 *
	 * @param packet The hello packet
	 */
	@Override
	public void handleHello(ClientboundHelloPacket packet) {
		// Handle hello packet (encryption request if needed)
		// This is where we would handle the server's hello packet
	}

	/**
	 * Handles custom payload packets from the server.
	 *
	 * @param packet The custom payload packet
	 */
	@Override
	public void handleCustomPayload(ClientboundCustomPayloadPacket packet) {
		handleCustomPayloadInternal(packet);
	}

	/**
	 * Handles disconnection from the server.
	 *
	 * @param packet The disconnect packet
	 */
	@Override
	public void handleDisconnect(ClientboundLoginDisconnectPacket packet) {
		handleDisconnectInternal(packet);
	}

	@Override
	public void handleKeepAlive(net.minecraft.network.protocol.common.ClientboundKeepAlivePacket packet) {
		// Handle keep alive packet
		// No action needed for singleplayer
	}

	@Override
	public void handlePing(net.minecraft.network.protocol.common.ClientboundPingPacket packet) {
		// Handle ping packet
		// No action needed for singleplayer
	}

	/**
	 * Handle resource pack from server
	 * @param url The resource pack URL
	 * @param hash The resource pack hash
	 */
	/**
	 * Handles resource pack information from the server.
	 *
	 * @param url The URL of the resource pack
	 * @param hash The hash of the resource pack
	 */
	private void handleResourcePack(String url, String hash) {
		try {
			// Handle resource pack download and application
			logger.info("Received resource pack: {}", url);
			// TODO: Implement resource pack handling
		} catch (Exception e) {
			logger.error("Error handling resource pack", e);
		}
	}

	@Override
	public void handleUpdateTags(net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket packet) {
		// Handle update tags from server
		// No action needed for singleplayer
	}

	/**
	 * Handles the game profile packet from the server.
	 * This is called when the server sends the player's game profile.
	 *
	 * @param packet The login packet containing game profile
	 */
	@Override
	public void handleGameProfile(net.minecraft.network.protocol.login.ClientboundHelloPacket packet) {
		this.networkManager.setProtocol(ConnectionProtocol.PLAY);
		int protocolVersion = packet.getProtocolVersion();
		GamePluginMessageProtocol mp = GamePluginMessageProtocol.getByVersion(protocolVersion);
		if (mp == null) {
			this.networkManager.disconnect(Component.literal("Unknown protocol selected: " + protocolVersion));
			return;
		}
		logger.info("Server is using protocol: {}", protocolVersion);
		
		// Create new packet listener for game state
		ClientPacketListener netHandler = new ClientPacketListener(
			this.minecraft, 
			this.previousScreen, 
			this.networkManager, 
			packet.playerInfo().profile(), 
			null
		);
		
		// Set up message controller for the protocol
		netHandler.setEaglerMessageController(
			new GameProtocolMessageController(
				mp, 
				GamePluginMessageConstants.CLIENT_TO_SERVER,
				GameProtocolMessageController.createClientHandler(protocolVersion, netHandler),
				(ch, msg) -> {
					FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer().writeBytes(msg));
					try {
						netHandler.send(new ServerboundCustomPayloadPacket(ch, buf));
					} finally {
						buf.release();
					}
				}
			)
		);
		
		// Set the new packet handler
		this.networkManager.setNetHandler(netHandler);
		
		// Send client signature if available
		byte[] signatureData = UpdateService.getClientSignatureData();
		if (signatureData != null) {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(signatureData.length).writeBytes(signatureData));
			try {
				this.networkManager.send(new ServerboundCustomPayloadPacket(
					new ResourceLocation("eagler", "update_cert"), 
					buf
				));
			} finally {
				buf.release();
			}
		}
	}

	/**
	 * Handles compression settings from the server.
	 *
	 * @param packet The compression packet
	 */
	@Override
	public void handleCompression(ClientboundLoginCompressionPacket packet) {
		// Set up compression if needed
		this.networkManager.setupCompression(packet.getCompressionThreshold(), false);
	}

	// Required interface methods with default implementations
	/**
	 * Checks if this handler is still accepting messages.
	 *
	 * @return true if accepting messages, false otherwise
	 */
	@Override 
	public boolean isAcceptingMessages() { 
		return true; 
	}
	

	
	/**
	 * Gets the connection protocol for this handler.
	 *
	 * @return The connection protocol
	 */
	@Override 
	public ConnectionProtocol protocol() { 
		return ConnectionProtocol.LOGIN; 
	}
	
	/**
	 * Gets the packet flow direction for this handler.
	 *
	 * @return The packet flow direction
	 */
	@Override 
	public PacketFlow flow() { 
		return PacketFlow.CLIENTBOUND; 
	}
	
	/**
	 * Handles unknown payload types.
	 *
	 * @param payload The unknown payload
	 */
	@Override 
	public void handleUnknownPayload(CustomPacketPayload payload) {
		logger.warn("Received unknown payload type: {}", payload.id());
	}
	
	/**
	 * Handles custom payloads from the server.
	 *
	 * @param packet The custom payload packet
	 */
	private void handleCustomPayloadInternal(ClientboundCustomPayloadPacket packet) {
		try {
			// Handle custom payload based on packet type
			if (packet.payload() != null) {
				logger.debug("Received custom payload: {}", packet.payload().id());
			}
		} catch (Exception e) {
			logger.error("Error handling custom payload", e);
		}
	}
	

	
	/**
	 * Handles the disconnection process.
	 *
	 * @param packet The disconnect packet
	 */
	private void handleDisconnectInternal(ClientboundLoginDisconnectPacket packet) {
		onDisconnect(packet.getReason());
	}

}