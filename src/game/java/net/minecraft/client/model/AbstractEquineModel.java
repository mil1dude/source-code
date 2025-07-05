package net.minecraft.client.model;

import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractEquineModel<T extends EquineRenderState> extends EntityModel<T> {
    private static final float DEG_125 = 2.1816616F;
    private static final float DEG_60 = (float) (Math.PI / 3);
    private static final float DEG_45 = (float) (Math.PI / 4);
    private static final float DEG_30 = (float) (Math.PI / 6);
    private static final float DEG_15 = (float) (Math.PI / 12);
    protected static final String HEAD_PARTS = "head_parts";
    private static final String SADDLE = "saddle";
    private static final String LEFT_SADDLE_MOUTH = "left_saddle_mouth";
    private static final String LEFT_SADDLE_LINE = "left_saddle_line";
    private static final String RIGHT_SADDLE_MOUTH = "right_saddle_mouth";
    private static final String RIGHT_SADDLE_LINE = "right_saddle_line";
    private static final String HEAD_SADDLE = "head_saddle";
    private static final String MOUTH_SADDLE_WRAP = "mouth_saddle_wrap";
    protected static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 16.2F, 1.36F, 2.7272F, 2.0F, 20.0F, Set.of("head_parts"));
    protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private final ModelPart[] saddleParts;
    private final ModelPart[] ridingParts;

    public AbstractEquineModel(ModelPart p_365985_) {
        super(p_365985_);
        this.body = p_365985_.getChild("body");
        this.headParts = p_365985_.getChild("head_parts");
        this.rightHindLeg = p_365985_.getChild("right_hind_leg");
        this.leftHindLeg = p_365985_.getChild("left_hind_leg");
        this.rightFrontLeg = p_365985_.getChild("right_front_leg");
        this.leftFrontLeg = p_365985_.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
        ModelPart modelpart = this.body.getChild("saddle");
        ModelPart modelpart1 = this.headParts.getChild("left_saddle_mouth");
        ModelPart modelpart2 = this.headParts.getChild("right_saddle_mouth");
        ModelPart modelpart3 = this.headParts.getChild("left_saddle_line");
        ModelPart modelpart4 = this.headParts.getChild("right_saddle_line");
        ModelPart modelpart5 = this.headParts.getChild("head_saddle");
        ModelPart modelpart6 = this.headParts.getChild("mouth_saddle_wrap");
        this.saddleParts = new ModelPart[]{modelpart, modelpart1, modelpart2, modelpart5, modelpart6};
        this.ridingParts = new ModelPart[]{modelpart3, modelpart4};
    }

    public static MeshDefinition createBodyMesh(CubeDeformation p_366362_) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "body",
            CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F, new CubeDeformation(0.05F)),
            PartPose.offset(0.0F, 11.0F, 5.0F)
        );
        PartDefinition partdefinition2 = partdefinition.addOrReplaceChild(
            "head_parts",
            CubeListBuilder.create().texOffs(0, 35).addBox(-2.05F, -6.0F, -2.0F, 4.0F, 12.0F, 7.0F),
            PartPose.offsetAndRotation(0.0F, 4.0F, -12.0F, (float) (Math.PI / 6), 0.0F, 0.0F)
        );
        PartDefinition partdefinition3 = partdefinition2.addOrReplaceChild(
            "head", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -11.0F, -2.0F, 6.0F, 5.0F, 7.0F, p_366362_), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "mane", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0F, -11.0F, 5.01F, 2.0F, 16.0F, 2.0F, p_366362_), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "upper_mouth", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -11.0F, -7.0F, 4.0F, 5.0F, 5.0F, p_366362_), PartPose.ZERO
        );
        partdefinition.addOrReplaceChild(
            "left_hind_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, p_366362_),
            PartPose.offset(4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_hind_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, p_366362_),
            PartPose.offset(-4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, p_366362_),
            PartPose.offset(4.0F, 14.0F, -10.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_front_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, p_366362_),
            PartPose.offset(-4.0F, 14.0F, -10.0F)
        );
        partdefinition1.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(42, 36).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F, p_366362_),
            PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, (float) (Math.PI / 6), 0.0F, 0.0F)
        );
        partdefinition1.addOrReplaceChild(
            "saddle",
            CubeListBuilder.create().texOffs(26, 0).addBox(-5.0F, -8.0F, -9.0F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0.5F)),
            PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "left_saddle_mouth", CubeListBuilder.create().texOffs(29, 5).addBox(2.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, p_366362_), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "right_saddle_mouth", CubeListBuilder.create().texOffs(29, 5).addBox(-3.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, p_366362_), PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "left_saddle_line",
            CubeListBuilder.create().texOffs(32, 2).addBox(3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F),
            PartPose.rotation((float) (-Math.PI / 6), 0.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "right_saddle_line",
            CubeListBuilder.create().texOffs(32, 2).addBox(-3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F),
            PartPose.rotation((float) (-Math.PI / 6), 0.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "head_saddle",
            CubeListBuilder.create().texOffs(1, 1).addBox(-3.0F, -11.0F, -1.9F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.22F)),
            PartPose.ZERO
        );
        partdefinition2.addOrReplaceChild(
            "mouth_saddle_wrap",
            CubeListBuilder.create().texOffs(19, 0).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.2F)),
            PartPose.ZERO
        );
        partdefinition3.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create().texOffs(19, 16).addBox(0.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.001F)),
            PartPose.ZERO
        );
        partdefinition3.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create().texOffs(19, 16).addBox(-2.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.001F)),
            PartPose.ZERO
        );
        return meshdefinition;
    }

    public static MeshDefinition createBabyMesh(CubeDeformation p_368877_) {
        return BABY_TRANSFORMER.apply(createFullScaleBabyMesh(p_368877_));
    }

    protected static MeshDefinition createFullScaleBabyMesh(CubeDeformation p_361331_) {
        MeshDefinition meshdefinition = createBodyMesh(p_361331_);
        PartDefinition partdefinition = meshdefinition.getRoot();
        CubeDeformation cubedeformation = p_361331_.extend(0.0F, 5.5F, 0.0F);
        partdefinition.addOrReplaceChild(
            "left_hind_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubedeformation),
            PartPose.offset(4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_hind_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubedeformation),
            PartPose.offset(-4.0F, 14.0F, 7.0F)
        );
        partdefinition.addOrReplaceChild(
            "left_front_leg",
            CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubedeformation),
            PartPose.offset(4.0F, 14.0F, -10.0F)
        );
        partdefinition.addOrReplaceChild(
            "right_front_leg",
            CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubedeformation),
            PartPose.offset(-4.0F, 14.0F, -10.0F)
        );
        return meshdefinition;
    }

    public void setupAnim(T p_360790_) {
        super.setupAnim(p_360790_);

        for (ModelPart modelpart : this.saddleParts) {
            modelpart.visible = p_360790_.isSaddled;
        }

        for (ModelPart modelpart1 : this.ridingParts) {
            modelpart1.visible = p_360790_.isRidden && p_360790_.isSaddled;
        }

        float f13 = Mth.clamp(p_360790_.yRot, -20.0F, 20.0F);
        float f14 = p_360790_.xRot * (float) (Math.PI / 180.0);
        float f15 = p_360790_.walkAnimationSpeed;
        float f16 = p_360790_.walkAnimationPos;
        if (f15 > 0.2F) {
            f14 += Mth.cos(f16 * 0.8F) * 0.15F * f15;
        }

        float f = p_360790_.eatAnimation;
        float f1 = p_360790_.standAnimation;
        float f2 = 1.0F - f1;
        float f3 = p_360790_.feedingAnimation;
        boolean flag = p_360790_.animateTail;
        this.headParts.xRot = (float) (Math.PI / 6) + f14;
        this.headParts.yRot = f13 * (float) (Math.PI / 180.0);
        float f4 = p_360790_.isInWater ? 0.2F : 1.0F;
        float f5 = Mth.cos(f4 * f16 * 0.6662F + (float) Math.PI);
        float f6 = f5 * 0.8F * f15;
        float f7 = (1.0F - Math.max(f1, f)) * ((float) (Math.PI / 6) + f14 + f3 * Mth.sin(p_360790_.ageInTicks) * 0.05F);
        this.headParts.xRot = f1 * ((float) (Math.PI / 12) + f14) + f * (2.1816616F + Mth.sin(p_360790_.ageInTicks) * 0.05F) + f7;
        this.headParts.yRot = f1 * f13 * (float) (Math.PI / 180.0) + (1.0F - Math.max(f1, f)) * this.headParts.yRot;
        float f8 = p_360790_.ageScale;
        this.headParts.y = this.headParts.y + Mth.lerp(f, Mth.lerp(f1, 0.0F, -8.0F * f8), 7.0F * f8);
        this.headParts.z = Mth.lerp(f1, this.headParts.z, -4.0F * f8);
        this.body.xRot = f1 * (float) (-Math.PI / 4) + f2 * this.body.xRot;
        float f9 = (float) (Math.PI / 12) * f1;
        float f10 = Mth.cos(p_360790_.ageInTicks * 0.6F + (float) Math.PI);
        this.leftFrontLeg.y -= 12.0F * f8 * f1;
        this.leftFrontLeg.z += 4.0F * f8 * f1;
        this.rightFrontLeg.y = this.leftFrontLeg.y;
        this.rightFrontLeg.z = this.leftFrontLeg.z;
        float f11 = ((float) (-Math.PI / 3) + f10) * f1 + f6 * f2;
        float f12 = ((float) (-Math.PI / 3) - f10) * f1 - f6 * f2;
        this.leftHindLeg.xRot = f9 - f5 * 0.5F * f15 * f2;
        this.rightHindLeg.xRot = f9 + f5 * 0.5F * f15 * f2;
        this.leftFrontLeg.xRot = f11;
        this.rightFrontLeg.xRot = f12;
        this.tail.xRot = (float) (Math.PI / 6) + f15 * 0.75F;
        this.tail.y += f15 * f8;
        this.tail.z += f15 * 2.0F * f8;
        if (flag) {
            this.tail.yRot = Mth.cos(p_360790_.ageInTicks * 0.7F);
        } else {
            this.tail.yRot = 0.0F;
        }
    }
}