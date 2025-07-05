package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeaconRenderer implements BlockEntityRenderer<BeaconBlockEntity> {
    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
    public static final int MAX_RENDER_Y = 1024;

    public BeaconRenderer(BlockEntityRendererProvider.Context p_173529_) {
    }

    public void render(BeaconBlockEntity p_112140_, float p_112141_, PoseStack p_112142_, MultiBufferSource p_112143_, int p_112144_, int p_112145_) {
        long i = p_112140_.getLevel().getGameTime();
        List<BeaconBlockEntity.BeaconBeamSection> list = p_112140_.getBeamSections();
        int j = 0;

        for (int k = 0; k < list.size(); k++) {
            BeaconBlockEntity.BeaconBeamSection beaconblockentity$beaconbeamsection = list.get(k);
            renderBeaconBeam(
                p_112142_,
                p_112143_,
                p_112141_,
                i,
                j,
                k == list.size() - 1 ? 1024 : beaconblockentity$beaconbeamsection.getHeight(),
                beaconblockentity$beaconbeamsection.getColor()
            );
            j += beaconblockentity$beaconbeamsection.getHeight();
        }
    }

    private static void renderBeaconBeam(
        PoseStack p_112177_, MultiBufferSource p_112178_, float p_112179_, long p_112180_, int p_112181_, int p_112182_, int p_344592_
    ) {
        renderBeaconBeam(p_112177_, p_112178_, BEAM_LOCATION, p_112179_, 1.0F, p_112180_, p_112181_, p_112182_, p_344592_, 0.2F, 0.25F);
    }

    public static void renderBeaconBeam(
        PoseStack p_112185_,
        MultiBufferSource p_112186_,
        ResourceLocation p_112187_,
        float p_112188_,
        float p_112189_,
        long p_112190_,
        int p_112191_,
        int p_112192_,
        int p_344215_,
        float p_112194_,
        float p_112195_
    ) {
        int i = p_112191_ + p_112192_;
        p_112185_.pushPose();
        p_112185_.translate(0.5, 0.0, 0.5);
        float f = (float)Math.floorMod(p_112190_, 40) + p_112188_;
        float f1 = p_112192_ < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        p_112185_.pushPose();
        p_112185_.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f3 = 0.0F;
        float f5 = 0.0F;
        float f6 = -p_112194_;
        float f7 = 0.0F;
        float f8 = 0.0F;
        float f9 = -p_112194_;
        float f10 = 0.0F;
        float f11 = 1.0F;
        float f12 = -1.0F + f2;
        float f13 = (float)p_112192_ * p_112189_ * (0.5F / p_112194_) + f12;
        renderPart(
            p_112185_,
            p_112186_.getBuffer(RenderType.beaconBeam(p_112187_, false)),
            p_344215_,
            p_112191_,
            i,
            0.0F,
            p_112194_,
            p_112194_,
            0.0F,
            f6,
            0.0F,
            0.0F,
            f9,
            0.0F,
            1.0F,
            f13,
            f12
        );
        p_112185_.popPose();
        f3 = -p_112195_;
        float f4 = -p_112195_;
        f5 = -p_112195_;
        f6 = -p_112195_;
        f10 = 0.0F;
        f11 = 1.0F;
        f12 = -1.0F + f2;
        f13 = (float)p_112192_ * p_112189_ + f12;
        renderPart(
            p_112185_,
            p_112186_.getBuffer(RenderType.beaconBeam(p_112187_, true)),
            ARGB.color(32, p_344215_),
            p_112191_,
            i,
            f3,
            f4,
            p_112195_,
            f5,
            f6,
            p_112195_,
            p_112195_,
            p_112195_,
            0.0F,
            1.0F,
            f13,
            f12
        );
        p_112185_.popPose();
    }

    private static void renderPart(
        PoseStack p_112156_,
        VertexConsumer p_112157_,
        int p_112162_,
        int p_112163_,
        int p_345221_,
        float p_112158_,
        float p_112159_,
        float p_112160_,
        float p_112161_,
        float p_112164_,
        float p_112165_,
        float p_112166_,
        float p_112167_,
        float p_112168_,
        float p_112169_,
        float p_112170_,
        float p_112171_
    ) {
        PoseStack.Pose posestack$pose = p_112156_.last();
        renderQuad(
            posestack$pose, p_112157_, p_112162_, p_112163_, p_345221_, p_112158_, p_112159_, p_112160_, p_112161_, p_112168_, p_112169_, p_112170_, p_112171_
        );
        renderQuad(
            posestack$pose, p_112157_, p_112162_, p_112163_, p_345221_, p_112166_, p_112167_, p_112164_, p_112165_, p_112168_, p_112169_, p_112170_, p_112171_
        );
        renderQuad(
            posestack$pose, p_112157_, p_112162_, p_112163_, p_345221_, p_112160_, p_112161_, p_112166_, p_112167_, p_112168_, p_112169_, p_112170_, p_112171_
        );
        renderQuad(
            posestack$pose, p_112157_, p_112162_, p_112163_, p_345221_, p_112164_, p_112165_, p_112158_, p_112159_, p_112168_, p_112169_, p_112170_, p_112171_
        );
    }

    private static void renderQuad(
        PoseStack.Pose p_332343_,
        VertexConsumer p_112122_,
        int p_112127_,
        int p_112128_,
        int p_345385_,
        float p_112123_,
        float p_112124_,
        float p_112125_,
        float p_112126_,
        float p_112129_,
        float p_112130_,
        float p_112131_,
        float p_112132_
    ) {
        addVertex(p_332343_, p_112122_, p_112127_, p_345385_, p_112123_, p_112124_, p_112130_, p_112131_);
        addVertex(p_332343_, p_112122_, p_112127_, p_112128_, p_112123_, p_112124_, p_112130_, p_112132_);
        addVertex(p_332343_, p_112122_, p_112127_, p_112128_, p_112125_, p_112126_, p_112129_, p_112132_);
        addVertex(p_332343_, p_112122_, p_112127_, p_345385_, p_112125_, p_112126_, p_112129_, p_112131_);
    }

    private static void addVertex(
        PoseStack.Pose p_334631_, VertexConsumer p_253894_, int p_254357_, int p_343267_, float p_253871_, float p_253841_, float p_254568_, float p_254361_
    ) {
        p_253894_.addVertex(p_334631_, p_253871_, (float)p_343267_, p_253841_)
            .setColor(p_254357_)
            .setUv(p_254568_, p_254361_)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(15728880)
            .setNormal(p_334631_, 0.0F, 1.0F, 0.0F);
    }

    public boolean shouldRenderOffScreen(BeaconBlockEntity p_112138_) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    public boolean shouldRender(BeaconBlockEntity p_173531_, Vec3 p_173532_) {
        return Vec3.atCenterOf(p_173531_.getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(p_173532_.multiply(1.0, 0.0, 1.0), (double)this.getViewDistance());
    }
}