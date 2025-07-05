package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public abstract class Model {
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
    protected final ModelPart root;
    protected final Function<ResourceLocation, RenderType> renderType;
    private final List<ModelPart> allParts;

    public Model(ModelPart p_362439_, Function<ResourceLocation, RenderType> p_103110_) {
        this.root = p_362439_;
        this.renderType = p_103110_;
        this.allParts = p_362439_.getAllParts().toList();
    }

    public final RenderType renderType(ResourceLocation p_103120_) {
        return this.renderType.apply(p_103120_);
    }

    public final void renderToBuffer(PoseStack p_103111_, VertexConsumer p_103112_, int p_103113_, int p_103114_, int p_345283_) {
        this.root().render(p_103111_, p_103112_, p_103113_, p_103114_, p_345283_);
    }

    public final void renderToBuffer(PoseStack p_345147_, VertexConsumer p_343104_, int p_342281_, int p_344413_) {
        this.renderToBuffer(p_345147_, p_343104_, p_342281_, p_344413_, -1);
    }

    public final ModelPart root() {
        return this.root;
    }

    public Optional<ModelPart> getAnyDescendantWithName(String p_363872_) {
        return p_363872_.equals("root")
            ? Optional.of(this.root())
            : this.root().getAllParts().filter(p_364767_ -> p_364767_.hasChild(p_363872_)).findFirst().map(p_366385_ -> p_366385_.getChild(p_363872_));
    }

    public final List<ModelPart> allParts() {
        return this.allParts;
    }

    public final void resetPose() {
        for (ModelPart modelpart : this.allParts) {
            modelpart.resetPose();
        }
    }

    protected void animate(AnimationState p_361867_, AnimationDefinition p_365477_, float p_361961_) {
        this.animate(p_361867_, p_365477_, p_361961_, 1.0F);
    }

    protected void animateWalk(AnimationDefinition p_363127_, float p_364817_, float p_364163_, float p_365350_, float p_365167_) {
        long i = (long)(p_364817_ * 50.0F * p_365350_);
        float f = Math.min(p_364163_ * p_365167_, 1.0F);
        KeyframeAnimations.animate(this, p_363127_, i, f, ANIMATION_VECTOR_CACHE);
    }

    protected void animate(AnimationState p_368871_, AnimationDefinition p_365491_, float p_363110_, float p_368202_) {
        p_368871_.ifStarted(
            p_368242_ -> KeyframeAnimations.animate(this, p_365491_, (long)((float)p_368242_.getTimeInMillis(p_363110_) * p_368202_), 1.0F, ANIMATION_VECTOR_CACHE)
        );
    }

    protected void applyStatic(AnimationDefinition p_369884_) {
        KeyframeAnimations.animate(this, p_369884_, 0L, 1.0F, ANIMATION_VECTOR_CACHE);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Simple extends Model {
        public Simple(ModelPart p_368796_, Function<ResourceLocation, RenderType> p_362226_) {
            super(p_368796_, p_362226_);
        }
    }
}