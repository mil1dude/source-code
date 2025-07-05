package net.minecraft.util.parsing.packrat;

import java.util.stream.Stream;

public interface SuggestionSupplier<S> {
    Stream<String> possibleValues(ParseState<S> p_335189_);

    static <S> SuggestionSupplier<S> empty() {
        return p_331625_ -> Stream.empty();
    }
}