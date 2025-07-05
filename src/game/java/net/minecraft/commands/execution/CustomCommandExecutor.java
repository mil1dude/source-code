package net.minecraft.commands.execution;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;

public interface CustomCommandExecutor<T> {
    void run(T p_310884_, ContextChain<T> p_312906_, ChainModifiers p_310837_, ExecutionControl<T> p_310586_);

    public interface CommandAdapter<T> extends Command<T>, CustomCommandExecutor<T> {
        @Override
        default int run(CommandContext<T> p_309955_) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }

    public abstract static class WithErrorHandling<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor<T> {
        public final void run(T p_310241_, ContextChain<T> p_311766_, ChainModifiers p_310779_, ExecutionControl<T> p_309382_) {
            try {
                this.runGuarded(p_310241_, p_311766_, p_310779_, p_309382_);
            } catch (CommandSyntaxException commandsyntaxexception) {
                this.onError(commandsyntaxexception, p_310241_, p_310779_, p_309382_.tracer());
                p_310241_.callback().onFailure();
            }
        }

        protected void onError(CommandSyntaxException p_313040_, T p_312743_, ChainModifiers p_309642_, @Nullable TraceCallbacks p_309545_) {
            p_312743_.handleError(p_313040_, p_309642_.isForked(), p_309545_);
        }

        protected abstract void runGuarded(T p_311664_, ContextChain<T> p_312225_, ChainModifiers p_309888_, ExecutionControl<T> p_313051_) throws CommandSyntaxException;
    }
}