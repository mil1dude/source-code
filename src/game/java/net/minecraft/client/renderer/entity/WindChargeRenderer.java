package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.WindChargeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WindChargeRenderer extends EntityRenderer<AbstractWindCharge, EntityRenderState> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/wind_charge.png");
    private final WindChargeModel model;

    public WindChargeRenderer(EntityRendererProvider.Context p_311606_) {
        super(p_311606_);
        this.model = new WindChargeModel(p_311606_.bakeLayer(ModelLayers.WIND_CHARGE));
    }

    @Override
    public void render(EntityRenderState p_369820_, PoseStack p_312831_, MultiBufferSource p_311698_, int p_311600_) {
        VertexConsumer vertexconsumer = p_311698_.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(p_369820_.ageInTicks) % 1.0F, 0.0F));
        this.model.setupAnim(p_369820_);
        this.model.renderToBuffer(p_312831_, vertexconsumer, p_311600_, OverlayTexture.NO_OVERLAY);
        super.render(p_369820_, p_312831_, p_311698_, p_311600_);
    }

    protected float xOffset(float p_311672_) {
        return p_311672_ * 0.03F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}