package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CrossbowPull implements RangeSelectItemModelProperty {
    public static final MapCodec<CrossbowPull> MAP_CODEC = MapCodec.unit(new CrossbowPull());

    @Override
    public float get(ItemStack p_376874_, @Nullable ClientLevel p_377083_, @Nullable LivingEntity p_376903_, int p_377235_) {
        if (p_376903_ == null) {
            return 0.0F;
        } else if (CrossbowItem.isCharged(p_376874_)) {
            return 0.0F;
        } else {
            int i = CrossbowItem.getChargeDuration(p_376874_, p_376903_);
            return (float)UseDuration.useDuration(p_376874_, p_376903_) / (float)i;
        }
    }

    @Override
    public MapCodec<CrossbowPull> type() {
        return MAP_CODEC;
    }
}