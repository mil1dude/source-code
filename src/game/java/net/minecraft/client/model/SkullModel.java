package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullModel extends SkullModelBase {
    protected final ModelPart head;

    public SkullModel(ModelPart p_170945_) {
        super(p_170945_);
        this.head = p_170945_.getChild("head");
    }

    public static MeshDefinition createHeadModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return meshdefinition;
    }

    public static LayerDefinition createHumanoidHeadLayer() {
        MeshDefinition meshdefinition = createHeadModel();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.getChild("head")
            .addOrReplaceChild(
                "hat",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)),
                PartPose.ZERO
            );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createMobHeadLayer() {
        MeshDefinition meshdefinition = createHeadModel();
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(float p_103811_, float p_103812_, float p_103813_) {
        this.head.yRot = p_103812_ * (float) (Math.PI / 180.0);
        this.head.xRot = p_103813_ * (float) (Math.PI / 180.0);
    }
}