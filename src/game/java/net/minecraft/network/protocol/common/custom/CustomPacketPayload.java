package net.minecraft.network.protocol.common.custom;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.resources.ResourceLocation;

public interface CustomPacketPayload {
    CustomPacketPayload.Type<? extends CustomPacketPayload> type();

    static <B extends ByteBuf, T extends CustomPacketPayload> StreamCodec<B, T> codec(StreamMemberEncoder<B, T> p_336135_, StreamDecoder<B, T> p_335771_) {
        return StreamCodec.ofMember(p_336135_, p_335771_);
    }

    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String p_331650_) {
        return new CustomPacketPayload.Type<>(ResourceLocation.withDefaultNamespace(p_331650_));
    }

    static <B extends FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> codec(
        final CustomPacketPayload.FallbackProvider<B> p_329573_, List<CustomPacketPayload.TypeAndCodec<? super B, ?>> p_333081_
    ) {
        final Map<ResourceLocation, StreamCodec<? super B, ? extends CustomPacketPayload>> map = p_333081_.stream()
            .collect(Collectors.toUnmodifiableMap(p_332174_ -> p_332174_.type().id(), CustomPacketPayload.TypeAndCodec::codec));
        return new StreamCodec<B, CustomPacketPayload>() {
            private StreamCodec<? super B, ? extends CustomPacketPayload> findCodec(ResourceLocation p_335824_) {
                StreamCodec<? super B, ? extends CustomPacketPayload> streamcodec = map.get(p_335824_);
                return streamcodec != null ? streamcodec : p_329573_.create(p_335824_);
            }

            private <T extends CustomPacketPayload> void writeCap(B p_332252_, CustomPacketPayload.Type<T> p_334465_, CustomPacketPayload p_334290_) {
                p_332252_.writeResourceLocation(p_334465_.id());
                StreamCodec<B, T> streamcodec = (StreamCodec)this.findCodec(p_334465_.id);
                streamcodec.encode(p_332252_, (T)p_334290_);
            }

            public void encode(B p_334992_, CustomPacketPayload p_329854_) {
                this.writeCap(p_334992_, p_329854_.type(), p_329854_);
            }

            public CustomPacketPayload decode(B p_334320_) {
                ResourceLocation resourcelocation = p_334320_.readResourceLocation();
                return (CustomPacketPayload)this.findCodec(resourcelocation).decode(p_334320_);
            }
        };
    }

    public interface FallbackProvider<B extends FriendlyByteBuf> {
        StreamCodec<B, ? extends CustomPacketPayload> create(ResourceLocation p_336163_);
    }

    public static record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }

    public static record TypeAndCodec<B extends FriendlyByteBuf, T extends CustomPacketPayload>(
        CustomPacketPayload.Type<T> type, StreamCodec<B, T> codec
    ) {
    }
}
