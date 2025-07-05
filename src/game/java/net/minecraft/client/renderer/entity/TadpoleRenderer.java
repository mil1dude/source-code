package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.TadpoleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TadpoleRenderer extends MobRenderer<Tadpole, LivingEntityRenderState, TadpoleModel> {
    private static final ResourceLocation TADPOLE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/tadpole/tadpole.png");

    public TadpoleRenderer(EntityRendererProvider.Context p_234655_) {
        super(p_234655_, new TadpoleModel(p_234655_.bakeLayer(ModelLayers.TADPOLE)), 0.14F);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState p_365093_) {
        return TADPOLE_TEXTURE;
    }

    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}