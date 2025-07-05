package net.minecraft.client.model;

import net.minecraft.client.animation.definitions.CamelAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.CamelRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CamelModel extends EntityModel<CamelRenderState> {
    private static final float MAX_WALK_ANIMATION_SPEED = 2.0F;
    private static final float WALK_ANIMATION_SCALE_FACTOR = 2.5F;
    public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.45F);
    private static final String SADDLE = "saddle";
    private static final String BRIDLE = "bridle";
    private static final String REINS = "reins";
    private final ModelPart head;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public CamelModel(ModelPart p_251834_) {
        super(p_251834_);
        ModelPart modelpart = p_251834_.getChild("body");
        this.head = modelpart.getChild("head");
        this.saddleParts = new ModelPart[]{modelpart.getChild("saddle"), this.head.getChild("bridle")};
        this.ridingParts = new ModelPart[]{this.head.getChild("reins")};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeDeformation cubedeformation = new CubeDeformation(0.05F);
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 25).addBox(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F), PartPose.offset(0.0F, 4.0F, 9.5F)
        );
        partdefinition1.addOrReplaceChild(
            "hump", CubeListBuilder.create().texOffs(74, 0).addBox(-4.5F, -5.0F, -5.5F, 9.0F, 5.0F, 11.0F), PartPose.offset(0.0F, -12.0F, -10.0F)
        );
        partdefinition1.addOrReplaceChild(
            "tail", CubeListBuilder.create().texOffs(122, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 0.0F), PartPose.offset(0.0F, -9.0F, 3.5F)
        );
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(60, 24)
                .addBox(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F)
                .texOffs(21, 0)
                .addBox(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F)
                .texOffs(50, 0)
                .addBox(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F),
            PartPose.offset(0.0F, -3.0F, -19.5F)
        );
        partdefinition2.addOrReplaceChild(
            "left_ear", CubeListBuilder.create().texOffs(45, 0).addBox(-0.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(2.5F, -21.0F, -9.5F)
        );
        partdefinition2.addOrReplaceChild(
            "right_ear", CubeListBuilder.create().texOffs(67, 0).addBox(-2.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(-2.5F, -21.0F, -9.5F)
        );
        partdefinition.addOrReplaceChild(
            "left_hind_leg",
            CubeListBuilder.create().texOffs(58, 16).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F),
            PartPose.offset(4.9F, 1.0F, 9.5F)
        );
        partdefinition.addOrReplaceChild(
            "right_hind_leg",
            CubeListBuilder.create().texOffs(94, 16).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F),
            PartPose.offset(-4.9F, 1.0F, 9.5F)
        );
        partdefinition.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F),
            PartPose.offset(4.9F, 1.0F, -10.5F)
        );
        partdefinition.addOrReplaceChild(
            "right_front_leg",
            CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F),
            PartPose.offset(-4.9F, 1.0F, -10.5F)
        );
        partdefinition1.addOrReplaceChild(
            "saddle",
            CubeListBuilder.create()
                .texOffs(74, 64)
                .addBox(-4.5F, -17.0F, -15.5F, 9.0F, 5.0F, 11.0F, cubedeformation)
                .texOffs(92, 114)
                .addBox(-3.5F, -20.0F, -15.5F, 7.0F, 3.0F, 11.0F, cubedeformation)
                .texOffs(0, 89)
                .addBox(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F, cubedeformation),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "reins",
            CubeListBuilder.create()
                .texOffs(98, 42)
                .addBox(3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F)
                .texOffs(84, 57)
                .addBox(-3.5F, -18.0F, -2.0F, 7.0F, 7.0F, 0.0F)
                .texOffs(98, 42)
                .addBox(-3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "bridle",
            CubeListBuilder.create()
                .texOffs(60, 87)
                .addBox(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F, cubedeformation)
                .texOffs(21, 64)
                .addBox(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F, cubedeformation)
                .texOffs(50, 64)
                .addBox(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F, cubedeformation)
                .texOffs(74, 70)
                .addBox(2.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F)
                .texOffs(74, 70)
                .mirror()
                .addBox(-3.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void setupAnim(CamelRenderState p_368486_) {
        super.setupAnim(p_368486_);
        this.applyHeadRotation(p_368486_, p_368486_.yRot, p_368486_.xRot);
        this.toggleInvisibleParts(p_368486_);
        this.animateWalk(CamelAnimation.CAMEL_WALK, p_368486_.walkAnimationPos, p_368486_.walkAnimationSpeed, 2.0F, 2.5F);
        this.animate(p_368486_.sitAnimationState, CamelAnimation.CAMEL_SIT, p_368486_.ageInTicks, 1.0F);
        this.animate(p_368486_.sitPoseAnimationState, CamelAnimation.CAMEL_SIT_POSE, p_368486_.ageInTicks, 1.0F);
        this.animate(p_368486_.sitUpAnimationState, CamelAnimation.CAMEL_STANDUP, p_368486_.ageInTicks, 1.0F);
        this.animate(p_368486_.idleAnimationState, CamelAnimation.CAMEL_IDLE, p_368486_.ageInTicks, 1.0F);
        this.animate(p_368486_.dashAnimationState, CamelAnimation.CAMEL_DASH, p_368486_.ageInTicks, 1.0F);
    }

    private void applyHeadRotation(CamelRenderState p_367716_, float p_249176_, float p_251814_) {
        p_249176_ = Mth.clamp(p_249176_, -30.0F, 30.0F);
        p_251814_ = Mth.clamp(p_251814_, -25.0F, 45.0F);
        if (p_367716_.jumpCooldown > 0.0F) {
            float f = 45.0F * p_367716_.jumpCooldown / 55.0F;
            p_251814_ = Mth.clamp(p_251814_ + f, -25.0F, 70.0F);
        }

        this.head.yRot = p_249176_ * (float) (Math.PI / 180.0);
        this.head.xRot = p_251814_ * (float) (Math.PI / 180.0);
    }

    private void toggleInvisibleParts(CamelRenderState p_363076_) {
        boolean flag = p_363076_.isSaddled;
        boolean flag1 = p_363076_.isRidden;

        for (ModelPart modelpart : this.saddleParts) {
            modelpart.visible = flag;
        }

        for (ModelPart modelpart1 : this.ridingParts) {
            modelpart1.visible = flag1 && flag;
        }
    }
}