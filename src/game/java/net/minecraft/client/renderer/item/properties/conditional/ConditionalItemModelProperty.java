package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ConditionalItemModelProperty {
    boolean get(ItemStack p_377867_, @Nullable ClientLevel p_377453_, @Nullable LivingEntity p_377577_, int p_378696_, ItemDisplayContext p_377698_);

    MapCodec<? extends ConditionalItemModelProperty> type();
}