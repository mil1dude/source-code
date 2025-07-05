package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractBoatRenderer extends EntityRenderer<AbstractBoat, BoatRenderState> {
    public AbstractBoatRenderer(EntityRendererProvider.Context p_366587_) {
        super(p_366587_);
        this.shadowRadius = 0.8F;
    }

    public void render(BoatRenderState p_367425_, PoseStack p_366270_, MultiBufferSource p_367987_, int p_363052_) {
        p_366270_.pushPose();
        p_366270_.translate(0.0F, 0.375F, 0.0F);
        p_366270_.mulPose(Axis.YP.rotationDegrees(180.0F - p_367425_.yRot));
        float f = p_367425_.hurtTime;
        if (f > 0.0F) {
            p_366270_.mulPose(Axis.XP.rotationDegrees(Mth.sin(f) * f * p_367425_.damageTime / 10.0F * (float)p_367425_.hurtDir));
        }

        if (!Mth.equal(p_367425_.bubbleAngle, 0.0F)) {
            p_366270_.mulPose(new Quaternionf().setAngleAxis(p_367425_.bubbleAngle * (float) (Math.PI / 180.0), 1.0F, 0.0F, 1.0F));
        }

        p_366270_.scale(-1.0F, -1.0F, 1.0F);
        p_366270_.mulPose(Axis.YP.rotationDegrees(90.0F));
        EntityModel<BoatRenderState> entitymodel = this.model();
        entitymodel.setupAnim(p_367425_);
        VertexConsumer vertexconsumer = p_367987_.getBuffer(this.renderType());
        entitymodel.renderToBuffer(p_366270_, vertexconsumer, p_363052_, OverlayTexture.NO_OVERLAY);
        this.renderTypeAdditions(p_367425_, p_366270_, p_367987_, p_363052_);
        p_366270_.popPose();
        super.render(p_367425_, p_366270_, p_367987_, p_363052_);
    }

    protected void renderTypeAdditions(BoatRenderState p_361534_, PoseStack p_362579_, MultiBufferSource p_361820_, int p_361807_) {
    }

    protected abstract EntityModel<BoatRenderState> model();

    protected abstract RenderType renderType();

    public BoatRenderState createRenderState() {
        return new BoatRenderState();
    }

    public void extractRenderState(AbstractBoat p_361976_, BoatRenderState p_368077_, float p_364654_) {
        super.extractRenderState(p_361976_, p_368077_, p_364654_);
        p_368077_.yRot = p_361976_.getYRot(p_364654_);
        p_368077_.hurtTime = (float)p_361976_.getHurtTime() - p_364654_;
        p_368077_.hurtDir = p_361976_.getHurtDir();
        p_368077_.damageTime = Math.max(p_361976_.getDamage() - p_364654_, 0.0F);
        p_368077_.bubbleAngle = p_361976_.getBubbleAngle(p_364654_);
        p_368077_.isUnderWater = p_361976_.isUnderWater();
        p_368077_.rowingTimeLeft = p_361976_.getRowingTime(0, p_364654_);
        p_368077_.rowingTimeRight = p_361976_.getRowingTime(1, p_364654_);
    }
}