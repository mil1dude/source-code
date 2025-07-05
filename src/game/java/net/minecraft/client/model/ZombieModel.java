package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieModel<S extends ZombieRenderState> extends AbstractZombieModel<S> {
    public ZombieModel(ModelPart p_171090_) {
        super(p_171090_);
    }
}