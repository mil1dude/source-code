package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;

public class UserBanList extends StoredUserList<GameProfile, UserBanListEntry> {
    public UserBanList(File p_11402_) {
        super(p_11402_);
    }

    @Override
    protected StoredUserEntry<GameProfile> createEntry(JsonObject p_11405_) {
        return new UserBanListEntry(p_11405_);
    }

    public boolean isBanned(GameProfile p_11407_) {
        return this.contains(p_11407_);
    }

    @Override
    public String[] getUserList() {
        return this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(GameProfile::getName).toArray(String[]::new);
    }

    protected String getKeyForUser(GameProfile p_11411_) {
        return p_11411_.getId().toString();
    }
}