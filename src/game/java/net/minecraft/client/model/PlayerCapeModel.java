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
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class PlayerCapeModel<T extends PlayerRenderState> extends HumanoidModel<T> {
    private static final String CAPE = "cape";
    private final ModelPart cape = this.body.getChild("cape");

    public PlayerCapeModel(ModelPart p_361880_) {
        super(p_361880_);
    }

    public static LayerDefinition createCapeLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.clearChild("head");
        partdefinition1.clearChild("hat");
        PartDefinition partdefinition2 = partdefinition.clearChild("body");
        partdefinition.clearChild("left_arm");
        partdefinition.clearChild("right_arm");
        partdefinition.clearChild("left_leg");
        partdefinition.clearChild("right_leg");
        partdefinition2.addOrReplaceChild(
            "cape",
            CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, CubeDeformation.NONE, 1.0F, 0.5F),
            PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.0F, (float) Math.PI, 0.0F)
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void setupAnim(T p_369467_) {
        super.setupAnim(p_369467_);
        this.cape
            .rotateBy(
                new Quaternionf()
                    .rotateY((float) -Math.PI)
                    .rotateX((6.0F + p_369467_.capeLean / 2.0F + p_369467_.capeFlap) * (float) (Math.PI / 180.0))
                    .rotateZ(p_369467_.capeLean2 / 2.0F * (float) (Math.PI / 180.0))
                    .rotateY((180.0F - p_369467_.capeLean2 / 2.0F) * (float) (Math.PI / 180.0))
            );
    }
}