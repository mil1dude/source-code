package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SpecialBlockModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockRenderDispatcher implements ResourceManagerReloadListener {
    private final BlockModelShaper blockModelShaper;
    private final ModelBlockRenderer modelRenderer;
    private final Supplier<SpecialBlockModelRenderer> specialBlockModelRenderer;
    private final LiquidBlockRenderer liquidBlockRenderer;
    private final RandomSource random = RandomSource.create();
    private final BlockColors blockColors;

    public BlockRenderDispatcher(BlockModelShaper p_173399_, Supplier<SpecialBlockModelRenderer> p_378785_, BlockColors p_173401_) {
        this.blockModelShaper = p_173399_;
        this.specialBlockModelRenderer = p_378785_;
        this.blockColors = p_173401_;
        this.modelRenderer = new ModelBlockRenderer(this.blockColors);
        this.liquidBlockRenderer = new LiquidBlockRenderer();
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    public void renderBreakingTexture(BlockState p_110919_, BlockPos p_110920_, BlockAndTintGetter p_110921_, PoseStack p_110922_, VertexConsumer p_110923_) {
        if (p_110919_.getRenderShape() == RenderShape.MODEL) {
            BakedModel bakedmodel = this.blockModelShaper.getBlockModel(p_110919_);
            long i = p_110919_.getSeed(p_110920_);
            this.modelRenderer.tesselateBlock(p_110921_, bakedmodel, p_110919_, p_110920_, p_110922_, p_110923_, true, this.random, i, OverlayTexture.NO_OVERLAY);
        }
    }

    public void renderBatched(
        BlockState p_234356_,
        BlockPos p_234357_,
        BlockAndTintGetter p_234358_,
        PoseStack p_234359_,
        VertexConsumer p_234360_,
        boolean p_234361_,
        RandomSource p_234362_
    ) {
        try {
            this.modelRenderer
                .tesselateBlock(
                    p_234358_,
                    this.getBlockModel(p_234356_),
                    p_234356_,
                    p_234357_,
                    p_234359_,
                    p_234360_,
                    p_234361_,
                    p_234362_,
                    p_234356_.getSeed(p_234357_),
                    OverlayTexture.NO_OVERLAY
                );
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashreportcategory, p_234358_, p_234357_, p_234356_);
            throw new ReportedException(crashreport);
        }
    }

    public void renderLiquid(BlockPos p_234364_, BlockAndTintGetter p_234365_, VertexConsumer p_234366_, BlockState p_234367_, FluidState p_234368_) {
        try {
            this.liquidBlockRenderer.tesselate(p_234365_, p_234364_, p_234366_, p_234367_, p_234368_);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating liquid in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashreportcategory, p_234365_, p_234364_, null);
            throw new ReportedException(crashreport);
        }
    }

    public ModelBlockRenderer getModelRenderer() {
        return this.modelRenderer;
    }

    public BakedModel getBlockModel(BlockState p_110911_) {
        return this.blockModelShaper.getBlockModel(p_110911_);
    }

    public void renderSingleBlock(BlockState p_110913_, PoseStack p_110914_, MultiBufferSource p_110915_, int p_110916_, int p_110917_) {
        RenderShape rendershape = p_110913_.getRenderShape();
        if (rendershape != RenderShape.INVISIBLE) {
            BakedModel bakedmodel = this.getBlockModel(p_110913_);
            int i = this.blockColors.getColor(p_110913_, null, null, 0);
            float f = (float)(i >> 16 & 0xFF) / 255.0F;
            float f1 = (float)(i >> 8 & 0xFF) / 255.0F;
            float f2 = (float)(i & 0xFF) / 255.0F;
            this.modelRenderer
                .renderModel(
                    p_110914_.last(), p_110915_.getBuffer(ItemBlockRenderTypes.getRenderType(p_110913_)), p_110913_, bakedmodel, f, f1, f2, p_110916_, p_110917_
                );
            this.specialBlockModelRenderer.get().renderByBlock(p_110913_.getBlock(), ItemDisplayContext.NONE, p_110914_, p_110915_, p_110916_, p_110917_);
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager p_110909_) {
        this.liquidBlockRenderer.setupSprites();
    }
}