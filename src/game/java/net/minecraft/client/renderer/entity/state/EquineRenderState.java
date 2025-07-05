package net.minecraft.client.renderer.entity.state;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EquineRenderState extends LivingEntityRenderState {
    public boolean isSaddled;
    public boolean isRidden;
    public boolean animateTail;
    public float eatAnimation;
    public float standAnimation;
    public float feedingAnimation;
}