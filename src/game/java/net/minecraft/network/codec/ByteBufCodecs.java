package net.minecraft.network.codec;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.VarLong;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface ByteBufCodecs {
    int MAX_INITIAL_COLLECTION_SIZE = 65536;
    StreamCodec<ByteBuf, Boolean> BOOL = new StreamCodec<ByteBuf, Boolean>() {
        public Boolean decode(ByteBuf p_332480_) {
            return p_332480_.readBoolean();
        }

        public void encode(ByteBuf p_332710_, Boolean p_330535_) {
            p_332710_.writeBoolean(p_330535_);
        }
    };
    StreamCodec<ByteBuf, Byte> BYTE = new StreamCodec<ByteBuf, Byte>() {
        public Byte decode(ByteBuf p_332150_) {
            return p_332150_.readByte();
        }

        public void encode(ByteBuf p_328538_, Byte p_327835_) {
            p_328538_.writeByte(p_327835_);
        }
    };
    StreamCodec<ByteBuf, Float> ROTATION_BYTE = BYTE.map(Mth::unpackDegrees, Mth::packDegrees);
    StreamCodec<ByteBuf, Short> SHORT = new StreamCodec<ByteBuf, Short>() {
        public Short decode(ByteBuf p_331682_) {
            return p_331682_.readShort();
        }

        public void encode(ByteBuf p_329734_, Short p_332862_) {
            p_329734_.writeShort(p_332862_);
        }
    };
    StreamCodec<ByteBuf, Integer> UNSIGNED_SHORT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_333416_) {
            return p_333416_.readUnsignedShort();
        }

        public void encode(ByteBuf p_334768_, Integer p_335195_) {
            p_334768_.writeShort(p_335195_);
        }
    };
    StreamCodec<ByteBuf, Integer> INT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_334363_) {
            return p_334363_.readInt();
        }

        public void encode(ByteBuf p_328174_, Integer p_329350_) {
            p_328174_.writeInt(p_329350_);
        }
    };
    StreamCodec<ByteBuf, Integer> VAR_INT = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_334861_) {
            return VarInt.read(p_334861_);
        }

        public void encode(ByteBuf p_333121_, Integer p_329976_) {
            VarInt.write(p_333121_, p_329976_);
        }
    };
    StreamCodec<ByteBuf, OptionalInt> OPTIONAL_VAR_INT = VAR_INT.map(
        p_358482_ -> p_358482_ == 0 ? OptionalInt.empty() : OptionalInt.of(p_358482_ - 1), p_358481_ -> p_358481_.isPresent() ? p_358481_.getAsInt() + 1 : 0
    );
    StreamCodec<ByteBuf, Long> LONG = new StreamCodec<ByteBuf, Long>() {
        public Long decode(ByteBuf p_330259_) {
            return p_330259_.readLong();
        }

        public void encode(ByteBuf p_332625_, Long p_327681_) {
            p_332625_.writeLong(p_327681_);
        }
    };
    StreamCodec<ByteBuf, Long> VAR_LONG = new StreamCodec<ByteBuf, Long>() {
        public Long decode(ByteBuf p_335511_) {
            return VarLong.read(p_335511_);
        }

        public void encode(ByteBuf p_331177_, Long p_364567_) {
            VarLong.write(p_331177_, p_364567_);
        }
    };
    StreamCodec<ByteBuf, Float> FLOAT = new StreamCodec<ByteBuf, Float>() {
        public Float decode(ByteBuf p_330378_) {
            return p_330378_.readFloat();
        }

        public void encode(ByteBuf p_329698_, Float p_365105_) {
            p_329698_.writeFloat(p_365105_);
        }
    };
    StreamCodec<ByteBuf, Double> DOUBLE = new StreamCodec<ByteBuf, Double>() {
        public Double decode(ByteBuf p_331124_) {
            return p_331124_.readDouble();
        }

        public void encode(ByteBuf p_327898_, Double p_363039_) {
            p_327898_.writeDouble(p_363039_);
        }
    };
    StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = new StreamCodec<ByteBuf, byte[]>() {
        public byte[] decode(ByteBuf p_333794_) {
            return FriendlyByteBuf.readByteArray(p_333794_);
        }

        public void encode(ByteBuf p_327981_, byte[] p_368580_) {
            FriendlyByteBuf.writeByteArray(p_327981_, p_368580_);
        }
    };
    StreamCodec<ByteBuf, String> STRING_UTF8 = stringUtf8(32767);
    StreamCodec<ByteBuf, Tag> TAG = tagCodec(() -> NbtAccounter.create(2097152L));
    StreamCodec<ByteBuf, Tag> TRUSTED_TAG = tagCodec(NbtAccounter::unlimitedHeap);
    StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = compoundTagCodec(() -> NbtAccounter.create(2097152L));
    StreamCodec<ByteBuf, CompoundTag> TRUSTED_COMPOUND_TAG = compoundTagCodec(NbtAccounter::unlimitedHeap);
    StreamCodec<ByteBuf, Optional<CompoundTag>> OPTIONAL_COMPOUND_TAG = new StreamCodec<ByteBuf, Optional<CompoundTag>>() {
        public Optional<CompoundTag> decode(ByteBuf p_334787_) {
            return Optional.ofNullable(FriendlyByteBuf.readNbt(p_334787_));
        }

        public void encode(ByteBuf p_332400_, Optional<CompoundTag> p_367662_) {
            FriendlyByteBuf.writeNbt(p_332400_, p_367662_.orElse(null));
        }
    };
    StreamCodec<ByteBuf, Vector3f> VECTOR3F = new StreamCodec<ByteBuf, Vector3f>() {
        public Vector3f decode(ByteBuf p_328716_) {
            return FriendlyByteBuf.readVector3f(p_328716_);
        }

        public void encode(ByteBuf p_327986_, Vector3f p_366037_) {
            FriendlyByteBuf.writeVector3f(p_327986_, p_366037_);
        }
    };
    StreamCodec<ByteBuf, Quaternionf> QUATERNIONF = new StreamCodec<ByteBuf, Quaternionf>() {
        public Quaternionf decode(ByteBuf p_335035_) {
            return FriendlyByteBuf.readQuaternion(p_335035_);
        }

        public void encode(ByteBuf p_328446_, Quaternionf p_361299_) {
            FriendlyByteBuf.writeQuaternion(p_328446_, p_361299_);
        }
    };
    StreamCodec<ByteBuf, Integer> CONTAINER_ID = new StreamCodec<ByteBuf, Integer>() {
        public Integer decode(ByteBuf p_331156_) {
            return FriendlyByteBuf.readContainerId(p_331156_);
        }

        public void encode(ByteBuf p_328803_, Integer p_361117_) {
            FriendlyByteBuf.writeContainerId(p_328803_, p_361117_);
        }
    };
    StreamCodec<ByteBuf, PropertyMap> GAME_PROFILE_PROPERTIES = new StreamCodec<ByteBuf, PropertyMap>() {
        private static final int MAX_PROPERTY_NAME_LENGTH = 64;
        private static final int MAX_PROPERTY_VALUE_LENGTH = 32767;
        private static final int MAX_PROPERTY_SIGNATURE_LENGTH = 1024;
        private static final int MAX_PROPERTIES = 16;

        public PropertyMap decode(ByteBuf p_366045_) {
            int i = ByteBufCodecs.readCount(p_366045_, 16);
            PropertyMap propertymap = new PropertyMap();

            for (int j = 0; j < i; j++) {
                String s = Utf8String.read(p_366045_, 64);
                String s1 = Utf8String.read(p_366045_, 32767);
                String s2 = FriendlyByteBuf.readNullable(p_366045_, p_365596_ -> Utf8String.read(p_365596_, 1024));
                Property property = new Property(s, s1, s2);
                propertymap.put(property.name(), property);
            }

            return propertymap;
        }

        public void encode(ByteBuf p_366012_, PropertyMap p_361810_) {
            ByteBufCodecs.writeCount(p_366012_, p_361810_.size(), 16);

            for (Property property : p_361810_.values()) {
                Utf8String.write(p_366012_, property.name(), 64);
                Utf8String.write(p_366012_, property.value(), 32767);
                FriendlyByteBuf.writeNullable(p_366012_, property.signature(), (p_362674_, p_360862_) -> Utf8String.write(p_362674_, p_360862_, 1024));
            }
        }
    };
    StreamCodec<ByteBuf, GameProfile> GAME_PROFILE = new StreamCodec<ByteBuf, GameProfile>() {
        public GameProfile decode(ByteBuf p_363537_) {
            UUID uuid = UUIDUtil.STREAM_CODEC.decode(p_363537_);
            String s = Utf8String.read(p_363537_, 16);
            GameProfile gameprofile = new GameProfile(uuid, s);
            gameprofile.getProperties().putAll(ByteBufCodecs.GAME_PROFILE_PROPERTIES.decode(p_363537_));
            return gameprofile;
        }

        public void encode(ByteBuf p_367459_, GameProfile p_368055_) {
            UUIDUtil.STREAM_CODEC.encode(p_367459_, p_368055_.getId());
            Utf8String.write(p_367459_, p_368055_.getName(), 16);
            ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode(p_367459_, p_368055_.getProperties());
        }
    };

    static StreamCodec<ByteBuf, byte[]> byteArray(final int p_329369_) {
        return new StreamCodec<ByteBuf, byte[]>() {
            public byte[] decode(ByteBuf p_330658_) {
                return FriendlyByteBuf.readByteArray(p_330658_, p_329369_);
            }

            public void encode(ByteBuf p_332407_, byte[] p_327934_) {
                if (p_327934_.length > p_329369_) {
                    throw new EncoderException("ByteArray with size " + p_327934_.length + " is bigger than allowed " + p_329369_);
                } else {
                    FriendlyByteBuf.writeByteArray(p_332407_, p_327934_);
                }
            }
        };
    }

    static StreamCodec<ByteBuf, String> stringUtf8(final int p_332577_) {
        return new StreamCodec<ByteBuf, String>() {
            public String decode(ByteBuf p_329846_) {
                return Utf8String.read(p_329846_, p_332577_);
            }

            public void encode(ByteBuf p_336297_, String p_362618_) {
                Utf8String.write(p_336297_, p_362618_, p_332577_);
            }
        };
    }

    static StreamCodec<ByteBuf, Tag> tagCodec(final Supplier<NbtAccounter> p_334674_) {
        return new StreamCodec<ByteBuf, Tag>() {
            public Tag decode(ByteBuf p_363937_) {
                Tag tag = FriendlyByteBuf.readNbt(p_363937_, p_334674_.get());
                if (tag == null) {
                    throw new DecoderException("Expected non-null compound tag");
                } else {
                    return tag;
                }
            }

            public void encode(ByteBuf p_367629_, Tag p_364359_) {
                if (p_364359_ == EndTag.INSTANCE) {
                    throw new EncoderException("Expected non-null compound tag");
                } else {
                    FriendlyByteBuf.writeNbt(p_367629_, p_364359_);
                }
            }
        };
    }

    static StreamCodec<ByteBuf, CompoundTag> compoundTagCodec(Supplier<NbtAccounter> p_334293_) {
        return tagCodec(p_334293_).map(p_329005_ -> {
            if (p_329005_ instanceof CompoundTag) {
                return (CompoundTag)p_329005_;
            } else {
                throw new DecoderException("Not a compound tag: " + p_329005_);
            }
        }, p_331817_ -> (Tag)p_331817_);
    }

    static <T> StreamCodec<ByteBuf, T> fromCodecTrusted(Codec<T> p_332454_) {
        return fromCodec(p_332454_, NbtAccounter::unlimitedHeap);
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> p_330766_) {
        return fromCodec(p_330766_, () -> NbtAccounter.create(2097152L));
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> p_332152_, Supplier<NbtAccounter> p_333221_) {
        return tagCodec(p_333221_)
            .map(
                p_328837_ -> p_332152_.parse(NbtOps.INSTANCE, p_328837_)
                        .getOrThrow(p_328190_ -> new DecoderException("Failed to decode: " + p_328190_ + " " + p_328837_)),
                p_329084_ -> p_332152_.encodeStart(NbtOps.INSTANCE, (T)p_329084_)
                        .getOrThrow(p_332410_ -> new EncoderException("Failed to encode: " + p_332410_ + " " + p_329084_))
            );
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistriesTrusted(Codec<T> p_331690_) {
        return fromCodecWithRegistries(p_331690_, NbtAccounter::unlimitedHeap);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(Codec<T> p_334037_) {
        return fromCodecWithRegistries(p_334037_, () -> NbtAccounter.create(2097152L));
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> fromCodecWithRegistries(final Codec<T> p_332747_, Supplier<NbtAccounter> p_329046_) {
        final StreamCodec<ByteBuf, Tag> streamcodec = tagCodec(p_329046_);
        return new StreamCodec<RegistryFriendlyByteBuf, T>() {
            public T decode(RegistryFriendlyByteBuf p_369597_) {
                Tag tag = streamcodec.decode(p_369597_);
                RegistryOps<Tag> registryops = p_369597_.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                return p_332747_.parse(registryops, tag).getOrThrow(p_363482_ -> new DecoderException("Failed to decode: " + p_363482_ + " " + tag));
            }

            public void encode(RegistryFriendlyByteBuf p_363183_, T p_368484_) {
                RegistryOps<Tag> registryops = p_363183_.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                Tag tag = p_332747_.encodeStart(registryops, p_368484_)
                    .getOrThrow(p_368533_ -> new EncoderException("Failed to encode: " + p_368533_ + " " + p_368484_));
                streamcodec.encode(p_363183_, tag);
            }
        };
    }

    static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(final StreamCodec<B, V> p_333614_) {
        return new StreamCodec<B, Optional<V>>() {
            public Optional<V> decode(B p_329844_) {
                return p_329844_.readBoolean() ? Optional.of(p_333614_.decode(p_329844_)) : Optional.empty();
            }

            public void encode(B p_335209_, Optional<V> p_367754_) {
                if (p_367754_.isPresent()) {
                    p_335209_.writeBoolean(true);
                    p_333614_.encode(p_335209_, p_367754_.get());
                } else {
                    p_335209_.writeBoolean(false);
                }
            }
        };
    }

    static int readCount(ByteBuf p_335948_, int p_329745_) {
        int i = VarInt.read(p_335948_);
        if (i > p_329745_) {
            throw new DecoderException(i + " elements exceeded max size of: " + p_329745_);
        } else {
            return i;
        }
    }

    static void writeCount(ByteBuf p_332743_, int p_332779_, int p_330804_) {
        if (p_332779_ > p_330804_) {
            throw new EncoderException(p_332779_ + " elements exceeded max size of: " + p_330804_);
        } else {
            VarInt.write(p_332743_, p_332779_);
        }
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(IntFunction<C> p_329603_, StreamCodec<? super B, V> p_335274_) {
        return collection(p_329603_, p_335274_, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(
        final IntFunction<C> p_330282_, final StreamCodec<? super B, V> p_329504_, final int p_331395_
    ) {
        return new StreamCodec<B, C>() {
            public C decode(B p_336330_) {
                int i = ByteBufCodecs.readCount(p_336330_, p_331395_);
                C c = p_330282_.apply(Math.min(i, 65536));

                for (int j = 0; j < i; j++) {
                    c.add(p_329504_.decode(p_336330_));
                }

                return c;
            }

            public void encode(B p_329166_, C p_361448_) {
                ByteBufCodecs.writeCount(p_329166_, p_361448_.size(), p_331395_);

                for (V v : p_361448_) {
                    p_329504_.encode(p_329166_, v);
                }
            }
        };
    }

    static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> p_333333_) {
        return p_331526_ -> collection(p_333333_, p_331526_);
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
        return p_331787_ -> collection(ArrayList::new, p_331787_);
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int p_331728_) {
        return p_328420_ -> collection(ArrayList::new, p_328420_, p_331728_);
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
        IntFunction<? extends M> p_329613_, StreamCodec<? super B, K> p_335749_, StreamCodec<? super B, V> p_332695_
    ) {
        return map(p_329613_, p_335749_, p_332695_, Integer.MAX_VALUE);
    }

    static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
        final IntFunction<? extends M> p_331225_, final StreamCodec<? super B, K> p_334555_, final StreamCodec<? super B, V> p_330391_, final int p_331122_
    ) {
        return new StreamCodec<B, M>() {
            public void encode(B p_335266_, M p_367308_) {
                ByteBufCodecs.writeCount(p_335266_, p_367308_.size(), p_331122_);
                p_367308_.forEach((p_365129_, p_361515_) -> {
                    p_334555_.encode(p_335266_, (K)p_365129_);
                    p_330391_.encode(p_335266_, (V)p_361515_);
                });
            }

            public M decode(B p_328010_) {
                int i = ByteBufCodecs.readCount(p_328010_, p_331122_);
                M m = (M)p_331225_.apply(Math.min(i, 65536));

                for (int j = 0; j < i; j++) {
                    K k = p_334555_.decode(p_328010_);
                    V v = p_330391_.decode(p_328010_);
                    m.put(k, v);
                }

                return m;
            }
        };
    }

    static <B extends ByteBuf, L, R> StreamCodec<B, Either<L, R>> either(
        final StreamCodec<? super B, L> p_330563_, final StreamCodec<? super B, R> p_328664_
    ) {
        return new StreamCodec<B, Either<L, R>>() {
            public Either<L, R> decode(B p_365796_) {
                return p_365796_.readBoolean() ? Either.left(p_330563_.decode(p_365796_)) : Either.right(p_328664_.decode(p_365796_));
            }

            public void encode(B p_362090_, Either<L, R> p_366716_) {
                p_366716_.ifLeft(p_367829_ -> {
                    p_362090_.writeBoolean(true);
                    p_330563_.encode(p_362090_, (L)p_367829_);
                }).ifRight(p_362222_ -> {
                    p_362090_.writeBoolean(false);
                    p_328664_.encode(p_362090_, (R)p_362222_);
                });
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> idMapper(final IntFunction<T> p_333433_, final ToIntFunction<T> p_334959_) {
        return new StreamCodec<ByteBuf, T>() {
            public T decode(ByteBuf p_368316_) {
                int i = VarInt.read(p_368316_);
                return p_333433_.apply(i);
            }

            public void encode(ByteBuf p_361972_, T p_363066_) {
                int i = p_334959_.applyAsInt(p_363066_);
                VarInt.write(p_361972_, i);
            }
        };
    }

    static <T> StreamCodec<ByteBuf, T> idMapper(IdMap<T> p_332036_) {
        return idMapper(p_332036_::byIdOrThrow, p_332036_::getIdOrThrow);
    }

    private static <T, R> StreamCodec<RegistryFriendlyByteBuf, R> registry(
        final ResourceKey<? extends Registry<T>> p_332046_, final Function<Registry<T>, IdMap<R>> p_332827_
    ) {
        return new StreamCodec<RegistryFriendlyByteBuf, R>() {
            private IdMap<R> getRegistryOrThrow(RegistryFriendlyByteBuf p_366778_) {
                return p_332827_.apply(p_366778_.registryAccess().lookupOrThrow(p_332046_));
            }

            public R decode(RegistryFriendlyByteBuf p_327957_) {
                int i = VarInt.read(p_327957_);
                return (R)this.getRegistryOrThrow(p_327957_).byIdOrThrow(i);
            }

            public void encode(RegistryFriendlyByteBuf p_327905_, R p_362272_) {
                int i = this.getRegistryOrThrow(p_327905_).getIdOrThrow(p_362272_);
                VarInt.write(p_327905_, i);
            }
        };
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, T> registry(ResourceKey<? extends Registry<T>> p_332712_) {
        return registry(p_332712_, p_335792_ -> p_335792_);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderRegistry(ResourceKey<? extends Registry<T>> p_332639_) {
        return registry(p_332639_, Registry::asHolderIdMap);
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holder(
        final ResourceKey<? extends Registry<T>> p_335347_, final StreamCodec<? super RegistryFriendlyByteBuf, T> p_329304_
    ) {
        return new StreamCodec<RegistryFriendlyByteBuf, Holder<T>>() {
            private static final int DIRECT_HOLDER_ID = 0;

            private IdMap<Holder<T>> getRegistryOrThrow(RegistryFriendlyByteBuf p_361818_) {
                return p_361818_.registryAccess().lookupOrThrow(p_335347_).asHolderIdMap();
            }

            public Holder<T> decode(RegistryFriendlyByteBuf p_361169_) {
                int i = VarInt.read(p_361169_);
                return i == 0 ? Holder.direct(p_329304_.decode(p_361169_)) : (Holder)this.getRegistryOrThrow(p_361169_).byIdOrThrow(i - 1);
            }

            public void encode(RegistryFriendlyByteBuf p_361045_, Holder<T> p_363715_) {
                switch (p_363715_.kind()) {
                    case REFERENCE:
                        int i = this.getRegistryOrThrow(p_361045_).getIdOrThrow(p_363715_);
                        VarInt.write(p_361045_, i + 1);
                        break;
                    case DIRECT:
                        VarInt.write(p_361045_, 0);
                        p_329304_.encode(p_361045_, p_363715_.value());
                }
            }
        };
    }

    static <T> StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>> holderSet(final ResourceKey<? extends Registry<T>> p_328506_) {
        return new StreamCodec<RegistryFriendlyByteBuf, HolderSet<T>>() {
            private static final int NAMED_SET = -1;
            private final StreamCodec<RegistryFriendlyByteBuf, Holder<T>> holderCodec = ByteBufCodecs.holderRegistry(p_328506_);

            public HolderSet<T> decode(RegistryFriendlyByteBuf p_362854_) {
                int i = VarInt.read(p_362854_) - 1;
                if (i == -1) {
                    Registry<T> registry = p_362854_.registryAccess().lookupOrThrow(p_328506_);
                    return registry.get(TagKey.create(p_328506_, ResourceLocation.STREAM_CODEC.decode(p_362854_))).orElseThrow();
                } else {
                    List<Holder<T>> list = new ArrayList<>(Math.min(i, 65536));

                    for (int j = 0; j < i; j++) {
                        list.add(this.holderCodec.decode(p_362854_));
                    }

                    return HolderSet.direct(list);
                }
            }

            public void encode(RegistryFriendlyByteBuf p_362273_, HolderSet<T> p_366520_) {
                Optional<TagKey<T>> optional = p_366520_.unwrapKey();
                if (optional.isPresent()) {
                    VarInt.write(p_362273_, 0);
                    ResourceLocation.STREAM_CODEC.encode(p_362273_, optional.get().location());
                } else {
                    VarInt.write(p_362273_, p_366520_.size() + 1);

                    for (Holder<T> holder : p_366520_) {
                        this.holderCodec.encode(p_362273_, holder);
                    }
                }
            }
        };
    }
}