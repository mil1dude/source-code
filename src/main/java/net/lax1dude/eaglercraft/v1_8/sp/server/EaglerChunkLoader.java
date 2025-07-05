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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class EaglerChunkLoader extends ChunkStorage {

	private static final String hex = "0123456789ABCDEF";
	private static final Logger logger = LogManager.getLogger("EaglerChunkLoader");

	public static String getChunkPath(int x, int z) {
		int unsignedX = x + 1900000;
		int unsignedZ = z + 1900000;
		
		char[] path = new char[12];
		for(int i = 5; i >= 0; --i) {
			path[i] = hex.charAt((unsignedX >>> (i << 2)) & 0xF);
			path[i + 6] = hex.charAt((unsignedZ >>> (i << 2)) & 0xF);
		}
		
		return new String(path);
	}

	public static ChunkPos getChunkCoords(String filename) {
		String strX = filename.substring(0, 6);
		String strZ = filename.substring(6);

		int retX = 0;
		int retZ = 0;

		for(int i = 0; i < 6; ++i) {
			retX |= hex.indexOf(strX.charAt(i)) << (i << 2);
			retZ |= hex.indexOf(strZ.charAt(i)) << (i << 2);
		}

		return new ChunkPos(retX - 1900000, retZ - 1900000);
	}

	public final VFile2 chunkDirectory;

	public EaglerChunkLoader(VFile2 chunkDirectory) {
		this.chunkDirectory = chunkDirectory;
	}

	@Override
	public CompletableFuture<ChunkAccess> loadChunk(ServerLevel level, ChunkPos pos) {
		VFile2 file = LevelsDB.newVFile(chunkDirectory, getChunkPath(pos.x, pos.z) + ".dat");
		if(!file.exists()) {
			return CompletableFuture.completedFuture(null);
		}
		try {
			CompoundTag nbt;
			try(InputStream is = file.getInputStream()) {
				nbt = NbtIo.readCompressed(is);
			}
			return CompletableFuture.completedFuture(checkedReadChunkFromNBT(level, pos, nbt));
		}catch(Throwable t) {
			return CompletableFuture.completedFuture(null);
		}
	}

	@Override
	public CompletableFuture<Void> saveChunk(ServerLevel level, ChunkAccess chunk) {
		level.getLightEngine().updateSectionStatus(chunk.getPos(), true);
		CompoundTag chunkData = new CompoundTag();
		chunk.save(level.registryAccess(), chunkData);
		CompoundTag fileData = new CompoundTag();
		fileData.put("Level", chunkData);
		
		return CompletableFuture.runAsync(() -> {
			try {
				VFile2 file = LevelsDB.newVFile(chunkDirectory, 
					getChunkPath(chunk.getPos().x, chunk.getPos().z) + ".dat");
				try(OutputStream os = file.getOutputStream()) {
					NbtIo.writeCompressed(fileData, os);
				}
			} catch (IOException e) {
				logger.error("Failed to save chunk", e);
			}
		});
	}

	@Override
	public CompletableFuture<Void> saveStructureData(ServerLevel level, ChunkPos pos, CompoundTag tag) {
		// Not implemented for Eaglercraft
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void tick() {
		// Process any pending chunk operations
	}

	@Override
	public void close() throws IOException {
		// Clean up any resources
	}

	private ChunkAccess checkedReadChunkFromNBT(ServerLevel level, ChunkPos pos, CompoundTag nbt) {
		try {
			CompoundTag levelTag = nbt.getCompound("Level");
			if (levelTag.isEmpty()) {
				return null;
			}
			return level.getChunkSource().getChunkGenerator().createChunk(level, level.getBiomeManager(), pos, levelTag);
		} catch (Exception e) {
			logger.error("Failed to load chunk " + pos, e);
			return null;
		}
	}
}