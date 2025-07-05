package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity, FallingBlockRenderState> {
    private final BlockRenderDispatcher dispatcher;

    public FallingBlockRenderer(EntityRendererProvider.Context p_174112_) {
        super(p_174112_);
        this.shadowRadius = 0.5F;
        this.dispatcher = p_174112_.getBlockRenderDispatcher();
    }

    public boolean shouldRender(FallingBlockEntity p_367111_, Frustum p_361639_, double p_368114_, double p_367640_, double p_370068_) {
        return !super.shouldRender(p_367111_, p_361639_, p_368114_, p_367640_, p_370068_)
            ? false
            : p_367111_.getBlockState() != p_367111_.level().getBlockState(p_367111_.blockPosition());
    }

    public void render(FallingBlockRenderState p_365447_, PoseStack p_114637_, MultiBufferSource p_114638_, int p_114639_) {
        BlockState blockstate = p_365447_.blockState;
        if (blockstate.getRenderShape() == RenderShape.MODEL) {
            p_114637_.pushPose();
            p_114637_.translate(-0.5, 0.0, -0.5);
            this.dispatcher
                .getModelRenderer()
                .tesselateBlock(
                    p_365447_,
                    this.dispatcher.getBlockModel(blockstate),
                    blockstate,
                    p_365447_.blockPos,
                    p_114637_,
                    p_114638_.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockstate)),
                    false,
                    RandomSource.create(),
                    blockstate.getSeed(p_365447_.startBlockPos),
                    OverlayTexture.NO_OVERLAY
                );
            p_114637_.popPose();
            super.render(p_365447_, p_114637_, p_114638_, p_114639_);
        }
    }

    public FallingBlockRenderState createRenderState() {
        return new FallingBlockRenderState();
    }

    public void extractRenderState(FallingBlockEntity p_364466_, FallingBlockRenderState p_362649_, float p_366753_) {
        super.extractRenderState(p_364466_, p_362649_, p_366753_);
        BlockPos blockpos = BlockPos.containing(p_364466_.getX(), p_364466_.getBoundingBox().maxY, p_364466_.getZ());
        p_362649_.startBlockPos = p_364466_.getStartPos();
        p_362649_.blockPos = blockpos;
        p_362649_.blockState = p_364466_.getBlockState();
        p_362649_.biome = p_364466_.level().getBiome(blockpos);
        p_362649_.level = p_364466_.level();
    }
}