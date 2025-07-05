package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record UseDuration(boolean remaining) implements RangeSelectItemModelProperty {
    public static final MapCodec<UseDuration> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_377651_ -> p_377651_.group(Codec.BOOL.optionalFieldOf("remaining", Boolean.valueOf(false)).forGetter(UseDuration::remaining))
                .apply(p_377651_, UseDuration::new)
    );

    @Override
    public float get(ItemStack p_376732_, @Nullable ClientLevel p_378428_, @Nullable LivingEntity p_375967_, int p_376186_) {
        if (p_375967_ != null && p_375967_.getUseItem() == p_376732_) {
            return this.remaining ? (float)p_375967_.getUseItemRemainingTicks() : (float)useDuration(p_376732_, p_375967_);
        } else {
            return 0.0F;
        }
    }

    @Override
    public MapCodec<UseDuration> type() {
        return MAP_CODEC;
    }

    public static int useDuration(ItemStack p_375921_, LivingEntity p_375843_) {
        return p_375921_.getUseDuration(p_375843_) - p_375843_.getUseItemRemainingTicks();
    }
}