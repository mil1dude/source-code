package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractZombieModel<S extends ZombieRenderState> extends HumanoidModel<S> {
    protected AbstractZombieModel(ModelPart p_170337_) {
        super(p_170337_);
    }

    public void setupAnim(S p_369859_) {
        super.setupAnim(p_369859_);
        float f = p_369859_.attackTime;
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, p_369859_.isAggressive, f, p_369859_.ageInTicks);
    }
}