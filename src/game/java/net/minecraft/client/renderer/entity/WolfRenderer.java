package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfRenderer extends AgeableMobRenderer<Wolf, WolfRenderState, WolfModel> {
    public WolfRenderer(EntityRendererProvider.Context p_174452_) {
        super(p_174452_, new WolfModel(p_174452_.bakeLayer(ModelLayers.WOLF)), new WolfModel(p_174452_.bakeLayer(ModelLayers.WOLF_BABY)), 0.5F);
        this.addLayer(new WolfArmorLayer(this, p_174452_.getModelSet(), p_174452_.getEquipmentRenderer()));
        this.addLayer(new WolfCollarLayer(this));
    }

    protected int getModelTint(WolfRenderState p_367577_) {
        float f = p_367577_.wetShade;
        return f == 1.0F ? -1 : ARGB.colorFromFloat(1.0F, f, f, f);
    }

    public ResourceLocation getTextureLocation(WolfRenderState p_364519_) {
        return p_364519_.texture;
    }

    public WolfRenderState createRenderState() {
        return new WolfRenderState();
    }

    public void extractRenderState(Wolf p_362379_, WolfRenderState p_361845_, float p_368662_) {
        super.extractRenderState(p_362379_, p_361845_, p_368662_);
        p_361845_.isAngry = p_362379_.isAngry();
        p_361845_.isSitting = p_362379_.isInSittingPose();
        p_361845_.tailAngle = p_362379_.getTailAngle();
        p_361845_.headRollAngle = p_362379_.getHeadRollAngle(p_368662_);
        p_361845_.shakeAnim = p_362379_.getShakeAnim(p_368662_);
        p_361845_.texture = p_362379_.getTexture();
        p_361845_.wetShade = p_362379_.getWetShade(p_368662_);
        p_361845_.collarColor = p_362379_.isTame() ? p_362379_.getCollarColor() : null;
        p_361845_.bodyArmorItem = p_362379_.getBodyArmorItem().copy();
    }
}