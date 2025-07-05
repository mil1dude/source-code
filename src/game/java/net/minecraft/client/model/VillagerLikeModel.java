package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface VillagerLikeModel {
    void hatVisible(boolean p_375953_);

    void translateToArms(PoseStack p_375845_);
}