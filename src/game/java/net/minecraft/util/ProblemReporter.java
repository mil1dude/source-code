package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public interface ProblemReporter {
    ProblemReporter forChild(String p_311126_);

    void report(String p_312072_);

    public static class Collector implements ProblemReporter {
        private final Multimap<String, String> problems;
        private final Supplier<String> path;
        @Nullable
        private String pathCache;

        public Collector() {
            this(HashMultimap.create(), () -> "");
        }

        private Collector(Multimap<String, String> p_311018_, Supplier<String> p_312668_) {
            this.problems = p_311018_;
            this.path = p_312668_;
        }

        private String getPath() {
            if (this.pathCache == null) {
                this.pathCache = this.path.get();
            }

            return this.pathCache;
        }

        @Override
        public ProblemReporter forChild(String p_311756_) {
            return new ProblemReporter.Collector(this.problems, () -> this.getPath() + p_311756_);
        }

        @Override
        public void report(String p_310299_) {
            this.problems.put(this.getPath(), p_310299_);
        }

        public Multimap<String, String> get() {
            return ImmutableMultimap.copyOf(this.problems);
        }

        public Optional<String> getReport() {
            Multimap<String, String> multimap = this.get();
            if (!multimap.isEmpty()) {
                String s = multimap.asMap()
                    .entrySet()
                    .stream()
                    .map(p_341246_ -> " at " + p_341246_.getKey() + ": " + String.join("; ", p_341246_.getValue()))
                    .collect(Collectors.joining("\n"));
                return Optional.of(s);
            } else {
                return Optional.empty();
            }
        }
    }
}