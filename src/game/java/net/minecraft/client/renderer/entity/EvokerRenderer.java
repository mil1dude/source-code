package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvokerRenderer<T extends SpellcasterIllager> extends IllagerRenderer<T, EvokerRenderState> {
    private static final ResourceLocation EVOKER_ILLAGER = ResourceLocation.withDefaultNamespace("textures/entity/illager/evoker.png");

    public EvokerRenderer(EntityRendererProvider.Context p_174108_) {
        super(p_174108_, new IllagerModel<>(p_174108_.bakeLayer(ModelLayers.EVOKER)), 0.5F);
        this.addLayer(
            new ItemInHandLayer<EvokerRenderState, IllagerModel<EvokerRenderState>>(this) {
                public void render(
                    PoseStack p_114558_, MultiBufferSource p_114559_, int p_114560_, EvokerRenderState p_364052_, float p_114562_, float p_114563_
                ) {
                    if (p_364052_.isCastingSpell) {
                        super.render(p_114558_, p_114559_, p_114560_, p_364052_, p_114562_, p_114563_);
                    }
                }
            }
        );
    }

    public ResourceLocation getTextureLocation(EvokerRenderState p_362508_) {
        return EVOKER_ILLAGER;
    }

    public EvokerRenderState createRenderState() {
        return new EvokerRenderState();
    }

    public void extractRenderState(T p_365266_, EvokerRenderState p_366454_, float p_367239_) {
        super.extractRenderState(p_365266_, p_366454_, p_367239_);
        p_366454_.isCastingSpell = p_365266_.isCastingSpell();
    }
}