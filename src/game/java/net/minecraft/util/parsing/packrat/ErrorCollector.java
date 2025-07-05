package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;

public interface ErrorCollector<S> {
    void store(int p_334236_, SuggestionSupplier<S> p_329361_, Object p_331748_);

    default void store(int p_330627_, Object p_332187_) {
        this.store(p_330627_, SuggestionSupplier.empty(), p_332187_);
    }

    void finish(int p_334270_);

    public static class LongestOnly<S> implements ErrorCollector<S> {
        private final List<ErrorEntry<S>> entries = new ArrayList<>();
        private int lastCursor = -1;

        private void discardErrorsFromShorterParse(int p_331637_) {
            if (p_331637_ > this.lastCursor) {
                this.lastCursor = p_331637_;
                this.entries.clear();
            }
        }

        @Override
        public void finish(int p_334009_) {
            this.discardErrorsFromShorterParse(p_334009_);
        }

        @Override
        public void store(int p_331115_, SuggestionSupplier<S> p_329965_, Object p_332125_) {
            this.discardErrorsFromShorterParse(p_331115_);
            if (p_331115_ == this.lastCursor) {
                this.entries.add(new ErrorEntry<>(p_331115_, p_329965_, p_332125_));
            }
        }

        public List<ErrorEntry<S>> entries() {
            return this.entries;
        }

        public int cursor() {
            return this.lastCursor;
        }
    }
}