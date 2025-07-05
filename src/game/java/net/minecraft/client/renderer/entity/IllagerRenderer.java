package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.item.CrossbowItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class IllagerRenderer<T extends AbstractIllager, S extends IllagerRenderState> extends MobRenderer<T, S, IllagerModel<S>> {
    protected IllagerRenderer(EntityRendererProvider.Context p_174182_, IllagerModel<S> p_174183_, float p_174184_) {
        super(p_174182_, p_174183_, p_174184_);
        this.addLayer(new CustomHeadLayer<>(this, p_174182_.getModelSet()));
    }

    public void extractRenderState(T p_360998_, S p_365392_, float p_369885_) {
        super.extractRenderState(p_360998_, p_365392_, p_369885_);
        ArmedEntityRenderState.extractArmedEntityRenderState(p_360998_, p_365392_, this.itemModelResolver);
        p_365392_.isRiding = p_360998_.isPassenger();
        p_365392_.mainArm = p_360998_.getMainArm();
        p_365392_.armPose = p_360998_.getArmPose();
        p_365392_.maxCrossbowChargeDuration = p_365392_.armPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE
            ? CrossbowItem.getChargeDuration(p_360998_.getUseItem(), p_360998_)
            : 0;
        p_365392_.ticksUsingItem = p_360998_.getTicksUsingItem();
        p_365392_.attackAnim = p_360998_.getAttackAnim(p_369885_);
        p_365392_.isAggressive = p_360998_.isAggressive();
    }
}