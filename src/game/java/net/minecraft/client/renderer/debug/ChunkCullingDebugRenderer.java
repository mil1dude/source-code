package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class ChunkCullingDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    public static final Direction[] DIRECTIONS = Direction.values();
    private final Minecraft minecraft;

    public ChunkCullingDebugRenderer(Minecraft p_365943_) {
        this.minecraft = p_365943_;
    }

    @Override
    public void render(PoseStack p_360717_, MultiBufferSource p_369170_, double p_361626_, double p_369161_, double p_367114_) {
        LevelRenderer levelrenderer = this.minecraft.levelRenderer;
        if (this.minecraft.sectionPath || this.minecraft.sectionVisibility) {
            SectionOcclusionGraph sectionocclusiongraph = levelrenderer.getSectionOcclusionGraph();

            for (SectionRenderDispatcher.RenderSection sectionrenderdispatcher$rendersection : levelrenderer.getVisibleSections()) {
                SectionOcclusionGraph.Node sectionocclusiongraph$node = sectionocclusiongraph.getNode(sectionrenderdispatcher$rendersection);
                if (sectionocclusiongraph$node != null) {
                    BlockPos blockpos = sectionrenderdispatcher$rendersection.getOrigin();
                    p_360717_.pushPose();
                    p_360717_.translate(
                        (double)blockpos.getX() - p_361626_, (double)blockpos.getY() - p_369161_, (double)blockpos.getZ() - p_367114_
                    );
                    Matrix4f matrix4f = p_360717_.last().pose();
                    if (this.minecraft.sectionPath) {
                        VertexConsumer vertexconsumer = p_369170_.getBuffer(RenderType.lines());
                        int i = sectionocclusiongraph$node.step == 0 ? 0 : Mth.hsvToRgb((float)sectionocclusiongraph$node.step / 50.0F, 0.9F, 0.9F);
                        int j = i >> 16 & 0xFF;
                        int k = i >> 8 & 0xFF;
                        int l = i & 0xFF;

                        for (int i1 = 0; i1 < DIRECTIONS.length; i1++) {
                            if (sectionocclusiongraph$node.hasSourceDirection(i1)) {
                                Direction direction = DIRECTIONS[i1];
                                vertexconsumer.addVertex(matrix4f, 8.0F, 8.0F, 8.0F)
                                    .setColor(j, k, l, 255)
                                    .setNormal((float)direction.getStepX(), (float)direction.getStepY(), (float)direction.getStepZ());
                                vertexconsumer.addVertex(
                                        matrix4f,
                                        (float)(8 - 16 * direction.getStepX()),
                                        (float)(8 - 16 * direction.getStepY()),
                                        (float)(8 - 16 * direction.getStepZ())
                                    )
                                    .setColor(j, k, l, 255)
                                    .setNormal((float)direction.getStepX(), (float)direction.getStepY(), (float)direction.getStepZ());
                            }
                        }
                    }

                    if (this.minecraft.sectionVisibility && sectionrenderdispatcher$rendersection.getCompiled().hasRenderableLayers()) {
                        VertexConsumer vertexconsumer3 = p_369170_.getBuffer(RenderType.lines());
                        int j1 = 0;

                        for (Direction direction2 : DIRECTIONS) {
                            for (Direction direction1 : DIRECTIONS) {
                                boolean flag = sectionrenderdispatcher$rendersection.getCompiled().facesCanSeeEachother(direction2, direction1);
                                if (!flag) {
                                    j1++;
                                    vertexconsumer3.addVertex(
                                            matrix4f,
                                            (float)(8 + 8 * direction2.getStepX()),
                                            (float)(8 + 8 * direction2.getStepY()),
                                            (float)(8 + 8 * direction2.getStepZ())
                                        )
                                        .setColor(255, 0, 0, 255)
                                        .setNormal((float)direction2.getStepX(), (float)direction2.getStepY(), (float)direction2.getStepZ());
                                    vertexconsumer3.addVertex(
                                            matrix4f,
                                            (float)(8 + 8 * direction1.getStepX()),
                                            (float)(8 + 8 * direction1.getStepY()),
                                            (float)(8 + 8 * direction1.getStepZ())
                                        )
                                        .setColor(255, 0, 0, 255)
                                        .setNormal((float)direction1.getStepX(), (float)direction1.getStepY(), (float)direction1.getStepZ());
                                }
                            }
                        }

                        if (j1 > 0) {
                            VertexConsumer vertexconsumer4 = p_369170_.getBuffer(RenderType.debugQuads());
                            float f = 0.5F;
                            float f1 = 0.2F;
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 0.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 15.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 15.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                            vertexconsumer4.addVertex(matrix4f, 0.5F, 0.5F, 15.5F).setColor(0.9F, 0.9F, 0.0F, 0.2F);
                        }
                    }

                    p_360717_.popPose();
                }
            }
        }

        Frustum frustum = levelrenderer.getCapturedFrustum();
        if (frustum != null) {
            p_360717_.pushPose();
            p_360717_.translate((float)(frustum.getCamX() - p_361626_), (float)(frustum.getCamY() - p_369161_), (float)(frustum.getCamZ() - p_367114_));
            Matrix4f matrix4f1 = p_360717_.last().pose();
            Vector4f[] avector4f = frustum.getFrustumPoints();
            VertexConsumer vertexconsumer1 = p_369170_.getBuffer(RenderType.debugQuads());
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 0, 1, 2, 3, 0, 1, 1);
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 4, 5, 6, 7, 1, 0, 0);
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 0, 1, 5, 4, 1, 1, 0);
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 2, 3, 7, 6, 0, 0, 1);
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 0, 4, 7, 3, 0, 1, 0);
            this.addFrustumQuad(vertexconsumer1, matrix4f1, avector4f, 1, 5, 6, 2, 1, 0, 1);
            VertexConsumer vertexconsumer2 = p_369170_.getBuffer(RenderType.lines());
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[0]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[1]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[1]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[2]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[2]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[3]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[3]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[0]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[4]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[5]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[5]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[6]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[6]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[7]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[7]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[4]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[0]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[4]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[1]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[5]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[2]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[6]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[3]);
            this.addFrustumVertex(vertexconsumer2, matrix4f1, avector4f[7]);
            p_360717_.popPose();
        }
    }

    private void addFrustumVertex(VertexConsumer p_361872_, Matrix4f p_369368_, Vector4f p_365578_) {
        p_361872_.addVertex(p_369368_, p_365578_.x(), p_365578_.y(), p_365578_.z()).setColor(-16777216).setNormal(0.0F, 0.0F, -1.0F);
    }

    private void addFrustumQuad(
        VertexConsumer p_365841_,
        Matrix4f p_367573_,
        Vector4f[] p_369613_,
        int p_360822_,
        int p_362980_,
        int p_367860_,
        int p_360867_,
        int p_367084_,
        int p_367738_,
        int p_367810_
    ) {
        float f = 0.25F;
        p_365841_.addVertex(p_367573_, p_369613_[p_360822_].x(), p_369613_[p_360822_].y(), p_369613_[p_360822_].z())
            .setColor((float)p_367084_, (float)p_367738_, (float)p_367810_, 0.25F);
        p_365841_.addVertex(p_367573_, p_369613_[p_362980_].x(), p_369613_[p_362980_].y(), p_369613_[p_362980_].z())
            .setColor((float)p_367084_, (float)p_367738_, (float)p_367810_, 0.25F);
        p_365841_.addVertex(p_367573_, p_369613_[p_367860_].x(), p_369613_[p_367860_].y(), p_369613_[p_367860_].z())
            .setColor((float)p_367084_, (float)p_367738_, (float)p_367810_, 0.25F);
        p_365841_.addVertex(p_367573_, p_369613_[p_360867_].x(), p_369613_[p_360867_].y(), p_369613_[p_360867_].z())
            .setColor((float)p_367084_, (float)p_367738_, (float)p_367810_, 0.25F);
    }
}