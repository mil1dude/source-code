package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TadpoleModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart tail;

    public TadpoleModel(ModelPart p_233443_) {
        super(p_233443_, RenderType::entityCutoutNoCull);
        this.tail = p_233443_.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        float f = 0.0F;
        float f1 = 22.0F;
        float f2 = -3.0F;
        partdefinition.addOrReplaceChild(
            "body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 3.0F), PartPose.offset(0.0F, 22.0F, -3.0F)
        );
        partdefinition.addOrReplaceChild(
            "tail", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, 0.0F, 0.0F, 2.0F, 7.0F), PartPose.offset(0.0F, 22.0F, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    public void setupAnim(LivingEntityRenderState p_367237_) {
        super.setupAnim(p_367237_);
        float f = p_367237_.isInWater ? 1.0F : 1.5F;
        this.tail.yRot = -f * 0.25F * Mth.sin(0.3F * p_367237_.ageInTicks);
    }
}