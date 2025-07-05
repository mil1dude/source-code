package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonClothingLayer<S extends SkeletonRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    private final SkeletonModel<S> layerModel;
    private final ResourceLocation clothesLocation;

    public SkeletonClothingLayer(RenderLayerParent<S, M> p_330715_, EntityModelSet p_334793_, ModelLayerLocation p_335699_, ResourceLocation p_330798_) {
        super(p_330715_);
        this.clothesLocation = p_330798_;
        this.layerModel = new SkeletonModel<>(p_334793_.bakeLayer(p_335699_));
    }

    public void render(PoseStack p_332269_, MultiBufferSource p_333438_, int p_331437_, S p_366917_, float p_330307_, float p_333019_) {
        coloredCutoutModelCopyLayerRender(this.layerModel, this.clothesLocation, p_332269_, p_333438_, p_331437_, p_366917_, -1);
    }
}