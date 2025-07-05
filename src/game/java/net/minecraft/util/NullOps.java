package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractUniversalBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NullOps implements DynamicOps<Unit> {
    public static final NullOps INSTANCE = new NullOps();

    private NullOps() {
    }

    public <U> U convertTo(DynamicOps<U> p_335263_, Unit p_330577_) {
        return p_335263_.empty();
    }

    public Unit empty() {
        return Unit.INSTANCE;
    }

    public Unit emptyMap() {
        return Unit.INSTANCE;
    }

    public Unit emptyList() {
        return Unit.INSTANCE;
    }

    public Unit createNumeric(Number p_333368_) {
        return Unit.INSTANCE;
    }

    public Unit createByte(byte p_332993_) {
        return Unit.INSTANCE;
    }

    public Unit createShort(short p_327812_) {
        return Unit.INSTANCE;
    }

    public Unit createInt(int p_336243_) {
        return Unit.INSTANCE;
    }

    public Unit createLong(long p_332190_) {
        return Unit.INSTANCE;
    }

    public Unit createFloat(float p_328652_) {
        return Unit.INSTANCE;
    }

    public Unit createDouble(double p_329743_) {
        return Unit.INSTANCE;
    }

    public Unit createBoolean(boolean p_332728_) {
        return Unit.INSTANCE;
    }

    public Unit createString(String p_331594_) {
        return Unit.INSTANCE;
    }

    public DataResult<Number> getNumberValue(Unit p_331567_) {
        return DataResult.error(() -> "Not a number");
    }

    public DataResult<Boolean> getBooleanValue(Unit p_330383_) {
        return DataResult.error(() -> "Not a boolean");
    }

    public DataResult<String> getStringValue(Unit p_328159_) {
        return DataResult.error(() -> "Not a string");
    }

    public DataResult<Unit> mergeToList(Unit p_332194_, Unit p_331336_) {
        return DataResult.success(Unit.INSTANCE);
    }

    public DataResult<Unit> mergeToList(Unit p_330584_, List<Unit> p_335250_) {
        return DataResult.success(Unit.INSTANCE);
    }

    public DataResult<Unit> mergeToMap(Unit p_328865_, Unit p_336101_, Unit p_328794_) {
        return DataResult.success(Unit.INSTANCE);
    }

    public DataResult<Unit> mergeToMap(Unit p_332909_, Map<Unit, Unit> p_336158_) {
        return DataResult.success(Unit.INSTANCE);
    }

    public DataResult<Unit> mergeToMap(Unit p_332286_, MapLike<Unit> p_332604_) {
        return DataResult.success(Unit.INSTANCE);
    }

    public DataResult<Stream<Pair<Unit, Unit>>> getMapValues(Unit p_332179_) {
        return DataResult.error(() -> "Not a map");
    }

    public DataResult<Consumer<BiConsumer<Unit, Unit>>> getMapEntries(Unit p_328934_) {
        return DataResult.error(() -> "Not a map");
    }

    public DataResult<MapLike<Unit>> getMap(Unit p_335542_) {
        return DataResult.error(() -> "Not a map");
    }

    public DataResult<Stream<Unit>> getStream(Unit p_332123_) {
        return DataResult.error(() -> "Not a list");
    }

    public DataResult<Consumer<Consumer<Unit>>> getList(Unit p_333959_) {
        return DataResult.error(() -> "Not a list");
    }

    public DataResult<ByteBuffer> getByteBuffer(Unit p_334054_) {
        return DataResult.error(() -> "Not a byte list");
    }

    public DataResult<IntStream> getIntStream(Unit p_328303_) {
        return DataResult.error(() -> "Not an int list");
    }

    public DataResult<LongStream> getLongStream(Unit p_331380_) {
        return DataResult.error(() -> "Not a long list");
    }

    public Unit createMap(Stream<Pair<Unit, Unit>> p_334610_) {
        return Unit.INSTANCE;
    }

    public Unit createMap(Map<Unit, Unit> p_333052_) {
        return Unit.INSTANCE;
    }

    public Unit createList(Stream<Unit> p_335375_) {
        return Unit.INSTANCE;
    }

    public Unit createByteList(ByteBuffer p_333560_) {
        return Unit.INSTANCE;
    }

    public Unit createIntList(IntStream p_329926_) {
        return Unit.INSTANCE;
    }

    public Unit createLongList(LongStream p_333189_) {
        return Unit.INSTANCE;
    }

    public Unit remove(Unit p_333113_, String p_328025_) {
        return p_333113_;
    }

    @Override
    public RecordBuilder<Unit> mapBuilder() {
        return new NullOps.NullMapBuilder(this);
    }

    @Override
    public String toString() {
        return "Null";
    }

    static final class NullMapBuilder extends AbstractUniversalBuilder<Unit, Unit> {
        public NullMapBuilder(DynamicOps<Unit> p_334750_) {
            super(p_334750_);
        }

        protected Unit initBuilder() {
            return Unit.INSTANCE;
        }

        protected Unit append(Unit p_332704_, Unit p_328574_, Unit p_333872_) {
            return p_333872_;
        }

        protected DataResult<Unit> build(Unit p_327742_, Unit p_335216_) {
            return DataResult.success(p_335216_);
        }
    }
}