package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record PotDecorations(Optional<Item> back, Optional<Item> left, Optional<Item> right, Optional<Item> front) {
    public static final PotDecorations EMPTY = new PotDecorations(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    public static final Codec<PotDecorations> CODEC = BuiltInRegistries.ITEM
        .byNameCodec()
        .sizeLimitedListOf(4)
        .xmap(PotDecorations::new, PotDecorations::ordered);
    public static final StreamCodec<RegistryFriendlyByteBuf, PotDecorations> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM)
        .apply(ByteBufCodecs.list(4))
        .map(PotDecorations::new, PotDecorations::ordered);

    private PotDecorations(List<Item> p_331996_) {
        this(getItem(p_331996_, 0), getItem(p_331996_, 1), getItem(p_331996_, 2), getItem(p_331996_, 3));
    }

    public PotDecorations(Item p_335624_, Item p_333843_, Item p_334423_, Item p_332271_) {
        this(List.of(p_335624_, p_333843_, p_334423_, p_332271_));
    }

    private static Optional<Item> getItem(List<Item> p_329359_, int p_331055_) {
        if (p_331055_ >= p_329359_.size()) {
            return Optional.empty();
        } else {
            Item item = p_329359_.get(p_331055_);
            return item == Items.BRICK ? Optional.empty() : Optional.of(item);
        }
    }

    public CompoundTag save(CompoundTag p_332345_) {
        if (this.equals(EMPTY)) {
            return p_332345_;
        } else {
            p_332345_.put("sherds", CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow());
            return p_332345_;
        }
    }

    public List<Item> ordered() {
        return Stream.of(this.back, this.left, this.right, this.front).map(p_330456_ -> p_330456_.orElse(Items.BRICK)).toList();
    }

    public static PotDecorations load(@Nullable CompoundTag p_334784_) {
        return p_334784_ != null && p_334784_.contains("sherds")
            ? CODEC.parse(NbtOps.INSTANCE, p_334784_.get("sherds")).result().orElse(EMPTY)
            : EMPTY;
    }
}