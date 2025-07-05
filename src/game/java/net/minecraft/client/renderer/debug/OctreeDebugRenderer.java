package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Octree;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;

@OnlyIn(Dist.CLIENT)
public class OctreeDebugRenderer {
    private final Minecraft minecraft;

    public OctreeDebugRenderer(Minecraft p_368722_) {
        this.minecraft = p_368722_;
    }

    public void render(PoseStack p_368352_, Frustum p_366121_, MultiBufferSource p_363548_, double p_363763_, double p_367605_, double p_361347_) {
        Octree octree = this.minecraft.levelRenderer.getSectionOcclusionGraph().getOctree();
        MutableInt mutableint = new MutableInt(0);
        octree.visitNodes(
            (p_367461_, p_361624_, p_368817_, p_363024_) -> this.renderNode(
                    p_367461_, p_368352_, p_363548_, p_363763_, p_367605_, p_361347_, p_368817_, p_361624_, mutableint, p_363024_
                ),
            p_366121_,
            32
        );
    }

    private void renderNode(
        Octree.Node p_365618_,
        PoseStack p_361623_,
        MultiBufferSource p_362449_,
        double p_368967_,
        double p_363341_,
        double p_365959_,
        int p_362077_,
        boolean p_364236_,
        MutableInt p_366104_,
        boolean p_362959_
    ) {
        AABB aabb = p_365618_.getAABB();
        double d0 = aabb.getXsize();
        long i = Math.round(d0 / 16.0);
        if (i == 1L) {
            p_366104_.add(1);
            double d1 = aabb.getCenter().x;
            double d2 = aabb.getCenter().y;
            double d3 = aabb.getCenter().z;
            int k = p_362959_ ? -16711936 : -1;
            DebugRenderer.renderFloatingText(p_361623_, p_362449_, String.valueOf(p_366104_.getValue()), d1, d2, d3, k, 0.3F);
        }

        VertexConsumer vertexconsumer = p_362449_.getBuffer(RenderType.lines());
        long j = i + 5L;
        ShapeRenderer.renderLineBox(
            p_361623_,
            vertexconsumer,
            aabb.deflate(0.1 * (double)p_362077_).move(-p_368967_, -p_363341_, -p_365959_),
            getColorComponent(j, 0.3F),
            getColorComponent(j, 0.8F),
            getColorComponent(j, 0.5F),
            p_364236_ ? 0.4F : 1.0F
        );
    }

    private static float getColorComponent(long p_368917_, float p_363248_) {
        float f = 0.1F;
        return Mth.frac(p_363248_ * (float)p_368917_) * 0.9F + 0.1F;
    }
}