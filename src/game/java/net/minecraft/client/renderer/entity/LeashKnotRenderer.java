package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeashKnotRenderer extends EntityRenderer<LeashFenceKnotEntity, EntityRenderState> {
    private static final ResourceLocation KNOT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/lead_knot.png");
    private final LeashKnotModel model;

    public LeashKnotRenderer(EntityRendererProvider.Context p_174284_) {
        super(p_174284_);
        this.model = new LeashKnotModel(p_174284_.bakeLayer(ModelLayers.LEASH_KNOT));
    }

    @Override
    public void render(EntityRenderState p_370198_, PoseStack p_115249_, MultiBufferSource p_115250_, int p_115251_) {
        p_115249_.pushPose();
        p_115249_.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(p_370198_);
        VertexConsumer vertexconsumer = p_115250_.getBuffer(this.model.renderType(KNOT_LOCATION));
        this.model.renderToBuffer(p_115249_, vertexconsumer, p_115251_, OverlayTexture.NO_OVERLAY);
        p_115249_.popPose();
        super.render(p_370198_, p_115249_, p_115250_, p_115251_);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}