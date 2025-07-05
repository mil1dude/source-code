package net.minecraft.util.parsing.packrat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

public abstract class ParseState<S> {
    private final Map<ParseState.CacheKey<?>, ParseState.CacheEntry<?>> ruleCache = new HashMap<>();
    private final Dictionary<S> dictionary;
    private final ErrorCollector<S> errorCollector;

    protected ParseState(Dictionary<S> p_331339_, ErrorCollector<S> p_333871_) {
        this.dictionary = p_331339_;
        this.errorCollector = p_333871_;
    }

    public ErrorCollector<S> errorCollector() {
        return this.errorCollector;
    }

    public <T> Optional<T> parseTopRule(Atom<T> p_334307_) {
        Optional<T> optional = this.parse(p_334307_);
        if (optional.isPresent()) {
            this.errorCollector.finish(this.mark());
        }

        return optional;
    }

    public <T> Optional<T> parse(Atom<T> p_335708_) {
        ParseState.CacheKey<T> cachekey = new ParseState.CacheKey<>(p_335708_, this.mark());
        ParseState.CacheEntry<T> cacheentry = this.lookupInCache(cachekey);
        if (cacheentry != null) {
            this.restore(cacheentry.mark());
            return cacheentry.value;
        } else {
            Rule<S, T> rule = this.dictionary.get(p_335708_);
            if (rule == null) {
                throw new IllegalStateException("No symbol " + p_335708_);
            } else {
                Optional<T> optional = rule.parse(this);
                this.storeInCache(cachekey, optional);
                return optional;
            }
        }
    }

    @Nullable
    private <T> ParseState.CacheEntry<T> lookupInCache(ParseState.CacheKey<T> p_333102_) {
        return (ParseState.CacheEntry<T>)this.ruleCache.get(p_333102_);
    }

    private <T> void storeInCache(ParseState.CacheKey<T> p_333772_, Optional<T> p_329813_) {
        this.ruleCache.put(p_333772_, new ParseState.CacheEntry<>(p_329813_, this.mark()));
    }

    public abstract S input();

    public abstract int mark();

    public abstract void restore(int p_331216_);

    static record CacheEntry<T>(Optional<T> value, int mark) {
    }

    static record CacheKey<T>(Atom<T> name, int mark) {
    }
}