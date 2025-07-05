package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArrowModel extends EntityModel<ArrowRenderState> {
    public ArrowModel(ModelPart p_364391_) {
        super(p_364391_, RenderType::entityCutout);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
            "back",
            CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.5F, -2.5F, 0.0F, 5.0F, 5.0F),
            PartPose.offsetAndRotation(-11.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F, 0.0F).withScale(0.8F)
        );
        CubeListBuilder cubelistbuilder = CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-12.0F, -2.0F, 0.0F, 16.0F, 4.0F, 0.0F, CubeDeformation.NONE, 1.0F, 0.8F);
        partdefinition.addOrReplaceChild("cross_1", cubelistbuilder, PartPose.rotation((float) (Math.PI / 4), 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("cross_2", cubelistbuilder, PartPose.rotation((float) (Math.PI * 3.0 / 4.0), 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition.transformed(p_368251_ -> p_368251_.scaled(0.9F)), 32, 32);
    }

    public void setupAnim(ArrowRenderState p_364270_) {
        super.setupAnim(p_364270_);
        if (p_364270_.shake > 0.0F) {
            float f = -Mth.sin(p_364270_.shake * 3.0F) * p_364270_.shake;
            this.root.zRot += f * (float) (Math.PI / 180.0);
        }
    }
}