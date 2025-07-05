package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;

public interface StringReaderTerms {
    static Term<StringReader> word(String p_327924_) {
        return new StringReaderTerms.TerminalWord(p_327924_);
    }

    static Term<StringReader> character(char p_329750_) {
        return new StringReaderTerms.TerminalCharacter(p_329750_);
    }

    public static record TerminalCharacter(char value) implements Term<StringReader> {
        @Override
        public boolean parse(ParseState<StringReader> p_330727_, Scope p_335740_, Control p_331061_) {
            p_330727_.input().skipWhitespace();
            int i = p_330727_.mark();
            if (p_330727_.input().canRead() && p_330727_.input().read() == this.value) {
                return true;
            } else {
                p_330727_.errorCollector()
                    .store(
                        i,
                        p_332558_ -> Stream.of(String.valueOf(this.value)),
                        CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(this.value)
                    );
                return false;
            }
        }
    }

    public static record TerminalWord(String value) implements Term<StringReader> {
        @Override
        public boolean parse(ParseState<StringReader> p_333566_, Scope p_332362_, Control p_328812_) {
            p_333566_.input().skipWhitespace();
            int i = p_333566_.mark();
            String s = p_333566_.input().readUnquotedString();
            if (!s.equals(this.value)) {
                p_333566_.errorCollector()
                    .store(i, p_331163_ -> Stream.of(this.value), CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(this.value));
                return false;
            } else {
                return true;
            }
        }
    }
}