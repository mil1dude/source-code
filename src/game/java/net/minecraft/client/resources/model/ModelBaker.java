package net.minecraft.client.resources.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.VisibleForDebug;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ModelBaker {
    BakedModel bake(ResourceLocation p_250776_, ModelState p_251280_);

    SpriteGetter sprites();

    @VisibleForDebug
    ModelDebugName rootName();
}