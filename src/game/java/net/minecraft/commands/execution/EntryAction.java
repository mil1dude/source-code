package net.minecraft.commands.execution;

@FunctionalInterface
public interface EntryAction<T> {
    void execute(ExecutionContext<T> p_312387_, Frame p_313236_);
}