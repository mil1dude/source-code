package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public class DebugConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> p_299014_) {
        p_299014_.register(
            Commands.literal("debugconfig")
                .requires(p_299396_ -> p_299396_.hasPermission(3))
                .then(
                    Commands.literal("config")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes(p_300433_ -> config(p_300433_.getSource(), EntityArgument.getPlayer(p_300433_, "target")))
                        )
                )
                .then(
                    Commands.literal("unconfig")
                        .then(
                            Commands.argument("target", UuidArgument.uuid())
                                .suggests((p_297904_, p_297883_) -> SharedSuggestionProvider.suggest(getUuidsInConfig(p_297904_.getSource().getServer()), p_297883_))
                                .executes(p_301004_ -> unconfig(p_301004_.getSource(), UuidArgument.getUuid(p_301004_, "target")))
                        )
                )
        );
    }

    private static Iterable<String> getUuidsInConfig(MinecraftServer p_299245_) {
        Set<String> set = new HashSet<>();

        for (Connection connection : p_299245_.getConnection().getConnections()) {
            if (connection.getPacketListener() instanceof ServerConfigurationPacketListenerImpl serverconfigurationpacketlistenerimpl) {
                set.add(serverconfigurationpacketlistenerimpl.getOwner().getId().toString());
            }
        }

        return set;
    }

    private static int config(CommandSourceStack p_297745_, ServerPlayer p_300074_) {
        GameProfile gameprofile = p_300074_.getGameProfile();
        p_300074_.connection.switchToConfig();
        p_297745_.sendSuccess(() -> Component.literal("Switched player " + gameprofile.getName() + "(" + gameprofile.getId() + ") to config mode"), false);
        return 1;
    }

    private static int unconfig(CommandSourceStack p_300627_, UUID p_299392_) {
        for (Connection connection : p_300627_.getServer().getConnection().getConnections()) {
            PacketListener packetlistener = connection.getPacketListener();
            if (packetlistener instanceof ServerConfigurationPacketListenerImpl) {
                ServerConfigurationPacketListenerImpl serverconfigurationpacketlistenerimpl = (ServerConfigurationPacketListenerImpl)packetlistener;
                if (serverconfigurationpacketlistenerimpl.getOwner().getId().equals(p_299392_)) {
                    serverconfigurationpacketlistenerimpl.returnToWorld();
                }
            }
        }

        p_300627_.sendFailure(Component.literal("Can't find player to unconfig"));
        return 0;
    }
}