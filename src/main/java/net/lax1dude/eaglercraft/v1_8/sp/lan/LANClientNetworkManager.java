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

package net.lax1dude.eaglercraft.v1_8.sp.lan;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.EagUtils;
import net.lax1dude.eaglercraft.v1_8.EaglerZLIB;
import net.lax1dude.eaglercraft.v1_8.internal.EnumEaglerConnectionState;
import net.lax1dude.eaglercraft.v1_8.internal.PlatformWebRTC;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.netty.ByteBuf;
import net.lax1dude.eaglercraft.v1_8.netty.Unpooled;
import net.lax1dude.eaglercraft.v1_8.socket.EaglercraftNetworkManager;
import net.lax1dude.eaglercraft.v1_8.sp.relay.RelayServerSocket;
import net.lax1dude.eaglercraft.v1_8.sp.relay.pkt.*;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LANClientNetworkManager extends EaglercraftNetworkManager {

	private static final Logger logger = LogManager.getLogger("LANClientNetworkManager");

	private static final int PRE = 0, INIT = 1, SENT_ICE_CANDIDATE = 2, SENT_DESCRIPTION = 3;

	public static final int fragmentSize = 0xFF00;

	private static final String[] initStateNames = new String[] { "PRE", "INIT", "SENT_ICE_CANDIDATE", "SENT_DESCRIPTION" };

	public final String displayCode;
	public final String displayRelay;

	private boolean firstPacket = true;

	private LANClientNetworkManager(String displayCode, String displayRelay) {
		super("");
		this.displayCode = displayCode;
		this.displayRelay = displayRelay;
		this.nethandler = null;
	}

	@Override
	public void connect() {
		fragmentedPacket.clear();
		firstPacket = true;
	}

	@Override
	public EnumEaglerConnectionState getConnectStatus() {
		return clientDisconnected ? EnumEaglerConnectionState.CLOSED : EnumEaglerConnectionState.CONNECTED;
	}

	public static LANClientNetworkManager connectToLevel(RelayServerSocket sock, String displayCode, String displayRelay) {
		PlatformWebRTC.clearLANClientState();
		int connectState = PRE;
		RelayPacket pkt;
		mainLoop: while(!sock.isClosed()) {
			PlatformWebRTC.runScheduledTasks();
			sock.update();
			if((pkt = sock.readPacket()) != null) {
				if(pkt instanceof RelayPacket00Handshake) {
					if(connectState == PRE) {

						// %%%%%%  Process IPacket00Handshake  %%%%%%

						logger.info("Relay [{}|{}] recieved handshake, client id: {}", displayRelay, displayCode, ((RelayPacket00Handshake)pkt).connectionCode);
						connectState = INIT;

					}else {
						sock.close();
						logger.error("Relay [{}|{}] unexpected packet: IPacket00Handshake in state {}", displayRelay, displayCode, initStateNames[connectState]);
						return null;
					}
				}else if(pkt instanceof RelayPacket01ICEServers) {
					if(connectState == INIT) {

						// %%%%%%  Process IPacket01ICEServers  %%%%%%

						RelayPacket01ICEServers ipkt = (RelayPacket01ICEServers) pkt;

						// print servers
						logger.info("Relay [{}|{}] provided ICE servers:", displayRelay, displayCode);
						List<String> servers = new ArrayList<>();
						for(RelayPacket01ICEServers.RelayServer srv : ipkt.servers) {
							logger.info("Relay [{}|{}]     {}: {}", displayRelay, displayCode, srv.type.name(), srv.address);
							servers.add(srv.getICEString());
						}

						// process
						PlatformWebRTC.clientLANSetICEServersAndConnect(servers.toArray(new String[servers.size()]));

						// await result
						long lm = EagRuntime.steadyTimeMillis();
						do {
							PlatformWebRTC.runScheduledTasks();
							String c = PlatformWebRTC.clientLANAwaitDescription();
							if(c != null) {
								logger.info("Relay [{}|{}] client sent description", displayRelay, displayCode);

								// 'this.descriptionHandler' was called, send result:
								sock.writePacket(new RelayPacket04Description("", c));

								connectState = SENT_DESCRIPTION;
								continue mainLoop;
							}
							EagUtils.sleep(20);
						}while(EagRuntime.steadyTimeMillis() - lm < 5000l);

						// no description was sent
						sock.close();
						logger.error("Relay [{}|{}] client provide description timeout", displayRelay, displayCode);
						return null;

					}else {
						sock.close();
						logger.error("Relay [{}|{}] unexpected packet: IPacket01ICEServers in state {}", displayRelay, displayCode, initStateNames[connectState]);
						return null;
					}
				}else if(pkt instanceof RelayPacket03ICECandidate) {
					if(connectState == SENT_ICE_CANDIDATE) {

						// %%%%%%  Process IPacket03ICECandidate  %%%%%%

						RelayPacket03ICECandidate ipkt = (RelayPacket03ICECandidate) pkt;

						// process
						logger.info("Relay [{}|{}] recieved server ICE candidate", displayRelay, displayCode);
						PlatformWebRTC.clientLANSetICECandidate(ipkt.getCandidateString());

						// await result
						long lm = EagRuntime.steadyTimeMillis();
						do {
							PlatformWebRTC.runScheduledTasks();
							if(PlatformWebRTC.clientLANAwaitChannel()) {
								logger.info("Relay [{}|{}] client opened data channel", displayRelay, displayCode);

								// 'this.remoteDataChannelHandler' was called, success
								sock.writePacket(new RelayPacket05ClientSuccess(ipkt.peerId));
								sock.close();
								return new LANClientNetworkManager(displayCode, displayRelay);

							}
							EagUtils.sleep(20);
						}while(EagRuntime.steadyTimeMillis() - lm < 10000l);

						// no channel was opened
						sock.writePacket(new RelayPacket06ClientFailure(ipkt.peerId));
						sock.close();
						logger.error("Relay [{}|{}] client open data channel timeout", displayRelay, displayCode);
						return null;

					}else {
						sock.close();
						logger.error("Relay [{}|{}] unexpected packet: IPacket03ICECandidate in state {}", displayRelay, displayCode, initStateNames[connectState]);
						return null;
					}
				}else if(pkt instanceof RelayPacket04Description) {
					if(connectState == SENT_DESCRIPTION) {

						// %%%%%%  Process IPacket04Description  %%%%%%

						RelayPacket04Description ipkt = (RelayPacket04Description) pkt;

						// process
						logger.info("Relay [{}|{}] recieved server description", displayRelay, displayCode);
						PlatformWebRTC.clientLANSetDescription(ipkt.getDescriptionString());

						// await result
						long lm = EagRuntime.steadyTimeMillis();
						do {
							PlatformWebRTC.runScheduledTasks();
							String c = PlatformWebRTC.clientLANAwaitICECandidate();
							if(c != null) {
								logger.info("Relay [{}|{}] client sent ICE candidate", displayRelay, displayCode);

								// 'this.iceCandidateHandler' was called, send result:
								sock.writePacket(new RelayPacket03ICECandidate("", c));

								connectState = SENT_ICE_CANDIDATE;
								continue mainLoop;
							}
							EagUtils.sleep(20);
						}while(EagRuntime.steadyTimeMillis() - lm < 10000l);

						// no ice candidates were sent
						sock.close();
						logger.error("Relay [{}|{}] client provide ICE candidate timeout", displayRelay, displayCode);
						return null;

					}else {
						sock.close();
						logger.error("Relay [{}|{}] unexpected packet: IPacket04Description in state {}", displayRelay, displayCode, initStateNames[connectState]);
						return null;
					}
				}else if(pkt instanceof RelayPacketFFErrorCode) {

					// %%%%%%  Process IPacketFFErrorCode  %%%%%%

					RelayPacketFFErrorCode ipkt = (RelayPacketFFErrorCode) pkt;
					logger.error("Relay [{}|{}] connection failed: {}({}): {}", displayRelay, displayCode, RelayPacketFFErrorCode.code2string(ipkt.code), ipkt.code, ipkt.desc);
					Throwable t;
					while((t = sock.getException()) != null) {
						logger.error(t);
					}
					sock.close();
					return null;

				}else {

					// %%%%%%  Unexpected Packet  %%%%%%

					logger.error("Relay [{}|{}] unexpected packet: {}", displayRelay, displayCode, pkt.getClass().getSimpleName());
					sock.close();
					return null;
				}
			}
			EagUtils.sleep(20);
		}
		return null;
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

		int len = temporaryBuffer.readableBytes();
		int fragmentSizeN1 = fragmentSize - 1;
		if(len > fragmentSizeN1) {
			do {
				int readLen = len > fragmentSizeN1 ? fragmentSizeN1 : len;
				byte[] frag = new byte[readLen + 1];
				temporaryBuffer.readBytes(frag, 1, readLen);
				frag[0] = temporaryBuffer.readableBytes() == 0 ? (byte)0 : (byte)1;
				PlatformWebRTC.clientLANSendPacket(frag);
			}while((len = temporaryBuffer.readableBytes()) > 0);
		}else {
			byte[] bytes = new byte[len + 1];
			bytes[0] = 0;
			temporaryBuffer.readBytes(bytes, 1, len);
			PlatformWebRTC.clientLANSendPacket(bytes);
		}
	}

	@Override
	public boolean isLocalChannel() {
		return true;
	}

	@Override
	public boolean isChannelOpen() {
		if (PlatformWebRTC.clientLANClosed()) {
			clientDisconnected = true;
		}
		return !clientDisconnected;
	}

	private List<byte[]> fragmentedPacket = new ArrayList<>();

	@Override
	public void processReceivedPackets() throws IOException {
		if(this.nethandler != null) {
			List<byte[]> packets = PlatformWebRTC.clientLANReadAllPacket();
			if(packets == null) {
				return;
			}
			for(int k = 0, l = packets.size(); k < l; ++k) {
				byte[] data = packets.get(k);

				if(firstPacket) {
					// 1.5 kick packet
					if(data.length == 31 && data[0] == (byte)0xFF && data[1] == (byte)0x00 && data[2] == (byte)0x0E) {
						logger.error("Detected a 1.5 LAN server!");
						this.closeChannel(new Component("singleplayer.outdatedLANServerKick"));
						firstPacket = false;
						return;
					}
					firstPacket = false;
				}

				byte[] fullData;
				boolean compressed = false;
				int off = 0;

				if (data[0] == 0 || data[0] == 2) {
					if(fragmentedPacket.isEmpty()) {
						fullData = data;
						off = 1;
					}else {
						fragmentedPacket.add(data);
						int len = 0;
						int fragCount = fragmentedPacket.size();
						for(int i = 0; i < fragCount; ++i) {
							len += fragmentedPacket.get(i).length - 1;
						}
						fullData = new byte[len];
						len = 0;
						for(int i = 0; i < fragCount; ++i) {
							byte[] f = fragmentedPacket.get(i);
							System.arraycopy(f, 1, fullData, len, f.length - 1);
							len += f.length - 1;
						}
						fragmentedPacket.clear();
					}
					compressed = data[0] == 2;
				} else if (data[0] == 1) {
					fragmentedPacket.add(data);
					continue;
				} else {
					logger.error("Recieved {} byte fragment of unknown type: {}", data.length, ((int)data[0] & 0xFF));
					continue;
				}

				if(compressed) {
					if(fullData.length < 4 + off) {
						throw new IOException("Recieved invalid " + fullData.length + " byte compressed packet");
					}
					int i = (((int) fullData[off] & 0xFF) << 24) | (((int) fullData[off + 1] & 0xFF) << 16)
							| (((int) fullData[off + 2] & 0xFF) << 8) | ((int) fullData[off + 3] & 0xFF);
					byte[] fullData2 = new byte[i];
					int r = EaglerZLIB.inflateFull(fullData, off + 4, fullData.length - off - 4, fullData2, 0, i);
					fullData = fullData2;
					off = 0;
					if (i != r) {
						logger.warn("Decompressed packet expected size {} differs from actual size {}!", i, r);
					}
				}

				ByteBuf nettyBuffer = Unpooled.buffer(fullData, fullData.length);
				nettyBuffer.writerIndex(fullData.length);
				nettyBuffer.readerIndex(off);
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
			}
		}
	}

	@Override
	public void closeChannel(Component reason) {
		if(!PlatformWebRTC.clientLANClosed()) {
			PlatformWebRTC.clientLANCloseConnection();
		}
		if(nethandler != null) {
			nethandler.onDisconnect(reason);
		}
		clientDisconnected = true;
	}

	@Override
	public boolean checkDisconnected() {
		if(PlatformWebRTC.clientLANClosed()) {
			clientDisconnected = false;
			try {
				processReceivedPackets(); // catch kick message
			} catch (IOException e) {
			}
			doClientDisconnect(new Component("disconnect.endOfStream"));
		}
		return clientDisconnected;
	}

}