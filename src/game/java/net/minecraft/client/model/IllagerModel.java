package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IllagerModel<S extends IllagerRenderState> extends EntityModel<S> implements ArmedModel, HeadedModel {
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart arms;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public IllagerModel(ModelPart p_170688_) {
        super(p_170688_);
        this.head = p_170688_.getChild("head");
        this.hat = this.head.getChild("hat");
        this.hat.visible = false;
        this.arms = p_170688_.getChild("arms");
        this.leftLeg = p_170688_.getChild("left_leg");
        this.rightLeg = p_170688_.getChild("right_leg");
        this.leftArm = p_170688_.getChild("left_arm");
        this.rightArm = p_170688_.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        partdefinition1.addOrReplaceChild(
            "hat",
            CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.45F)),
            PartPose.ZERO
        );
        partdefinition1.addOrReplaceChild(
            "nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, -2.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(16, 20)
                .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
                .texOffs(0, 38)
                .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 20.0F, 6.0F, new CubeDeformation(0.5F)),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        PartDefinition partdefinition2 = partdefinition.addOrReplaceChild(
            "arms",
            CubeListBuilder.create()
                .texOffs(44, 22)
                .addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F)
                .texOffs(40, 38)
                .addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
            PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "left_shoulder", CubeListBuilder.create().texOffs(44, 22).mirror().addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.ZERO
        );
        partdefinition.addOrReplaceChild(
            "right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-2.0F, 12.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
            PartPose.offset(2.0F, 12.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_arm", CubeListBuilder.create().texOffs(40, 46).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create().texOffs(40, 46).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
            PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void setupAnim(S p_366958_) {
        super.setupAnim(p_366958_);
        this.head.yRot = p_366958_.yRot * (float) (Math.PI / 180.0);
        this.head.xRot = p_366958_.xRot * (float) (Math.PI / 180.0);
        if (p_366958_.isRiding) {
            this.rightArm.xRot = (float) (-Math.PI / 5);
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = (float) (-Math.PI / 5);
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = (float) (Math.PI / 10);
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = (float) (-Math.PI / 10);
            this.leftLeg.zRot = -0.07853982F;
        } else {
            float f = p_366958_.walkAnimationSpeed;
            float f1 = p_366958_.walkAnimationPos;
            this.rightArm.xRot = Mth.cos(f1 * 0.6662F + (float) Math.PI) * 2.0F * f * 0.5F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = Mth.cos(f1 * 0.6662F) * 2.0F * f * 0.5F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = Mth.cos(f1 * 0.6662F) * 1.4F * f * 0.5F;
            this.rightLeg.yRot = 0.0F;
            this.rightLeg.zRot = 0.0F;
            this.leftLeg.xRot = Mth.cos(f1 * 0.6662F + (float) Math.PI) * 1.4F * f * 0.5F;
            this.leftLeg.yRot = 0.0F;
            this.leftLeg.zRot = 0.0F;
        }

        AbstractIllager.IllagerArmPose abstractillager$illagerarmpose = p_366958_.armPose;
        if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.ATTACKING) {
            if (p_366958_.getMainHandItem().isEmpty()) {
                AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, p_366958_.attackAnim, p_366958_.ageInTicks);
            } else {
                AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, p_366958_.mainArm, p_366958_.attackAnim, p_366958_.ageInTicks);
            }
        } else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.SPELLCASTING) {
            this.rightArm.z = 0.0F;
            this.rightArm.x = -5.0F;
            this.leftArm.z = 0.0F;
            this.leftArm.x = 5.0F;
            this.rightArm.xRot = Mth.cos(p_366958_.ageInTicks * 0.6662F) * 0.25F;
            this.leftArm.xRot = Mth.cos(p_366958_.ageInTicks * 0.6662F) * 0.25F;
            this.rightArm.zRot = (float) (Math.PI * 3.0 / 4.0);
            this.leftArm.zRot = (float) (-Math.PI * 3.0 / 4.0);
            this.rightArm.yRot = 0.0F;
            this.leftArm.yRot = 0.0F;
        } else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
            this.rightArm.yRot = -0.1F + this.head.yRot;
            this.rightArm.xRot = (float) (-Math.PI / 2) + this.head.xRot;
            this.leftArm.xRot = -0.9424779F + this.head.xRot;
            this.leftArm.yRot = this.head.yRot - 0.4F;
            this.leftArm.zRot = (float) (Math.PI / 2);
        } else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) {
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
        } else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE) {
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, (float)p_366958_.maxCrossbowChargeDuration, p_366958_.ticksUsingItem, true);
        } else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CELEBRATING) {
            this.rightArm.z = 0.0F;
            this.rightArm.x = -5.0F;
            this.rightArm.xRot = Mth.cos(p_366958_.ageInTicks * 0.6662F) * 0.05F;
            this.rightArm.zRot = 2.670354F;
            this.rightArm.yRot = 0.0F;
            this.leftArm.z = 0.0F;
            this.leftArm.x = 5.0F;
            this.leftArm.xRot = Mth.cos(p_366958_.ageInTicks * 0.6662F) * 0.05F;
            this.leftArm.zRot = (float) (-Math.PI * 3.0 / 4.0);
            this.leftArm.yRot = 0.0F;
        }

        boolean flag = abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CROSSED;
        this.arms.visible = flag;
        this.leftArm.visible = !flag;
        this.rightArm.visible = !flag;
    }

    private ModelPart getArm(HumanoidArm p_102923_) {
        return p_102923_ == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }

    public ModelPart getHat() {
        return this.hat;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public void translateToHand(HumanoidArm p_102925_, PoseStack p_102926_) {
        this.root.translateAndRotate(p_102926_);
        this.getArm(p_102925_).translateAndRotate(p_102926_);
    }
}