package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractBoatModel extends EntityModel<BoatRenderState> {
    private final ModelPart leftPaddle;
    private final ModelPart rightPaddle;

    public AbstractBoatModel(ModelPart p_369766_) {
        super(p_369766_);
        this.leftPaddle = p_369766_.getChild("left_paddle");
        this.rightPaddle = p_369766_.getChild("right_paddle");
    }

    public void setupAnim(BoatRenderState p_369502_) {
        super.setupAnim(p_369502_);
        animatePaddle(p_369502_.rowingTimeLeft, 0, this.leftPaddle);
        animatePaddle(p_369502_.rowingTimeRight, 1, this.rightPaddle);
    }

    private static void animatePaddle(float p_369972_, int p_367778_, ModelPart p_361377_) {
        p_361377_.xRot = Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (Mth.sin(-p_369972_) + 1.0F) / 2.0F);
        p_361377_.yRot = Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (Mth.sin(-p_369972_ + 1.0F) + 1.0F) / 2.0F);
        if (p_367778_ == 1) {
            p_361377_.yRot = (float) Math.PI - p_361377_.yRot;
        }
    }
}