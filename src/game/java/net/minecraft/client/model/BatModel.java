package net.minecraft.client.model;

import net.minecraft.client.animation.definitions.BatAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BatModel extends EntityModel<BatRenderState> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWingTip;
    private final ModelPart feet;

    public BatModel(ModelPart p_170427_) {
        super(p_170427_, RenderType::entityCutout);
        this.body = p_170427_.getChild("body");
        this.head = p_170427_.getChild("head");
        this.rightWing = this.body.getChild("right_wing");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");
        this.leftWing = this.body.getChild("left_wing");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
        this.feet = this.body.getChild("feet");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F), PartPose.offset(0.0F, 17.0F, 0.0F)
        );
        PartDefinition partdefinition2 = partdefinition.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 7).addBox(-2.0F, -3.0F, -1.0F, 4.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 17.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "right_ear", CubeListBuilder.create().texOffs(1, 15).addBox(-2.5F, -4.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.offset(-1.5F, -2.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "left_ear", CubeListBuilder.create().texOffs(8, 15).addBox(-0.1F, -3.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.offset(1.1F, -3.0F, 0.0F)
        );
        PartDefinition partdefinition3 = partdefinition1.addOrReplaceChild(
            "right_wing", CubeListBuilder.create().texOffs(12, 0).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 7.0F, 0.0F), PartPose.offset(-1.5F, 0.0F, 0.0F)
        );
        partdefinition3.addOrReplaceChild(
            "right_wing_tip",
            CubeListBuilder.create().texOffs(16, 0).addBox(-6.0F, -2.0F, 0.0F, 6.0F, 8.0F, 0.0F),
            PartPose.offset(-2.0F, 0.0F, 0.0F)
        );
        PartDefinition partdefinition4 = partdefinition1.addOrReplaceChild(
            "left_wing", CubeListBuilder.create().texOffs(12, 7).addBox(0.0F, -2.0F, 0.0F, 2.0F, 7.0F, 0.0F), PartPose.offset(1.5F, 0.0F, 0.0F)
        );
        partdefinition4.addOrReplaceChild(
            "left_wing_tip", CubeListBuilder.create().texOffs(16, 8).addBox(0.0F, -2.0F, 0.0F, 6.0F, 8.0F, 0.0F), PartPose.offset(2.0F, 0.0F, 0.0F)
        );
        partdefinition1.addOrReplaceChild(
            "feet", CubeListBuilder.create().texOffs(16, 16).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 2.0F, 0.0F), PartPose.offset(0.0F, 5.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public void setupAnim(BatRenderState p_361483_) {
        super.setupAnim(p_361483_);
        if (p_361483_.isResting) {
            this.applyHeadRotation(p_361483_.yRot);
        }

        this.animate(p_361483_.flyAnimationState, BatAnimation.BAT_FLYING, p_361483_.ageInTicks, 1.0F);
        this.animate(p_361483_.restAnimationState, BatAnimation.BAT_RESTING, p_361483_.ageInTicks, 1.0F);
    }

    private void applyHeadRotation(float p_310053_) {
        this.head.yRot = p_310053_ * (float) (Math.PI / 180.0);
    }
}