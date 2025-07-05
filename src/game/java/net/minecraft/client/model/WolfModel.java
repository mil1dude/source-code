package net.minecraft.client.model;

import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfModel extends EntityModel<WolfRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(Set.of("head"));
    private static final String REAL_HEAD = "real_head";
    private static final String UPPER_BODY = "upper_body";
    private static final String REAL_TAIL = "real_tail";
    private final ModelPart head;
    private final ModelPart realHead;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private final ModelPart realTail;
    private final ModelPart upperBody;
    private static final int LEG_SIZE = 8;

    public WolfModel(ModelPart p_171087_) {
        super(p_171087_);
        this.head = p_171087_.getChild("head");
        this.realHead = this.head.getChild("real_head");
        this.body = p_171087_.getChild("body");
        this.upperBody = p_171087_.getChild("upper_body");
        this.rightHindLeg = p_171087_.getChild("right_hind_leg");
        this.leftHindLeg = p_171087_.getChild("left_hind_leg");
        this.rightFrontLeg = p_171087_.getChild("right_front_leg");
        this.leftFrontLeg = p_171087_.getChild("left_front_leg");
        this.tail = p_171087_.getChild("tail");
        this.realTail = this.tail.getChild("real_tail");
    }

    public static MeshDefinition createMeshDefinition(CubeDeformation p_330628_) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        float f = 13.5F;
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0F, 13.5F, -7.0F));
        partdefinition1.addOrReplaceChild(
            "real_head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F, p_330628_)
                .texOffs(16, 14)
                .addBox(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F, p_330628_)
                .texOffs(16, 14)
                .addBox(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F, p_330628_)
                .texOffs(0, 10)
                .addBox(-0.5F, -0.001F, -5.0F, 3.0F, 3.0F, 4.0F, p_330628_),
            PartPose.ZERO
        );
        partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(18, 14).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F, p_330628_),
            PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "upper_body",
            CubeListBuilder.create().texOffs(21, 0).addBox(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F, p_330628_),
            PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, p_330628_);
        partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-2.5F, 16.0F, 7.0F));
        partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(0.5F, 16.0F, 7.0F));
        partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-2.5F, 16.0F, -4.0F));
        partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(0.5F, 16.0F, -4.0F));
        PartDefinition partdefinition2 = partdefinition.addOrReplaceChild(
            "tail", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 12.0F, 8.0F, (float) (Math.PI / 5), 0.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "real_tail", CubeListBuilder.create().texOffs(9, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, p_330628_), PartPose.ZERO
        );
        return meshdefinition;
    }

    public void setupAnim(WolfRenderState p_363855_) {
        super.setupAnim(p_363855_);
        float f = p_363855_.walkAnimationPos;
        float f1 = p_363855_.walkAnimationSpeed;
        if (p_363855_.isAngry) {
            this.tail.yRot = 0.0F;
        } else {
            this.tail.yRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        }

        if (p_363855_.isSitting) {
            float f2 = p_363855_.ageScale;
            this.upperBody.y += 2.0F * f2;
            this.upperBody.xRot = (float) (Math.PI * 2.0 / 5.0);
            this.upperBody.yRot = 0.0F;
            this.body.y += 4.0F * f2;
            this.body.z -= 2.0F * f2;
            this.body.xRot = (float) (Math.PI / 4);
            this.tail.y += 9.0F * f2;
            this.tail.z -= 2.0F * f2;
            this.rightHindLeg.y += 6.7F * f2;
            this.rightHindLeg.z -= 5.0F * f2;
            this.rightHindLeg.xRot = (float) (Math.PI * 3.0 / 2.0);
            this.leftHindLeg.y += 6.7F * f2;
            this.leftHindLeg.z -= 5.0F * f2;
            this.leftHindLeg.xRot = (float) (Math.PI * 3.0 / 2.0);
            this.rightFrontLeg.xRot = 5.811947F;
            this.rightFrontLeg.x += 0.01F * f2;
            this.rightFrontLeg.y += 1.0F * f2;
            this.leftFrontLeg.xRot = 5.811947F;
            this.leftFrontLeg.x -= 0.01F * f2;
            this.leftFrontLeg.y += 1.0F * f2;
        } else {
            this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
            this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * f1;
            this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * f1;
            this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        }

        this.realHead.zRot = p_363855_.headRollAngle + p_363855_.getBodyRollAngle(0.0F);
        this.upperBody.zRot = p_363855_.getBodyRollAngle(-0.08F);
        this.body.zRot = p_363855_.getBodyRollAngle(-0.16F);
        this.realTail.zRot = p_363855_.getBodyRollAngle(-0.2F);
        this.head.xRot = p_363855_.xRot * (float) (Math.PI / 180.0);
        this.head.yRot = p_363855_.yRot * (float) (Math.PI / 180.0);
        this.tail.xRot = p_363855_.tailAngle;
    }
}