package net.minecraft.client.color.item;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ItemTintSource {
    int calculate(ItemStack p_376017_, @Nullable ClientLevel p_378129_, @Nullable LivingEntity p_376885_);

    MapCodec<? extends ItemTintSource> type();
}