package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BellModel extends Model {
    private static final String BELL_BODY = "bell_body";
    private final ModelPart bellBody;

    public BellModel(ModelPart p_363820_) {
        super(p_363820_, RenderType::entitySolid);
        this.bellBody = p_363820_.getChild("bell_body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition partdefinition1 = partdefinition.addOrReplaceChild(
            "bell_body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 7.0F, 6.0F), PartPose.offset(8.0F, 12.0F, 8.0F)
        );
        partdefinition1.addOrReplaceChild(
            "bell_base", CubeListBuilder.create().texOffs(0, 13).addBox(4.0F, 4.0F, 4.0F, 8.0F, 2.0F, 8.0F), PartPose.offset(-8.0F, -12.0F, -8.0F)
        );
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    public void setupAnim(BellBlockEntity p_368573_, float p_361141_) {
        float f = (float)p_368573_.ticks + p_361141_;
        float f1 = 0.0F;
        float f2 = 0.0F;
        if (p_368573_.shaking) {
            float f3 = Mth.sin(f / (float) Math.PI) / (4.0F + f / 3.0F);
            if (p_368573_.clickDirection == Direction.NORTH) {
                f1 = -f3;
            } else if (p_368573_.clickDirection == Direction.SOUTH) {
                f1 = f3;
            } else if (p_368573_.clickDirection == Direction.EAST) {
                f2 = -f3;
            } else if (p_368573_.clickDirection == Direction.WEST) {
                f2 = f3;
            }
        }

        this.bellBody.xRot = f1;
        this.bellBody.zRot = f2;
    }
}