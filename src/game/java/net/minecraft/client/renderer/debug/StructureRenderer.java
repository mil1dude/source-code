package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<ResourceKey<Level>, Map<String, BoundingBox>> postMainBoxes = Maps.newIdentityHashMap();
    private final Map<ResourceKey<Level>, Map<String, StructuresDebugPayload.PieceInfo>> postPieces = Maps.newIdentityHashMap();
    private static final int MAX_RENDER_DIST = 500;

    public StructureRenderer(Minecraft p_113680_) {
        this.minecraft = p_113680_;
    }

    @Override
    public void render(PoseStack p_113688_, MultiBufferSource p_113689_, double p_113690_, double p_113691_, double p_113692_) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        ResourceKey<Level> resourcekey = this.minecraft.level.dimension();
        BlockPos blockpos = BlockPos.containing(camera.getPosition().x, 0.0, camera.getPosition().z);
        VertexConsumer vertexconsumer = p_113689_.getBuffer(RenderType.lines());
        if (this.postMainBoxes.containsKey(resourcekey)) {
            for (BoundingBox boundingbox : this.postMainBoxes.get(resourcekey).values()) {
                if (blockpos.closerThan(boundingbox.getCenter(), 500.0)) {
                    ShapeRenderer.renderLineBox(
                        p_113688_,
                        vertexconsumer,
                        (double)boundingbox.minX() - p_113690_,
                        (double)boundingbox.minY() - p_113691_,
                        (double)boundingbox.minZ() - p_113692_,
                        (double)(boundingbox.maxX() + 1) - p_113690_,
                        (double)(boundingbox.maxY() + 1) - p_113691_,
                        (double)(boundingbox.maxZ() + 1) - p_113692_,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F
                    );
                }
            }
        }

        Map<String, StructuresDebugPayload.PieceInfo> map = this.postPieces.get(resourcekey);
        if (map != null) {
            for (StructuresDebugPayload.PieceInfo structuresdebugpayload$pieceinfo : map.values()) {
                BoundingBox boundingbox1 = structuresdebugpayload$pieceinfo.boundingBox();
                if (blockpos.closerThan(boundingbox1.getCenter(), 500.0)) {
                    if (structuresdebugpayload$pieceinfo.isStart()) {
                        ShapeRenderer.renderLineBox(
                            p_113688_,
                            vertexconsumer,
                            (double)boundingbox1.minX() - p_113690_,
                            (double)boundingbox1.minY() - p_113691_,
                            (double)boundingbox1.minZ() - p_113692_,
                            (double)(boundingbox1.maxX() + 1) - p_113690_,
                            (double)(boundingbox1.maxY() + 1) - p_113691_,
                            (double)(boundingbox1.maxZ() + 1) - p_113692_,
                            0.0F,
                            1.0F,
                            0.0F,
                            1.0F,
                            0.0F,
                            1.0F,
                            0.0F
                        );
                    } else {
                        ShapeRenderer.renderLineBox(
                            p_113688_,
                            vertexconsumer,
                            (double)boundingbox1.minX() - p_113690_,
                            (double)boundingbox1.minY() - p_113691_,
                            (double)boundingbox1.minZ() - p_113692_,
                            (double)(boundingbox1.maxX() + 1) - p_113690_,
                            (double)(boundingbox1.maxY() + 1) - p_113691_,
                            (double)(boundingbox1.maxZ() + 1) - p_113692_,
                            0.0F,
                            0.0F,
                            1.0F,
                            1.0F,
                            0.0F,
                            0.0F,
                            1.0F
                        );
                    }
                }
            }
        }
    }

    public void addBoundingBox(BoundingBox p_113683_, List<StructuresDebugPayload.PieceInfo> p_113684_, ResourceKey<Level> p_297588_) {
        this.postMainBoxes.computeIfAbsent(p_297588_, p_299944_ -> new HashMap<>()).put(p_113683_.toString(), p_113683_);
        Map<String, StructuresDebugPayload.PieceInfo> map = this.postPieces.computeIfAbsent(p_297588_, p_298617_ -> new HashMap<>());

        for (StructuresDebugPayload.PieceInfo structuresdebugpayload$pieceinfo : p_113684_) {
            map.put(structuresdebugpayload$pieceinfo.boundingBox().toString(), structuresdebugpayload$pieceinfo);
        }
    }

    @Override
    public void clear() {
        this.postMainBoxes.clear();
        this.postPieces.clear();
    }
}