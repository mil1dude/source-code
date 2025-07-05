package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SkyRenderer implements AutoCloseable {
    private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
    private static final ResourceLocation MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    public static final ResourceLocation END_SKY_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");
    private static final float SKY_DISC_RADIUS = 512.0F;
    private final VertexBuffer starBuffer = VertexBuffer.uploadStatic(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION, this::buildStars);
    private final VertexBuffer topSkyBuffer = VertexBuffer.uploadStatic(
        VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION, p_378556_ -> this.buildSkyDisc(p_378556_, 16.0F)
    );
    private final VertexBuffer bottomSkyBuffer = VertexBuffer.uploadStatic(
        VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION, p_376158_ -> this.buildSkyDisc(p_376158_, -16.0F)
    );
    private final VertexBuffer endSkyBuffer = VertexBuffer.uploadStatic(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR, this::buildEndSky);

    private void buildStars(VertexConsumer p_375895_) {
        RandomSource randomsource = RandomSource.create(10842L);
        int i = 1500;
        float f = 100.0F;

        for (int j = 0; j < 1500; j++) {
            float f1 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f2 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f3 = randomsource.nextFloat() * 2.0F - 1.0F;
            float f4 = 0.15F + randomsource.nextFloat() * 0.1F;
            float f5 = Mth.lengthSquared(f1, f2, f3);
            if (!(f5 <= 0.010000001F) && !(f5 >= 1.0F)) {
                Vector3f vector3f = new Vector3f(f1, f2, f3).normalize(100.0F);
                float f6 = (float)(randomsource.nextDouble() * (float) Math.PI * 2.0);
                Matrix3f matrix3f = new Matrix3f().rotateTowards(new Vector3f(vector3f).negate(), new Vector3f(0.0F, 1.0F, 0.0F)).rotateZ(-f6);
                p_375895_.addVertex(new Vector3f(f4, -f4, 0.0F).mul(matrix3f).add(vector3f));
                p_375895_.addVertex(new Vector3f(f4, f4, 0.0F).mul(matrix3f).add(vector3f));
                p_375895_.addVertex(new Vector3f(-f4, f4, 0.0F).mul(matrix3f).add(vector3f));
                p_375895_.addVertex(new Vector3f(-f4, -f4, 0.0F).mul(matrix3f).add(vector3f));
            }
        }
    }

    private void buildSkyDisc(VertexConsumer p_375466_, float p_363584_) {
        float f = Math.signum(p_363584_) * 512.0F;
        p_375466_.addVertex(0.0F, p_363584_, 0.0F);

        for (int i = -180; i <= 180; i += 45) {
            p_375466_.addVertex(f * Mth.cos((float)i * (float) (Math.PI / 180.0)), p_363584_, 512.0F * Mth.sin((float)i * (float) (Math.PI / 180.0)));
        }
    }

    public void renderSkyDisc(float p_369198_, float p_369913_, float p_362432_) {
        RenderSystem.setShaderColor(p_369198_, p_369913_, p_362432_, 1.0F);
        this.topSkyBuffer.drawWithRenderType(RenderType.sky());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderDarkDisc(PoseStack p_367581_) {
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
        p_367581_.pushPose();
        p_367581_.translate(0.0F, 12.0F, 0.0F);
        this.bottomSkyBuffer.drawWithRenderType(RenderType.sky());
        p_367581_.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderSunMoonAndStars(
        PoseStack p_362673_,
        MultiBufferSource.BufferSource p_376689_,
        float p_369057_,
        int p_364932_,
        float p_366540_,
        float p_368016_,
        FogParameters p_362209_
    ) {
        p_362673_.pushPose();
        p_362673_.mulPose(Axis.YP.rotationDegrees(-90.0F));
        p_362673_.mulPose(Axis.XP.rotationDegrees(p_369057_ * 360.0F));
        this.renderSun(p_366540_, p_376689_, p_362673_);
        this.renderMoon(p_364932_, p_366540_, p_376689_, p_362673_);
        p_376689_.endBatch();
        if (p_368016_ > 0.0F) {
            this.renderStars(p_362209_, p_368016_, p_362673_);
        }

        p_362673_.popPose();
    }

    private void renderSun(float p_363755_, MultiBufferSource p_376565_, PoseStack p_369287_) {
        float f = 30.0F;
        float f1 = 100.0F;
        VertexConsumer vertexconsumer = p_376565_.getBuffer(RenderType.celestial(SUN_LOCATION));
        int i = ARGB.white(p_363755_);
        Matrix4f matrix4f = p_369287_.last().pose();
        vertexconsumer.addVertex(matrix4f, -30.0F, 100.0F, -30.0F).setUv(0.0F, 0.0F).setColor(i);
        vertexconsumer.addVertex(matrix4f, 30.0F, 100.0F, -30.0F).setUv(1.0F, 0.0F).setColor(i);
        vertexconsumer.addVertex(matrix4f, 30.0F, 100.0F, 30.0F).setUv(1.0F, 1.0F).setColor(i);
        vertexconsumer.addVertex(matrix4f, -30.0F, 100.0F, 30.0F).setUv(0.0F, 1.0F).setColor(i);
    }

    private void renderMoon(int p_367893_, float p_364034_, MultiBufferSource p_377520_, PoseStack p_369177_) {
        float f = 20.0F;
        int i = p_367893_ % 4;
        int j = p_367893_ / 4 % 2;
        float f1 = (float)(i + 0) / 4.0F;
        float f2 = (float)(j + 0) / 2.0F;
        float f3 = (float)(i + 1) / 4.0F;
        float f4 = (float)(j + 1) / 2.0F;
        float f5 = 100.0F;
        VertexConsumer vertexconsumer = p_377520_.getBuffer(RenderType.celestial(MOON_LOCATION));
        int k = ARGB.white(p_364034_);
        Matrix4f matrix4f = p_369177_.last().pose();
        vertexconsumer.addVertex(matrix4f, -20.0F, -100.0F, 20.0F).setUv(f3, f4).setColor(k);
        vertexconsumer.addVertex(matrix4f, 20.0F, -100.0F, 20.0F).setUv(f1, f4).setColor(k);
        vertexconsumer.addVertex(matrix4f, 20.0F, -100.0F, -20.0F).setUv(f1, f2).setColor(k);
        vertexconsumer.addVertex(matrix4f, -20.0F, -100.0F, -20.0F).setUv(f3, f2).setColor(k);
    }

    private void renderStars(FogParameters p_362284_, float p_361462_, PoseStack p_364130_) {
        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        matrix4fstack.pushMatrix();
        matrix4fstack.mul(p_364130_.last().pose());
        RenderSystem.setShaderColor(p_361462_, p_361462_, p_361462_, p_361462_);
        RenderSystem.setShaderFog(FogParameters.NO_FOG);
        this.starBuffer.drawWithRenderType(RenderType.stars());
        RenderSystem.setShaderFog(p_362284_);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrix4fstack.popMatrix();
    }

    public void renderSunriseAndSunset(PoseStack p_365939_, MultiBufferSource.BufferSource p_377149_, float p_368996_, int p_365467_) {
        p_365939_.pushPose();
        p_365939_.mulPose(Axis.XP.rotationDegrees(90.0F));
        float f = Mth.sin(p_368996_) < 0.0F ? 180.0F : 0.0F;
        p_365939_.mulPose(Axis.ZP.rotationDegrees(f));
        p_365939_.mulPose(Axis.ZP.rotationDegrees(90.0F));
        Matrix4f matrix4f = p_365939_.last().pose();
        VertexConsumer vertexconsumer = p_377149_.getBuffer(RenderType.sunriseSunset());
        float f1 = ARGB.alphaFloat(p_365467_);
        vertexconsumer.addVertex(matrix4f, 0.0F, 100.0F, 0.0F).setColor(p_365467_);
        int i = ARGB.transparent(p_365467_);
        int j = 16;

        for (int k = 0; k <= 16; k++) {
            float f2 = (float)k * (float) (Math.PI * 2) / 16.0F;
            float f3 = Mth.sin(f2);
            float f4 = Mth.cos(f2);
            vertexconsumer.addVertex(matrix4f, f3 * 120.0F, f4 * 120.0F, -f4 * 40.0F * f1).setColor(i);
        }

        p_365939_.popPose();
    }

    private void buildEndSky(VertexConsumer p_377899_) {
        for (int i = 0; i < 6; i++) {
            Matrix4f matrix4f = new Matrix4f();
            switch (i) {
                case 1:
                    matrix4f.rotationX((float) (Math.PI / 2));
                    break;
                case 2:
                    matrix4f.rotationX((float) (-Math.PI / 2));
                    break;
                case 3:
                    matrix4f.rotationX((float) Math.PI);
                    break;
                case 4:
                    matrix4f.rotationZ((float) (Math.PI / 2));
                    break;
                case 5:
                    matrix4f.rotationZ((float) (-Math.PI / 2));
            }

            p_377899_.addVertex(matrix4f, -100.0F, -100.0F, -100.0F).setUv(0.0F, 0.0F).setColor(-14145496);
            p_377899_.addVertex(matrix4f, -100.0F, -100.0F, 100.0F).setUv(0.0F, 16.0F).setColor(-14145496);
            p_377899_.addVertex(matrix4f, 100.0F, -100.0F, 100.0F).setUv(16.0F, 16.0F).setColor(-14145496);
            p_377899_.addVertex(matrix4f, 100.0F, -100.0F, -100.0F).setUv(16.0F, 0.0F).setColor(-14145496);
        }
    }

    public void renderEndSky() {
        this.endSkyBuffer.drawWithRenderType(RenderType.endSky());
    }

    @Override
    public void close() {
        this.starBuffer.close();
        this.topSkyBuffer.close();
        this.bottomSkyBuffer.close();
        this.endSkyBuffer.close();
    }
}