package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class HeightMapRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int CHUNK_DIST = 2;
    private static final float BOX_HEIGHT = 0.09375F;

    public HeightMapRenderer(Minecraft p_113572_) {
        this.minecraft = p_113572_;
    }

    @Override
    public void render(PoseStack p_113576_, MultiBufferSource p_113577_, double p_113578_, double p_113579_, double p_113580_) {
        LevelAccessor levelaccessor = this.minecraft.level;
        VertexConsumer vertexconsumer = p_113577_.getBuffer(RenderType.debugFilledBox());
        BlockPos blockpos = BlockPos.containing(p_113578_, 0.0, p_113580_);

        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                ChunkAccess chunkaccess = levelaccessor.getChunk(blockpos.offset(i * 16, 0, j * 16));

                for (Entry<Heightmap.Types, Heightmap> entry : chunkaccess.getHeightmaps()) {
                    Heightmap.Types heightmap$types = entry.getKey();
                    ChunkPos chunkpos = chunkaccess.getPos();
                    Vector3f vector3f = this.getColor(heightmap$types);

                    for (int k = 0; k < 16; k++) {
                        for (int l = 0; l < 16; l++) {
                            int i1 = SectionPos.sectionToBlockCoord(chunkpos.x, k);
                            int j1 = SectionPos.sectionToBlockCoord(chunkpos.z, l);
                            float f = (float)(
                                (double)((float)levelaccessor.getHeight(heightmap$types, i1, j1) + (float)heightmap$types.ordinal() * 0.09375F) - p_113579_
                            );
                            ShapeRenderer.addChainedFilledBoxVertices(
                                p_113576_,
                                vertexconsumer,
                                (double)((float)i1 + 0.25F) - p_113578_,
                                (double)f,
                                (double)((float)j1 + 0.25F) - p_113580_,
                                (double)((float)i1 + 0.75F) - p_113578_,
                                (double)(f + 0.09375F),
                                (double)((float)j1 + 0.75F) - p_113580_,
                                vector3f.x(),
                                vector3f.y(),
                                vector3f.z(),
                                1.0F
                            );
                        }
                    }
                }
            }
        }
    }

    private Vector3f getColor(Heightmap.Types p_113574_) {
        return switch (p_113574_) {
            case WORLD_SURFACE_WG -> new Vector3f(1.0F, 1.0F, 0.0F);
            case OCEAN_FLOOR_WG -> new Vector3f(1.0F, 0.0F, 1.0F);
            case WORLD_SURFACE -> new Vector3f(0.0F, 0.7F, 0.0F);
            case OCEAN_FLOOR -> new Vector3f(0.0F, 0.0F, 0.5F);
            case MOTION_BLOCKING -> new Vector3f(0.0F, 0.3F, 0.3F);
            case MOTION_BLOCKING_NO_LEAVES -> new Vector3f(0.0F, 0.5F, 0.5F);
        };
    }
}