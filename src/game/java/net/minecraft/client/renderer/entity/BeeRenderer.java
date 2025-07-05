package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Bee;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeRenderer extends AgeableMobRenderer<Bee, BeeRenderState, BeeModel> {
    private static final ResourceLocation ANGRY_BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_angry.png");
    private static final ResourceLocation ANGRY_NECTAR_BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_angry_nectar.png");
    private static final ResourceLocation BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee.png");
    private static final ResourceLocation NECTAR_BEE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_nectar.png");

    public BeeRenderer(EntityRendererProvider.Context p_173931_) {
        super(p_173931_, new BeeModel(p_173931_.bakeLayer(ModelLayers.BEE)), new BeeModel(p_173931_.bakeLayer(ModelLayers.BEE_BABY)), 0.4F);
    }

    public ResourceLocation getTextureLocation(BeeRenderState p_368863_) {
        if (p_368863_.isAngry) {
            return p_368863_.hasNectar ? ANGRY_NECTAR_BEE_TEXTURE : ANGRY_BEE_TEXTURE;
        } else {
            return p_368863_.hasNectar ? NECTAR_BEE_TEXTURE : BEE_TEXTURE;
        }
    }

    public BeeRenderState createRenderState() {
        return new BeeRenderState();
    }

    public void extractRenderState(Bee p_362651_, BeeRenderState p_362934_, float p_366251_) {
        super.extractRenderState(p_362651_, p_362934_, p_366251_);
        p_362934_.rollAmount = p_362651_.getRollAmount(p_366251_);
        p_362934_.hasStinger = !p_362651_.hasStung();
        p_362934_.isOnGround = p_362651_.onGround() && p_362651_.getDeltaMovement().lengthSqr() < 1.0E-7;
        p_362934_.isAngry = p_362651_.isAngry();
        p_362934_.hasNectar = p_362651_.hasNectar();
    }
}