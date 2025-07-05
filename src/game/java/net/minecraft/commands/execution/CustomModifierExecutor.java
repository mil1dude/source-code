package net.minecraft.commands.execution;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;

public interface CustomModifierExecutor<T> {
    void apply(T p_312110_, List<T> p_310809_, ContextChain<T> p_312566_, ChainModifiers p_312568_, ExecutionControl<T> p_313127_);

    public interface ModifierAdapter<T> extends RedirectModifier<T>, CustomModifierExecutor<T> {
        @Override
        default Collection<T> apply(CommandContext<T> p_311038_) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}