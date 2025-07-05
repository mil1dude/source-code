package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobAppearanceParticle extends Particle {
    private final Model model;
    private final RenderType renderType = RenderType.entityTranslucent(ElderGuardianRenderer.GUARDIAN_ELDER_LOCATION);

    MobAppearanceParticle(ClientLevel p_107114_, double p_107115_, double p_107116_, double p_107117_) {
        super(p_107114_, p_107115_, p_107116_, p_107117_);
        this.model = new GuardianModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELDER_GUARDIAN));
        this.gravity = 0.0F;
        this.lifetime = 30;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void renderCustom(PoseStack p_377884_, MultiBufferSource p_377063_, Camera p_375622_, float p_376735_) {
        float f = ((float)this.age + p_376735_) / (float)this.lifetime;
        float f1 = 0.05F + 0.5F * Mth.sin(f * (float) Math.PI);
        int i = ARGB.colorFromFloat(f1, 1.0F, 1.0F, 1.0F);
        p_377884_.pushPose();
        p_377884_.mulPose(p_375622_.rotation());
        p_377884_.mulPose(Axis.XP.rotationDegrees(60.0F - 150.0F * f));
        float f2 = 0.42553192F;
        p_377884_.scale(0.42553192F, -0.42553192F, -0.42553192F);
        p_377884_.translate(0.0F, -0.56F, 3.5F);
        VertexConsumer vertexconsumer = p_377063_.getBuffer(this.renderType);
        this.model.renderToBuffer(p_377884_, vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY, i);
        p_377884_.popPose();
    }

    @Override
    public void render(VertexConsumer p_107125_, Camera p_107126_, float p_107127_) {
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType p_107140_,
            ClientLevel p_107141_,
            double p_107142_,
            double p_107143_,
            double p_107144_,
            double p_107145_,
            double p_107146_,
            double p_107147_
        ) {
            return new MobAppearanceParticle(p_107141_, p_107142_, p_107143_, p_107144_);
        }
    }
}