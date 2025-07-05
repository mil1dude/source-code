package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractHorseRenderer<T extends AbstractHorse, S extends EquineRenderState, M extends EntityModel<? super S>>
    extends AgeableMobRenderer<T, S, M> {
    public AbstractHorseRenderer(EntityRendererProvider.Context p_173906_, M p_367626_, M p_364695_) {
        super(p_173906_, p_367626_, p_364695_, 0.75F);
    }

    public void extractRenderState(T p_361889_, S p_361124_, float p_366226_) {
        super.extractRenderState(p_361889_, p_361124_, p_366226_);
        p_361124_.isSaddled = p_361889_.isSaddled();
        p_361124_.isRidden = p_361889_.isVehicle();
        p_361124_.eatAnimation = p_361889_.getEatAnim(p_366226_);
        p_361124_.standAnimation = p_361889_.getStandAnim(p_366226_);
        p_361124_.feedingAnimation = p_361889_.getMouthAnim(p_366226_);
        p_361124_.animateTail = p_361889_.tailCounter > 0;
    }
}