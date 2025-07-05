package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;

public record SoundEvent(ResourceLocation location, Optional<Float> fixedRange) {
    public static final Codec<SoundEvent> DIRECT_CODEC = RecordCodecBuilder.create(
        p_326482_ -> p_326482_.group(
                    ResourceLocation.CODEC.fieldOf("sound_id").forGetter(SoundEvent::location),
                    Codec.FLOAT.lenientOptionalFieldOf("range").forGetter(SoundEvent::fixedRange)
                )
                .apply(p_326482_, SoundEvent::create)
    );
    public static final Codec<Holder<SoundEvent>> CODEC = RegistryFileCodec.create(Registries.SOUND_EVENT, DIRECT_CODEC);
    public static final StreamCodec<ByteBuf, SoundEvent> DIRECT_STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        SoundEvent::location,
        ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional),
        SoundEvent::fixedRange,
        SoundEvent::create
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SoundEvent>> STREAM_CODEC = ByteBufCodecs.holder(Registries.SOUND_EVENT, DIRECT_STREAM_CODEC);

    private static SoundEvent create(ResourceLocation p_263406_, Optional<Float> p_263346_) {
        return p_263346_.<SoundEvent>map(p_263360_ -> createFixedRangeEvent(p_263406_, p_263360_)).orElseGet(() -> createVariableRangeEvent(p_263406_));
    }

    public static SoundEvent createVariableRangeEvent(ResourceLocation p_262973_) {
        return new SoundEvent(p_262973_, Optional.empty());
    }

    public static SoundEvent createFixedRangeEvent(ResourceLocation p_263003_, float p_263029_) {
        return new SoundEvent(p_263003_, Optional.of(p_263029_));
    }

    public float getRange(float p_215669_) {
        return this.fixedRange.orElse(p_215669_ > 1.0F ? 16.0F * p_215669_ : 16.0F);
    }
}