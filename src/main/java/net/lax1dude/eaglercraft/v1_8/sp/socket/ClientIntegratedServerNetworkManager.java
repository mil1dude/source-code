/*
 * Copyright (c) 2023-2024 lax1dude, ayunami2000. All Rights Reserved.
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.internal.EnumEaglerConnectionState;
import net.lax1dude.eaglercraft.v1_8.internal.IPCPacketData;
import net.lax1dude.eaglercraft.v1_8.netty.ByteBuf;
import net.lax1dude.eaglercraft.v1_8.netty.Unpooled;
import net.lax1dude.eaglercraft.v1_8.socket.EaglercraftNetworkManager;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.sp.internal.ClientPlatformSingleplayer;
import net.lax1dude.eaglercraft.v1_8.sp.lan.LANServerController;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;

public class ClientIntegratedServerNetworkManager extends EaglercraftNetworkManager {

	private int debugPacketCounter = 0;
	private final List<byte[]> recievedFriendlyByteBuf = new LinkedList<>();
	public boolean isPlayerChannelOpen = false;

	public ClientIntegratedServerNetworkManager(String channel) {
		super(channel);
	}

	@Override
	public void connect() {
		clearRecieveQueue();
		SingleplayerServerController.openLocalPlayerChannel();
	}

	@Override
	public EnumEaglerConnectionState getConnectStatus() {
		return isPlayerChannelOpen ? EnumEaglerConnectionState.CONNECTED : EnumEaglerConnectionState.CLOSED;
	}

	@Override
	public void closeChannel(Component reason) {
		LANServerController.closeLAN();
		SingleplayerServerController.closeLocalPlayerChannel();
		if(nethandler != null) {
			nethandler.onDisconnect(reason);
		}
		clearRecieveQueue();
		clientDisconnected = true;
	}

	public void addRecievedPacket(byte[] next) {
		recievedFriendlyByteBuf.add(next);
	}

	@Override
	public void processReceivedPackets() throws IOException {
		if(nethandler == null) return;

		while(!recievedFriendlyByteBuf.isEmpty()) {
			byte[] next = recievedFriendlyByteBuf.remove(0);
			++debugPacketCounter;
			try {
				ByteBuf nettyBuffer = Unpooled.buffer(next, next.length);
				nettyBuffer.writerIndex(next.length);
				FriendlyByteBuf input = new FriendlyByteBuf(nettyBuffer);
				int pktId = input.readVarIntFromBuffer();
				
				Packet pkt;
				try {
					pkt = packetState.getPacket(PacketFlow.CLIENTBOUND, pktId);
				}catch(IllegalAccessException | InstantiationException ex) {
					throw new IOException("Recieved a packet with type " + pktId + " which is invalid!");
				}
				
				if(pkt == null) {
					throw new IOException("Recieved packet type " + pktId + " which is undefined in state " + packetState);
				}
				
				try {
					pkt.readPacketData(input);
				}catch(Throwable t) {
					throw new IOException("Failed to read packet type '" + pkt.getClass().getSimpleName() + "'", t);
				}
				
				try {
					pkt.processPacket(nethandler);
				}catch(Throwable t) {
					logger.error("Failed to process {}! It'll be skipped for debug purposes.", pkt.getClass().getSimpleName());
					logger.error(t);
				}
				
			}catch(Throwable t) {
				logger.error("Failed to process socket frame {}! It'll be skipped for debug purposes.", debugPacketCounter);
				logger.error(t);
			}
		}
	}

	@Override
	public void sendPacket(Packet pkt) {
		if(!isChannelOpen()) {
			logger.error("Packet was sent on a closed connection: {}", pkt.getClass().getSimpleName());
			return;
		}
		
		int i;
		try {
			i = packetState.getPacketId(PacketFlow.SERVERBOUND, pkt);
		}catch(Throwable t) {
			logger.error("Incorrect packet for state: {}", pkt.getClass().getSimpleName());
			return;
		}
		
		temporaryBuffer.clear();
		temporaryBuffer.writeVarIntToBuffer(i);
		try {
			pkt.writePacketData(temporaryBuffer);
		}catch(IOException ex) {
			logger.error("Failed to write packet {}!", pkt.getClass().getSimpleName());
			return;
		}
		
		int len = temporaryBuffer.writerIndex();
		byte[] bytes = new byte[len];
		temporaryBuffer.getBytes(0, bytes);
		
		ClientPlatformSingleplayer.sendPacket(new IPCPacketData(address, bytes));
	}

	@Override
	public boolean checkDisconnected() {
		if(!isPlayerChannelOpen) {
			try {
				processReceivedPackets(); // catch kick message
			} catch (IOException e) {
			}
			clearRecieveQueue();
			doClientDisconnect(new Component("disconnect.endOfStream"));
			return true;
		}else {
			return false;
		}
	}

	@Override
	public boolean isLocalChannel() {
		return true;
	}

	public void clearRecieveQueue() {
		recievedFriendlyByteBuf.clear();
	}
}