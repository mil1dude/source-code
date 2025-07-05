package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandArmorModel extends HumanoidModel<ArmorStandRenderState> {
    public ArmorStandArmorModel(ModelPart p_170346_) {
        super(p_170346_);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation p_170348_) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(p_170348_, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, p_170348_),
            PartPose.offset(0.0F, 1.0F, 0.0F)
        );
        partdefinition1.addOrReplaceChild(
            "hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, p_170348_.extend(0.5F)), PartPose.ZERO
        );
        partdefinition.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170348_.extend(-0.1F)),
            PartPose.offset(-1.9F, 11.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170348_.extend(-0.1F)),
            PartPose.offset(1.9F, 11.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public void setupAnim(ArmorStandRenderState p_368790_) {
        super.setupAnim(p_368790_);
        this.head.xRot = (float) (Math.PI / 180.0) * p_368790_.headPose.getX();
        this.head.yRot = (float) (Math.PI / 180.0) * p_368790_.headPose.getY();
        this.head.zRot = (float) (Math.PI / 180.0) * p_368790_.headPose.getZ();
        this.body.xRot = (float) (Math.PI / 180.0) * p_368790_.bodyPose.getX();
        this.body.yRot = (float) (Math.PI / 180.0) * p_368790_.bodyPose.getY();
        this.body.zRot = (float) (Math.PI / 180.0) * p_368790_.bodyPose.getZ();
        this.leftArm.xRot = (float) (Math.PI / 180.0) * p_368790_.leftArmPose.getX();
        this.leftArm.yRot = (float) (Math.PI / 180.0) * p_368790_.leftArmPose.getY();
        this.leftArm.zRot = (float) (Math.PI / 180.0) * p_368790_.leftArmPose.getZ();
        this.rightArm.xRot = (float) (Math.PI / 180.0) * p_368790_.rightArmPose.getX();
        this.rightArm.yRot = (float) (Math.PI / 180.0) * p_368790_.rightArmPose.getY();
        this.rightArm.zRot = (float) (Math.PI / 180.0) * p_368790_.rightArmPose.getZ();
        this.leftLeg.xRot = (float) (Math.PI / 180.0) * p_368790_.leftLegPose.getX();
        this.leftLeg.yRot = (float) (Math.PI / 180.0) * p_368790_.leftLegPose.getY();
        this.leftLeg.zRot = (float) (Math.PI / 180.0) * p_368790_.leftLegPose.getZ();
        this.rightLeg.xRot = (float) (Math.PI / 180.0) * p_368790_.rightLegPose.getX();
        this.rightLeg.yRot = (float) (Math.PI / 180.0) * p_368790_.rightLegPose.getY();
        this.rightLeg.zRot = (float) (Math.PI / 180.0) * p_368790_.rightLegPose.getZ();
    }
}