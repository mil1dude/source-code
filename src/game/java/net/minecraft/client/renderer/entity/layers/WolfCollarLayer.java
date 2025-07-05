package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfCollarLayer extends RenderLayer<WolfRenderState, WolfModel> {
    private static final ResourceLocation WOLF_COLLAR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_collar.png");

    public WolfCollarLayer(RenderLayerParent<WolfRenderState, WolfModel> p_117707_) {
        super(p_117707_);
    }

    public void render(PoseStack p_117709_, MultiBufferSource p_117710_, int p_117711_, WolfRenderState p_365773_, float p_117713_, float p_117714_) {
        DyeColor dyecolor = p_365773_.collarColor;
        if (dyecolor != null && !p_365773_.isInvisible) {
            int i = dyecolor.getTextureDiffuseColor();
            VertexConsumer vertexconsumer = p_117710_.getBuffer(RenderType.entityCutoutNoCull(WOLF_COLLAR_LOCATION));
            this.getParentModel().renderToBuffer(p_117709_, vertexconsumer, p_117711_, OverlayTexture.NO_OVERLAY, i);
        }
    }
}