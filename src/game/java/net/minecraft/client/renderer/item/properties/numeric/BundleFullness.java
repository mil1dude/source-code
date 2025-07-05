package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record BundleFullness() implements RangeSelectItemModelProperty {
    public static final MapCodec<BundleFullness> MAP_CODEC = MapCodec.unit(new BundleFullness());

    @Override
    public float get(ItemStack p_375568_, @Nullable ClientLevel p_375750_, @Nullable LivingEntity p_377005_, int p_376397_) {
        return BundleItem.getFullnessDisplay(p_375568_);
    }

    @Override
    public MapCodec<BundleFullness> type() {
        return MAP_CODEC;
    }
}