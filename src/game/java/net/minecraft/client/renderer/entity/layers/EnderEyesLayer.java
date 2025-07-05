package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnderEyesLayer extends EyesLayer<EndermanRenderState, EndermanModel<EndermanRenderState>> {
    private static final RenderType ENDERMAN_EYES = RenderType.eyes(ResourceLocation.withDefaultNamespace("textures/entity/enderman/enderman_eyes.png"));

    public EnderEyesLayer(RenderLayerParent<EndermanRenderState, EndermanModel<EndermanRenderState>> p_116964_) {
        super(p_116964_);
    }

    @Override
    public RenderType renderType() {
        return ENDERMAN_EYES;
    }
}