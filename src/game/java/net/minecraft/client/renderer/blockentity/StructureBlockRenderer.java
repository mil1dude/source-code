package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureBlockRenderer implements BlockEntityRenderer<StructureBlockEntity> {
    public StructureBlockRenderer(BlockEntityRendererProvider.Context p_173675_) {
    }

    public void render(StructureBlockEntity p_112583_, float p_112584_, PoseStack p_112585_, MultiBufferSource p_112586_, int p_112587_, int p_112588_) {
        if (Minecraft.getInstance().player.canUseGameMasterBlocks() || Minecraft.getInstance().player.isSpectator()) {
            BlockPos blockpos = p_112583_.getStructurePos();
            Vec3i vec3i = p_112583_.getStructureSize();
            if (vec3i.getX() >= 1 && vec3i.getY() >= 1 && vec3i.getZ() >= 1) {
                if (p_112583_.getMode() == StructureMode.SAVE || p_112583_.getMode() == StructureMode.LOAD) {
                    double d0 = (double)blockpos.getX();
                    double d1 = (double)blockpos.getZ();
                    double d5 = (double)blockpos.getY();
                    double d8 = d5 + (double)vec3i.getY();
                    double d2;
                    double d3;
                    switch (p_112583_.getMirror()) {
                        case LEFT_RIGHT:
                            d2 = (double)vec3i.getX();
                            d3 = (double)(-vec3i.getZ());
                            break;
                        case FRONT_BACK:
                            d2 = (double)(-vec3i.getX());
                            d3 = (double)vec3i.getZ();
                            break;
                        default:
                            d2 = (double)vec3i.getX();
                            d3 = (double)vec3i.getZ();
                    }

                    double d4;
                    double d6;
                    double d7;
                    double d9;
                    switch (p_112583_.getRotation()) {
                        case CLOCKWISE_90:
                            d4 = d3 < 0.0 ? d0 : d0 + 1.0;
                            d6 = d2 < 0.0 ? d1 + 1.0 : d1;
                            d7 = d4 - d3;
                            d9 = d6 + d2;
                            break;
                        case CLOCKWISE_180:
                            d4 = d2 < 0.0 ? d0 : d0 + 1.0;
                            d6 = d3 < 0.0 ? d1 : d1 + 1.0;
                            d7 = d4 - d2;
                            d9 = d6 - d3;
                            break;
                        case COUNTERCLOCKWISE_90:
                            d4 = d3 < 0.0 ? d0 + 1.0 : d0;
                            d6 = d2 < 0.0 ? d1 : d1 + 1.0;
                            d7 = d4 + d3;
                            d9 = d6 - d2;
                            break;
                        default:
                            d4 = d2 < 0.0 ? d0 + 1.0 : d0;
                            d6 = d3 < 0.0 ? d1 + 1.0 : d1;
                            d7 = d4 + d2;
                            d9 = d6 + d3;
                    }

                    float f = 1.0F;
                    float f1 = 0.9F;
                    float f2 = 0.5F;
                    if (p_112583_.getMode() == StructureMode.SAVE || p_112583_.getShowBoundingBox()) {
                        VertexConsumer vertexconsumer = p_112586_.getBuffer(RenderType.lines());
                        ShapeRenderer.renderLineBox(p_112585_, vertexconsumer, d4, d5, d6, d7, d8, d9, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
                    }

                    if (p_112583_.getMode() == StructureMode.SAVE && p_112583_.getShowAir()) {
                        this.renderInvisibleBlocks(p_112583_, p_112586_, p_112585_);
                    }
                }
            }
        }
    }

    private void renderInvisibleBlocks(StructureBlockEntity p_173677_, MultiBufferSource p_342236_, PoseStack p_173680_) {
        BlockGetter blockgetter = p_173677_.getLevel();
        VertexConsumer vertexconsumer = p_342236_.getBuffer(RenderType.lines());
        BlockPos blockpos = p_173677_.getBlockPos();
        BlockPos blockpos1 = StructureUtils.getStructureOrigin(p_173677_);

        for (BlockPos blockpos2 : BlockPos.betweenClosed(blockpos1, blockpos1.offset(p_173677_.getStructureSize()).offset(-1, -1, -1))) {
            BlockState blockstate = blockgetter.getBlockState(blockpos2);
            boolean flag = blockstate.isAir();
            boolean flag1 = blockstate.is(Blocks.STRUCTURE_VOID);
            boolean flag2 = blockstate.is(Blocks.BARRIER);
            boolean flag3 = blockstate.is(Blocks.LIGHT);
            boolean flag4 = flag1 || flag2 || flag3;
            if (flag || flag4) {
                float f = flag ? 0.05F : 0.0F;
                double d0 = (double)((float)(blockpos2.getX() - blockpos.getX()) + 0.45F - f);
                double d1 = (double)((float)(blockpos2.getY() - blockpos.getY()) + 0.45F - f);
                double d2 = (double)((float)(blockpos2.getZ() - blockpos.getZ()) + 0.45F - f);
                double d3 = (double)((float)(blockpos2.getX() - blockpos.getX()) + 0.55F + f);
                double d4 = (double)((float)(blockpos2.getY() - blockpos.getY()) + 0.55F + f);
                double d5 = (double)((float)(blockpos2.getZ() - blockpos.getZ()) + 0.55F + f);
                if (flag) {
                    ShapeRenderer.renderLineBox(p_173680_, vertexconsumer, d0, d1, d2, d3, d4, d5, 0.5F, 0.5F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F);
                } else if (flag1) {
                    ShapeRenderer.renderLineBox(p_173680_, vertexconsumer, d0, d1, d2, d3, d4, d5, 1.0F, 0.75F, 0.75F, 1.0F, 1.0F, 0.75F, 0.75F);
                } else if (flag2) {
                    ShapeRenderer.renderLineBox(p_173680_, vertexconsumer, d0, d1, d2, d3, d4, d5, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F);
                } else if (flag3) {
                    ShapeRenderer.renderLineBox(p_173680_, vertexconsumer, d0, d1, d2, d3, d4, d5, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F);
                }
            }
        }
    }

    private void renderStructureVoids(StructureBlockEntity p_343397_, VertexConsumer p_344738_, PoseStack p_342214_) {
        BlockGetter blockgetter = p_343397_.getLevel();
        if (blockgetter != null) {
            BlockPos blockpos = p_343397_.getBlockPos();
            BlockPos blockpos1 = StructureUtils.getStructureOrigin(p_343397_);
            Vec3i vec3i = p_343397_.getStructureSize();
            DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(vec3i.getX(), vec3i.getY(), vec3i.getZ());

            for (BlockPos blockpos2 : BlockPos.betweenClosed(blockpos1, blockpos1.offset(vec3i).offset(-1, -1, -1))) {
                if (blockgetter.getBlockState(blockpos2).is(Blocks.STRUCTURE_VOID)) {
                    discretevoxelshape.fill(
                        blockpos2.getX() - blockpos1.getX(),
                        blockpos2.getY() - blockpos1.getY(),
                        blockpos2.getZ() - blockpos1.getZ()
                    );
                }
            }

            discretevoxelshape.forAllFaces((p_342299_, p_345263_, p_344165_, p_344494_) -> {
                float f = 0.48F;
                float f1 = (float)(p_345263_ + blockpos1.getX() - blockpos.getX()) + 0.5F - 0.48F;
                float f2 = (float)(p_344165_ + blockpos1.getY() - blockpos.getY()) + 0.5F - 0.48F;
                float f3 = (float)(p_344494_ + blockpos1.getZ() - blockpos.getZ()) + 0.5F - 0.48F;
                float f4 = (float)(p_345263_ + blockpos1.getX() - blockpos.getX()) + 0.5F + 0.48F;
                float f5 = (float)(p_344165_ + blockpos1.getY() - blockpos.getY()) + 0.5F + 0.48F;
                float f6 = (float)(p_344494_ + blockpos1.getZ() - blockpos.getZ()) + 0.5F + 0.48F;
                ShapeRenderer.renderFace(p_342214_, p_344738_, p_342299_, f1, f2, f3, f4, f5, f6, 0.75F, 0.75F, 1.0F, 0.2F);
            });
        }
    }

    public boolean shouldRenderOffScreen(StructureBlockEntity p_112581_) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}