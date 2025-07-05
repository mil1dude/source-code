package net.minecraft.client.data.models.blockstates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Function;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VariantProperty<T> {
    final String key;
    final Function<T, JsonElement> serializer;

    public VariantProperty(String p_376086_, Function<T, JsonElement> p_378721_) {
        this.key = p_376086_;
        this.serializer = p_378721_;
    }

    public VariantProperty<T>.Value withValue(T p_377956_) {
        return new VariantProperty.Value(p_377956_);
    }

    @Override
    public String toString() {
        return this.key;
    }

    @OnlyIn(Dist.CLIENT)
    public class Value {
        private final T value;

        public Value(final T p_378727_) {
            this.value = p_378727_;
        }

        public VariantProperty<T> getKey() {
            return VariantProperty.this;
        }

        public void addToVariant(JsonObject p_375542_) {
            p_375542_.add(VariantProperty.this.key, VariantProperty.this.serializer.apply(this.value));
        }

        @Override
        public String toString() {
            return VariantProperty.this.key + "=" + this.value;
        }
    }
}