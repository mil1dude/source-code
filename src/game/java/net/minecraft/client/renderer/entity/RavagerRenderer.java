package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RavagerRenderer extends MobRenderer<Ravager, RavagerRenderState, RavagerModel> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/illager/ravager.png");

    public RavagerRenderer(EntityRendererProvider.Context p_174362_) {
        super(p_174362_, new RavagerModel(p_174362_.bakeLayer(ModelLayers.RAVAGER)), 1.1F);
    }

    public ResourceLocation getTextureLocation(RavagerRenderState p_369960_) {
        return TEXTURE_LOCATION;
    }

    public RavagerRenderState createRenderState() {
        return new RavagerRenderState();
    }

    public void extractRenderState(Ravager p_364295_, RavagerRenderState p_364078_, float p_362823_) {
        super.extractRenderState(p_364295_, p_364078_, p_362823_);
        p_364078_.stunnedTicksRemaining = (float)p_364295_.getStunnedTick() > 0.0F ? (float)p_364295_.getStunnedTick() - p_362823_ : 0.0F;
        p_364078_.attackTicksRemaining = (float)p_364295_.getAttackTick() > 0.0F ? (float)p_364295_.getAttackTick() - p_362823_ : 0.0F;
        if (p_364295_.getRoarTick() > 0) {
            p_364078_.roarAnimation = ((float)(20 - p_364295_.getRoarTick()) + p_362823_) / 20.0F;
        } else {
            p_364078_.roarAnimation = 0.0F;
        }
    }
}