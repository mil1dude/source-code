package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Display;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class DisplayEntityRenderState extends EntityRenderState {
    @Nullable
    public Display.RenderState renderState;
    public float interpolationProgress;
    public float entityYRot;
    public float entityXRot;

    public abstract boolean hasSubState();
}