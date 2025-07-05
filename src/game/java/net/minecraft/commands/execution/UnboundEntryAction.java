package net.minecraft.commands.execution;

@FunctionalInterface
public interface UnboundEntryAction<T> {
    void execute(T p_311343_, ExecutionContext<T> p_309614_, Frame p_309740_);

    default EntryAction<T> bind(T p_312071_) {
        return (p_309583_, p_311194_) -> this.execute(p_312071_, p_309583_, p_311194_);
    }
}