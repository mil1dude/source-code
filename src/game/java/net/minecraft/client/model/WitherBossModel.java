package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherBossModel extends EntityModel<WitherRenderState> {
    private static final String RIBCAGE = "ribcage";
    private static final String CENTER_HEAD = "center_head";
    private static final String RIGHT_HEAD = "right_head";
    private static final String LEFT_HEAD = "left_head";
    private static final float RIBCAGE_X_ROT_OFFSET = 0.065F;
    private static final float TAIL_X_ROT_OFFSET = 0.265F;
    private final ModelPart centerHead;
    private final ModelPart rightHead;
    private final ModelPart leftHead;
    private final ModelPart ribcage;
    private final ModelPart tail;

    public WitherBossModel(ModelPart p_171070_) {
        super(p_171070_);
        this.ribcage = p_171070_.getChild("ribcage");
        this.tail = p_171070_.getChild("tail");
        this.centerHead = p_171070_.getChild("center_head");
        this.rightHead = p_171070_.getChild("right_head");
        this.leftHead = p_171070_.getChild("left_head");
    }

    public static LayerDefinition createBodyLayer(CubeDeformation p_171076_) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
            "shoulders", CubeListBuilder.create().texOffs(0, 16).addBox(-10.0F, 3.9F, -0.5F, 20.0F, 3.0F, 3.0F, p_171076_), PartPose.ZERO
        );
        float f = 0.20420352F;
        partdefinition.addOrReplaceChild(
            "ribcage",
            CubeListBuilder.create()
                .texOffs(0, 22)
                .addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, p_171076_)
                .texOffs(24, 22)
                .addBox(-4.0F, 1.5F, 0.5F, 11.0F, 2.0F, 2.0F, p_171076_)
                .texOffs(24, 22)
                .addBox(-4.0F, 4.0F, 0.5F, 11.0F, 2.0F, 2.0F, p_171076_)
                .texOffs(24, 22)
                .addBox(-4.0F, 6.5F, 0.5F, 11.0F, 2.0F, 2.0F, p_171076_),
            PartPose.offsetAndRotation(-2.0F, 6.9F, -0.5F, 0.20420352F, 0.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "tail",
            CubeListBuilder.create().texOffs(12, 22).addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, p_171076_),
            PartPose.offsetAndRotation(-2.0F, 6.9F + Mth.cos(0.20420352F) * 10.0F, -0.5F + Mth.sin(0.20420352F) * 10.0F, 0.83252203F, 0.0F, 0.0F)
        );
        partdefinition.addOrReplaceChild(
            "center_head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, p_171076_), PartPose.ZERO
        );
        CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, p_171076_);
        partdefinition.addOrReplaceChild("right_head", cubelistbuilder, PartPose.offset(-8.0F, 4.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_head", cubelistbuilder, PartPose.offset(10.0F, 4.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void setupAnim(WitherRenderState p_370145_) {
        super.setupAnim(p_370145_);
        setupHeadRotation(p_370145_, this.rightHead, 0);
        setupHeadRotation(p_370145_, this.leftHead, 1);
        float f = Mth.cos(p_370145_.ageInTicks * 0.1F);
        this.ribcage.xRot = (0.065F + 0.05F * f) * (float) Math.PI;
        this.tail.setPos(-2.0F, 6.9F + Mth.cos(this.ribcage.xRot) * 10.0F, -0.5F + Mth.sin(this.ribcage.xRot) * 10.0F);
        this.tail.xRot = (0.265F + 0.1F * f) * (float) Math.PI;
        this.centerHead.yRot = p_370145_.yRot * (float) (Math.PI / 180.0);
        this.centerHead.xRot = p_370145_.xRot * (float) (Math.PI / 180.0);
    }

    private static void setupHeadRotation(WitherRenderState p_364901_, ModelPart p_171073_, int p_171074_) {
        p_171073_.yRot = (p_364901_.yHeadRots[p_171074_] - p_364901_.bodyRot) * (float) (Math.PI / 180.0);
        p_171073_.xRot = p_364901_.xHeadRots[p_171074_] * (float) (Math.PI / 180.0);
    }
}