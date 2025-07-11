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

package net.lax1dude.eaglercraft.v1_8.sp.server.skins;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.lax1dude.eaglercraft.v1_8.Base64;
import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.EaglercraftUUID;
import net.lax1dude.eaglercraft.v1_8.crypto.SHA1Digest;
import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.GameMessagePacket;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.pkt.server.SPacketOtherSkinPresetEAG;
import net.lax1dude.eaglercraft.v1_8.socket.protocol.util.SkinPacketVersionCache;
import net.lax1dude.eaglercraft.v1_8.sp.server.LevelsDB;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

public class IntegratedSkinService {

	public static final Logger logger = LogManager.getLogger("IntegratedSkinService");

	public static final byte[] skullNotFoundTexture = new byte[4096];

	static {
		for(int y = 0; y < 16; ++y) {
			for(int x = 0; x < 64; ++x) {
				int i = (y << 8) | (x << 2);
				byte j = ((x + y) & 1) == 1 ? (byte)255 : 0;
				skullNotFoundTexture[i] = (byte)255;
				skullNotFoundTexture[i + 1] = j;
				skullNotFoundTexture[i + 2] = 0;
				skullNotFoundTexture[i + 3] = j;
			}
		}
	}

	public final VFile2 skullsDirectory;

	public final Map<EaglercraftUUID,SkinPacketVersionCache> playerSkins = new HashMap<>();
	public final Map<String,CustomSkullData> customSkulls = new HashMap<>();

	private long lastFlush = 0l;

	public IntegratedSkinService(VFile2 skullsDirectory) {
		this.skullsDirectory = skullsDirectory;
	}

	public void processLoginPacket(byte[] packetData, ServerPlayer sender, int protocolVers) {
		try {
			IntegratedSkinPackets.registerEaglerPlayer(sender.getUniqueID(), packetData, this, protocolVers);
		} catch (IOException e) {
			logger.error("Invalid skin data packet recieved from player {}!", sender.getName());
			logger.error(e);
			sender.playerNetServerHandler.kickPlayerFromServer("Invalid skin data packet recieved!");
		}
	}

	public void processPacketGetOtherSkin(EaglercraftUUID searchUUID, ServerPlayer sender) {
		SkinPacketVersionCache playerSkin = playerSkins.get(searchUUID);
		GameMessagePacket toSend = null;
		if(playerSkin != null) {
			toSend = playerSkin.get(sender.playerNetServerHandler.getEaglerMessageProtocol());
		}else {
			toSend = IntegratedSkinPackets.makePresetResponse(searchUUID);
		}
		sender.playerNetServerHandler.sendEaglerMessage(toSend);
	}

	public void processPacketGetOtherSkin(EaglercraftUUID searchUUID, String urlStr, ServerPlayer sender) {
		urlStr = urlStr.toLowerCase();
		GameMessagePacket playerSkin;
		if(!urlStr.startsWith("eagler://")) {
			playerSkin = new SPacketOtherSkinPresetEAG(searchUUID.msb, searchUUID.lsb, 0);
		}else {
			urlStr = urlStr.substring(9);
			if(urlStr.contains(VFile2.pathSeperator)) {
				playerSkin = new SPacketOtherSkinPresetEAG(searchUUID.msb, searchUUID.lsb, 0);
			}else {
				CustomSkullData sk = customSkulls.get(urlStr);
				if(sk == null) {
					customSkulls.put(urlStr, sk = loadCustomSkull(urlStr));
				}else {
					sk.lastHit = EagRuntime.steadyTimeMillis();
				}
				playerSkin = sk.getSkinPacket(searchUUID, sender.playerNetServerHandler.getEaglerMessageProtocol());
			}
		}
		sender.playerNetServerHandler.sendEaglerMessage(playerSkin);
	}

	public void processPacketPlayerSkin(EaglercraftUUID clientUUID, SkinPacketVersionCache generatedPacket, int skinModel) {
		playerSkins.put(clientUUID, generatedPacket);
	}

	public void unregisterPlayer(EaglercraftUUID clientUUID) {
		playerSkins.remove(clientUUID);
	}

	public void processPacketInstallNewSkin(byte[] skullData, ServerPlayer sender) {
		if(!sender.canCommandSenderUseCommand(2, "give")) {
			Component cc = new Component("command.skull.nopermission");
			cc.getChatStyle().setColor(ChatFormatting.RED);
			sender.addChatMessage(cc);
			return;
		}
		String fileName = "eagler://" + installNewSkull(skullData);
		CompoundTag rootTagCompound = new CompoundTag();
		CompoundTag ownerTagCompound = new CompoundTag();
		ownerTagCompound.setString("Name", "Eagler");
		ownerTagCompound.setString("Id", EaglercraftUUID.nameUUIDFromBytes((("EaglerSkullUUID:" + fileName).getBytes(StandardCharsets.UTF_8))).toString());
		CompoundTag propertiesTagCompound = new CompoundTag();
		ListTag texturesTagList = new ListTag();
		CompoundTag texturesTagCompound = new CompoundTag();
		String texturesProp = "{\"textures\":{\"SKIN\":{\"url\":\"" + fileName + "\",\"metadata\":{\"model\":\"default\"}}}}";
		texturesTagCompound.setString("Value", Base64.encodeBase64String(texturesProp.getBytes(StandardCharsets.UTF_8)));
		texturesTagList.appendTag(texturesTagCompound);
		propertiesTagCompound.setTag("textures", texturesTagList);
		ownerTagCompound.setTag("Properties", propertiesTagCompound);
		rootTagCompound.setTag("SkullOwner", ownerTagCompound);
		CompoundTag displayTagCompound = new CompoundTag();
		displayTagCompound.setString("Name", ChatFormatting.RESET + "Custom Eaglercraft Skull");
		ListTag loreList = new ListTag();
		loreList.appendTag(new StringTag(ChatFormatting.GRAY + (fileName.length() > 24 ? (fileName.substring(0, 22) + "...") : fileName)));
		displayTagCompound.setTag("Lore", loreList);
		rootTagCompound.setTag("display", displayTagCompound);
		ItemStack stack = new ItemStack(Items.skull, 1, 3);
		stack.setTag(rootTagCompound);
		boolean flag = sender.inventory.addItemStackToInventory(stack);
		if (flag) {
			sender.worldObj.playSoundAtEntity(sender, "random.pop", 0.2F,
					((sender.getRNG().nextFloat() - sender.getRNG().nextFloat()) * 0.7F + 1.0F)
							* 2.0F);
			sender.inventoryContainer.detectAndSendChanges();
		}
		sender.addChatMessage(new Component("command.skull.feedback", fileName));
	}

	private static final String hex = "0123456789abcdef";

	public String installNewSkull(byte[] skullData) {
		// set to 16384 to save a full 64x64 skin
		if(skullData.length > 4096) {
			byte[] tmp = skullData;
			skullData = new byte[4096];
			System.arraycopy(tmp, 0, skullData, 0, 4096);
		}
		SHA1Digest sha = new SHA1Digest();
		sha.update(skullData, 0, skullData.length);
		byte[] hash = new byte[20];
		sha.doFinal(hash, 0);
		char[] hashText = new char[40];
		for(int i = 0; i < 20; ++i) {
			hashText[i << 1] = hex.charAt((hash[i] & 0xF0) >> 4);
			hashText[(i << 1) + 1] = hex.charAt(hash[i] & 0x0F);
		}
		String str = "skin-" + new String(hashText) + ".bmp";
		customSkulls.put(str, new CustomSkullData(str, skullData));
		LevelsDB.newVFile(skullsDirectory, str).setAllBytes(skullData);
		return str;
	}

	private CustomSkullData loadCustomSkull(String urlStr) {
		byte[] data = LevelsDB.newVFile(skullsDirectory, urlStr).getAllBytes();
		if(data == null) {
			return new CustomSkullData(urlStr, skullNotFoundTexture);
		}else {
			return new CustomSkullData(urlStr, data);
		}
	}

	public void flushCache() {
		long cur = EagRuntime.steadyTimeMillis();
		if(cur - lastFlush > 300000l) {
			lastFlush = cur;
			Iterator<CustomSkullData> customSkullsItr = customSkulls.values().iterator();
			while(customSkullsItr.hasNext()) {
				if(cur - customSkullsItr.next().lastHit > 900000l) {
					customSkullsItr.remove();
				}
			}
		}
	}
}