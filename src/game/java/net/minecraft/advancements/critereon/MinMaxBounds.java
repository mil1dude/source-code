package net.minecraft.advancements.critereon;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

public interface MinMaxBounds<T extends Number> {
    SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(Component.translatable("argument.range.empty"));
    SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(Component.translatable("argument.range.swapped"));

    Optional<T> min();

    Optional<T> max();

    default boolean isAny() {
        return this.min().isEmpty() && this.max().isEmpty();
    }

    default Optional<T> unwrapPoint() {
        Optional<T> optional = this.min();
        Optional<T> optional1 = this.max();
        return optional.equals(optional1) ? optional : Optional.empty();
    }

    static <T extends Number, R extends MinMaxBounds<T>> Codec<R> createCodec(Codec<T> p_297837_, MinMaxBounds.BoundsFactory<T, R> p_298619_) {
        Codec<R> codec = RecordCodecBuilder.create(
            p_325235_ -> p_325235_.group(
                        p_297837_.optionalFieldOf("min").forGetter(MinMaxBounds::min),
                        p_297837_.optionalFieldOf("max").forGetter(MinMaxBounds::max)
                    )
                    .apply(p_325235_, p_298619_::create)
        );
        return Codec.either(codec, p_297837_)
            .xmap(
                p_299505_ -> p_299505_.map(p_300863_ -> (R)p_300863_, p_300712_ -> p_298619_.create(Optional.of((T)p_300712_), Optional.of((T)p_300712_))),
                p_300263_ -> {
                    Optional<T> optional = p_300263_.unwrapPoint();
                    return optional.isPresent() ? Either.right(optional.get()) : Either.left((R)p_300263_);
                }
            );
    }

    static <T extends Number, R extends MinMaxBounds<T>> R fromReader(
        StringReader p_55314_,
        MinMaxBounds.BoundsFromReaderFactory<T, R> p_55315_,
        Function<String, T> p_55316_,
        Supplier<DynamicCommandExceptionType> p_55317_,
        Function<T, T> p_55318_
    ) throws CommandSyntaxException {
        if (!p_55314_.canRead()) {
            throw ERROR_EMPTY.createWithContext(p_55314_);
        } else {
            int i = p_55314_.getCursor();

            try {
                Optional<T> optional = readNumber(p_55314_, p_55316_, p_55317_).map(p_55318_);
                Optional<T> optional1;
                if (p_55314_.canRead(2) && p_55314_.peek() == '.' && p_55314_.peek(1) == '.') {
                    p_55314_.skip();
                    p_55314_.skip();
                    optional1 = readNumber(p_55314_, p_55316_, p_55317_).map(p_55318_);
                    if (optional.isEmpty() && optional1.isEmpty()) {
                        throw ERROR_EMPTY.createWithContext(p_55314_);
                    }
                } else {
                    optional1 = optional;
                }

                if (optional.isEmpty() && optional1.isEmpty()) {
                    throw ERROR_EMPTY.createWithContext(p_55314_);
                } else {
                    return p_55315_.create(p_55314_, optional, optional1);
                }
            } catch (CommandSyntaxException commandsyntaxexception) {
                p_55314_.setCursor(i);
                throw new CommandSyntaxException(commandsyntaxexception.getType(), commandsyntaxexception.getRawMessage(), commandsyntaxexception.getInput(), i);
            }
        }
    }

    private static <T extends Number> Optional<T> readNumber(StringReader p_55320_, Function<String, T> p_55321_, Supplier<DynamicCommandExceptionType> p_55322_) throws CommandSyntaxException {
        int i = p_55320_.getCursor();

        while (p_55320_.canRead() && isAllowedInputChat(p_55320_)) {
            p_55320_.skip();
        }

        String s = p_55320_.getString().substring(i, p_55320_.getCursor());
        if (s.isEmpty()) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(p_55321_.apply(s));
            } catch (NumberFormatException numberformatexception) {
                throw p_55322_.get().createWithContext(p_55320_, s);
            }
        }
    }

    private static boolean isAllowedInputChat(StringReader p_55312_) {
        char c0 = p_55312_.peek();
        if ((c0 < '0' || c0 > '9') && c0 != '-') {
            return c0 != '.' ? false : !p_55312_.canRead(2) || p_55312_.peek(1) != '.';
        } else {
            return true;
        }
    }

    @FunctionalInterface
    public interface BoundsFactory<T extends Number, R extends MinMaxBounds<T>> {
        R create(Optional<T> p_300137_, Optional<T> p_298711_);
    }

    @FunctionalInterface
    public interface BoundsFromReaderFactory<T extends Number, R extends MinMaxBounds<T>> {
        R create(StringReader p_55333_, Optional<T> p_297501_, Optional<T> p_300423_) throws CommandSyntaxException;
    }

    public static record Doubles(Optional<Double> min, Optional<Double> max, Optional<Double> minSq, Optional<Double> maxSq)
        implements MinMaxBounds<Double> {
        public static final MinMaxBounds.Doubles ANY = new MinMaxBounds.Doubles(Optional.empty(), Optional.empty());
        public static final Codec<MinMaxBounds.Doubles> CODEC = MinMaxBounds.<Double, Doubles>createCodec(Codec.DOUBLE, MinMaxBounds.Doubles::new);

        private Doubles(Optional<Double> p_299492_, Optional<Double> p_300933_) {
            this(p_299492_, p_300933_, squareOpt(p_299492_), squareOpt(p_300933_));
        }

        private static MinMaxBounds.Doubles create(StringReader p_154796_, Optional<Double> p_299495_, Optional<Double> p_301206_) throws CommandSyntaxException {
            if (p_299495_.isPresent() && p_301206_.isPresent() && p_299495_.get() > p_301206_.get()) {
                throw ERROR_SWAPPED.createWithContext(p_154796_);
            } else {
                return new MinMaxBounds.Doubles(p_299495_, p_301206_);
            }
        }

        private static Optional<Double> squareOpt(Optional<Double> p_299805_) {
            return p_299805_.map(p_296138_ -> p_296138_ * p_296138_);
        }

        public static MinMaxBounds.Doubles exactly(double p_154787_) {
            return new MinMaxBounds.Doubles(Optional.of(p_154787_), Optional.of(p_154787_));
        }

        public static MinMaxBounds.Doubles between(double p_154789_, double p_154790_) {
            return new MinMaxBounds.Doubles(Optional.of(p_154789_), Optional.of(p_154790_));
        }

        public static MinMaxBounds.Doubles atLeast(double p_154805_) {
            return new MinMaxBounds.Doubles(Optional.of(p_154805_), Optional.empty());
        }

        public static MinMaxBounds.Doubles atMost(double p_154809_) {
            return new MinMaxBounds.Doubles(Optional.empty(), Optional.of(p_154809_));
        }

        public boolean matches(double p_154811_) {
            return this.min.isPresent() && this.min.get() > p_154811_ ? false : this.max.isEmpty() || !(this.max.get() < p_154811_);
        }

        public boolean matchesSqr(double p_154813_) {
            return this.minSq.isPresent() && this.minSq.get() > p_154813_ ? false : this.maxSq.isEmpty() || !(this.maxSq.get() < p_154813_);
        }

        public static MinMaxBounds.Doubles fromReader(StringReader p_154794_) throws CommandSyntaxException {
            return fromReader(p_154794_, p_154807_ -> p_154807_);
        }

        public static MinMaxBounds.Doubles fromReader(StringReader p_154800_, Function<Double, Double> p_154801_) throws CommandSyntaxException {
            return MinMaxBounds.fromReader(
                p_154800_, MinMaxBounds.Doubles::create, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble, p_154801_
            );
        }

        @Override
        public Optional<Double> min() {
            return this.min;
        }

        @Override
        public Optional<Double> max() {
            return this.max;
        }
    }

    public static record Ints(Optional<Integer> min, Optional<Integer> max, Optional<Long> minSq, Optional<Long> maxSq)
        implements MinMaxBounds<Integer> {
        public static final MinMaxBounds.Ints ANY = new MinMaxBounds.Ints(Optional.empty(), Optional.empty());
        public static final Codec<MinMaxBounds.Ints> CODEC = MinMaxBounds.<Integer, Ints>createCodec(Codec.INT, MinMaxBounds.Ints::new);

        private Ints(Optional<Integer> p_299979_, Optional<Integer> p_297344_) {
            this(p_299979_, p_297344_, p_299979_.map(p_296140_ -> p_296140_.longValue() * p_296140_.longValue()), squareOpt(p_297344_));
        }

        private static MinMaxBounds.Ints create(StringReader p_55378_, Optional<Integer> p_297316_, Optional<Integer> p_300359_) throws CommandSyntaxException {
            if (p_297316_.isPresent() && p_300359_.isPresent() && p_297316_.get() > p_300359_.get()) {
                throw ERROR_SWAPPED.createWithContext(p_55378_);
            } else {
                return new MinMaxBounds.Ints(p_297316_, p_300359_);
            }
        }

        private static Optional<Long> squareOpt(Optional<Integer> p_300285_) {
            return p_300285_.map(p_296139_ -> p_296139_.longValue() * p_296139_.longValue());
        }

        public static MinMaxBounds.Ints exactly(int p_55372_) {
            return new MinMaxBounds.Ints(Optional.of(p_55372_), Optional.of(p_55372_));
        }

        public static MinMaxBounds.Ints between(int p_154815_, int p_154816_) {
            return new MinMaxBounds.Ints(Optional.of(p_154815_), Optional.of(p_154816_));
        }

        public static MinMaxBounds.Ints atLeast(int p_55387_) {
            return new MinMaxBounds.Ints(Optional.of(p_55387_), Optional.empty());
        }

        public static MinMaxBounds.Ints atMost(int p_154820_) {
            return new MinMaxBounds.Ints(Optional.empty(), Optional.of(p_154820_));
        }

        public boolean matches(int p_55391_) {
            return this.min.isPresent() && this.min.get() > p_55391_ ? false : this.max.isEmpty() || this.max.get() >= p_55391_;
        }

        public boolean matchesSqr(long p_154818_) {
            return this.minSq.isPresent() && this.minSq.get() > p_154818_ ? false : this.maxSq.isEmpty() || this.maxSq.get() >= p_154818_;
        }

        public static MinMaxBounds.Ints fromReader(StringReader p_55376_) throws CommandSyntaxException {
            return fromReader(p_55376_, p_55389_ -> p_55389_);
        }

        public static MinMaxBounds.Ints fromReader(StringReader p_55382_, Function<Integer, Integer> p_55383_) throws CommandSyntaxException {
            return MinMaxBounds.fromReader(
                p_55382_, MinMaxBounds.Ints::create, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, p_55383_
            );
        }

        @Override
        public Optional<Integer> min() {
            return this.min;
        }

        @Override
        public Optional<Integer> max() {
            return this.max;
        }
    }
}
