package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemCost(Holder<Item> item, int count, DataComponentPredicate components, ItemStack itemStack) {
    public static final Codec<ItemCost> CODEC = RecordCodecBuilder.create(
        p_359936_ -> p_359936_.group(
                    Item.CODEC.fieldOf("id").forGetter(ItemCost::item),
                    ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemCost::count),
                    DataComponentPredicate.CODEC.optionalFieldOf("components", DataComponentPredicate.EMPTY).forGetter(ItemCost::components)
                )
                .apply(p_359936_, ItemCost::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCost> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderRegistry(Registries.ITEM),
        ItemCost::item,
        ByteBufCodecs.VAR_INT,
        ItemCost::count,
        DataComponentPredicate.STREAM_CODEC,
        ItemCost::components,
        ItemCost::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemCost>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs::optional);

    public ItemCost(ItemLike p_333321_) {
        this(p_333321_, 1);
    }

    public ItemCost(ItemLike p_332783_, int p_331715_) {
        this(p_332783_.asItem().builtInRegistryHolder(), p_331715_, DataComponentPredicate.EMPTY);
    }

    public ItemCost(Holder<Item> p_331233_, int p_334492_, DataComponentPredicate p_330788_) {
        this(p_331233_, p_334492_, p_330788_, createStack(p_331233_, p_334492_, p_330788_));
    }

    public ItemCost withComponents(UnaryOperator<DataComponentPredicate.Builder> p_328625_) {
        return new ItemCost(this.item, this.count, p_328625_.apply(DataComponentPredicate.builder()).build());
    }

    private static ItemStack createStack(Holder<Item> p_329043_, int p_329370_, DataComponentPredicate p_330789_) {
        return new ItemStack(p_329043_, p_329370_, p_330789_.asPatch());
    }

    public boolean test(ItemStack p_331178_) {
        return p_331178_.is(this.item) && this.components.test(p_331178_);
    }
}