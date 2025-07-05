package net.minecraft.world.item.equipment.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public record TrimPattern(ResourceLocation assetId, Holder<Item> templateItem, Component description, boolean decal) {
    public static final Codec<TrimPattern> DIRECT_CODEC = RecordCodecBuilder.create(
        p_361258_ -> p_361258_.group(
                    ResourceLocation.CODEC.fieldOf("asset_id").forGetter(TrimPattern::assetId),
                    Item.CODEC.fieldOf("template_item").forGetter(TrimPattern::templateItem),
                    ComponentSerialization.CODEC.fieldOf("description").forGetter(TrimPattern::description),
                    Codec.BOOL.fieldOf("decal").orElse(false).forGetter(TrimPattern::decal)
                )
                .apply(p_361258_, TrimPattern::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, TrimPattern> DIRECT_STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        TrimPattern::assetId,
        ByteBufCodecs.holderRegistry(Registries.ITEM),
        TrimPattern::templateItem,
        ComponentSerialization.STREAM_CODEC,
        TrimPattern::description,
        ByteBufCodecs.BOOL,
        TrimPattern::decal,
        TrimPattern::new
    );
    public static final Codec<Holder<TrimPattern>> CODEC = RegistryFileCodec.create(Registries.TRIM_PATTERN, DIRECT_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<TrimPattern>> STREAM_CODEC = ByteBufCodecs.holder(Registries.TRIM_PATTERN, DIRECT_STREAM_CODEC);

    public Component copyWithStyle(Holder<TrimMaterial> p_365604_) {
        return this.description.copy().withStyle(p_365604_.value().description().getStyle());
    }
}