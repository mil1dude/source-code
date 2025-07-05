package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerInfo extends ValueObject implements ReflectionBasedSerialization {
    @SerializedName("name")
    @Nullable
    private String name;
    @SerializedName("uuid")
    private UUID uuid;
    @SerializedName("operator")
    private boolean operator;
    @SerializedName("accepted")
    private boolean accepted;
    @SerializedName("online")
    private boolean online;

    public String getName() {
        return this.name == null ? "" : this.name;
    }

    public void setName(String p_87449_) {
        this.name = p_87449_;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID p_300492_) {
        this.uuid = p_300492_;
    }

    public boolean isOperator() {
        return this.operator;
    }

    public void setOperator(boolean p_87451_) {
        this.operator = p_87451_;
    }

    public boolean getAccepted() {
        return this.accepted;
    }

    public void setAccepted(boolean p_87456_) {
        this.accepted = p_87456_;
    }

    public boolean getOnline() {
        return this.online;
    }

    public void setOnline(boolean p_87459_) {
        this.online = p_87459_;
    }
}