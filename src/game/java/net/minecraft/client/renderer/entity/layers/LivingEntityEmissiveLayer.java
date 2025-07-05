package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LivingEntityEmissiveLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    private final ResourceLocation texture;
    private final LivingEntityEmissiveLayer.AlphaFunction<S> alphaFunction;
    private final LivingEntityEmissiveLayer.DrawSelector<S, M> drawSelector;
    private final Function<ResourceLocation, RenderType> bufferProvider;
    private final boolean alwaysVisible;

    public LivingEntityEmissiveLayer(
        RenderLayerParent<S, M> p_366651_,
        ResourceLocation p_369672_,
        LivingEntityEmissiveLayer.AlphaFunction<S> p_362758_,
        LivingEntityEmissiveLayer.DrawSelector<S, M> p_362516_,
        Function<ResourceLocation, RenderType> p_360840_,
        boolean p_375805_
    ) {
        super(p_366651_);
        this.texture = p_369672_;
        this.alphaFunction = p_362758_;
        this.drawSelector = p_362516_;
        this.bufferProvider = p_360840_;
        this.alwaysVisible = p_375805_;
    }

    public void render(PoseStack p_366547_, MultiBufferSource p_366685_, int p_367458_, S p_364851_, float p_362186_, float p_367844_) {
        if (!p_364851_.isInvisible || this.alwaysVisible) {
            if (this.onlyDrawSelectedParts(p_364851_)) {
                VertexConsumer vertexconsumer = p_366685_.getBuffer(this.bufferProvider.apply(this.texture));
                float f = this.alphaFunction.apply(p_364851_, p_364851_.ageInTicks);
                int i = ARGB.color(Mth.floor(f * 255.0F), 255, 255, 255);
                this.getParentModel().renderToBuffer(p_366547_, vertexconsumer, p_367458_, LivingEntityRenderer.getOverlayCoords(p_364851_, 0.0F), i);
                this.resetDrawForAllParts();
            }
        }
    }

    private boolean onlyDrawSelectedParts(S p_365935_) {
        List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel(), p_365935_);
        if (list.isEmpty()) {
            return false;
        } else {
            this.getParentModel().allParts().forEach(p_366127_ -> p_366127_.skipDraw = true);
            list.forEach(p_363099_ -> p_363099_.skipDraw = false);
            return true;
        }
    }

    private void resetDrawForAllParts() {
        this.getParentModel().allParts().forEach(p_367968_ -> p_367968_.skipDraw = false);
    }

    @OnlyIn(Dist.CLIENT)
    public interface AlphaFunction<S extends LivingEntityRenderState> {
        float apply(S p_370169_, float p_364679_);
    }

    @OnlyIn(Dist.CLIENT)
    public interface DrawSelector<S extends LivingEntityRenderState, M extends EntityModel<S>> {
        List<ModelPart> getPartsToDraw(M p_365165_, S p_368326_);
    }
}