package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

    public static ResourceLocationArgument id() {
        return new ResourceLocationArgument();
    }

    public static ResourceLocation getId(CommandContext<CommandSourceStack> p_107012_, String p_107013_) {
        return p_107012_.getArgument(p_107013_, ResourceLocation.class);
    }

    public ResourceLocation parse(StringReader p_106986_) throws CommandSyntaxException {
        return ResourceLocation.read(p_106986_);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}