package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.state.TippableArrowRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TippableArrowRenderer extends ArrowRenderer<Arrow, TippableArrowRenderState> {
    public static final ResourceLocation NORMAL_ARROW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    public static final ResourceLocation TIPPED_ARROW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/tipped_arrow.png");

    public TippableArrowRenderer(EntityRendererProvider.Context p_174422_) {
        super(p_174422_);
    }

    protected ResourceLocation getTextureLocation(TippableArrowRenderState p_365661_) {
        return p_365661_.isTipped ? TIPPED_ARROW_LOCATION : NORMAL_ARROW_LOCATION;
    }

    public TippableArrowRenderState createRenderState() {
        return new TippableArrowRenderState();
    }

    public void extractRenderState(Arrow p_367269_, TippableArrowRenderState p_361376_, float p_361545_) {
        super.extractRenderState(p_367269_, p_361376_, p_361545_);
        p_361376_.isTipped = p_367269_.getColor() > 0;
    }
}