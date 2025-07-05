package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;

public class PacketReport implements DataProvider {
    private final PackOutput output;

    public PacketReport(PackOutput p_343965_) {
        this.output = p_343965_;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput p_344127_) {
        Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("packets.json");
        return DataProvider.saveStable(p_344127_, this.serializePackets(), path);
    }

    private JsonElement serializePackets() {
        JsonObject jsonobject = new JsonObject();
        Stream.of(
                HandshakeProtocols.SERVERBOUND_TEMPLATE,
                StatusProtocols.CLIENTBOUND_TEMPLATE,
                StatusProtocols.SERVERBOUND_TEMPLATE,
                LoginProtocols.CLIENTBOUND_TEMPLATE,
                LoginProtocols.SERVERBOUND_TEMPLATE,
                ConfigurationProtocols.CLIENTBOUND_TEMPLATE,
                ConfigurationProtocols.SERVERBOUND_TEMPLATE,
                GameProtocols.CLIENTBOUND_TEMPLATE,
                GameProtocols.SERVERBOUND_TEMPLATE
            )
            .collect(Collectors.groupingBy(ProtocolInfo.Unbound::id))
            .forEach((p_345445_, p_343440_) -> {
                JsonObject jsonobject1 = new JsonObject();
                jsonobject.add(p_345445_.id(), jsonobject1);
                p_343440_.forEach(p_342835_ -> {
                    JsonObject jsonobject2 = new JsonObject();
                    jsonobject1.add(p_342835_.flow().id(), jsonobject2);
                    p_342835_.listPackets((p_342767_, p_343941_) -> {
                        JsonObject jsonobject3 = new JsonObject();
                        jsonobject3.addProperty("protocol_id", p_343941_);
                        jsonobject2.add(p_342767_.id().toString(), jsonobject3);
                    });
                });
            });
        return jsonobject;
    }

    @Override
    public String getName() {
        return "Packet Report";
    }
}