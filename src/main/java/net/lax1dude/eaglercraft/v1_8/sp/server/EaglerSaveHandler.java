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

import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;
import net.minecraft.world.level.storage.LevelStorageSource; // Updated to new package name
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess; // Updated to new package name
//import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.nbt.CompoundTag;

public class EaglerSaveHandler extends net.minecraft.world.level.storage.LevelStorageSource {

	public EaglerSaveHandler(VFile2 savesDirectory, String directoryName) {
		super(savesDirectory, directoryName);
	}

	public net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess getChunkLoader(net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess provider) {
		return new EaglerChunkLoader(LevelsDB.newVFile(this.getLevelDirectory(), "level" + provider.getDimensionId()));
	}

	public void saveLevelDataWithPlayer(LevelData worldInformation, CompoundTag tagCompound) {
		worldInformation.setSaveVersion(19133);
		super.saveLevelDataWithPlayer(worldInformation, tagCompound);
	}
}