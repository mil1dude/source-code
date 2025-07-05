package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseMarkingLayer extends RenderLayer<HorseRenderState, HorseModel> {
    private static final Map<Markings, ResourceLocation> LOCATION_BY_MARKINGS = Util.make(Maps.newEnumMap(Markings.class), p_340945_ -> {
        p_340945_.put(Markings.NONE, null);
        p_340945_.put(Markings.WHITE, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_white.png"));
        p_340945_.put(Markings.WHITE_FIELD, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_whitefield.png"));
        p_340945_.put(Markings.WHITE_DOTS, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_whitedots.png"));
        p_340945_.put(Markings.BLACK_DOTS, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_markings_blackdots.png"));
    });

    public HorseMarkingLayer(RenderLayerParent<HorseRenderState, HorseModel> p_117045_) {
        super(p_117045_);
    }

    public void render(PoseStack p_117058_, MultiBufferSource p_117059_, int p_117060_, HorseRenderState p_366742_, float p_117062_, float p_117063_) {
        ResourceLocation resourcelocation = LOCATION_BY_MARKINGS.get(p_366742_.markings);
        if (resourcelocation != null && !p_366742_.isInvisible) {
            VertexConsumer vertexconsumer = p_117059_.getBuffer(RenderType.entityTranslucent(resourcelocation));
            this.getParentModel().renderToBuffer(p_117058_, vertexconsumer, p_117060_, LivingEntityRenderer.getOverlayCoords(p_366742_, 0.0F));
        }
    }
}