package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchModel extends EntityModel<WitchRenderState> implements HeadedModel, VillagerLikeModel {
    protected final ModelPart nose;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart hatRim;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart arms;

    public WitchModel(ModelPart p_171055_) {
        super(p_171055_);
        this.head = p_171055_.getChild("head");
        this.hat = this.head.getChild("hat");
        this.hatRim = this.hat.getChild("hat_rim");
        this.nose = this.head.getChild("nose");
        this.rightLeg = p_171055_.getChild("right_leg");
        this.leftLeg = p_171055_.getChild("left_leg");
        this.arms = p_171055_.getChild("arms");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = VillagerModel.createBodyModel();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.ZERO
        );
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild(
            "hat", CubeListBuilder.create().texOffs(0, 64).addBox(0.0F, 0.0F, 0.0F, 10.0F, 2.0F, 10.0F), PartPose.offset(-5.0F, -10.03125F, -5.0F)
        );
        PartDefinition partdefinition3 = partdefinition2.addOrReplaceChild(
            "hat2",
            CubeListBuilder.create().texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 7.0F, 4.0F, 7.0F),
            PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.05235988F, 0.0F, 0.02617994F)
        );
        PartDefinition partdefinition4 = partdefinition3.addOrReplaceChild(
            "hat3",
            CubeListBuilder.create().texOffs(0, 87).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F),
            PartPose.offsetAndRotation(1.75F, -4.0F, 2.0F, -0.10471976F, 0.0F, 0.05235988F)
        );
        partdefinition4.addOrReplaceChild(
            "hat4",
            CubeListBuilder.create().texOffs(0, 95).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)),
            PartPose.offsetAndRotation(1.75F, -2.0F, 2.0F, (float) (-Math.PI / 15), 0.0F, 0.10471976F)
        );
        PartDefinition partdefinition5 = partdefinition1.getChild("nose");
        partdefinition5.addOrReplaceChild(
            "mole",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 3.0F, -6.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.25F)),
            PartPose.offset(0.0F, -2.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    public void setupAnim(WitchRenderState p_366406_) {
        super.setupAnim(p_366406_);
        this.head.yRot = p_366406_.yRot * (float) (Math.PI / 180.0);
        this.head.xRot = p_366406_.xRot * (float) (Math.PI / 180.0);
        this.rightLeg.xRot = Mth.cos(p_366406_.walkAnimationPos * 0.6662F) * 1.4F * p_366406_.walkAnimationSpeed * 0.5F;
        this.leftLeg.xRot = Mth.cos(p_366406_.walkAnimationPos * 0.6662F + (float) Math.PI) * 1.4F * p_366406_.walkAnimationSpeed * 0.5F;
        float f = 0.01F * (float)(p_366406_.entityId % 10);
        this.nose.xRot = Mth.sin(p_366406_.ageInTicks * f) * 4.5F * (float) (Math.PI / 180.0);
        this.nose.zRot = Mth.cos(p_366406_.ageInTicks * f) * 2.5F * (float) (Math.PI / 180.0);
        if (p_366406_.isHoldingItem) {
            this.nose.setPos(0.0F, 1.0F, -1.5F);
            this.nose.xRot = -0.9F;
        }
    }

    public ModelPart getNose() {
        return this.nose;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void hatVisible(boolean p_363566_) {
        this.head.visible = p_363566_;
        this.hat.visible = p_363566_;
        this.hatRim.visible = p_363566_;
    }

    @Override
    public void translateToArms(PoseStack p_375815_) {
        this.root.translateAndRotate(p_375815_);
        this.arms.translateAndRotate(p_375815_);
    }
}