package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SkullModelBase extends Model {
    public SkullModelBase(ModelPart p_365382_) {
        super(p_365382_, RenderType::entityTranslucent);
    }

    public abstract void setupAnim(float p_170950_, float p_170951_, float p_170952_);
}