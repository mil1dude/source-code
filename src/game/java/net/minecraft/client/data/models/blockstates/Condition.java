package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface Condition extends Supplier<JsonElement> {
    void validate(StateDefinition<?, ?> p_378274_);

    static Condition.TerminalCondition condition() {
        return new Condition.TerminalCondition();
    }

    static Condition and(Condition... p_376370_) {
        return new Condition.CompositeCondition(Condition.Operation.AND, Arrays.asList(p_376370_));
    }

    static Condition or(Condition... p_376775_) {
        return new Condition.CompositeCondition(Condition.Operation.OR, Arrays.asList(p_376775_));
    }

    @OnlyIn(Dist.CLIENT)
    public static class CompositeCondition implements Condition {
        private final Condition.Operation operation;
        private final List<Condition> subconditions;

        CompositeCondition(Condition.Operation p_376168_, List<Condition> p_375599_) {
            this.operation = p_376168_;
            this.subconditions = p_375599_;
        }

        @Override
        public void validate(StateDefinition<?, ?> p_375975_) {
            this.subconditions.forEach(p_377907_ -> p_377907_.validate(p_375975_));
        }

        public JsonElement get() {
            JsonArray jsonarray = new JsonArray();
            this.subconditions.stream().map(Supplier::get).forEach(jsonarray::add);
            JsonObject jsonobject = new JsonObject();
            jsonobject.add(this.operation.id, jsonarray);
            return jsonobject;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Operation {
        AND("AND"),
        OR("OR");

        final String id;

        private Operation(final String p_375720_) {
            this.id = p_375720_;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TerminalCondition implements Condition {
        private final Map<Property<?>, String> terms = Maps.newHashMap();

        private static <T extends Comparable<T>> String joinValues(Property<T> p_376628_, Stream<T> p_376793_) {
            return p_376793_.map(p_376628_::getName).collect(Collectors.joining("|"));
        }

        private static <T extends Comparable<T>> String getTerm(Property<T> p_375838_, T p_376932_, T[] p_377385_) {
            return joinValues(p_375838_, Stream.concat(Stream.of(p_376932_), Stream.of(p_377385_)));
        }

        private <T extends Comparable<T>> void putValue(Property<T> p_378347_, String p_375759_) {
            String s = this.terms.put(p_378347_, p_375759_);
            if (s != null) {
                throw new IllegalStateException("Tried to replace " + p_378347_ + " value from " + s + " to " + p_375759_);
            }
        }

        public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> p_377417_, T p_376821_) {
            this.putValue(p_377417_, p_377417_.getName(p_376821_));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> p_376002_, T p_377971_, T... p_376529_) {
            this.putValue(p_376002_, getTerm(p_376002_, p_377971_, p_376529_));
            return this;
        }

        public final <T extends Comparable<T>> Condition.TerminalCondition negatedTerm(Property<T> p_377663_, T p_378614_) {
            this.putValue(p_377663_, "!" + p_377663_.getName(p_378614_));
            return this;
        }

        @SafeVarargs
        public final <T extends Comparable<T>> Condition.TerminalCondition negatedTerm(Property<T> p_377804_, T p_377881_, T... p_375630_) {
            this.putValue(p_377804_, "!" + getTerm(p_377804_, p_377881_, p_375630_));
            return this;
        }

        public JsonElement get() {
            JsonObject jsonobject = new JsonObject();
            this.terms.forEach((p_375818_, p_378345_) -> jsonobject.addProperty(p_375818_.getName(), p_378345_));
            return jsonobject;
        }

        @Override
        public void validate(StateDefinition<?, ?> p_378182_) {
            List<Property<?>> list = this.terms
                .keySet()
                .stream()
                .filter(p_375684_ -> p_378182_.getProperty(p_375684_.getName()) != p_375684_)
                .collect(Collectors.toList());
            if (!list.isEmpty()) {
                throw new IllegalStateException("Properties " + list + " are missing from " + p_378182_);
            }
        }
    }
}