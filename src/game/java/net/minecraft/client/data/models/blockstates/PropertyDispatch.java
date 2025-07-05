package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class PropertyDispatch {
    private final Map<Selector, List<Variant>> values = Maps.newHashMap();

    protected void putValue(Selector p_378338_, List<Variant> p_376928_) {
        List<Variant> list = this.values.put(p_378338_, p_376928_);
        if (list != null) {
            throw new IllegalStateException("Value " + p_378338_ + " is already defined");
        }
    }

    Map<Selector, List<Variant>> getEntries() {
        this.verifyComplete();
        return ImmutableMap.copyOf(this.values);
    }

    private void verifyComplete() {
        List<Property<?>> list = this.getDefinedProperties();
        Stream<Selector> stream = Stream.of(Selector.empty());

        for (Property<?> property : list) {
            stream = stream.flatMap(p_378010_ -> property.getAllValues().map(p_378010_::extend));
        }

        List<Selector> list1 = stream.filter(p_377619_ -> !this.values.containsKey(p_377619_)).collect(Collectors.toList());
        if (!list1.isEmpty()) {
            throw new IllegalStateException("Missing definition for properties: " + list1);
        }
    }

    abstract List<Property<?>> getDefinedProperties();

    public static <T1 extends Comparable<T1>> PropertyDispatch.C1<T1> property(Property<T1> p_375693_) {
        return new PropertyDispatch.C1<>(p_375693_);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>> PropertyDispatch.C2<T1, T2> properties(Property<T1> p_378486_, Property<T2> p_376121_) {
        return new PropertyDispatch.C2<>(p_378486_, p_376121_);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> PropertyDispatch.C3<T1, T2, T3> properties(
        Property<T1> p_378219_, Property<T2> p_376157_, Property<T3> p_377920_
    ) {
        return new PropertyDispatch.C3<>(p_378219_, p_376157_, p_377920_);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> PropertyDispatch.C4<T1, T2, T3, T4> properties(
        Property<T1> p_376975_, Property<T2> p_376597_, Property<T3> p_375517_, Property<T4> p_375767_
    ) {
        return new PropertyDispatch.C4<>(p_376975_, p_376597_, p_375517_, p_375767_);
    }

    public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> PropertyDispatch.C5<T1, T2, T3, T4, T5> properties(
        Property<T1> p_378288_, Property<T2> p_376698_, Property<T3> p_375794_, Property<T4> p_377627_, Property<T5> p_377745_
    ) {
        return new PropertyDispatch.C5<>(p_378288_, p_376698_, p_375794_, p_377627_, p_377745_);
    }

    @OnlyIn(Dist.CLIENT)
    public static class C1<T1 extends Comparable<T1>> extends PropertyDispatch {
        private final Property<T1> property1;

        C1(Property<T1> p_377319_) {
            this.property1 = p_377319_;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1);
        }

        public PropertyDispatch.C1<T1> select(T1 p_377138_, List<Variant> p_378173_) {
            Selector selector = Selector.of(this.property1.value(p_377138_));
            this.putValue(selector, p_378173_);
            return this;
        }

        public PropertyDispatch.C1<T1> select(T1 p_378325_, Variant p_378155_) {
            return this.select(p_378325_, Collections.singletonList(p_378155_));
        }

        public PropertyDispatch generate(Function<T1, Variant> p_376293_) {
            this.property1.getPossibleValues().forEach(p_376551_ -> this.select((T1)p_376551_, p_376293_.apply((T1)p_376551_)));
            return this;
        }

        public PropertyDispatch generateList(Function<T1, List<Variant>> p_375562_) {
            this.property1.getPossibleValues().forEach(p_376612_ -> this.select((T1)p_376612_, p_375562_.apply((T1)p_376612_)));
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class C2<T1 extends Comparable<T1>, T2 extends Comparable<T2>> extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;

        C2(Property<T1> p_377098_, Property<T2> p_375939_) {
            this.property1 = p_377098_;
            this.property2 = p_375939_;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2);
        }

        public PropertyDispatch.C2<T1, T2> select(T1 p_376122_, T2 p_377213_, List<Variant> p_377552_) {
            Selector selector = Selector.of(this.property1.value(p_376122_), this.property2.value(p_377213_));
            this.putValue(selector, p_377552_);
            return this;
        }

        public PropertyDispatch.C2<T1, T2> select(T1 p_375979_, T2 p_375490_, Variant p_376065_) {
            return this.select(p_375979_, p_375490_, Collections.singletonList(p_376065_));
        }

        public PropertyDispatch generate(BiFunction<T1, T2, Variant> p_376615_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_377154_ -> this.property2
                            .getPossibleValues()
                            .forEach(p_376123_ -> this.select((T1)p_377154_, (T2)p_376123_, p_376615_.apply((T1)p_377154_, (T2)p_376123_)))
                );
            return this;
        }

        public PropertyDispatch generateList(BiFunction<T1, T2, List<Variant>> p_378695_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_375491_ -> this.property2
                            .getPossibleValues()
                            .forEach(p_375416_ -> this.select((T1)p_375491_, (T2)p_375416_, p_378695_.apply((T1)p_375491_, (T2)p_375416_)))
                );
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class C3<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;

        C3(Property<T1> p_378639_, Property<T2> p_378424_, Property<T3> p_376367_) {
            this.property1 = p_378639_;
            this.property2 = p_378424_;
            this.property3 = p_376367_;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2, this.property3);
        }

        public PropertyDispatch.C3<T1, T2, T3> select(T1 p_376032_, T2 p_377076_, T3 p_378351_, List<Variant> p_378797_) {
            Selector selector = Selector.of(this.property1.value(p_376032_), this.property2.value(p_377076_), this.property3.value(p_378351_));
            this.putValue(selector, p_378797_);
            return this;
        }

        public PropertyDispatch.C3<T1, T2, T3> select(T1 p_375963_, T2 p_376963_, T3 p_376668_, Variant p_378537_) {
            return this.select(p_375963_, p_376963_, p_376668_, Collections.singletonList(p_378537_));
        }

        public PropertyDispatch generate(PropertyDispatch.TriFunction<T1, T2, T3, Variant> p_377597_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_377047_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_377231_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_378204_ -> this.select(
                                                    (T1)p_377047_,
                                                    (T2)p_377231_,
                                                    (T3)p_378204_,
                                                    p_377597_.apply((T1)p_377047_, (T2)p_377231_, (T3)p_378204_)
                                                )
                                        )
                            )
                );
            return this;
        }

        public PropertyDispatch generateList(PropertyDispatch.TriFunction<T1, T2, T3, List<Variant>> p_377011_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_378613_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_376416_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_377650_ -> this.select(
                                                    (T1)p_378613_,
                                                    (T2)p_376416_,
                                                    (T3)p_377650_,
                                                    p_377011_.apply((T1)p_378613_, (T2)p_376416_, (T3)p_377650_)
                                                )
                                        )
                            )
                );
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class C4<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;
        private final Property<T4> property4;

        C4(Property<T1> p_377852_, Property<T2> p_377209_, Property<T3> p_378386_, Property<T4> p_376113_) {
            this.property1 = p_377852_;
            this.property2 = p_377209_;
            this.property3 = p_378386_;
            this.property4 = p_376113_;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2, this.property3, this.property4);
        }

        public PropertyDispatch.C4<T1, T2, T3, T4> select(T1 p_375560_, T2 p_377739_, T3 p_375969_, T4 p_378016_, List<Variant> p_377859_) {
            Selector selector = Selector.of(
                this.property1.value(p_375560_), this.property2.value(p_377739_), this.property3.value(p_375969_), this.property4.value(p_378016_)
            );
            this.putValue(selector, p_377859_);
            return this;
        }

        public PropertyDispatch.C4<T1, T2, T3, T4> select(T1 p_378307_, T2 p_376465_, T3 p_377599_, T4 p_378302_, Variant p_377845_) {
            return this.select(p_378307_, p_376465_, p_377599_, p_378302_, Collections.singletonList(p_377845_));
        }

        public PropertyDispatch generate(PropertyDispatch.QuadFunction<T1, T2, T3, T4, Variant> p_377044_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_376254_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_375541_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_376281_ -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        p_378745_ -> this.select(
                                                                (T1)p_376254_,
                                                                (T2)p_375541_,
                                                                (T3)p_376281_,
                                                                (T4)p_378745_,
                                                                p_377044_.apply((T1)p_376254_, (T2)p_375541_, (T3)p_376281_, (T4)p_378745_)
                                                            )
                                                    )
                                        )
                            )
                );
            return this;
        }

        public PropertyDispatch generateList(PropertyDispatch.QuadFunction<T1, T2, T3, T4, List<Variant>> p_377997_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_375676_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_377581_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_377467_ -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        p_378381_ -> this.select(
                                                                (T1)p_375676_,
                                                                (T2)p_377581_,
                                                                (T3)p_377467_,
                                                                (T4)p_378381_,
                                                                p_377997_.apply((T1)p_375676_, (T2)p_377581_, (T3)p_377467_, (T4)p_378381_)
                                                            )
                                                    )
                                        )
                            )
                );
            return this;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class C5<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>>
        extends PropertyDispatch {
        private final Property<T1> property1;
        private final Property<T2> property2;
        private final Property<T3> property3;
        private final Property<T4> property4;
        private final Property<T5> property5;

        C5(Property<T1> p_375447_, Property<T2> p_377052_, Property<T3> p_378060_, Property<T4> p_376870_, Property<T5> p_375803_) {
            this.property1 = p_375447_;
            this.property2 = p_377052_;
            this.property3 = p_378060_;
            this.property4 = p_376870_;
            this.property5 = p_375803_;
        }

        @Override
        public List<Property<?>> getDefinedProperties() {
            return ImmutableList.of(this.property1, this.property2, this.property3, this.property4, this.property5);
        }

        public PropertyDispatch.C5<T1, T2, T3, T4, T5> select(T1 p_378643_, T2 p_377480_, T3 p_376302_, T4 p_375916_, T5 p_378810_, List<Variant> p_376554_) {
            Selector selector = Selector.of(
                this.property1.value(p_378643_),
                this.property2.value(p_377480_),
                this.property3.value(p_376302_),
                this.property4.value(p_375916_),
                this.property5.value(p_378810_)
            );
            this.putValue(selector, p_376554_);
            return this;
        }

        public PropertyDispatch.C5<T1, T2, T3, T4, T5> select(T1 p_376524_, T2 p_376144_, T3 p_375814_, T4 p_377177_, T5 p_377254_, Variant p_377784_) {
            return this.select(p_376524_, p_376144_, p_375814_, p_377177_, p_377254_, Collections.singletonList(p_377784_));
        }

        public PropertyDispatch generate(PropertyDispatch.PentaFunction<T1, T2, T3, T4, T5, Variant> p_378383_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_376257_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_378211_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_376810_ -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        p_378107_ -> this.property5
                                                                .getPossibleValues()
                                                                .forEach(
                                                                    p_375506_ -> this.select(
                                                                            (T1)p_376257_,
                                                                            (T2)p_378211_,
                                                                            (T3)p_376810_,
                                                                            (T4)p_378107_,
                                                                            (T5)p_375506_,
                                                                            p_378383_.apply(
                                                                                (T1)p_376257_, (T2)p_378211_, (T3)p_376810_, (T4)p_378107_, (T5)p_375506_
                                                                            )
                                                                        )
                                                                )
                                                    )
                                        )
                            )
                );
            return this;
        }

        public PropertyDispatch generateList(PropertyDispatch.PentaFunction<T1, T2, T3, T4, T5, List<Variant>> p_376321_) {
            this.property1
                .getPossibleValues()
                .forEach(
                    p_378354_ -> this.property2
                            .getPossibleValues()
                            .forEach(
                                p_377191_ -> this.property3
                                        .getPossibleValues()
                                        .forEach(
                                            p_376509_ -> this.property4
                                                    .getPossibleValues()
                                                    .forEach(
                                                        p_375940_ -> this.property5
                                                                .getPossibleValues()
                                                                .forEach(
                                                                    p_377883_ -> this.select(
                                                                            (T1)p_378354_,
                                                                            (T2)p_377191_,
                                                                            (T3)p_376509_,
                                                                            (T4)p_375940_,
                                                                            (T5)p_377883_,
                                                                            p_376321_.apply(
                                                                                (T1)p_378354_, (T2)p_377191_, (T3)p_376509_, (T4)p_375940_, (T5)p_377883_
                                                                            )
                                                                        )
                                                                )
                                                    )
                                        )
                            )
                );
            return this;
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface PentaFunction<P1, P2, P3, P4, P5, R> {
        R apply(P1 p_376349_, P2 p_378563_, P3 p_378655_, P4 p_378004_, P5 p_377691_);
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface QuadFunction<P1, P2, P3, P4, R> {
        R apply(P1 p_377975_, P2 p_375938_, P3 p_378667_, P4 p_378201_);
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface TriFunction<P1, P2, P3, R> {
        R apply(P1 p_377666_, P2 p_375642_, P3 p_377932_);
    }
}