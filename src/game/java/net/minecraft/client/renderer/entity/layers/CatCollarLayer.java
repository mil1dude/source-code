package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatCollarLayer extends RenderLayer<CatRenderState, CatModel> {
    private static final ResourceLocation CAT_COLLAR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/cat/cat_collar.png");
    private final CatModel adultModel;
    private final CatModel babyModel;

    public CatCollarLayer(RenderLayerParent<CatRenderState, CatModel> p_174468_, EntityModelSet p_174469_) {
        super(p_174468_);
        this.adultModel = new CatModel(p_174469_.bakeLayer(ModelLayers.CAT_COLLAR));
        this.babyModel = new CatModel(p_174469_.bakeLayer(ModelLayers.CAT_BABY_COLLAR));
    }

    public void render(PoseStack p_116666_, MultiBufferSource p_116667_, int p_116668_, CatRenderState p_368880_, float p_116670_, float p_116671_) {
        DyeColor dyecolor = p_368880_.collarColor;
        if (dyecolor != null) {
            int i = dyecolor.getTextureDiffuseColor();
            CatModel catmodel = p_368880_.isBaby ? this.babyModel : this.adultModel;
            coloredCutoutModelCopyLayerRender(catmodel, CAT_COLLAR_LOCATION, p_116666_, p_116667_, p_116668_, p_368880_, i);
        }
    }
}