package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ExtendedView() implements ConditionalItemModelProperty {
    public static final MapCodec<ExtendedView> MAP_CODEC = MapCodec.unit(new ExtendedView());

    @Override
    public boolean get(
        ItemStack p_378151_, @Nullable ClientLevel p_378363_, @Nullable LivingEntity p_377133_, int p_378535_, ItemDisplayContext p_375662_
    ) {
        return p_375662_ == ItemDisplayContext.GUI && Screen.hasShiftDown();
    }

    @Override
    public MapCodec<ExtendedView> type() {
        return MAP_CODEC;
    }
}