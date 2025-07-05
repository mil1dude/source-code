package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BannerFlagModel extends Model {
    private final ModelPart flag;

    public BannerFlagModel(ModelPart p_378523_) {
        super(p_378523_, RenderType::entitySolid);
        this.flag = p_378523_.getChild("flag");
    }

    public static LayerDefinition createFlagLayer(boolean p_376570_) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
            "flag",
            CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F),
            PartPose.offset(0.0F, p_376570_ ? -44.0F : -20.5F, p_376570_ ? 0.0F : 10.5F)
        );
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void setupAnim(float p_377810_) {
        this.flag.xRot = (-0.0125F + 0.01F * Mth.cos((float) (Math.PI * 2) * p_377810_)) * (float) Math.PI;
    }
}