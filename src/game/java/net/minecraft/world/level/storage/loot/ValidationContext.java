package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;

public class ValidationContext {
    private final ProblemReporter reporter;
    private final ContextKeySet contextKeySet;
    private final Optional<HolderGetter.Provider> resolver;
    private final Set<ResourceKey<?>> visitedElements;

    public ValidationContext(ProblemReporter p_312350_, ContextKeySet p_368637_, HolderGetter.Provider p_331032_) {
        this(p_312350_, p_368637_, Optional.of(p_331032_), Set.of());
    }

    public ValidationContext(ProblemReporter p_310867_, ContextKeySet p_363228_) {
        this(p_310867_, p_363228_, Optional.empty(), Set.of());
    }

    private ValidationContext(ProblemReporter p_345071_, ContextKeySet p_370050_, Optional<HolderGetter.Provider> p_343446_, Set<ResourceKey<?>> p_344231_) {
        this.reporter = p_345071_;
        this.contextKeySet = p_370050_;
        this.resolver = p_343446_;
        this.visitedElements = p_344231_;
    }

    public ValidationContext forChild(String p_79366_) {
        return new ValidationContext(this.reporter.forChild(p_79366_), this.contextKeySet, this.resolver, this.visitedElements);
    }

    public ValidationContext enterElement(String p_279180_, ResourceKey<?> p_331211_) {
        Set<ResourceKey<?>> set = ImmutableSet.<ResourceKey<?>>builder().addAll(this.visitedElements).add(p_331211_).build();
        return new ValidationContext(this.reporter.forChild(p_279180_), this.contextKeySet, this.resolver, set);
    }

    public boolean hasVisitedElement(ResourceKey<?> p_335461_) {
        return this.visitedElements.contains(p_335461_);
    }

    public void reportProblem(String p_79358_) {
        this.reporter.report(p_79358_);
    }

    public void validateContextUsage(LootContextUser p_368628_) {
        Set<ContextKey<?>> set = p_368628_.getReferencedContextParams();
        Set<ContextKey<?>> set1 = Sets.difference(set, this.contextKeySet.allowed());
        if (!set1.isEmpty()) {
            this.reporter.report("Parameters " + set1 + " are not provided in this context");
        }
    }

    public HolderGetter.Provider resolver() {
        return this.resolver.orElseThrow(() -> new UnsupportedOperationException("References not allowed"));
    }

    public boolean allowsReferences() {
        return this.resolver.isPresent();
    }

    public ValidationContext setContextKeySet(ContextKeySet p_369204_) {
        return new ValidationContext(this.reporter, p_369204_, this.resolver, this.visitedElements);
    }

    public ProblemReporter reporter() {
        return this.reporter;
    }
}