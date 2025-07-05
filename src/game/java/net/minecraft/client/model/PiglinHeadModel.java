package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PiglinHeadModel extends SkullModelBase {
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;

    public PiglinHeadModel(ModelPart p_261926_) {
        super(p_261926_);
        this.head = p_261926_.getChild("head");
        this.leftEar = this.head.getChild("left_ear");
        this.rightEar = this.head.getChild("right_ear");
    }

    public static MeshDefinition createHeadModel() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PiglinModel.addHead(CubeDeformation.NONE, meshdefinition);
        return meshdefinition;
    }

    @Override
    public void setupAnim(float p_261561_, float p_261750_, float p_261549_) {
        this.head.yRot = p_261750_ * (float) (Math.PI / 180.0);
        this.head.xRot = p_261549_ * (float) (Math.PI / 180.0);
        float f = 1.2F;
        this.leftEar.zRot = (float)(-(Math.cos((double)(p_261561_ * (float) Math.PI * 0.2F * 1.2F)) + 2.5)) * 0.2F;
        this.rightEar.zRot = (float)(Math.cos((double)(p_261561_ * (float) Math.PI * 0.2F)) + 2.5) * 0.2F;
    }
}