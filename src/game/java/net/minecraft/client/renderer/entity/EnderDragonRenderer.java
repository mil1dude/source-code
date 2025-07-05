package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.dragon.EnderDragonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class EnderDragonRenderer extends EntityRenderer<EnderDragon, EnderDragonRenderState> {
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_crystal/end_crystal_beam.png");
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation DRAGON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation DRAGON_EYES_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_eyes.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(DRAGON_LOCATION);
    private static final RenderType DECAL = RenderType.entityDecal(DRAGON_LOCATION);
    private static final RenderType EYES = RenderType.eyes(DRAGON_EYES_LOCATION);
    private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final EnderDragonModel model;

    public EnderDragonRenderer(EntityRendererProvider.Context p_173973_) {
        super(p_173973_);
        this.shadowRadius = 0.5F;
        this.model = new EnderDragonModel(p_173973_.bakeLayer(ModelLayers.ENDER_DRAGON));
    }

    public void render(EnderDragonRenderState p_361915_, PoseStack p_114202_, MultiBufferSource p_114203_, int p_114204_) {
        p_114202_.pushPose();
        float f = p_361915_.getHistoricalPos(7).yRot();
        float f1 = (float)(p_361915_.getHistoricalPos(5).y() - p_361915_.getHistoricalPos(10).y());
        p_114202_.mulPose(Axis.YP.rotationDegrees(-f));
        p_114202_.mulPose(Axis.XP.rotationDegrees(f1 * 10.0F));
        p_114202_.translate(0.0F, 0.0F, 1.0F);
        p_114202_.scale(-1.0F, -1.0F, 1.0F);
        p_114202_.translate(0.0F, -1.501F, 0.0F);
        this.model.setupAnim(p_361915_);
        if (p_361915_.deathTime > 0.0F) {
            float f2 = p_361915_.deathTime / 200.0F;
            int i = ARGB.color(Mth.floor(f2 * 255.0F), -1);
            VertexConsumer vertexconsumer = p_114203_.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION));
            this.model.renderToBuffer(p_114202_, vertexconsumer, p_114204_, OverlayTexture.NO_OVERLAY, i);
            VertexConsumer vertexconsumer1 = p_114203_.getBuffer(DECAL);
            this.model.renderToBuffer(p_114202_, vertexconsumer1, p_114204_, OverlayTexture.pack(0.0F, p_361915_.hasRedOverlay));
        } else {
            VertexConsumer vertexconsumer2 = p_114203_.getBuffer(RENDER_TYPE);
            this.model.renderToBuffer(p_114202_, vertexconsumer2, p_114204_, OverlayTexture.pack(0.0F, p_361915_.hasRedOverlay));
        }

        VertexConsumer vertexconsumer3 = p_114203_.getBuffer(EYES);
        this.model.renderToBuffer(p_114202_, vertexconsumer3, p_114204_, OverlayTexture.NO_OVERLAY);
        if (p_361915_.deathTime > 0.0F) {
            float f3 = p_361915_.deathTime / 200.0F;
            p_114202_.pushPose();
            p_114202_.translate(0.0F, -1.0F, -2.0F);
            renderRays(p_114202_, f3, p_114203_.getBuffer(RenderType.dragonRays()));
            renderRays(p_114202_, f3, p_114203_.getBuffer(RenderType.dragonRaysDepth()));
            p_114202_.popPose();
        }

        p_114202_.popPose();
        if (p_361915_.beamOffset != null) {
            renderCrystalBeams(
                (float)p_361915_.beamOffset.x,
                (float)p_361915_.beamOffset.y,
                (float)p_361915_.beamOffset.z,
                p_361915_.ageInTicks,
                p_114202_,
                p_114203_,
                p_114204_
            );
        }

        super.render(p_361915_, p_114202_, p_114203_, p_114204_);
    }

    private static void renderRays(PoseStack p_345439_, float p_344944_, VertexConsumer p_344181_) {
        p_345439_.pushPose();
        float f = Math.min(p_344944_ > 0.8F ? (p_344944_ - 0.8F) / 0.2F : 0.0F, 1.0F);
        int i = ARGB.colorFromFloat(1.0F - f, 1.0F, 1.0F, 1.0F);
        int j = 16711935;
        RandomSource randomsource = RandomSource.create(432L);
        Vector3f vector3f = new Vector3f();
        Vector3f vector3f1 = new Vector3f();
        Vector3f vector3f2 = new Vector3f();
        Vector3f vector3f3 = new Vector3f();
        Quaternionf quaternionf = new Quaternionf();
        int k = Mth.floor((p_344944_ + p_344944_ * p_344944_) / 2.0F * 60.0F);

        for (int l = 0; l < k; l++) {
            quaternionf.rotationXYZ(
                    randomsource.nextFloat() * (float) (Math.PI * 2),
                    randomsource.nextFloat() * (float) (Math.PI * 2),
                    randomsource.nextFloat() * (float) (Math.PI * 2)
                )
                .rotateXYZ(
                    randomsource.nextFloat() * (float) (Math.PI * 2),
                    randomsource.nextFloat() * (float) (Math.PI * 2),
                    randomsource.nextFloat() * (float) (Math.PI * 2) + p_344944_ * (float) (Math.PI / 2)
                );
            p_345439_.mulPose(quaternionf);
            float f1 = randomsource.nextFloat() * 20.0F + 5.0F + f * 10.0F;
            float f2 = randomsource.nextFloat() * 2.0F + 1.0F + f * 2.0F;
            vector3f1.set(-HALF_SQRT_3 * f2, f1, -0.5F * f2);
            vector3f2.set(HALF_SQRT_3 * f2, f1, -0.5F * f2);
            vector3f3.set(0.0F, f1, f2);
            PoseStack.Pose posestack$pose = p_345439_.last();
            p_344181_.addVertex(posestack$pose, vector3f).setColor(i);
            p_344181_.addVertex(posestack$pose, vector3f1).setColor(16711935);
            p_344181_.addVertex(posestack$pose, vector3f2).setColor(16711935);
            p_344181_.addVertex(posestack$pose, vector3f).setColor(i);
            p_344181_.addVertex(posestack$pose, vector3f2).setColor(16711935);
            p_344181_.addVertex(posestack$pose, vector3f3).setColor(16711935);
            p_344181_.addVertex(posestack$pose, vector3f).setColor(i);
            p_344181_.addVertex(posestack$pose, vector3f3).setColor(16711935);
            p_344181_.addVertex(posestack$pose, vector3f1).setColor(16711935);
        }

        p_345439_.popPose();
    }

    public static void renderCrystalBeams(
        float p_114188_, float p_114189_, float p_114190_, float p_114191_, PoseStack p_114193_, MultiBufferSource p_114194_, int p_114192_
    ) {
        float f = Mth.sqrt(p_114188_ * p_114188_ + p_114190_ * p_114190_);
        float f1 = Mth.sqrt(p_114188_ * p_114188_ + p_114189_ * p_114189_ + p_114190_ * p_114190_);
        p_114193_.pushPose();
        p_114193_.translate(0.0F, 2.0F, 0.0F);
        p_114193_.mulPose(Axis.YP.rotation((float)(-Math.atan2((double)p_114190_, (double)p_114188_)) - (float) (Math.PI / 2)));
        p_114193_.mulPose(Axis.XP.rotation((float)(-Math.atan2((double)f, (double)p_114189_)) - (float) (Math.PI / 2)));
        VertexConsumer vertexconsumer = p_114194_.getBuffer(BEAM);
        float f2 = 0.0F - p_114191_ * 0.01F;
        float f3 = f1 / 32.0F - p_114191_ * 0.01F;
        int i = 8;
        float f4 = 0.0F;
        float f5 = 0.75F;
        float f6 = 0.0F;
        PoseStack.Pose posestack$pose = p_114193_.last();

        for (int j = 1; j <= 8; j++) {
            float f7 = Mth.sin((float)j * (float) (Math.PI * 2) / 8.0F) * 0.75F;
            float f8 = Mth.cos((float)j * (float) (Math.PI * 2) / 8.0F) * 0.75F;
            float f9 = (float)j / 8.0F;
            vertexconsumer.addVertex(posestack$pose, f4 * 0.2F, f5 * 0.2F, 0.0F)
                .setColor(-16777216)
                .setUv(f6, f2)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(p_114192_)
                .setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
            vertexconsumer.addVertex(posestack$pose, f4, f5, f1)
                .setColor(-1)
                .setUv(f6, f3)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(p_114192_)
                .setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
            vertexconsumer.addVertex(posestack$pose, f7, f8, f1)
                .setColor(-1)
                .setUv(f9, f3)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(p_114192_)
                .setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
            vertexconsumer.addVertex(posestack$pose, f7 * 0.2F, f8 * 0.2F, 0.0F)
                .setColor(-16777216)
                .setUv(f9, f2)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(p_114192_)
                .setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
            f4 = f7;
            f5 = f8;
            f6 = f9;
        }

        p_114193_.popPose();
    }

    public EnderDragonRenderState createRenderState() {
        return new EnderDragonRenderState();
    }

    public void extractRenderState(EnderDragon p_367718_, EnderDragonRenderState p_360720_, float p_367927_) {
        super.extractRenderState(p_367718_, p_360720_, p_367927_);
        p_360720_.flapTime = Mth.lerp(p_367927_, p_367718_.oFlapTime, p_367718_.flapTime);
        p_360720_.deathTime = p_367718_.dragonDeathTime > 0 ? (float)p_367718_.dragonDeathTime + p_367927_ : 0.0F;
        p_360720_.hasRedOverlay = p_367718_.hurtTime > 0;
        EndCrystal endcrystal = p_367718_.nearestCrystal;
        if (endcrystal != null) {
            Vec3 vec3 = endcrystal.getPosition(p_367927_).add(0.0, (double)EndCrystalRenderer.getY((float)endcrystal.time + p_367927_), 0.0);
            p_360720_.beamOffset = vec3.subtract(p_367718_.getPosition(p_367927_));
        } else {
            p_360720_.beamOffset = null;
        }

        DragonPhaseInstance dragonphaseinstance = p_367718_.getPhaseManager().getCurrentPhase();
        p_360720_.isLandingOrTakingOff = dragonphaseinstance == EnderDragonPhase.LANDING || dragonphaseinstance == EnderDragonPhase.TAKEOFF;
        p_360720_.isSitting = dragonphaseinstance.isSitting();
        BlockPos blockpos = p_367718_.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(p_367718_.getFightOrigin()));
        p_360720_.distanceToEgg = blockpos.distToCenterSqr(p_367718_.position());
        p_360720_.partialTicks = p_367718_.isDeadOrDying() ? 0.0F : p_367927_;
        p_360720_.flightHistory.copyFrom(p_367718_.flightHistory);
    }

    protected boolean affectedByCulling(EnderDragon p_362111_) {
        return false;
    }
}