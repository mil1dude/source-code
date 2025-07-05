package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import java.util.Optional;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class TagParseRule implements Rule<StringReader, Tag> {
    public static final Rule<StringReader, Tag> INSTANCE = new TagParseRule();

    private TagParseRule() {
    }

    @Override
    public Optional<Tag> parse(ParseState<StringReader> p_334310_) {
        p_334310_.input().skipWhitespace();
        int i = p_334310_.mark();

        try {
            return Optional.of(new TagParser(p_334310_.input()).readValue());
        } catch (Exception exception) {
            p_334310_.errorCollector().store(i, exception);
            return Optional.empty();
        }
    }
}