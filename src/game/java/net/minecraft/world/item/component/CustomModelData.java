package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record CustomModelData(List<Float> floats, List<Boolean> flags, List<String> strings, List<Integer> colors) {
    public static final CustomModelData EMPTY = new CustomModelData(List.of(), List.of(), List.of(), List.of());
    public static final Codec<CustomModelData> CODEC = RecordCodecBuilder.create(
        p_378135_ -> p_378135_.group(
                    Codec.FLOAT.listOf().optionalFieldOf("floats", List.of()).forGetter(CustomModelData::floats),
                    Codec.BOOL.listOf().optionalFieldOf("flags", List.of()).forGetter(CustomModelData::flags),
                    Codec.STRING.listOf().optionalFieldOf("strings", List.of()).forGetter(CustomModelData::strings),
                    ExtraCodecs.RGB_COLOR_CODEC.listOf().optionalFieldOf("colors", List.of()).forGetter(CustomModelData::colors)
                )
                .apply(p_378135_, CustomModelData::new)
    );
    public static final StreamCodec<ByteBuf, CustomModelData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()),
        CustomModelData::floats,
        ByteBufCodecs.BOOL.apply(ByteBufCodecs.list()),
        CustomModelData::flags,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
        CustomModelData::strings,
        ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
        CustomModelData::colors,
        CustomModelData::new
    );

    @Nullable
    private static <T> T getSafe(List<T> p_378266_, int p_376966_) {
        return p_376966_ >= 0 && p_376966_ < p_378266_.size() ? p_378266_.get(p_376966_) : null;
    }

    @Nullable
    public Float getFloat(int p_378793_) {
        return getSafe(this.floats, p_378793_);
    }

    @Nullable
    public Boolean getBoolean(int p_378052_) {
        return getSafe(this.flags, p_378052_);
    }

    @Nullable
    public String getString(int p_378544_) {
        return getSafe(this.strings, p_378544_);
    }

    @Nullable
    public Integer getColor(int p_376081_) {
        return getSafe(this.colors, p_376081_);
    }
}