package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EndCrystalModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EndCrystalRenderer extends EntityRenderer<EndCrystal, EndCrystalRenderState> {
    private static final ResourceLocation END_CRYSTAL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
    private final EndCrystalModel model;

    public EndCrystalRenderer(EntityRendererProvider.Context p_173970_) {
        super(p_173970_);
        this.shadowRadius = 0.5F;
        this.model = new EndCrystalModel(p_173970_.bakeLayer(ModelLayers.END_CRYSTAL));
    }

    public void render(EndCrystalRenderState p_362241_, PoseStack p_114147_, MultiBufferSource p_114148_, int p_114149_) {
        p_114147_.pushPose();
        p_114147_.scale(2.0F, 2.0F, 2.0F);
        p_114147_.translate(0.0F, -0.5F, 0.0F);
        this.model.setupAnim(p_362241_);
        this.model.renderToBuffer(p_114147_, p_114148_.getBuffer(RENDER_TYPE), p_114149_, OverlayTexture.NO_OVERLAY);
        p_114147_.popPose();
        Vec3 vec3 = p_362241_.beamOffset;
        if (vec3 != null) {
            float f = getY(p_362241_.ageInTicks);
            float f1 = (float)vec3.x;
            float f2 = (float)vec3.y;
            float f3 = (float)vec3.z;
            p_114147_.translate(vec3);
            EnderDragonRenderer.renderCrystalBeams(-f1, -f2 + f, -f3, p_362241_.ageInTicks, p_114147_, p_114148_, p_114149_);
        }

        super.render(p_362241_, p_114147_, p_114148_, p_114149_);
    }

    public static float getY(float p_114160_) {
        float f = Mth.sin(p_114160_ * 0.2F) / 2.0F + 0.5F;
        f = (f * f + f) * 0.4F;
        return f - 1.4F;
    }

    public EndCrystalRenderState createRenderState() {
        return new EndCrystalRenderState();
    }

    public void extractRenderState(EndCrystal p_362048_, EndCrystalRenderState p_362246_, float p_367199_) {
        super.extractRenderState(p_362048_, p_362246_, p_367199_);
        p_362246_.ageInTicks = (float)p_362048_.time + p_367199_;
        p_362246_.showsBottom = p_362048_.showsBottom();
        BlockPos blockpos = p_362048_.getBeamTarget();
        if (blockpos != null) {
            p_362246_.beamOffset = Vec3.atCenterOf(blockpos).subtract(p_362048_.getPosition(p_367199_));
        } else {
            p_362246_.beamOffset = null;
        }
    }

    public boolean shouldRender(EndCrystal p_114169_, Frustum p_114170_, double p_114171_, double p_114172_, double p_114173_) {
        return super.shouldRender(p_114169_, p_114170_, p_114171_, p_114172_, p_114173_) || p_114169_.getBeamTarget() != null;
    }
}