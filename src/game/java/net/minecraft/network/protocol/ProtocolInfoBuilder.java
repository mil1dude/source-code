package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.codec.StreamCodec;

public class ProtocolInfoBuilder<T extends PacketListener, B extends ByteBuf> {
    final ConnectionProtocol protocol;
    final PacketFlow flow;
    private final List<ProtocolInfoBuilder.CodecEntry<T, ?, B>> codecs = new ArrayList<>();
    @Nullable
    private BundlerInfo bundlerInfo;

    public ProtocolInfoBuilder(ConnectionProtocol p_334175_, PacketFlow p_335651_) {
        this.protocol = p_334175_;
        this.flow = p_335651_;
    }

    public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B> addPacket(PacketType<P> p_335373_, StreamCodec<? super B, P> p_333531_) {
        this.codecs.add(new ProtocolInfoBuilder.CodecEntry<>(p_335373_, p_333531_));
        return this;
    }

    public <P extends BundlePacket<? super T>, D extends BundleDelimiterPacket<? super T>> ProtocolInfoBuilder<T, B> withBundlePacket(
        PacketType<P> p_336277_, Function<Iterable<Packet<? super T>>, P> p_331716_, D p_328432_
    ) {
        StreamCodec<ByteBuf, D> streamcodec = StreamCodec.unit(p_328432_);
        PacketType<D> packettype = (PacketType)p_328432_.type();
        this.codecs.add(new ProtocolInfoBuilder.CodecEntry<>(packettype, streamcodec));
        this.bundlerInfo = BundlerInfo.createForPacket(p_336277_, p_331716_, p_328432_);
        return this;
    }

    StreamCodec<ByteBuf, Packet<? super T>> buildPacketCodec(Function<ByteBuf, B> p_331741_, List<ProtocolInfoBuilder.CodecEntry<T, ?, B>> p_329135_) {
        ProtocolCodecBuilder<ByteBuf, T> protocolcodecbuilder = new ProtocolCodecBuilder<>(this.flow);

        for (ProtocolInfoBuilder.CodecEntry<T, ?, B> codecentry : p_329135_) {
            codecentry.addToBuilder(protocolcodecbuilder, p_331741_);
        }

        return protocolcodecbuilder.build();
    }

    public ProtocolInfo<T> build(Function<ByteBuf, B> p_336320_) {
        return new ProtocolInfoBuilder.Implementation<>(this.protocol, this.flow, this.buildPacketCodec(p_336320_, this.codecs), this.bundlerInfo);
    }

    public ProtocolInfo.Unbound<T, B> buildUnbound() {
        final List<ProtocolInfoBuilder.CodecEntry<T, ?, B>> list = List.copyOf(this.codecs);
        final BundlerInfo bundlerinfo = this.bundlerInfo;
        return new ProtocolInfo.Unbound<T, B>() {
            @Override
            public ProtocolInfo<T> bind(Function<ByteBuf, B> p_343642_) {
                return new ProtocolInfoBuilder.Implementation<>(
                    ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec(p_343642_, list), bundlerinfo
                );
            }

            @Override
            public ConnectionProtocol id() {
                return ProtocolInfoBuilder.this.protocol;
            }

            @Override
            public PacketFlow flow() {
                return ProtocolInfoBuilder.this.flow;
            }

            @Override
            public void listPackets(ProtocolInfo.Unbound.PacketVisitor p_343184_) {
                for (int i = 0; i < list.size(); i++) {
                    ProtocolInfoBuilder.CodecEntry<T, ?, B> codecentry = list.get(i);
                    p_343184_.accept(codecentry.type, i);
                }
            }
        };
    }

    private static <L extends PacketListener, B extends ByteBuf> ProtocolInfo.Unbound<L, B> protocol(
        ConnectionProtocol p_330235_, PacketFlow p_335045_, Consumer<ProtocolInfoBuilder<L, B>> p_329753_
    ) {
        ProtocolInfoBuilder<L, B> protocolinfobuilder = new ProtocolInfoBuilder<>(p_330235_, p_335045_);
        p_329753_.accept(protocolinfobuilder);
        return protocolinfobuilder.buildUnbound();
    }

    public static <T extends ServerboundPacketListener, B extends ByteBuf> ProtocolInfo.Unbound<T, B> serverboundProtocol(
        ConnectionProtocol p_331618_, Consumer<ProtocolInfoBuilder<T, B>> p_330318_
    ) {
        return protocol(p_331618_, PacketFlow.SERVERBOUND, p_330318_);
    }

    public static <T extends ClientboundPacketListener, B extends ByteBuf> ProtocolInfo.Unbound<T, B> clientboundProtocol(
        ConnectionProtocol p_329688_, Consumer<ProtocolInfoBuilder<T, B>> p_332900_
    ) {
        return protocol(p_329688_, PacketFlow.CLIENTBOUND, p_332900_);
    }

    static record CodecEntry<T extends PacketListener, P extends Packet<? super T>, B extends ByteBuf>(
        PacketType<P> type, StreamCodec<? super B, P> serializer
    ) {
        public void addToBuilder(ProtocolCodecBuilder<ByteBuf, T> p_328095_, Function<ByteBuf, B> p_333803_) {
            StreamCodec<ByteBuf, P> streamcodec = this.serializer.mapStream(p_333803_);
            p_328095_.add(this.type, streamcodec);
        }
    }

    static record Implementation<L extends PacketListener>(
        ConnectionProtocol id, PacketFlow flow, StreamCodec<ByteBuf, Packet<? super L>> codec, @Nullable BundlerInfo bundlerInfo
    ) implements ProtocolInfo<L> {
        @Nullable
        @Override
        public BundlerInfo bundlerInfo() {
            return this.bundlerInfo;
        }

        @Override
        public ConnectionProtocol id() {
            return this.id;
        }

        @Override
        public PacketFlow flow() {
            return this.flow;
        }

        @Override
        public StreamCodec<ByteBuf, Packet<? super L>> codec() {
            return this.codec;
        }
    }
}
