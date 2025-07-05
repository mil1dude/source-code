package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ShapeRenderer {
    public static void renderShape(
        PoseStack p_362127_, VertexConsumer p_362290_, VoxelShape p_362784_, double p_360742_, double p_360770_, double p_368227_, int p_362030_
    ) {
        PoseStack.Pose posestack$pose = p_362127_.last();
        p_362784_.forAllEdges(
            (p_368095_, p_361366_, p_363660_, p_361928_, p_364145_, p_361311_) -> {
                Vector3f vector3f = new Vector3f((float)(p_361928_ - p_368095_), (float)(p_364145_ - p_361366_), (float)(p_361311_ - p_363660_)).normalize();
                p_362290_.addVertex(posestack$pose, (float)(p_368095_ + p_360742_), (float)(p_361366_ + p_360770_), (float)(p_363660_ + p_368227_))
                    .setColor(p_362030_)
                    .setNormal(posestack$pose, vector3f);
                p_362290_.addVertex(posestack$pose, (float)(p_361928_ + p_360742_), (float)(p_364145_ + p_360770_), (float)(p_361311_ + p_368227_))
                    .setColor(p_362030_)
                    .setNormal(posestack$pose, vector3f);
            }
        );
    }

    public static void renderLineBox(
        PoseStack p_367242_, VertexConsumer p_368944_, AABB p_369230_, float p_364083_, float p_362021_, float p_362124_, float p_367649_
    ) {
        renderLineBox(
            p_367242_,
            p_368944_,
            p_369230_.minX,
            p_369230_.minY,
            p_369230_.minZ,
            p_369230_.maxX,
            p_369230_.maxY,
            p_369230_.maxZ,
            p_364083_,
            p_362021_,
            p_362124_,
            p_367649_,
            p_364083_,
            p_362021_,
            p_362124_
        );
    }

    public static void renderLineBox(
        PoseStack p_366452_,
        VertexConsumer p_365817_,
        double p_362632_,
        double p_362535_,
        double p_368825_,
        double p_363850_,
        double p_361520_,
        double p_367127_,
        float p_363525_,
        float p_365172_,
        float p_361957_,
        float p_362174_
    ) {
        renderLineBox(
            p_366452_,
            p_365817_,
            p_362632_,
            p_362535_,
            p_368825_,
            p_363850_,
            p_361520_,
            p_367127_,
            p_363525_,
            p_365172_,
            p_361957_,
            p_362174_,
            p_363525_,
            p_365172_,
            p_361957_
        );
    }

    public static void renderLineBox(
        PoseStack p_363808_,
        VertexConsumer p_365769_,
        double p_364950_,
        double p_364934_,
        double p_370117_,
        double p_365763_,
        double p_361638_,
        double p_360926_,
        float p_362396_,
        float p_361053_,
        float p_363674_,
        float p_366690_,
        float p_365217_,
        float p_367742_,
        float p_364278_
    ) {
        PoseStack.Pose posestack$pose = p_363808_.last();
        float f = (float)p_364950_;
        float f1 = (float)p_364934_;
        float f2 = (float)p_370117_;
        float f3 = (float)p_365763_;
        float f4 = (float)p_361638_;
        float f5 = (float)p_360926_;
        p_365769_.addVertex(posestack$pose, f, f1, f2).setColor(p_362396_, p_367742_, p_364278_, p_366690_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f3, f1, f2).setColor(p_362396_, p_367742_, p_364278_, p_366690_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f, f1, f2).setColor(p_365217_, p_361053_, p_364278_, p_366690_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f, f4, f2).setColor(p_365217_, p_361053_, p_364278_, p_366690_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f, f1, f2).setColor(p_365217_, p_367742_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
        p_365769_.addVertex(posestack$pose, f, f1, f5).setColor(p_365217_, p_367742_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
        p_365769_.addVertex(posestack$pose, f3, f1, f2).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f3, f4, f2).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f3, f4, f2).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, -1.0F, 0.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f, f4, f2).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, -1.0F, 0.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f, f4, f2).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
        p_365769_.addVertex(posestack$pose, f, f4, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
        p_365769_.addVertex(posestack$pose, f, f4, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f, f1, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, -1.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f, f1, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f3, f1, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f3, f1, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 0.0F, -1.0F);
        p_365769_.addVertex(posestack$pose, f3, f1, f2).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 0.0F, -1.0F);
        p_365769_.addVertex(posestack$pose, f, f4, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f3, f4, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 1.0F, 0.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f3, f1, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f3, f4, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 1.0F, 0.0F);
        p_365769_.addVertex(posestack$pose, f3, f4, f2).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
        p_365769_.addVertex(posestack$pose, f3, f4, f5).setColor(p_362396_, p_361053_, p_363674_, p_366690_).setNormal(posestack$pose, 0.0F, 0.0F, 1.0F);
    }

    public static void addChainedFilledBoxVertices(
        PoseStack p_364970_,
        VertexConsumer p_368145_,
        double p_361406_,
        double p_360919_,
        double p_368183_,
        double p_369129_,
        double p_366679_,
        double p_368318_,
        float p_365390_,
        float p_360927_,
        float p_369810_,
        float p_368692_
    ) {
        addChainedFilledBoxVertices(
            p_364970_,
            p_368145_,
            (float)p_361406_,
            (float)p_360919_,
            (float)p_368183_,
            (float)p_369129_,
            (float)p_366679_,
            (float)p_368318_,
            p_365390_,
            p_360927_,
            p_369810_,
            p_368692_
        );
    }

    public static void addChainedFilledBoxVertices(
        PoseStack p_363033_,
        VertexConsumer p_368281_,
        float p_363400_,
        float p_368959_,
        float p_368839_,
        float p_363598_,
        float p_369683_,
        float p_364534_,
        float p_369605_,
        float p_364542_,
        float p_367457_,
        float p_362117_
    ) {
        Matrix4f matrix4f = p_363033_.last().pose();
        p_368281_.addVertex(matrix4f, p_363400_, p_368959_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_368959_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_368959_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_368959_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_369683_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_369683_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_369683_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_368959_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_369683_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_368959_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_368959_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_368959_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_369683_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_369683_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_369683_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_368959_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_369683_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_368959_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_368959_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_368959_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_368959_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_368959_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_368959_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_369683_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_369683_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363400_, p_369683_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_369683_, p_368839_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_369683_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_369683_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
        p_368281_.addVertex(matrix4f, p_363598_, p_369683_, p_364534_).setColor(p_369605_, p_364542_, p_367457_, p_362117_);
    }

    public static void renderFace(
        PoseStack p_361398_,
        VertexConsumer p_368208_,
        Direction p_364940_,
        float p_361821_,
        float p_366736_,
        float p_364720_,
        float p_369092_,
        float p_365269_,
        float p_361985_,
        float p_366223_,
        float p_362144_,
        float p_364969_,
        float p_369822_
    ) {
        Matrix4f matrix4f = p_361398_.last().pose();
        switch (p_364940_) {
            case DOWN:
                p_368208_.addVertex(matrix4f, p_361821_, p_366736_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_366736_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_366736_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_361821_, p_366736_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                break;
            case UP:
                p_368208_.addVertex(matrix4f, p_361821_, p_365269_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_361821_, p_365269_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_365269_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_365269_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                break;
            case NORTH:
                p_368208_.addVertex(matrix4f, p_361821_, p_366736_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_361821_, p_365269_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_365269_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_366736_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                break;
            case SOUTH:
                p_368208_.addVertex(matrix4f, p_361821_, p_366736_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_366736_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_365269_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_361821_, p_365269_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                break;
            case WEST:
                p_368208_.addVertex(matrix4f, p_361821_, p_366736_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_361821_, p_366736_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_361821_, p_365269_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_361821_, p_365269_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                break;
            case EAST:
                p_368208_.addVertex(matrix4f, p_369092_, p_366736_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_365269_, p_364720_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_365269_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
                p_368208_.addVertex(matrix4f, p_369092_, p_366736_, p_361985_).setColor(p_366223_, p_362144_, p_364969_, p_369822_);
        }
    }

    public static void renderVector(PoseStack p_366769_, VertexConsumer p_362011_, Vector3f p_367001_, Vec3 p_367730_, int p_363783_) {
        PoseStack.Pose posestack$pose = p_366769_.last();
        p_362011_.addVertex(posestack$pose, p_367001_)
            .setColor(p_363783_)
            .setNormal(posestack$pose, (float)p_367730_.x, (float)p_367730_.y, (float)p_367730_.z);
        p_362011_.addVertex(
                posestack$pose,
                (float)((double)p_367001_.x() + p_367730_.x),
                (float)((double)p_367001_.y() + p_367730_.y),
                (float)((double)p_367001_.z() + p_367730_.z)
            )
            .setColor(p_363783_)
            .setNormal(posestack$pose, (float)p_367730_.x, (float)p_367730_.y, (float)p_367730_.z);
    }
}