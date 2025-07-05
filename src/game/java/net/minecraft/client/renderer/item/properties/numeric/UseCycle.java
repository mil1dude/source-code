package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record UseCycle(float period) implements RangeSelectItemModelProperty {
    public static final MapCodec<UseCycle> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_375755_ -> p_375755_.group(ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("period", 1.0F).forGetter(UseCycle::period)).apply(p_375755_, UseCycle::new)
    );

    @Override
    public float get(ItemStack p_377041_, @Nullable ClientLevel p_378065_, @Nullable LivingEntity p_375409_, int p_376704_) {
        return p_375409_ != null && p_375409_.getUseItem() == p_377041_ ? (float)p_375409_.getUseItemRemainingTicks() % this.period : 0.0F;
    }

    @Override
    public MapCodec<UseCycle> type() {
        return MAP_CODEC;
    }
}