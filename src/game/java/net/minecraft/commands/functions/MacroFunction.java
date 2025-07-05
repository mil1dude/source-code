package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MacroFunction<T extends ExecutionCommandSource<T>> implements CommandFunction<T> {
    private static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#"), p_312286_ -> {
        p_312286_.setMaximumFractionDigits(15);
        p_312286_.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
    });
    private static final int MAX_CACHE_ENTRIES = 8;
    private final List<String> parameters;
    private final Object2ObjectLinkedOpenHashMap<List<String>, InstantiatedFunction<T>> cache = new Object2ObjectLinkedOpenHashMap<>(8, 0.25F);
    private final ResourceLocation id;
    private final List<MacroFunction.Entry<T>> entries;

    public MacroFunction(ResourceLocation p_311437_, List<MacroFunction.Entry<T>> p_310862_, List<String> p_310686_) {
        this.id = p_311437_;
        this.entries = p_310862_;
        this.parameters = p_310686_;
    }

    @Override
    public ResourceLocation id() {
        return this.id;
    }

    @Override
    public InstantiatedFunction<T> instantiate(@Nullable CompoundTag p_309697_, CommandDispatcher<T> p_309980_) throws FunctionInstantiationException {
        if (p_309697_ == null) {
            throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_arguments", Component.translationArg(this.id())));
        } else {
            List<String> list = new ArrayList<>(this.parameters.size());

            for (String s : this.parameters) {
                Tag tag = p_309697_.get(s);
                if (tag == null) {
                    throw new FunctionInstantiationException(
                        Component.translatable("commands.function.error.missing_argument", Component.translationArg(this.id()), s)
                    );
                }

                list.add(stringify(tag));
            }

            InstantiatedFunction<T> instantiatedfunction = this.cache.getAndMoveToLast(list);
            if (instantiatedfunction != null) {
                return instantiatedfunction;
            } else {
                if (this.cache.size() >= 8) {
                    this.cache.removeFirst();
                }

                InstantiatedFunction<T> instantiatedfunction1 = this.substituteAndParse(this.parameters, list, p_309980_);
                this.cache.put(list, instantiatedfunction1);
                return instantiatedfunction1;
            }
        }
    }

    private static String stringify(Tag p_313061_) {
        if (p_313061_ instanceof FloatTag floattag) {
            return DECIMAL_FORMAT.format((double)floattag.getAsFloat());
        } else if (p_313061_ instanceof DoubleTag doubletag) {
            return DECIMAL_FORMAT.format(doubletag.getAsDouble());
        } else if (p_313061_ instanceof ByteTag bytetag) {
            return String.valueOf(bytetag.getAsByte());
        } else if (p_313061_ instanceof ShortTag shorttag) {
            return String.valueOf(shorttag.getAsShort());
        } else {
            return p_313061_ instanceof LongTag longtag ? String.valueOf(longtag.getAsLong()) : p_313061_.getAsString();
        }
    }

    private static void lookupValues(List<String> p_313206_, IntList p_310595_, List<String> p_310258_) {
        p_310258_.clear();
        p_310595_.forEach(p_312583_ -> p_310258_.add(p_313206_.get(p_312583_)));
    }

    private InstantiatedFunction<T> substituteAndParse(List<String> p_312865_, List<String> p_312778_, CommandDispatcher<T> p_311234_) throws FunctionInstantiationException {
        List<UnboundEntryAction<T>> list = new ArrayList<>(this.entries.size());
        List<String> list1 = new ArrayList<>(p_312778_.size());

        for (MacroFunction.Entry<T> entry : this.entries) {
            lookupValues(p_312778_, entry.parameters(), list1);
            list.add(entry.instantiate(list1, p_311234_, this.id));
        }

        return new PlainTextFunction<>(this.id().withPath(p_312634_ -> p_312634_ + "/" + p_312865_.hashCode()), list);
    }

    interface Entry<T> {
        IntList parameters();

        UnboundEntryAction<T> instantiate(List<String> p_312452_, CommandDispatcher<T> p_313016_, ResourceLocation p_311242_) throws FunctionInstantiationException;
    }

    static class MacroEntry<T extends ExecutionCommandSource<T>> implements MacroFunction.Entry<T> {
        private final StringTemplate template;
        private final IntList parameters;
        private final T compilationContext;

        public MacroEntry(StringTemplate p_309563_, IntList p_312180_, T p_336169_) {
            this.template = p_309563_;
            this.parameters = p_312180_;
            this.compilationContext = p_336169_;
        }

        @Override
        public IntList parameters() {
            return this.parameters;
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> p_312101_, CommandDispatcher<T> p_309379_, ResourceLocation p_312655_) throws FunctionInstantiationException {
            String s = this.template.substitute(p_312101_);

            try {
                return CommandFunction.parseCommand(p_309379_, this.compilationContext, new StringReader(s));
            } catch (CommandSyntaxException commandsyntaxexception) {
                throw new FunctionInstantiationException(
                    Component.translatable("commands.function.error.parse", Component.translationArg(p_312655_), s, commandsyntaxexception.getMessage())
                );
            }
        }
    }

    static class PlainTextEntry<T> implements MacroFunction.Entry<T> {
        private final UnboundEntryAction<T> compiledAction;

        public PlainTextEntry(UnboundEntryAction<T> p_309648_) {
            this.compiledAction = p_309648_;
        }

        @Override
        public IntList parameters() {
            return IntLists.emptyList();
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> p_311533_, CommandDispatcher<T> p_311835_, ResourceLocation p_311102_) {
            return this.compiledAction;
        }
    }
}