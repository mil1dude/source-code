package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class QuadrupedModel<T extends LivingEntityRenderState> extends EntityModel<T> {
    protected final ModelPart head;
    protected final ModelPart body;
    protected final ModelPart rightHindLeg;
    protected final ModelPart leftHindLeg;
    protected final ModelPart rightFrontLeg;
    protected final ModelPart leftFrontLeg;

    protected QuadrupedModel(ModelPart p_170857_) {
        super(p_170857_);
        this.head = p_170857_.getChild("head");
        this.body = p_170857_.getChild("body");
        this.rightHindLeg = p_170857_.getChild("right_hind_leg");
        this.leftHindLeg = p_170857_.getChild("left_hind_leg");
        this.rightFrontLeg = p_170857_.getChild("right_front_leg");
        this.leftFrontLeg = p_170857_.getChild("left_front_leg");
    }

    public static MeshDefinition createBodyMesh(int p_170865_, CubeDeformation p_170866_) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
            "head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, p_170866_),
            PartPose.offset(0.0F, (float)(18 - p_170865_), -6.0F)
        );
        partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(28, 8).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, p_170866_),
            PartPose.offsetAndRotation(0.0F, (float)(17 - p_170865_), 2.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)p_170865_, 4.0F, p_170866_);
        partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-3.0F, (float)(24 - p_170865_), 7.0F));
        partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(3.0F, (float)(24 - p_170865_), 7.0F));
        partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-3.0F, (float)(24 - p_170865_), -5.0F));
        partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(3.0F, (float)(24 - p_170865_), -5.0F));
        return meshdefinition;
    }

    public void setupAnim(T p_364834_) {
        super.setupAnim(p_364834_);
        this.head.xRot = p_364834_.xRot * (float) (Math.PI / 180.0);
        this.head.yRot = p_364834_.yRot * (float) (Math.PI / 180.0);
        float f = p_364834_.walkAnimationPos;
        float f1 = p_364834_.walkAnimationSpeed;
        this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
        this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * f1;
        this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * f1;
        this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
    }
}