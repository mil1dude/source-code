package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface NoDataSpecialModelRenderer extends SpecialModelRenderer<Void> {
    @Nullable
    default Void extractArgument(ItemStack p_376871_) {
        return null;
    }

    default void render(
        @Nullable Void p_378392_,
        ItemDisplayContext p_377834_,
        PoseStack p_378650_,
        MultiBufferSource p_378478_,
        int p_378005_,
        int p_376839_,
        boolean p_375776_
    ) {
        this.render(p_377834_, p_378650_, p_378478_, p_378005_, p_376839_, p_375776_);
    }

    void render(ItemDisplayContext p_376384_, PoseStack p_377457_, MultiBufferSource p_378580_, int p_375653_, int p_376500_, boolean p_376690_);
}