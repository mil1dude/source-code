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

package net.lax1dude.eaglercraft.v1_8.sp;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;

public class SingleplayerSaveHandler {

    private final ServerLevelData worldInfo;
    private final LevelStorageAccess levelStorageAccess;
    private final PlayerDataStorage playerDataStorage;

    public SingleplayerSaveHandler(ServerLevelData worldInfo, LevelStorageAccess levelStorageAccess) {
        this.worldInfo = worldInfo;
        this.levelStorageAccess = levelStorageAccess;
        this.playerDataStorage = new PlayerDataStorage(levelStorageAccess);
    }

    public ServerLevelData loadLevelData() {
        return worldInfo;
    }

    public PlayerDataStorage getPlayerDataStorage() {
        return playerDataStorage;
    }

    public LevelStorageAccess getLevelStorageAccess() {
        return levelStorageAccess;
    }

    public static ServerLevelData createDefaultLevelData(LevelSettings settings, String levelName) {
        CompoundTag compoundnbt = new CompoundTag();
        CompoundTag compoundnbt1 = new CompoundTag();
        compoundnbt1.putString("generatorName", "flat");
        compoundnbt1.putString("generatorOptions", "{}");
        compoundnbt1.putString("levelType", "default_1_1");
        compoundnbt.put("GameRules", new GameRules().createTag());
        ServerLevelData serverleveldata = new ServerLevelData();
        serverleveldata.setDifficulty(Difficulty.NORMAL);
        serverleveldata.setDifficultyLocked(false);
        serverleveldata.setGameType(GameType.SURVIVAL);
        serverleveldata.setLevelName(levelName);
        serverleveldata.setSpawn(0, 64, 0, 0.0F);
        serverleveldata.setGameRules(new GameRules());
        return serverleveldata;
    }
}