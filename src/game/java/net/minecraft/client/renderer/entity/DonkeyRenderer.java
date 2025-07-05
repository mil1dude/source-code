package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.DonkeyModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.state.DonkeyRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DonkeyRenderer<T extends AbstractChestedHorse> extends AbstractHorseRenderer<T, DonkeyRenderState, DonkeyModel> {
    public static final ResourceLocation DONKEY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/horse/donkey.png");
    public static final ResourceLocation MULE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/horse/mule.png");
    private final ResourceLocation texture;

    public DonkeyRenderer(EntityRendererProvider.Context p_362293_, ModelLayerLocation p_368663_, ModelLayerLocation p_368478_, boolean p_362367_) {
        super(p_362293_, new DonkeyModel(p_362293_.bakeLayer(p_368663_)), new DonkeyModel(p_362293_.bakeLayer(p_368478_)));
        this.texture = p_362367_ ? MULE_TEXTURE : DONKEY_TEXTURE;
    }

    public ResourceLocation getTextureLocation(DonkeyRenderState p_367902_) {
        return this.texture;
    }

    public DonkeyRenderState createRenderState() {
        return new DonkeyRenderState();
    }

    public void extractRenderState(T p_363167_, DonkeyRenderState p_369827_, float p_366107_) {
        super.extractRenderState(p_363167_, p_369827_, p_366107_);
        p_369827_.hasChest = p_363167_.hasChest();
    }
}