package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaRenderer extends AgeableMobRenderer<Llama, LlamaRenderState, LlamaModel> {
    private static final ResourceLocation CREAMY = ResourceLocation.withDefaultNamespace("textures/entity/llama/creamy.png");
    private static final ResourceLocation WHITE = ResourceLocation.withDefaultNamespace("textures/entity/llama/white.png");
    private static final ResourceLocation BROWN = ResourceLocation.withDefaultNamespace("textures/entity/llama/brown.png");
    private static final ResourceLocation GRAY = ResourceLocation.withDefaultNamespace("textures/entity/llama/gray.png");

    public LlamaRenderer(EntityRendererProvider.Context p_174293_, ModelLayerLocation p_174294_, ModelLayerLocation p_361682_) {
        super(p_174293_, new LlamaModel(p_174293_.bakeLayer(p_174294_)), new LlamaModel(p_174293_.bakeLayer(p_361682_)), 0.7F);
        this.addLayer(new LlamaDecorLayer(this, p_174293_.getModelSet(), p_174293_.getEquipmentRenderer()));
    }

    public ResourceLocation getTextureLocation(LlamaRenderState p_366123_) {
        return switch (p_366123_.variant) {
            case CREAMY -> CREAMY;
            case WHITE -> WHITE;
            case BROWN -> BROWN;
            case GRAY -> GRAY;
        };
    }

    public LlamaRenderState createRenderState() {
        return new LlamaRenderState();
    }

    public void extractRenderState(Llama p_368927_, LlamaRenderState p_369159_, float p_368423_) {
        super.extractRenderState(p_368927_, p_369159_, p_368423_);
        p_369159_.variant = p_368927_.getVariant();
        p_369159_.hasChest = !p_368927_.isBaby() && p_368927_.hasChest();
        p_369159_.bodyItem = p_368927_.getBodyArmorItem();
        p_369159_.isTraderLlama = p_368927_.isTraderLlama();
    }
}