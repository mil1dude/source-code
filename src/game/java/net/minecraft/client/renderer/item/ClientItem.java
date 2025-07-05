package net.minecraft.client.renderer.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ClientItem(ItemModel.Unbaked model, ClientItem.Properties properties) {
    public static final Codec<ClientItem> CODEC = RecordCodecBuilder.create(
        p_377165_ -> p_377165_.group(
                    ItemModels.CODEC.fieldOf("model").forGetter(ClientItem::model), ClientItem.Properties.MAP_CODEC.forGetter(ClientItem::properties)
                )
                .apply(p_377165_, ClientItem::new)
    );

    @OnlyIn(Dist.CLIENT)
    public static record Properties(boolean handAnimationOnSwap) {
        public static final ClientItem.Properties DEFAULT = new ClientItem.Properties(true);
        public static final MapCodec<ClientItem.Properties> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_376311_ -> p_376311_.group(
                        Codec.BOOL.optionalFieldOf("hand_animation_on_swap", Boolean.valueOf(true)).forGetter(ClientItem.Properties::handAnimationOnSwap)
                    )
                    .apply(p_376311_, ClientItem.Properties::new)
        );
    }
}