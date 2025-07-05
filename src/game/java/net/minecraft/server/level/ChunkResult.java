package net.minecraft.server.level;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public interface ChunkResult<T> {
    static <T> ChunkResult<T> of(T p_333970_) {
        return new ChunkResult.Success<>(p_333970_);
    }

    static <T> ChunkResult<T> error(String p_331314_) {
        return error(() -> p_331314_);
    }

    static <T> ChunkResult<T> error(Supplier<String> p_331628_) {
        return new ChunkResult.Fail<>(p_331628_);
    }

    boolean isSuccess();

    @Nullable
    T orElse(@Nullable T p_329164_);

    @Nullable
    static <R> R orElse(ChunkResult<? extends R> p_331028_, @Nullable R p_331551_) {
        R r = (R)p_331028_.orElse(null);
        return r != null ? r : p_331551_;
    }

    @Nullable
    String getError();

    ChunkResult<T> ifSuccess(Consumer<T> p_334389_);

    <R> ChunkResult<R> map(Function<T, R> p_334390_);

    <E extends Throwable> T orElseThrow(Supplier<E> p_330106_) throws E;

    public static record Fail<T>(Supplier<String> error) implements ChunkResult<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Nullable
        @Override
        public T orElse(@Nullable T p_330895_) {
            return p_330895_;
        }

        @Override
        public String getError() {
            return this.error.get();
        }

        @Override
        public ChunkResult<T> ifSuccess(Consumer<T> p_331855_) {
            return this;
        }

        @Override
        public <R> ChunkResult<R> map(Function<T, R> p_333275_) {
            return new ChunkResult.Fail(this.error);
        }

        @Override
        public <E extends Throwable> T orElseThrow(Supplier<E> p_331734_) throws E {
            throw p_331734_.get();
        }
    }

    public static record Success<T>(T value) implements ChunkResult<T> {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T orElse(@Nullable T p_332434_) {
            return this.value;
        }

        @Nullable
        @Override
        public String getError() {
            return null;
        }

        @Override
        public ChunkResult<T> ifSuccess(Consumer<T> p_328048_) {
            p_328048_.accept(this.value);
            return this;
        }

        @Override
        public <R> ChunkResult<R> map(Function<T, R> p_331436_) {
            return new ChunkResult.Success<>(p_331436_.apply(this.value));
        }

        @Override
        public <E extends Throwable> T orElseThrow(Supplier<E> p_335933_) throws E {
            return this.value;
        }
    }
}