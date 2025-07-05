package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerEarsModel extends HumanoidModel<PlayerRenderState> {
    public PlayerEarsModel(ModelPart p_368646_) {
        super(p_368646_);
    }

    public static LayerDefinition createEarsLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.clearChild("head");
        partdefinition1.clearChild("hat");
        partdefinition.clearChild("body");
        partdefinition.clearChild("left_arm");
        partdefinition.clearChild("right_arm");
        partdefinition.clearChild("left_leg");
        partdefinition.clearChild("right_leg");
        CubeListBuilder cubelistbuilder = CubeListBuilder.create()
            .texOffs(24, 0)
            .addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(1.0F));
        partdefinition1.addOrReplaceChild("left_ear", cubelistbuilder, PartPose.offset(-6.0F, -6.0F, 0.0F));
        partdefinition1.addOrReplaceChild("right_ear", cubelistbuilder, PartPose.offset(6.0F, -6.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}