package net.minecraft.client.model;

import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChickenModel extends EntityModel<ChickenRenderState> {
    public static final String RED_THING = "red_thing";
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(Set.of("head", "beak", "red_thing"));
    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightWing;
    private final ModelPart leftWing;

    public ChickenModel(ModelPart p_170490_) {
        super(p_170490_);
        this.head = p_170490_.getChild("head");
        this.rightLeg = p_170490_.getChild("right_leg");
        this.leftLeg = p_170490_.getChild("left_leg");
        this.rightWing = p_170490_.getChild("right_wing");
        this.leftWing = p_170490_.getChild("left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        int i = 16;
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 15.0F, -4.0F)
        );
        partdefinition1.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F), PartPose.ZERO);
        partdefinition1.addOrReplaceChild(
            "red_thing", CubeListBuilder.create().texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F), PartPose.ZERO
        );
        partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F),
            PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F)
        );
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F);
        partdefinition.addOrReplaceChild("right_leg", cubelistbuilder, PartPose.offset(-2.0F, 19.0F, 1.0F));
        partdefinition.addOrReplaceChild("left_leg", cubelistbuilder, PartPose.offset(1.0F, 19.0F, 1.0F));
        partdefinition.addOrReplaceChild(
            "right_wing", CubeListBuilder.create().texOffs(24, 13).addBox(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(-4.0F, 13.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_wing", CubeListBuilder.create().texOffs(24, 13).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(4.0F, 13.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public void setupAnim(ChickenRenderState p_369602_) {
        super.setupAnim(p_369602_);
        float f = (Mth.sin(p_369602_.flap) + 1.0F) * p_369602_.flapSpeed;
        this.head.xRot = p_369602_.xRot * (float) (Math.PI / 180.0);
        this.head.yRot = p_369602_.yRot * (float) (Math.PI / 180.0);
        float f1 = p_369602_.walkAnimationSpeed;
        float f2 = p_369602_.walkAnimationPos;
        this.rightLeg.xRot = Mth.cos(f2 * 0.6662F) * 1.4F * f1;
        this.leftLeg.xRot = Mth.cos(f2 * 0.6662F + (float) Math.PI) * 1.4F * f1;
        this.rightWing.zRot = f;
        this.leftWing.zRot = -f;
    }
}