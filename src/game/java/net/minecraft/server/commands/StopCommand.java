package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> p_138786_) {
        p_138786_.register(Commands.literal("stop").requires(p_138790_ -> p_138790_.hasPermission(4)).executes(p_288628_ -> {
            p_288628_.getSource().sendSuccess(() -> Component.translatable("commands.stop.stopping"), true);
            p_288628_.getSource().getServer().halt(false);
            return 1;
        }));
    }
}