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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelData;

public class EaglerSaveFormat extends LevelStorageSource {

	public EaglerSaveFormat(VFile2 parFile) {
		super(parFile);
	}

	public static final VFile2 worldsList = LevelsDB.newVFile("worlds_list.txt");
	public static final VFile2 worldsFolder = LevelsDB.newVFile("worlds");

	public String getName() {
		return "eagler";
	}

	public LevelStorageSource.LevelStorageAccess getSaveLoader(String s, boolean flag) {
		return new EaglerSaveHandler(this.savesDirectory, s);
	}

	public List<LevelSummary> getSaveList() {
		ArrayList<LevelSummary> arraylist = Lists.newArrayList();
		if(worldsList.exists()) {
			String[] lines = worldsList.getAllLines();
			for (int i = 0; i < lines.length; ++i) {
				String s = lines[i];
				LevelData worldinfo = this.getLevelData(s);
				if (worldinfo != null
						&& (worldinfo.getSaveVersion() == 19132 || worldinfo.getSaveVersion() == 19133)) {
					boolean flag = worldinfo.getSaveVersion() != this.getSaveVersion();
					String s1 = worldinfo.getLevelName();
					if (StringUtils.isEmpty(s1)) {
						s1 = s;
					}

					arraylist.add(new LevelSummary(s, s1, worldinfo.getLastTimePlayed(), 0l,
							worldinfo.getGameType(), flag, worldinfo.isHardcoreModeEnabled(),
							worldinfo.areCommandsAllowed(), null));
				}
			}
		}
		return arraylist;
	}

	public void clearPlayers(String worldFolder) {
		VFile2 file1 = LevelsDB.newVFile(this.savesDirectory, worldFolder, "player");
		deleteFiles(file1.listFiles(true), null);
	}

	protected int getSaveVersion() {
		return 19133; // why notch?
	}

	public boolean duplicateLevel(String worldFolder, String displayName) {
		String newFolderName = displayName.replaceAll("[\\./\"]", "_");
		VFile2 newFolder = LevelsDB.newVFile(savesDirectory, newFolderName);
		while((LevelsDB.newVFile(newFolder, "level.dat")).exists() || (LevelsDB.newVFile(newFolder, "level.dat_old")).exists()) {
			newFolderName += "_";
			newFolder = LevelsDB.newVFile(savesDirectory, newFolderName);
		}
		VFile2 oldFolder = LevelsDB.newVFile(this.savesDirectory, worldFolder);
		String oldPath = oldFolder.getPath();
		int totalSize = 0;
		int lastUpdate = 0;
		final VFile2 finalNewFolder = newFolder;
		List<VFile2> vfl = oldFolder.listFiles(true);
		for(int i = 0, l = vfl.size(); i < l; ++i) {
			VFile2 vf = vfl.get(i);
			String fileNameRelative = vf.getPath().substring(oldPath.length() + 1);
			totalSize += VFile2.copyFile(vf, LevelsDB.newVFile(finalNewFolder, fileNameRelative));
			if (totalSize - lastUpdate > 10000) {
				lastUpdate = totalSize;
				EaglerIntegratedServerWorker.sendProgress("singleplayer.busy.duplicating", totalSize);
			}
		}
		String[] worldsTxt = worldsList.getAllLines();
		if(worldsTxt == null || worldsTxt.length <= 0) {
			worldsTxt = new String[] { newFolderName };
		}else {
			String[] tmp = worldsTxt;
			worldsTxt = new String[worldsTxt.length + 1];
			System.arraycopy(tmp, 0, worldsTxt, 0, tmp.length);
			worldsTxt[worldsTxt.length - 1] = newFolderName;
		}
		worldsList.setAllChars(String.join("\n", worldsTxt));
		return renameLevel(newFolderName, displayName);
	}
}