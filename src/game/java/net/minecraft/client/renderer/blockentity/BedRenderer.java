package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BedRenderer implements BlockEntityRenderer<BedBlockEntity> {
    private final Model headModel;
    private final Model footModel;

    public BedRenderer(BlockEntityRendererProvider.Context p_173540_) {
        this(p_173540_.getModelSet());
    }

    public BedRenderer(EntityModelSet p_377493_) {
        this.headModel = new Model.Simple(p_377493_.bakeLayer(ModelLayers.BED_HEAD), RenderType::entitySolid);
        this.footModel = new Model.Simple(p_377493_.bakeLayer(ModelLayers.BED_FOOT), RenderType::entitySolid);
    }

    public static LayerDefinition createHeadLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(50, 6).addBox(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F),
            PartPose.rotation((float) (Math.PI / 2), 0.0F, (float) (Math.PI / 2))
        );
        partdefinition.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(50, 18).addBox(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F),
            PartPose.rotation((float) (Math.PI / 2), 0.0F, (float) Math.PI)
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createFootLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 22).addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(50, 0).addBox(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F),
            PartPose.rotation((float) (Math.PI / 2), 0.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(50, 12).addBox(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F),
            PartPose.rotation((float) (Math.PI / 2), 0.0F, (float) (Math.PI * 3.0 / 2.0))
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void render(BedBlockEntity p_112205_, float p_112206_, PoseStack p_112207_, MultiBufferSource p_112208_, int p_112209_, int p_112210_) {
        Level level = p_112205_.getLevel();
        if (level != null) {
            Material material = Sheets.getBedMaterial(p_112205_.getColor());
            BlockState blockstate = p_112205_.getBlockState();
            DoubleBlockCombiner.NeighborCombineResult<? extends BedBlockEntity> neighborcombineresult = DoubleBlockCombiner.combineWithNeigbour(
                BlockEntityType.BED,
                BedBlock::getBlockType,
                BedBlock::getConnectedDirection,
                ChestBlock.FACING,
                blockstate,
                level,
                p_112205_.getBlockPos(),
                (p_112202_, p_112203_) -> false
            );
            int i = neighborcombineresult.apply(new BrightnessCombiner<>()).get(p_112209_);
            this.renderPiece(
                p_112207_,
                p_112208_,
                blockstate.getValue(BedBlock.PART) == BedPart.HEAD ? this.headModel : this.footModel,
                blockstate.getValue(BedBlock.FACING),
                material,
                i,
                p_112210_,
                false
            );
        }
    }

    public void renderInHand(PoseStack p_377951_, MultiBufferSource p_377093_, int p_377719_, int p_375884_, Material p_376840_) {
        this.renderPiece(p_377951_, p_377093_, this.headModel, Direction.SOUTH, p_376840_, p_377719_, p_375884_, false);
        this.renderPiece(p_377951_, p_377093_, this.footModel, Direction.SOUTH, p_376840_, p_377719_, p_375884_, true);
    }

    private void renderPiece(
        PoseStack p_173542_,
        MultiBufferSource p_173543_,
        Model p_363903_,
        Direction p_173545_,
        Material p_173546_,
        int p_173547_,
        int p_173548_,
        boolean p_173549_
    ) {
        p_173542_.pushPose();
        p_173542_.translate(0.0F, 0.5625F, p_173549_ ? -1.0F : 0.0F);
        p_173542_.mulPose(Axis.XP.rotationDegrees(90.0F));
        p_173542_.translate(0.5F, 0.5F, 0.5F);
        p_173542_.mulPose(Axis.ZP.rotationDegrees(180.0F + p_173545_.toYRot()));
        p_173542_.translate(-0.5F, -0.5F, -0.5F);
        VertexConsumer vertexconsumer = p_173546_.buffer(p_173543_, RenderType::entitySolid);
        p_363903_.renderToBuffer(p_173542_, vertexconsumer, p_173547_, p_173548_);
        p_173542_.popPose();
    }
}