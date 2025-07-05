package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrownedModel extends ZombieModel<ZombieRenderState> {
    public DrownedModel(ModelPart p_170534_) {
        super(p_170534_);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation p_170536_) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(p_170536_, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170536_),
            PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170536_),
            PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(ZombieRenderState p_368669_) {
        super.setupAnim(p_368669_);
        if (p_368669_.leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float) Math.PI;
            this.leftArm.yRot = 0.0F;
        }

        if (p_368669_.rightArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float) Math.PI;
            this.rightArm.yRot = 0.0F;
        }

        float f = p_368669_.swimAmount;
        if (f > 0.0F) {
            this.rightArm.xRot = Mth.rotLerpRad(f, this.rightArm.xRot, (float) (-Math.PI * 4.0 / 5.0))
                + f * 0.35F * Mth.sin(0.1F * p_368669_.ageInTicks);
            this.leftArm.xRot = Mth.rotLerpRad(f, this.leftArm.xRot, (float) (-Math.PI * 4.0 / 5.0))
                - f * 0.35F * Mth.sin(0.1F * p_368669_.ageInTicks);
            this.rightArm.zRot = Mth.rotLerpRad(f, this.rightArm.zRot, -0.15F);
            this.leftArm.zRot = Mth.rotLerpRad(f, this.leftArm.zRot, 0.15F);
            this.leftLeg.xRot = this.leftLeg.xRot - f * 0.55F * Mth.sin(0.1F * p_368669_.ageInTicks);
            this.rightLeg.xRot = this.rightLeg.xRot + f * 0.55F * Mth.sin(0.1F * p_368669_.ageInTicks);
            this.head.xRot = 0.0F;
        }
    }
}