package net.minecraft.client.model.geom.builders;

import net.minecraft.client.model.geom.PartPose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface MeshTransformer {
    static MeshTransformer scaling(float p_365952_) {
        float f = 24.016F * (1.0F - p_365952_);
        return p_368477_ -> p_368477_.transformed(p_366355_ -> p_366355_.scaled(p_365952_).translated(0.0F, f, 0.0F));
    }

    MeshDefinition apply(MeshDefinition p_369124_);
}