package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerModel extends HumanoidModel<PlayerRenderState> {
    private static final String LEFT_SLEEVE = "left_sleeve";
    private static final String RIGHT_SLEEVE = "right_sleeve";
    private static final String LEFT_PANTS = "left_pants";
    private static final String RIGHT_PANTS = "right_pants";
    private final List<ModelPart> bodyParts;
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final boolean slim;

    public PlayerModel(ModelPart p_170821_, boolean p_170822_) {
        super(p_170821_, RenderType::entityTranslucent);
        this.slim = p_170822_;
        this.leftSleeve = this.leftArm.getChild("left_sleeve");
        this.rightSleeve = this.rightArm.getChild("right_sleeve");
        this.leftPants = this.leftLeg.getChild("left_pants");
        this.rightPants = this.rightLeg.getChild("right_pants");
        this.jacket = this.body.getChild("jacket");
        this.bodyParts = List.of(this.head, this.body, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg);
    }

    public static MeshDefinition createMesh(CubeDeformation p_170826_, boolean p_170827_) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(p_170826_, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        float f = 0.25F;
        if (p_170827_) {
            PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_),
                PartPose.offset(5.0F, 2.0F, 0.0F)
            );
            PartDefinition partdefinition2 = partdefinition.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
            );
            partdefinition1.addOrReplaceChild(
                "left_sleeve",
                CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)),
                PartPose.ZERO
            );
            partdefinition2.addOrReplaceChild(
                "right_sleeve",
                CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)),
                PartPose.ZERO
            );
        } else {
            PartDefinition partdefinition4 = partdefinition.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_),
                PartPose.offset(5.0F, 2.0F, 0.0F)
            );
            PartDefinition partdefinition6 = partdefinition.getChild("right_arm");
            partdefinition4.addOrReplaceChild(
                "left_sleeve",
                CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)),
                PartPose.ZERO
            );
            partdefinition6.addOrReplaceChild(
                "right_sleeve",
                CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)),
                PartPose.ZERO
            );
        }

        PartDefinition partdefinition5 = partdefinition.addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_),
            PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        PartDefinition partdefinition7 = partdefinition.getChild("right_leg");
        partdefinition5.addOrReplaceChild(
            "left_pants",
            CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)),
            PartPose.ZERO
        );
        partdefinition7.addOrReplaceChild(
            "right_pants",
            CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)),
            PartPose.ZERO
        );
        PartDefinition partdefinition3 = partdefinition.getChild("body");
        partdefinition3.addOrReplaceChild(
            "jacket",
            CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)),
            PartPose.ZERO
        );
        return meshdefinition;
    }

    public void setupAnim(PlayerRenderState p_365185_) {
        boolean flag = !p_365185_.isSpectator;
        this.body.visible = flag;
        this.rightArm.visible = flag;
        this.leftArm.visible = flag;
        this.rightLeg.visible = flag;
        this.leftLeg.visible = flag;
        this.hat.visible = p_365185_.showHat;
        this.jacket.visible = p_365185_.showJacket;
        this.leftPants.visible = p_365185_.showLeftPants;
        this.rightPants.visible = p_365185_.showRightPants;
        this.leftSleeve.visible = p_365185_.showLeftSleeve;
        this.rightSleeve.visible = p_365185_.showRightSleeve;
        super.setupAnim(p_365185_);
    }

    @Override
    public void setAllVisible(boolean p_103419_) {
        super.setAllVisible(p_103419_);
        this.leftSleeve.visible = p_103419_;
        this.rightSleeve.visible = p_103419_;
        this.leftPants.visible = p_103419_;
        this.rightPants.visible = p_103419_;
        this.jacket.visible = p_103419_;
    }

    @Override
    public void translateToHand(HumanoidArm p_103392_, PoseStack p_103393_) {
        this.root().translateAndRotate(p_103393_);
        ModelPart modelpart = this.getArm(p_103392_);
        if (this.slim) {
            float f = 0.5F * (float)(p_103392_ == HumanoidArm.RIGHT ? 1 : -1);
            modelpart.x += f;
            modelpart.translateAndRotate(p_103393_);
            modelpart.x -= f;
        } else {
            modelpart.translateAndRotate(p_103393_);
        }
    }

    public ModelPart getRandomBodyPart(RandomSource p_370076_) {
        return Util.getRandom(this.bodyParts, p_370076_);
    }
}