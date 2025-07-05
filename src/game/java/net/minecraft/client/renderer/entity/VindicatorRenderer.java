package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VindicatorRenderer extends IllagerRenderer<Vindicator, IllagerRenderState> {
    private static final ResourceLocation VINDICATOR = ResourceLocation.withDefaultNamespace("textures/entity/illager/vindicator.png");

    public VindicatorRenderer(EntityRendererProvider.Context p_174439_) {
        super(p_174439_, new IllagerModel<>(p_174439_.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.addLayer(
            new ItemInHandLayer<IllagerRenderState, IllagerModel<IllagerRenderState>>(this) {
                public void render(
                    PoseStack p_116330_, MultiBufferSource p_116331_, int p_116332_, IllagerRenderState p_361563_, float p_116334_, float p_116335_
                ) {
                    if (p_361563_.isAggressive) {
                        super.render(p_116330_, p_116331_, p_116332_, p_361563_, p_116334_, p_116335_);
                    }
                }
            }
        );
    }

    public ResourceLocation getTextureLocation(IllagerRenderState p_361816_) {
        return VINDICATOR;
    }

    public IllagerRenderState createRenderState() {
        return new IllagerRenderState();
    }
}