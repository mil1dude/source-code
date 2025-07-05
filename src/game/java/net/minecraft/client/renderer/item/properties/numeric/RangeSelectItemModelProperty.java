package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface RangeSelectItemModelProperty {
    float get(ItemStack p_376822_, @Nullable ClientLevel p_376153_, @Nullable LivingEntity p_377311_, int p_376174_);

    MapCodec<? extends RangeSelectItemModelProperty> type();
}