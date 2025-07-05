package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrownedOuterLayer extends RenderLayer<ZombieRenderState, DrownedModel> {
    private static final ResourceLocation DROWNED_OUTER_LAYER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/drowned_outer_layer.png");
    private final DrownedModel model;
    private final DrownedModel babyModel;

    public DrownedOuterLayer(RenderLayerParent<ZombieRenderState, DrownedModel> p_174490_, EntityModelSet p_174491_) {
        super(p_174490_);
        this.model = new DrownedModel(p_174491_.bakeLayer(ModelLayers.DROWNED_OUTER_LAYER));
        this.babyModel = new DrownedModel(p_174491_.bakeLayer(ModelLayers.DROWNED_BABY_OUTER_LAYER));
    }

    public void render(PoseStack p_116924_, MultiBufferSource p_116925_, int p_116926_, ZombieRenderState p_369850_, float p_116928_, float p_116929_) {
        DrownedModel drownedmodel = p_369850_.isBaby ? this.babyModel : this.model;
        coloredCutoutModelCopyLayerRender(drownedmodel, DROWNED_OUTER_LAYER_LOCATION, p_116924_, p_116925_, p_116926_, p_369850_, -1);
    }
}