package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonModel<S extends SkeletonRenderState> extends HumanoidModel<S> {
    public SkeletonModel(ModelPart p_170941_) {
        super(p_170941_);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        createDefaultSkeletonMesh(partdefinition);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    protected static void createDefaultSkeletonMesh(PartDefinition p_329924_) {
        p_329924_.addOrReplaceChild(
            "right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        p_329924_.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F),
            PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        p_329924_.addOrReplaceChild(
            "right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-2.0F, 12.0F, 0.0F)
        );
        p_329924_.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F),
            PartPose.offset(2.0F, 12.0F, 0.0F)
        );
    }

    public void setupAnim(S p_368002_) {
        super.setupAnim(p_368002_);
        if (p_368002_.isAggressive && !p_368002_.isHoldingBow) {
            float f = p_368002_.attackTime;
            float f1 = Mth.sin(f * (float) Math.PI);
            float f2 = Mth.sin((1.0F - (1.0F - f) * (1.0F - f)) * (float) Math.PI);
            this.rightArm.zRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightArm.yRot = -(0.1F - f1 * 0.6F);
            this.leftArm.yRot = 0.1F - f1 * 0.6F;
            this.rightArm.xRot = (float) (-Math.PI / 2);
            this.leftArm.xRot = (float) (-Math.PI / 2);
            this.rightArm.xRot -= f1 * 1.2F - f2 * 0.4F;
            this.leftArm.xRot -= f1 * 1.2F - f2 * 0.4F;
            AnimationUtils.bobArms(this.rightArm, this.leftArm, p_368002_.ageInTicks);
        }
    }

    @Override
    public void translateToHand(HumanoidArm p_103778_, PoseStack p_103779_) {
        this.root().translateAndRotate(p_103779_);
        float f = p_103778_ == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        ModelPart modelpart = this.getArm(p_103778_);
        modelpart.x += f;
        modelpart.translateAndRotate(p_103779_);
        modelpart.x -= f;
    }
}