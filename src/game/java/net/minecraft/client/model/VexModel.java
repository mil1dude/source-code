package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VexModel extends EntityModel<VexRenderState> implements ArmedModel {
    private final ModelPart body = this.root.getChild("body");
    private final ModelPart rightArm = this.body.getChild("right_arm");
    private final ModelPart leftArm = this.body.getChild("left_arm");
    private final ModelPart rightWing = this.body.getChild("right_wing");
    private final ModelPart leftWing = this.body.getChild("left_wing");
    private final ModelPart head = this.root.getChild("head");

    public VexModel(ModelPart p_171045_) {
        super(p_171045_.getChild("root"), RenderType::entityTranslucent);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, 0.0F));
        partdefinition1.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 20.0F, 0.0F)
        );
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(0, 10)
                .addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16)
                .addBox(-1.5F, 1.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)),
            PartPose.offset(0.0F, 20.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create().texOffs(23, 0).addBox(-1.25F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)),
            PartPose.offset(-1.75F, 0.25F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create().texOffs(23, 6).addBox(-0.75F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)),
            PartPose.offset(1.75F, 0.25F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "left_wing",
            CubeListBuilder.create().texOffs(16, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false),
            PartPose.offset(0.5F, 1.0F, 1.0F)
        );
        partdefinition2.addOrReplaceChild(
            "right_wing",
            CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-0.5F, 1.0F, 1.0F)
        );
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public void setupAnim(VexRenderState p_362568_) {
        super.setupAnim(p_362568_);
        this.head.yRot = p_362568_.yRot * (float) (Math.PI / 180.0);
        this.head.xRot = p_362568_.xRot * (float) (Math.PI / 180.0);
        float f = Mth.cos(p_362568_.ageInTicks * 5.5F * (float) (Math.PI / 180.0)) * 0.1F;
        this.rightArm.zRot = (float) (Math.PI / 5) + f;
        this.leftArm.zRot = -((float) (Math.PI / 5) + f);
        if (p_362568_.isCharging) {
            this.body.xRot = 0.0F;
            this.setArmsCharging(!p_362568_.rightHandItem.isEmpty(), !p_362568_.leftHandItem.isEmpty(), f);
        } else {
            this.body.xRot = (float) (Math.PI / 20);
        }

        this.leftWing.yRot = 1.0995574F + Mth.cos(p_362568_.ageInTicks * 45.836624F * (float) (Math.PI / 180.0)) * (float) (Math.PI / 180.0) * 16.2F;
        this.rightWing.yRot = -this.leftWing.yRot;
        this.leftWing.xRot = 0.47123888F;
        this.leftWing.zRot = -0.47123888F;
        this.rightWing.xRot = 0.47123888F;
        this.rightWing.zRot = 0.47123888F;
    }

    private void setArmsCharging(boolean p_365310_, boolean p_367220_, float p_265125_) {
        if (!p_365310_ && !p_367220_) {
            this.rightArm.xRot = -1.2217305F;
            this.rightArm.yRot = (float) (Math.PI / 12);
            this.rightArm.zRot = -0.47123888F - p_265125_;
            this.leftArm.xRot = -1.2217305F;
            this.leftArm.yRot = (float) (-Math.PI / 12);
            this.leftArm.zRot = 0.47123888F + p_265125_;
        } else {
            if (p_365310_) {
                this.rightArm.xRot = (float) (Math.PI * 7.0 / 6.0);
                this.rightArm.yRot = (float) (Math.PI / 12);
                this.rightArm.zRot = -0.47123888F - p_265125_;
            }

            if (p_367220_) {
                this.leftArm.xRot = (float) (Math.PI * 7.0 / 6.0);
                this.leftArm.yRot = (float) (-Math.PI / 12);
                this.leftArm.zRot = 0.47123888F + p_265125_;
            }
        }
    }

    @Override
    public void translateToHand(HumanoidArm p_259770_, PoseStack p_260351_) {
        boolean flag = p_259770_ == HumanoidArm.RIGHT;
        ModelPart modelpart = flag ? this.rightArm : this.leftArm;
        this.root.translateAndRotate(p_260351_);
        this.body.translateAndRotate(p_260351_);
        modelpart.translateAndRotate(p_260351_);
        p_260351_.scale(0.55F, 0.55F, 0.55F);
        this.offsetStackPosition(p_260351_, flag);
    }

    private void offsetStackPosition(PoseStack p_263343_, boolean p_263414_) {
        if (p_263414_) {
            p_263343_.translate(0.046875, -0.15625, 0.078125);
        } else {
            p_263343_.translate(-0.046875, -0.15625, 0.078125);
        }
    }
}