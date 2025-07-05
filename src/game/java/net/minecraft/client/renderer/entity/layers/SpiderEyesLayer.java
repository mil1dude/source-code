package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderEyesLayer<M extends SpiderModel> extends EyesLayer<LivingEntityRenderState, M> {
    private static final RenderType SPIDER_EYES = RenderType.eyes(ResourceLocation.withDefaultNamespace("textures/entity/spider_eyes.png"));

    public SpiderEyesLayer(RenderLayerParent<LivingEntityRenderState, M> p_117507_) {
        super(p_117507_);
    }

    @Override
    public RenderType renderType() {
        return SPIDER_EYES;
    }
}