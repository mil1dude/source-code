package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsServerAddress extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    public String address;
    @Nullable
    public String resourcePackUrl;
    @Nullable
    public String resourcePackHash;

    public static RealmsServerAddress parse(String p_87572_) {
        RealmsServerAddress realmsserveraddress = new RealmsServerAddress();

        try {
            JsonObject jsonobject = JsonParser.parseString(p_87572_).getAsJsonObject();
            realmsserveraddress.address = JsonUtils.getStringOr("address", jsonobject, null);
            realmsserveraddress.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", jsonobject, null);
            realmsserveraddress.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", jsonobject, null);
        } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsServerAddress: {}", exception.getMessage());
        }

        return realmsserveraddress;
    }
}