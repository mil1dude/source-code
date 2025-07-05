package net.minecraft.world.item.component;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

public final class CustomData {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CustomData EMPTY = new CustomData(new CompoundTag());
    private static final String TYPE_TAG = "id";
    public static final Codec<CustomData> CODEC = Codec.withAlternative(CompoundTag.CODEC, TagParser.AS_CODEC)
        .xmap(CustomData::new, p_327962_ -> p_327962_.tag);
    public static final Codec<CustomData> CODEC_WITH_ID = CODEC.validate(
        p_332921_ -> p_332921_.getUnsafe().contains("id", 8)
                ? DataResult.success(p_332921_)
                : DataResult.error(() -> "Missing id for entity in: " + p_332921_)
    );
    @Deprecated
    public static final StreamCodec<ByteBuf, CustomData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(CustomData::new, p_329964_ -> p_329964_.tag);
    private final CompoundTag tag;

    private CustomData(CompoundTag p_331981_) {
        this.tag = p_331981_;
    }

    public static CustomData of(CompoundTag p_334177_) {
        return new CustomData(p_334177_.copy());
    }

    public static Predicate<ItemStack> itemMatcher(DataComponentType<CustomData> p_329049_, CompoundTag p_330570_) {
        return p_334391_ -> {
            CustomData customdata = p_334391_.getOrDefault(p_329049_, EMPTY);
            return customdata.matchedBy(p_330570_);
        };
    }

    public boolean matchedBy(CompoundTag p_328523_) {
        return NbtUtils.compareNbt(p_328523_, this.tag, true);
    }

    public static void update(DataComponentType<CustomData> p_336008_, ItemStack p_335562_, Consumer<CompoundTag> p_332401_) {
        CustomData customdata = p_335562_.getOrDefault(p_336008_, EMPTY).update(p_332401_);
        if (customdata.tag.isEmpty()) {
            p_335562_.remove(p_336008_);
        } else {
            p_335562_.set(p_336008_, customdata);
        }
    }

    public static void set(DataComponentType<CustomData> p_327973_, ItemStack p_332195_, CompoundTag p_330130_) {
        if (!p_330130_.isEmpty()) {
            p_332195_.set(p_327973_, of(p_330130_));
        } else {
            p_332195_.remove(p_327973_);
        }
    }

    public CustomData update(Consumer<CompoundTag> p_336344_) {
        CompoundTag compoundtag = this.tag.copy();
        p_336344_.accept(compoundtag);
        return new CustomData(compoundtag);
    }

    @Nullable
    public ResourceLocation parseEntityId() {
        return !this.tag.contains("id", 8) ? null : ResourceLocation.tryParse(this.tag.getString("id"));
    }

    @Nullable
    public <T> T parseEntityType(HolderLookup.Provider p_377065_, ResourceKey<? extends Registry<T>> p_376865_) {
        ResourceLocation resourcelocation = this.parseEntityId();
        return resourcelocation == null
            ? null
            : p_377065_.lookup(p_376865_)
                .flatMap(p_375298_ -> p_375298_.get(ResourceKey.create(p_376865_, resourcelocation)))
                .map(Holder::value)
                .orElse(null);
    }

    public void loadInto(Entity p_328148_) {
        CompoundTag compoundtag = p_328148_.saveWithoutId(new CompoundTag());
        UUID uuid = p_328148_.getUUID();
        compoundtag.merge(this.tag);
        p_328148_.load(compoundtag);
        p_328148_.setUUID(uuid);
    }

    public boolean loadInto(BlockEntity p_335855_, HolderLookup.Provider p_331192_) {
        CompoundTag compoundtag = p_335855_.saveCustomOnly(p_331192_);
        CompoundTag compoundtag1 = compoundtag.copy();
        compoundtag.merge(this.tag);
        if (!compoundtag.equals(compoundtag1)) {
            try {
                p_335855_.loadCustomOnly(compoundtag, p_331192_);
                p_335855_.setChanged();
                return true;
            } catch (Exception exception1) {
                LOGGER.warn("Failed to apply custom data to block entity at {}", p_335855_.getBlockPos(), exception1);

                try {
                    p_335855_.loadCustomOnly(compoundtag1, p_331192_);
                } catch (Exception exception) {
                    LOGGER.warn("Failed to rollback block entity at {} after failure", p_335855_.getBlockPos(), exception);
                }
            }
        }

        return false;
    }

    public <T> DataResult<CustomData> update(DynamicOps<Tag> p_342271_, MapEncoder<T> p_328479_, T p_328689_) {
        return p_328479_.encode(p_328689_, p_342271_, p_342271_.mapBuilder()).build(this.tag).map(p_327948_ -> new CustomData((CompoundTag)p_327948_));
    }

    public <T> DataResult<T> read(MapDecoder<T> p_333786_) {
        return this.read(NbtOps.INSTANCE, p_333786_);
    }

    public <T> DataResult<T> read(DynamicOps<Tag> p_345359_, MapDecoder<T> p_342176_) {
        MapLike<Tag> maplike = p_345359_.getMap(this.tag).getOrThrow();
        return p_342176_.decode(p_345359_, maplike);
    }

    public int size() {
        return this.tag.size();
    }

    public boolean isEmpty() {
        return this.tag.isEmpty();
    }

    public CompoundTag copyTag() {
        return this.tag.copy();
    }

    public boolean contains(String p_331160_) {
        return this.tag.contains(p_331160_);
    }

    @Override
    public boolean equals(Object p_335284_) {
        if (p_335284_ == this) {
            return true;
        } else {
            return p_335284_ instanceof CustomData customdata ? this.tag.equals(customdata.tag) : false;
        }
    }

    @Override
    public int hashCode() {
        return this.tag.hashCode();
    }

    @Override
    public String toString() {
        return this.tag.toString();
    }

    @Deprecated
    public CompoundTag getUnsafe() {
        return this.tag;
    }
}