package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CowRenderer extends AgeableMobRenderer<Cow, LivingEntityRenderState, CowModel> {
    private static final ResourceLocation COW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/cow/cow.png");

    public CowRenderer(EntityRendererProvider.Context p_173956_) {
        super(p_173956_, new CowModel(p_173956_.bakeLayer(ModelLayers.COW)), new CowModel(p_173956_.bakeLayer(ModelLayers.COW_BABY)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState p_363298_) {
        return COW_LOCATION;
    }

    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    public void extractRenderState(Cow p_368549_, LivingEntityRenderState p_369552_, float p_367056_) {
        super.extractRenderState(p_368549_, p_369552_, p_367056_);
    }
}