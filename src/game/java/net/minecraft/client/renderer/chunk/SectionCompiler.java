package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SectionCompiler {
    private final BlockRenderDispatcher blockRenderer;
    private final BlockEntityRenderDispatcher blockEntityRenderer;

    public SectionCompiler(BlockRenderDispatcher p_344503_, BlockEntityRenderDispatcher p_345164_) {
        this.blockRenderer = p_344503_;
        this.blockEntityRenderer = p_345164_;
    }

    public SectionCompiler.Results compile(SectionPos p_344383_, RenderChunkRegion p_342669_, VertexSorting p_342522_, SectionBufferBuilderPack p_343546_) {
        SectionCompiler.Results sectioncompiler$results = new SectionCompiler.Results();
        BlockPos blockpos = p_344383_.origin();
        BlockPos blockpos1 = blockpos.offset(15, 15, 15);
        VisGraph visgraph = new VisGraph();
        PoseStack posestack = new PoseStack();
        ModelBlockRenderer.enableCaching();
        Map<RenderType, BufferBuilder> map = new Reference2ObjectArrayMap<>(RenderType.chunkBufferLayers().size());
        RandomSource randomsource = RandomSource.create();

        for (BlockPos blockpos2 : BlockPos.betweenClosed(blockpos, blockpos1)) {
            BlockState blockstate = p_342669_.getBlockState(blockpos2);
            if (blockstate.isSolidRender()) {
                visgraph.setOpaque(blockpos2);
            }

            if (blockstate.hasBlockEntity()) {
                BlockEntity blockentity = p_342669_.getBlockEntity(blockpos2);
                if (blockentity != null) {
                    this.handleBlockEntity(sectioncompiler$results, blockentity);
                }
            }

            FluidState fluidstate = blockstate.getFluidState();
            if (!fluidstate.isEmpty()) {
                RenderType rendertype = ItemBlockRenderTypes.getRenderLayer(fluidstate);
                BufferBuilder bufferbuilder = this.getOrBeginLayer(map, p_343546_, rendertype);
                this.blockRenderer.renderLiquid(blockpos2, p_342669_, bufferbuilder, blockstate, fluidstate);
            }

            if (blockstate.getRenderShape() == RenderShape.MODEL) {
                RenderType rendertype2 = ItemBlockRenderTypes.getChunkRenderType(blockstate);
                BufferBuilder bufferbuilder1 = this.getOrBeginLayer(map, p_343546_, rendertype2);
                posestack.pushPose();
                posestack.translate(
                    (float)SectionPos.sectionRelative(blockpos2.getX()),
                    (float)SectionPos.sectionRelative(blockpos2.getY()),
                    (float)SectionPos.sectionRelative(blockpos2.getZ())
                );
                this.blockRenderer.renderBatched(blockstate, blockpos2, p_342669_, posestack, bufferbuilder1, true, randomsource);
                posestack.popPose();
            }
        }

        for (Entry<RenderType, BufferBuilder> entry : map.entrySet()) {
            RenderType rendertype1 = entry.getKey();
            MeshData meshdata = entry.getValue().build();
            if (meshdata != null) {
                if (rendertype1 == RenderType.translucent()) {
                    sectioncompiler$results.transparencyState = meshdata.sortQuads(p_343546_.buffer(RenderType.translucent()), p_342522_);
                }

                sectioncompiler$results.renderedLayers.put(rendertype1, meshdata);
            }
        }

        ModelBlockRenderer.clearCache();
        sectioncompiler$results.visibilitySet = visgraph.resolve();
        return sectioncompiler$results;
    }

    private BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> p_344204_, SectionBufferBuilderPack p_344936_, RenderType p_343427_) {
        BufferBuilder bufferbuilder = p_344204_.get(p_343427_);
        if (bufferbuilder == null) {
            ByteBufferBuilder bytebufferbuilder = p_344936_.buffer(p_343427_);
            bufferbuilder = new BufferBuilder(bytebufferbuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            p_344204_.put(p_343427_, bufferbuilder);
        }

        return bufferbuilder;
    }

    private <E extends BlockEntity> void handleBlockEntity(SectionCompiler.Results p_343713_, E p_343478_) {
        BlockEntityRenderer<E> blockentityrenderer = this.blockEntityRenderer.getRenderer(p_343478_);
        if (blockentityrenderer != null) {
            p_343713_.blockEntities.add(p_343478_);
            if (blockentityrenderer.shouldRenderOffScreen(p_343478_)) {
                p_343713_.globalBlockEntities.add(p_343478_);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Results {
        public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
        public final List<BlockEntity> blockEntities = new ArrayList<>();
        public final Map<RenderType, MeshData> renderedLayers = new Reference2ObjectArrayMap<>();
        public VisibilitySet visibilitySet = new VisibilitySet();
        @Nullable
        public MeshData.SortState transparencyState;

        public void release() {
            this.renderedLayers.values().forEach(MeshData::close);
        }
    }
}