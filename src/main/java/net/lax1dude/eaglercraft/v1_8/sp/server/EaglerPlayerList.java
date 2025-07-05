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

package net.lax1dude.eaglercraft.v1_8.sp.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType; // MCP Reborn 1.21.4 package

public class EaglerPlayerList extends PlayerList {
	
	private CompoundTag hostPlayerNBT = null;

	public EaglerPlayerList(MinecraftServer par1MinecraftServer, int viewDistance) {
		super(par1MinecraftServer);
		this.viewDistance = viewDistance;
	}

	protected void writePlayerData(ServerPlayer par1ServerPlayer) {
		if (par1ServerPlayer.getName().equals(this.getServerInstance().getServerOwner())) {
			this.hostPlayerNBT = new CompoundTag();
			par1ServerPlayer.writeToNBT(hostPlayerNBT);
		}
		super.writePlayerData(par1ServerPlayer);
	}
	
	public CompoundTag getHostPlayerData() {
		return this.hostPlayerNBT;
	}

	@Override
	public GameType getGameType() {
		return ((EaglerMinecraftServer)this.getServerInstance()).getGameType();
	}

	@Override
	public void setGameType(GameType type) {
		((EaglerMinecraftServer)this.getServerInstance()).setGameType(type);
	}

	public void playerLoggedOut(ServerPlayer playerIn) {
		super.playerLoggedOut(playerIn);
		EaglerMinecraftServer svr = (EaglerMinecraftServer)getServerInstance();
		svr.skinService.unregisterPlayer(playerIn.getUniqueID());
		svr.capeService.unregisterPlayer(playerIn.getUniqueID());
	}
}