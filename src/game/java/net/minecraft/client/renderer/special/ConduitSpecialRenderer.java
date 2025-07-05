package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConduitSpecialRenderer implements NoDataSpecialModelRenderer {
    private final ModelPart model;

    public ConduitSpecialRenderer(ModelPart p_377637_) {
        this.model = p_377637_;
    }

    @Override
    public void render(ItemDisplayContext p_375931_, PoseStack p_376919_, MultiBufferSource p_377501_, int p_377485_, int p_378131_, boolean p_375519_) {
        VertexConsumer vertexconsumer = ConduitRenderer.SHELL_TEXTURE.buffer(p_377501_, RenderType::entitySolid);
        p_376919_.pushPose();
        p_376919_.translate(0.5F, 0.5F, 0.5F);
        this.model.render(p_376919_, vertexconsumer, p_377485_, p_378131_);
        p_376919_.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<ConduitSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new ConduitSpecialRenderer.Unbaked());

        @Override
        public MapCodec<ConduitSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_378239_) {
            return new ConduitSpecialRenderer(p_378239_.bakeLayer(ModelLayers.CONDUIT_SHELL));
        }
    }
}