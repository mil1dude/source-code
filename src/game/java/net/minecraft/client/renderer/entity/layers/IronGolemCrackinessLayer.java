package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Crackiness;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemCrackinessLayer extends RenderLayer<IronGolemRenderState, IronGolemModel> {
    private static final Map<Crackiness.Level, ResourceLocation> resourceLocations = ImmutableMap.of(
        Crackiness.Level.LOW,
        ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_low.png"),
        Crackiness.Level.MEDIUM,
        ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_medium.png"),
        Crackiness.Level.HIGH,
        ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_high.png")
    );

    public IronGolemCrackinessLayer(RenderLayerParent<IronGolemRenderState, IronGolemModel> p_117135_) {
        super(p_117135_);
    }

    public void render(PoseStack p_117137_, MultiBufferSource p_117138_, int p_117139_, IronGolemRenderState p_362621_, float p_117141_, float p_117142_) {
        if (!p_362621_.isInvisible) {
            Crackiness.Level crackiness$level = p_362621_.crackiness;
            if (crackiness$level != Crackiness.Level.NONE) {
                ResourceLocation resourcelocation = resourceLocations.get(crackiness$level);
                renderColoredCutoutModel(this.getParentModel(), resourcelocation, p_117137_, p_117138_, p_117139_, p_362621_, -1);
            }
        }
    }
}