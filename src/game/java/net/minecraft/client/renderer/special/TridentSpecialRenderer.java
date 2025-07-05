package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TridentSpecialRenderer implements NoDataSpecialModelRenderer {
    private final TridentModel model;

    public TridentSpecialRenderer(TridentModel p_377002_) {
        this.model = p_377002_;
    }

    @Override
    public void render(ItemDisplayContext p_377926_, PoseStack p_375657_, MultiBufferSource p_376715_, int p_377943_, int p_376595_, boolean p_375950_) {
        p_375657_.pushPose();
        p_375657_.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer vertexconsumer = ItemRenderer.getFoilBuffer(p_376715_, this.model.renderType(TridentModel.TEXTURE), false, p_375950_);
        this.model.renderToBuffer(p_375657_, vertexconsumer, p_377943_, p_376595_);
        p_375657_.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<TridentSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new TridentSpecialRenderer.Unbaked());

        @Override
        public MapCodec<TridentSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_378766_) {
            return new TridentSpecialRenderer(new TridentModel(p_378766_.bakeLayer(ModelLayers.TRIDENT)));
        }
    }
}