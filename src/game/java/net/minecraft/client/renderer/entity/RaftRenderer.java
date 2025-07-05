package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.RaftModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RaftRenderer extends AbstractBoatRenderer {
    private final EntityModel<BoatRenderState> model;
    private final ResourceLocation texture;

    public RaftRenderer(EntityRendererProvider.Context p_366402_, ModelLayerLocation p_364351_) {
        super(p_366402_);
        this.texture = p_364351_.model().withPath(p_369320_ -> "textures/entity/" + p_369320_ + ".png");
        this.model = new RaftModel(p_366402_.bakeLayer(p_364351_));
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }

    @Override
    protected RenderType renderType() {
        return this.model.renderType(this.texture);
    }
}