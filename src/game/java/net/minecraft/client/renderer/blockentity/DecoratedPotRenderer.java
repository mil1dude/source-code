package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DecoratedPotRenderer implements BlockEntityRenderer<DecoratedPotBlockEntity> {
    private static final String NECK = "neck";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private final ModelPart neck;
    private final ModelPart frontSide;
    private final ModelPart backSide;
    private final ModelPart leftSide;
    private final ModelPart rightSide;
    private final ModelPart top;
    private final ModelPart bottom;
    private static final float WOBBLE_AMPLITUDE = 0.125F;

    public DecoratedPotRenderer(BlockEntityRendererProvider.Context p_272872_) {
        this(p_272872_.getModelSet());
    }

    public DecoratedPotRenderer(EntityModelSet p_376368_) {
        ModelPart modelpart = p_376368_.bakeLayer(ModelLayers.DECORATED_POT_BASE);
        this.neck = modelpart.getChild("neck");
        this.top = modelpart.getChild("top");
        this.bottom = modelpart.getChild("bottom");
        ModelPart modelpart1 = p_376368_.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
        this.frontSide = modelpart1.getChild("front");
        this.backSide = modelpart1.getChild("back");
        this.leftSide = modelpart1.getChild("left");
        this.rightSide = modelpart1.getChild("right");
    }

    public static LayerDefinition createBaseLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeDeformation cubedeformation = new CubeDeformation(0.2F);
        CubeDeformation cubedeformation1 = new CubeDeformation(-0.1F);
        partdefinition.addOrReplaceChild(
            "neck",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(4.0F, 17.0F, 4.0F, 8.0F, 3.0F, 8.0F, cubedeformation1)
                .texOffs(0, 5)
                .addBox(5.0F, 20.0F, 5.0F, 6.0F, 1.0F, 6.0F, cubedeformation),
            PartPose.offsetAndRotation(0.0F, 37.0F, 16.0F, (float) Math.PI, 0.0F, 0.0F)
        );
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(-14, 13).addBox(0.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F);
        partdefinition.addOrReplaceChild("top", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("bottom", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public static LayerDefinition createSidesLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeListBuilder cubelistbuilder = CubeListBuilder.create()
            .texOffs(1, 0)
            .addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 0.0F, EnumSet.of(Direction.NORTH));
        partdefinition.addOrReplaceChild("back", cubelistbuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 1.0F, 0.0F, 0.0F, (float) Math.PI));
        partdefinition.addOrReplaceChild("left", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, (float) (-Math.PI / 2), (float) Math.PI));
        partdefinition.addOrReplaceChild("right", cubelistbuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 15.0F, 0.0F, (float) (Math.PI / 2), (float) Math.PI));
        partdefinition.addOrReplaceChild("front", cubelistbuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 15.0F, (float) Math.PI, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static Material getSideMaterial(Optional<Item> p_344130_) {
        if (p_344130_.isPresent()) {
            Material material = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getPatternFromItem(p_344130_.get()));
            if (material != null) {
                return material;
            }
        }

        return Sheets.DECORATED_POT_SIDE;
    }

    public void render(DecoratedPotBlockEntity p_273776_, float p_273103_, PoseStack p_273455_, MultiBufferSource p_273010_, int p_273407_, int p_273059_) {
        p_273455_.pushPose();
        Direction direction = p_273776_.getDirection();
        p_273455_.translate(0.5, 0.0, 0.5);
        p_273455_.mulPose(Axis.YP.rotationDegrees(180.0F - direction.toYRot()));
        p_273455_.translate(-0.5, 0.0, -0.5);
        DecoratedPotBlockEntity.WobbleStyle decoratedpotblockentity$wobblestyle = p_273776_.lastWobbleStyle;
        if (decoratedpotblockentity$wobblestyle != null && p_273776_.getLevel() != null) {
            float f = ((float)(p_273776_.getLevel().getGameTime() - p_273776_.wobbleStartedAtTick) + p_273103_) / (float)decoratedpotblockentity$wobblestyle.duration;
            if (f >= 0.0F && f <= 1.0F) {
                if (decoratedpotblockentity$wobblestyle == DecoratedPotBlockEntity.WobbleStyle.POSITIVE) {
                    float f1 = 0.015625F;
                    float f2 = f * (float) (Math.PI * 2);
                    float f3 = -1.5F * (Mth.cos(f2) + 0.5F) * Mth.sin(f2 / 2.0F);
                    p_273455_.rotateAround(Axis.XP.rotation(f3 * 0.015625F), 0.5F, 0.0F, 0.5F);
                    float f4 = Mth.sin(f2);
                    p_273455_.rotateAround(Axis.ZP.rotation(f4 * 0.015625F), 0.5F, 0.0F, 0.5F);
                } else {
                    float f5 = Mth.sin(-f * 3.0F * (float) Math.PI) * 0.125F;
                    float f6 = 1.0F - f;
                    p_273455_.rotateAround(Axis.YP.rotation(f5 * f6), 0.5F, 0.0F, 0.5F);
                }
            }
        }

        this.render(p_273455_, p_273010_, p_273407_, p_273059_, p_273776_.getDecorations());
        p_273455_.popPose();
    }

    public void renderInHand(PoseStack p_376090_, MultiBufferSource p_378049_, int p_376175_, int p_377059_, PotDecorations p_375435_) {
        this.render(p_376090_, p_378049_, p_376175_, p_377059_, p_375435_);
    }

    private void render(PoseStack p_375797_, MultiBufferSource p_375609_, int p_376933_, int p_376356_, PotDecorations p_376443_) {
        VertexConsumer vertexconsumer = Sheets.DECORATED_POT_BASE.buffer(p_375609_, RenderType::entitySolid);
        this.neck.render(p_375797_, vertexconsumer, p_376933_, p_376356_);
        this.top.render(p_375797_, vertexconsumer, p_376933_, p_376356_);
        this.bottom.render(p_375797_, vertexconsumer, p_376933_, p_376356_);
        this.renderSide(this.frontSide, p_375797_, p_375609_, p_376933_, p_376356_, getSideMaterial(p_376443_.front()));
        this.renderSide(this.backSide, p_375797_, p_375609_, p_376933_, p_376356_, getSideMaterial(p_376443_.back()));
        this.renderSide(this.leftSide, p_375797_, p_375609_, p_376933_, p_376356_, getSideMaterial(p_376443_.left()));
        this.renderSide(this.rightSide, p_375797_, p_375609_, p_376933_, p_376356_, getSideMaterial(p_376443_.right()));
    }

    private void renderSide(ModelPart p_273495_, PoseStack p_272899_, MultiBufferSource p_273582_, int p_273242_, int p_273108_, Material p_273173_) {
        p_273495_.render(p_272899_, p_273173_.buffer(p_273582_, RenderType::entitySolid), p_273242_, p_273108_);
    }
}