package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombifiedPiglinModel extends AbstractPiglinModel<ZombifiedPiglinRenderState> {
    public ZombifiedPiglinModel(ModelPart p_366911_) {
        super(p_366911_);
    }

    public void setupAnim(ZombifiedPiglinRenderState p_365785_) {
        super.setupAnim(p_365785_);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, p_365785_.isAggressive, p_365785_.attackTime, p_365785_.ageInTicks);
    }

    @Override
    public void setAllVisible(boolean p_369867_) {
        super.setAllVisible(p_369867_);
        this.leftSleeve.visible = p_369867_;
        this.rightSleeve.visible = p_369867_;
        this.leftPants.visible = p_369867_;
        this.rightPants.visible = p_369867_;
        this.jacket.visible = p_369867_;
    }
}