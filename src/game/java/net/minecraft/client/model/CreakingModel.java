package net.minecraft.client.model;

import java.util.List;
import net.minecraft.client.animation.definitions.CreakingAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreakingModel extends EntityModel<CreakingRenderState> {
    public static final List<ModelPart> NO_PARTS = List.of();
    private final ModelPart head;
    private final List<ModelPart> headParts;

    public CreakingModel(ModelPart p_363019_) {
        super(p_363019_);
        ModelPart modelpart = p_363019_.getChild("root");
        ModelPart modelpart1 = modelpart.getChild("upper_body");
        this.head = modelpart1.getChild("head");
        this.headParts = List.of(this.head);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.offset(-1.0F, -19.0F, 0.0F));
        partdefinition2.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-3.0F, -10.0F, -3.0F, 6.0F, 10.0F, 6.0F)
                .texOffs(28, 31)
                .addBox(-3.0F, -13.0F, -3.0F, 6.0F, 3.0F, 6.0F)
                .texOffs(12, 40)
                .addBox(3.0F, -13.0F, 0.0F, 9.0F, 14.0F, 0.0F)
                .texOffs(34, 12)
                .addBox(-12.0F, -14.0F, 0.0F, 9.0F, 14.0F, 0.0F),
            PartPose.offset(-3.0F, -11.0F, 0.0F)
        );
        partdefinition2.addOrReplaceChild(
            "body",
            CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(0.0F, -3.0F, -3.0F, 6.0F, 13.0F, 5.0F)
                .texOffs(24, 0)
                .addBox(-6.0F, -4.0F, -3.0F, 6.0F, 7.0F, 5.0F),
            PartPose.offset(0.0F, -7.0F, 1.0F)
        );
        partdefinition2.addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create()
                .texOffs(22, 13)
                .addBox(-2.0F, -1.5F, -1.5F, 3.0F, 21.0F, 3.0F)
                .texOffs(46, 0)
                .addBox(-2.0F, 19.5F, -1.5F, 3.0F, 4.0F, 3.0F),
            PartPose.offset(-7.0F, -9.5F, 1.5F)
        );
        partdefinition2.addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create()
                .texOffs(30, 40)
                .addBox(0.0F, -1.0F, -1.5F, 3.0F, 16.0F, 3.0F)
                .texOffs(52, 12)
                .addBox(0.0F, -5.0F, -1.5F, 3.0F, 4.0F, 3.0F)
                .texOffs(52, 19)
                .addBox(0.0F, 15.0F, -1.5F, 3.0F, 4.0F, 3.0F),
            PartPose.offset(6.0F, -9.0F, 0.5F)
        );
        partdefinition1.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create()
                .texOffs(42, 40)
                .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F)
                .texOffs(45, 55)
                .addBox(-1.5F, 15.7F, -4.5F, 5.0F, 0.0F, 9.0F),
            PartPose.offset(1.5F, -16.0F, 0.5F)
        );
        partdefinition1.addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create()
                .texOffs(0, 34)
                .addBox(-3.0F, -1.5F, -1.5F, 3.0F, 19.0F, 3.0F)
                .texOffs(45, 46)
                .addBox(-5.0F, 17.2F, -4.5F, 5.0F, 0.0F, 9.0F)
                .texOffs(12, 34)
                .addBox(-3.0F, -4.5F, -1.5F, 3.0F, 3.0F, 3.0F),
            PartPose.offset(-1.0F, -17.5F, 0.5F)
        );
        return meshdefinition;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = createMesh();
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void setupAnim(CreakingRenderState p_362230_) {
        super.setupAnim(p_362230_);
        this.head.xRot = p_362230_.xRot * (float) (Math.PI / 180.0);
        this.head.yRot = p_362230_.yRot * (float) (Math.PI / 180.0);
        if (p_362230_.canMove) {
            this.animateWalk(CreakingAnimation.CREAKING_WALK, p_362230_.walkAnimationPos, p_362230_.walkAnimationSpeed, 1.0F, 1.0F);
        }

        this.animate(p_362230_.attackAnimationState, CreakingAnimation.CREAKING_ATTACK, p_362230_.ageInTicks);
        this.animate(p_362230_.invulnerabilityAnimationState, CreakingAnimation.CREAKING_INVULNERABLE, p_362230_.ageInTicks);
        this.animate(p_362230_.deathAnimationState, CreakingAnimation.CREAKING_DEATH, p_362230_.ageInTicks);
    }

    public List<ModelPart> getHeadModelParts(CreakingRenderState p_363264_) {
        return !p_363264_.eyesGlowing ? NO_PARTS : this.headParts;
    }
}