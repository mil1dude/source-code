package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldOptions extends ValueObject {
    public final boolean pvp;
    public final boolean spawnMonsters;
    public final int spawnProtection;
    public final boolean commandBlocks;
    public final boolean forceGameMode;
    public final int difficulty;
    public final int gameMode;
    public final boolean hardcore;
    private final String slotName;
    public final String version;
    public final RealmsServer.Compatibility compatibility;
    public long templateId;
    @Nullable
    public String templateImage;
    public boolean empty;
    private static final boolean DEFAULT_FORCE_GAME_MODE = false;
    private static final boolean DEFAULT_PVP = true;
    private static final boolean DEFAULT_SPAWN_MONSTERS = true;
    private static final int DEFAULT_SPAWN_PROTECTION = 0;
    private static final boolean DEFAULT_COMMAND_BLOCKS = false;
    private static final int DEFAULT_DIFFICULTY = 2;
    private static final int DEFAULT_GAME_MODE = 0;
    private static final boolean DEFAULT_HARDCORE_MODE = false;
    private static final String DEFAULT_SLOT_NAME = "";
    private static final String DEFAULT_VERSION = "";
    private static final RealmsServer.Compatibility DEFAULT_COMPATIBILITY = RealmsServer.Compatibility.UNVERIFIABLE;
    private static final long DEFAULT_TEMPLATE_ID = -1L;
    private static final String DEFAULT_TEMPLATE_IMAGE = null;

    public RealmsWorldOptions(
        boolean p_167302_,
        boolean p_167303_,
        int p_167306_,
        boolean p_167304_,
        int p_167308_,
        int p_167309_,
        boolean p_167305_,
        boolean p_167307_,
        String p_167311_,
        String p_311180_,
        RealmsServer.Compatibility p_311981_
    ) {
        this.pvp = p_167302_;
        this.spawnMonsters = p_167303_;
        this.spawnProtection = p_167306_;
        this.commandBlocks = p_167304_;
        this.difficulty = p_167308_;
        this.gameMode = p_167309_;
        this.hardcore = p_167305_;
        this.forceGameMode = p_167307_;
        this.slotName = p_167311_;
        this.version = p_311180_;
        this.compatibility = p_311981_;
    }

    public static RealmsWorldOptions createDefaults() {
        return new RealmsWorldOptions(true, true, 0, false, 2, 0, false, false, "", "", DEFAULT_COMPATIBILITY);
    }

    public static RealmsWorldOptions createDefaultsWith(GameType p_364043_, Difficulty p_366299_, boolean p_368672_, String p_361621_, String p_365919_) {
        return new RealmsWorldOptions(true, true, 0, false, p_366299_.getId(), p_364043_.getId(), p_368672_, false, p_365919_, p_361621_, DEFAULT_COMPATIBILITY);
    }

    public static RealmsWorldOptions createFromSettings(LevelSettings p_361674_, String p_370223_) {
        return createDefaultsWith(p_361674_.gameType(), p_361674_.difficulty(), p_361674_.hardcore(), p_370223_, p_361674_.levelName());
    }

    public static RealmsWorldOptions createEmptyDefaults() {
        RealmsWorldOptions realmsworldoptions = createDefaults();
        realmsworldoptions.setEmpty(true);
        return realmsworldoptions;
    }

    public void setEmpty(boolean p_87631_) {
        this.empty = p_87631_;
    }

    public static RealmsWorldOptions parse(JsonObject p_87629_, RealmsSettings p_363227_) {
        RealmsWorldOptions realmsworldoptions = new RealmsWorldOptions(
            JsonUtils.getBooleanOr("pvp", p_87629_, true),
            JsonUtils.getBooleanOr("spawnMonsters", p_87629_, true),
            JsonUtils.getIntOr("spawnProtection", p_87629_, 0),
            JsonUtils.getBooleanOr("commandBlocks", p_87629_, false),
            JsonUtils.getIntOr("difficulty", p_87629_, 2),
            JsonUtils.getIntOr("gameMode", p_87629_, 0),
            p_363227_.hardcore(),
            JsonUtils.getBooleanOr("forceGameMode", p_87629_, false),
            JsonUtils.getRequiredStringOr("slotName", p_87629_, ""),
            JsonUtils.getRequiredStringOr("version", p_87629_, ""),
            RealmsServer.getCompatibility(JsonUtils.getRequiredStringOr("compatibility", p_87629_, RealmsServer.Compatibility.UNVERIFIABLE.name()))
        );
        realmsworldoptions.templateId = JsonUtils.getLongOr("worldTemplateId", p_87629_, -1L);
        realmsworldoptions.templateImage = JsonUtils.getStringOr("worldTemplateImage", p_87629_, DEFAULT_TEMPLATE_IMAGE);
        return realmsworldoptions;
    }

    public String getSlotName(int p_87627_) {
        if (StringUtil.isBlank(this.slotName)) {
            return this.empty ? I18n.get("mco.configure.world.slot.empty") : this.getDefaultSlotName(p_87627_);
        } else {
            return this.slotName;
        }
    }

    public String getDefaultSlotName(int p_87634_) {
        return I18n.get("mco.configure.world.slot", p_87634_);
    }

    public String toJson() {
        JsonObject jsonobject = new JsonObject();
        if (!this.pvp) {
            jsonobject.addProperty("pvp", this.pvp);
        }

        if (!this.spawnMonsters) {
            jsonobject.addProperty("spawnMonsters", this.spawnMonsters);
        }

        if (this.spawnProtection != 0) {
            jsonobject.addProperty("spawnProtection", this.spawnProtection);
        }

        if (this.commandBlocks) {
            jsonobject.addProperty("commandBlocks", this.commandBlocks);
        }

        if (this.difficulty != 2) {
            jsonobject.addProperty("difficulty", this.difficulty);
        }

        if (this.gameMode != 0) {
            jsonobject.addProperty("gameMode", this.gameMode);
        }

        if (this.hardcore) {
            jsonobject.addProperty("hardcore", this.hardcore);
        }

        if (this.forceGameMode) {
            jsonobject.addProperty("forceGameMode", this.forceGameMode);
        }

        if (!Objects.equals(this.slotName, "")) {
            jsonobject.addProperty("slotName", this.slotName);
        }

        if (!Objects.equals(this.version, "")) {
            jsonobject.addProperty("version", this.version);
        }

        if (this.compatibility != DEFAULT_COMPATIBILITY) {
            jsonobject.addProperty("compatibility", this.compatibility.name());
        }

        return jsonobject.toString();
    }

    public RealmsWorldOptions clone() {
        return new RealmsWorldOptions(
            this.pvp,
            this.spawnMonsters,
            this.spawnProtection,
            this.commandBlocks,
            this.difficulty,
            this.gameMode,
            this.hardcore,
            this.forceGameMode,
            this.slotName,
            this.version,
            this.compatibility
        );
    }
}