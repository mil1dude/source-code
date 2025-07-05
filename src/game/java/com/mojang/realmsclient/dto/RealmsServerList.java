package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsServerList extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public List<RealmsServer> servers;

    public static RealmsServerList parse(String p_87578_) {
        RealmsServerList realmsserverlist = new RealmsServerList();
        realmsserverlist.servers = new ArrayList<>();

        try {
            JsonObject jsonobject = JsonParser.parseString(p_87578_).getAsJsonObject();
            if (jsonobject.get("servers").isJsonArray()) {
                for (JsonElement jsonelement : jsonobject.get("servers").getAsJsonArray()) {
                    realmsserverlist.servers.add(RealmsServer.parse(jsonelement.getAsJsonObject()));
                }
            }
        } catch (Exception exception) {
            LOGGER.error("Could not parse McoServerList: {}", exception.getMessage());
        }

        return realmsserverlist;
    }
}