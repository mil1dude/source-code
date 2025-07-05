package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PhantomEyesLayer extends EyesLayer<PhantomRenderState, PhantomModel> {
    private static final RenderType PHANTOM_EYES = RenderType.eyes(ResourceLocation.withDefaultNamespace("textures/entity/phantom_eyes.png"));

    public PhantomEyesLayer(RenderLayerParent<PhantomRenderState, PhantomModel> p_117342_) {
        super(p_117342_);
    }

    @Override
    public RenderType renderType() {
        return PHANTOM_EYES;
    }
}