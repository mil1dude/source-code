package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Calendar;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChestRenderer<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T> {
    private final ChestModel singleModel;
    private final ChestModel doubleLeftModel;
    private final ChestModel doubleRightModel;
    private final boolean xmasTextures = xmasTextures();

    public ChestRenderer(BlockEntityRendererProvider.Context p_173607_) {
        this.singleModel = new ChestModel(p_173607_.bakeLayer(ModelLayers.CHEST));
        this.doubleLeftModel = new ChestModel(p_173607_.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
        this.doubleRightModel = new ChestModel(p_173607_.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));
    }

    public static boolean xmasTextures() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26;
    }

    @Override
    public void render(T p_112363_, float p_112364_, PoseStack p_112365_, MultiBufferSource p_112366_, int p_112367_, int p_112368_) {
        Level level = p_112363_.getLevel();
        boolean flag = level != null;
        BlockState blockstate = flag ? p_112363_.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        ChestType chesttype = blockstate.hasProperty(ChestBlock.TYPE) ? blockstate.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        if (blockstate.getBlock() instanceof AbstractChestBlock<?> abstractchestblock) {
            boolean flag1 = chesttype != ChestType.SINGLE;
            p_112365_.pushPose();
            float f = blockstate.getValue(ChestBlock.FACING).toYRot();
            p_112365_.translate(0.5F, 0.5F, 0.5F);
            p_112365_.mulPose(Axis.YP.rotationDegrees(-f));
            p_112365_.translate(-0.5F, -0.5F, -0.5F);
            DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborcombineresult;
            if (flag) {
                neighborcombineresult = abstractchestblock.combine(blockstate, level, p_112363_.getBlockPos(), true);
            } else {
                neighborcombineresult = DoubleBlockCombiner.Combiner::acceptNone;
            }

            float f1 = neighborcombineresult.apply(ChestBlock.opennessCombiner(p_112363_)).get(p_112364_);
            f1 = 1.0F - f1;
            f1 = 1.0F - f1 * f1 * f1;
            int i = neighborcombineresult.apply(new BrightnessCombiner<>()).applyAsInt(p_112367_);
            Material material = Sheets.chooseMaterial(p_112363_, chesttype, this.xmasTextures);
            VertexConsumer vertexconsumer = material.buffer(p_112366_, RenderType::entityCutout);
            if (flag1) {
                if (chesttype == ChestType.LEFT) {
                    this.render(p_112365_, vertexconsumer, this.doubleLeftModel, f1, i, p_112368_);
                } else {
                    this.render(p_112365_, vertexconsumer, this.doubleRightModel, f1, i, p_112368_);
                }
            } else {
                this.render(p_112365_, vertexconsumer, this.singleModel, f1, i, p_112368_);
            }

            p_112365_.popPose();
        }
    }

    private void render(PoseStack p_112370_, VertexConsumer p_112371_, ChestModel p_363333_, float p_112375_, int p_112376_, int p_112377_) {
        p_363333_.setupAnim(p_112375_);
        p_363333_.renderToBuffer(p_112370_, p_112371_, p_112376_, p_112377_);
    }
}