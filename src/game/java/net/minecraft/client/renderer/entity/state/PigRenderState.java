package net.minecraft.client.renderer.entity.state;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PigRenderState extends LivingEntityRenderState implements SaddleableRenderState {
    public boolean isSaddled;

    @Override
    public boolean isSaddled() {
        return this.isSaddled;
    }
}