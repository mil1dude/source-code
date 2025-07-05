package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public abstract class ResourceLookupRule<C, V> implements Rule<StringReader, V>, ResourceSuggestion {
    private final Atom<ResourceLocation> idParser;
    protected final C context;

    protected ResourceLookupRule(Atom<ResourceLocation> p_330644_, C p_330414_) {
        this.idParser = p_330644_;
        this.context = p_330414_;
    }

    @Override
    public Optional<V> parse(ParseState<StringReader> p_332578_) {
        p_332578_.input().skipWhitespace();
        int i = p_332578_.mark();
        Optional<ResourceLocation> optional = p_332578_.parse(this.idParser);
        if (optional.isPresent()) {
            try {
                return Optional.of(this.validateElement(p_332578_.input(), optional.get()));
            } catch (Exception exception) {
                p_332578_.errorCollector().store(i, this, exception);
                return Optional.empty();
            }
        } else {
            p_332578_.errorCollector().store(i, this, ResourceLocation.ERROR_INVALID.createWithContext(p_332578_.input()));
            return Optional.empty();
        }
    }

    protected abstract V validateElement(ImmutableStringReader p_336199_, ResourceLocation p_330230_) throws Exception;
}