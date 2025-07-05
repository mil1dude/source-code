package net.minecraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.execution.TraceCallbacks;

public interface ExecutionCommandSource<T extends ExecutionCommandSource<T>> {
    boolean hasPermission(int p_309473_);

    T withCallback(CommandResultCallback p_311254_);

    CommandResultCallback callback();

    default T clearCallbacks() {
        return this.withCallback(CommandResultCallback.EMPTY);
    }

    CommandDispatcher<T> dispatcher();

    void handleError(CommandExceptionType p_311834_, Message p_310647_, boolean p_310226_, @Nullable TraceCallbacks p_312033_);

    boolean isSilent();

    default void handleError(CommandSyntaxException p_311076_, boolean p_310707_, @Nullable TraceCallbacks p_311569_) {
        this.handleError(p_311076_.getType(), p_311076_.getRawMessage(), p_310707_, p_311569_);
    }

    static <T extends ExecutionCommandSource<T>> ResultConsumer<T> resultConsumer() {
        return (p_310000_, p_311414_, p_311999_) -> p_310000_.getSource().callback().onResult(p_311414_, p_311999_);
    }
}