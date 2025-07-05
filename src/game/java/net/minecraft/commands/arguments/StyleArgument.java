package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParserUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class StyleArgument implements ArgumentType<Style> {
    private static final Collection<String> EXAMPLES = List.of("{\"bold\": true}\n");
    public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType(
        p_310381_ -> Component.translatableEscape("argument.style.invalid", p_310381_)
    );
    private final HolderLookup.Provider registries;

    private StyleArgument(HolderLookup.Provider p_329379_) {
        this.registries = p_329379_;
    }

    public static Style getStyle(CommandContext<CommandSourceStack> p_311982_, String p_309702_) {
        return p_311982_.getArgument(p_309702_, Style.class);
    }

    public static StyleArgument style(CommandBuildContext p_331105_) {
        return new StyleArgument(p_331105_);
    }

    public Style parse(StringReader p_311382_) throws CommandSyntaxException {
        try {
            return ParserUtils.parseJson(this.registries, p_311382_, Style.Serializer.CODEC);
        } catch (Exception exception) {
            String s = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();
            throw ERROR_INVALID_JSON.createWithContext(p_311382_, s);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}