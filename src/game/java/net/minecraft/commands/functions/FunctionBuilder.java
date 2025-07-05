package net.minecraft.commands.functions;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.resources.ResourceLocation;

class FunctionBuilder<T extends ExecutionCommandSource<T>> {
    @Nullable
    private List<UnboundEntryAction<T>> plainEntries = new ArrayList<>();
    @Nullable
    private List<MacroFunction.Entry<T>> macroEntries;
    private final List<String> macroArguments = new ArrayList<>();

    public void addCommand(UnboundEntryAction<T> p_309592_) {
        if (this.macroEntries != null) {
            this.macroEntries.add(new MacroFunction.PlainTextEntry<>(p_309592_));
        } else {
            this.plainEntries.add(p_309592_);
        }
    }

    private int getArgumentIndex(String p_312711_) {
        int i = this.macroArguments.indexOf(p_312711_);
        if (i == -1) {
            i = this.macroArguments.size();
            this.macroArguments.add(p_312711_);
        }

        return i;
    }

    private IntList convertToIndices(List<String> p_311467_) {
        IntArrayList intarraylist = new IntArrayList(p_311467_.size());

        for (String s : p_311467_) {
            intarraylist.add(this.getArgumentIndex(s));
        }

        return intarraylist;
    }

    public void addMacro(String p_312905_, int p_310777_, T p_328106_) {
        StringTemplate stringtemplate = StringTemplate.fromString(p_312905_, p_310777_);
        if (this.plainEntries != null) {
            this.macroEntries = new ArrayList<>(this.plainEntries.size() + 1);

            for (UnboundEntryAction<T> unboundentryaction : this.plainEntries) {
                this.macroEntries.add(new MacroFunction.PlainTextEntry<>(unboundentryaction));
            }

            this.plainEntries = null;
        }

        this.macroEntries.add(new MacroFunction.MacroEntry<>(stringtemplate, this.convertToIndices(stringtemplate.variables()), p_328106_));
    }

    public CommandFunction<T> build(ResourceLocation p_311383_) {
        return (CommandFunction<T>)(this.macroEntries != null
            ? new MacroFunction<>(p_311383_, this.macroEntries, this.macroArguments)
            : new PlainTextFunction<>(p_311383_, this.plainEntries));
    }
}