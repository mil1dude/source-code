package net.minecraft.network.protocol.status;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record ServerStatus(
    Component description,
    Optional<ServerStatus.Players> players,
    Optional<ServerStatus.Version> version,
    Optional<ServerStatus.Favicon> favicon,
    boolean enforcesSecureChat
) {
    public static final Codec<ServerStatus> CODEC = RecordCodecBuilder.create(
        p_326135_ -> p_326135_.group(
                    ComponentSerialization.CODEC.lenientOptionalFieldOf("description", CommonComponents.EMPTY).forGetter(ServerStatus::description),
                    ServerStatus.Players.CODEC.lenientOptionalFieldOf("players").forGetter(ServerStatus::players),
                    ServerStatus.Version.CODEC.lenientOptionalFieldOf("version").forGetter(ServerStatus::version),
                    ServerStatus.Favicon.CODEC.lenientOptionalFieldOf("favicon").forGetter(ServerStatus::favicon),
                    Codec.BOOL.lenientOptionalFieldOf("enforcesSecureChat", Boolean.valueOf(false)).forGetter(ServerStatus::enforcesSecureChat)
                )
                .apply(p_326135_, ServerStatus::new)
    );

    public static record Favicon(byte[] iconBytes) {
        private static final String PREFIX = "data:image/png;base64,";
        public static final Codec<ServerStatus.Favicon> CODEC = Codec.STRING.comapFlatMap(p_274795_ -> {
            if (!p_274795_.startsWith("data:image/png;base64,")) {
                return DataResult.error(() -> "Unknown format");
            } else {
                try {
                    String s = p_274795_.substring("data:image/png;base64,".length()).replaceAll("\n", "");
                    byte[] abyte = Base64.getDecoder().decode(s.getBytes(StandardCharsets.UTF_8));
                    return DataResult.success(new ServerStatus.Favicon(abyte));
                } catch (IllegalArgumentException illegalargumentexception) {
                    return DataResult.error(() -> "Malformed base64 server icon");
                }
            }
        }, p_273258_ -> "data:image/png;base64," + new String(Base64.getEncoder().encode(p_273258_.iconBytes), StandardCharsets.UTF_8));
    }

    public static record Players(int max, int online, List<GameProfile> sample) {
        private static final Codec<GameProfile> PROFILE_CODEC = RecordCodecBuilder.create(
            p_272926_ -> p_272926_.group(
                        UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(GameProfile::getId), Codec.STRING.fieldOf("name").forGetter(GameProfile::getName)
                    )
                    .apply(p_272926_, GameProfile::new)
        );
        public static final Codec<ServerStatus.Players> CODEC = RecordCodecBuilder.create(
            p_326136_ -> p_326136_.group(
                        Codec.INT.fieldOf("max").forGetter(ServerStatus.Players::max),
                        Codec.INT.fieldOf("online").forGetter(ServerStatus.Players::online),
                        PROFILE_CODEC.listOf().lenientOptionalFieldOf("sample", List.of()).forGetter(ServerStatus.Players::sample)
                    )
                    .apply(p_326136_, ServerStatus.Players::new)
        );
    }

    public static record Version(String name, int protocol) {
        public static final Codec<ServerStatus.Version> CODEC = RecordCodecBuilder.create(
            p_273157_ -> p_273157_.group(
                        Codec.STRING.fieldOf("name").forGetter(ServerStatus.Version::name),
                        Codec.INT.fieldOf("protocol").forGetter(ServerStatus.Version::protocol)
                    )
                    .apply(p_273157_, ServerStatus.Version::new)
        );

        public static ServerStatus.Version current() {
            WorldVersion worldversion = SharedConstants.getCurrentVersion();
            return new ServerStatus.Version(worldversion.getName(), worldversion.getProtocolVersion());
        }
    }
}