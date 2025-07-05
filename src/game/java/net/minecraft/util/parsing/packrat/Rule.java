package net.minecraft.util.parsing.packrat;

import java.util.Optional;

public interface Rule<S, T> {
    Optional<T> parse(ParseState<S> p_335539_);

    static <S, T> Rule<S, T> fromTerm(Term<S> p_334127_, Rule.RuleAction<S, T> p_334890_) {
        return new Rule.WrappedTerm<>(p_334890_, p_334127_);
    }

    static <S, T> Rule<S, T> fromTerm(Term<S> p_336211_, Rule.SimpleRuleAction<T> p_332994_) {
        return new Rule.WrappedTerm<>((p_331302_, p_331658_) -> Optional.of(p_332994_.run(p_331658_)), p_336211_);
    }

    @FunctionalInterface
    public interface RuleAction<S, T> {
        Optional<T> run(ParseState<S> p_332162_, Scope p_335135_);
    }

    @FunctionalInterface
    public interface SimpleRuleAction<T> {
        T run(Scope p_332535_);
    }

    public static record WrappedTerm<S, T>(Rule.RuleAction<S, T> action, Term<S> child) implements Rule<S, T> {
        @Override
        public Optional<T> parse(ParseState<S> p_328860_) {
            Scope scope = new Scope();
            return this.child.parse(p_328860_, scope, Control.UNBOUND) ? this.action.run(p_328860_, scope) : Optional.empty();
        }
    }
}