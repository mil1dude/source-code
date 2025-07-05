package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PufferfishRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PufferfishRenderer extends MobRenderer<Pufferfish, PufferfishRenderState, EntityModel<EntityRenderState>> {
    private static final ResourceLocation PUFFER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fish/pufferfish.png");
    private final EntityModel<EntityRenderState> small;
    private final EntityModel<EntityRenderState> mid;
    private final EntityModel<EntityRenderState> big = this.getModel();

    public PufferfishRenderer(EntityRendererProvider.Context p_174358_) {
        super(p_174358_, new PufferfishBigModel(p_174358_.bakeLayer(ModelLayers.PUFFERFISH_BIG)), 0.2F);
        this.mid = new PufferfishMidModel(p_174358_.bakeLayer(ModelLayers.PUFFERFISH_MEDIUM));
        this.small = new PufferfishSmallModel(p_174358_.bakeLayer(ModelLayers.PUFFERFISH_SMALL));
    }

    public ResourceLocation getTextureLocation(PufferfishRenderState p_366694_) {
        return PUFFER_LOCATION;
    }

    public PufferfishRenderState createRenderState() {
        return new PufferfishRenderState();
    }

    protected float getShadowRadius(PufferfishRenderState p_376900_) {
        return 0.1F + 0.1F * (float)p_376900_.puffState;
    }

    public void render(PufferfishRenderState p_367057_, PoseStack p_115751_, MultiBufferSource p_115752_, int p_115753_) {
        this.model = switch (p_367057_.puffState) {
            case 0 -> this.small;
            case 1 -> this.mid;
            default -> this.big;
        };
        super.render(p_367057_, p_115751_, p_115752_, p_115753_);
    }

    public void extractRenderState(Pufferfish p_364666_, PufferfishRenderState p_362078_, float p_368845_) {
        super.extractRenderState(p_364666_, p_362078_, p_368845_);
        p_362078_.puffState = p_364666_.getPuffState();
    }

    protected void setupRotations(PufferfishRenderState p_362931_, PoseStack p_115785_, float p_115786_, float p_115787_) {
        p_115785_.translate(0.0F, Mth.cos(p_362931_.ageInTicks * 0.05F) * 0.08F, 0.0F);
        super.setupRotations(p_362931_, p_115785_, p_115786_, p_115787_);
    }
}