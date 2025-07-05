package net.minecraft.client.renderer.entity.state;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherSkullRenderState extends EntityRenderState {
    public boolean isDangerous;
    public float xRot;
    public float yRot;
}