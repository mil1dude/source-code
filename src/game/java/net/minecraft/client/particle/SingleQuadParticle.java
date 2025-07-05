package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public abstract class SingleQuadParticle extends Particle {
    protected float quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;

    protected SingleQuadParticle(ClientLevel p_107665_, double p_107666_, double p_107667_, double p_107668_) {
        super(p_107665_, p_107666_, p_107667_, p_107668_);
    }

    protected SingleQuadParticle(
        ClientLevel p_107670_, double p_107671_, double p_107672_, double p_107673_, double p_107674_, double p_107675_, double p_107676_
    ) {
        super(p_107670_, p_107671_, p_107672_, p_107673_, p_107674_, p_107675_, p_107676_);
    }

    public SingleQuadParticle.FacingCameraMode getFacingCameraMode() {
        return SingleQuadParticle.FacingCameraMode.LOOKAT_XYZ;
    }

    @Override
    public void render(VertexConsumer p_107678_, Camera p_107679_, float p_107680_) {
        Quaternionf quaternionf = new Quaternionf();
        this.getFacingCameraMode().setRotation(quaternionf, p_107679_, p_107680_);
        if (this.roll != 0.0F) {
            quaternionf.rotateZ(Mth.lerp(p_107680_, this.oRoll, this.roll));
        }

        this.renderRotatedQuad(p_107678_, p_107679_, quaternionf, p_107680_);
    }

    protected void renderRotatedQuad(VertexConsumer p_342045_, Camera p_344083_, Quaternionf p_342719_, float p_343457_) {
        Vec3 vec3 = p_344083_.getPosition();
        float f = (float)(Mth.lerp((double)p_343457_, this.xo, this.x) - vec3.x());
        float f1 = (float)(Mth.lerp((double)p_343457_, this.yo, this.y) - vec3.y());
        float f2 = (float)(Mth.lerp((double)p_343457_, this.zo, this.z) - vec3.z());
        this.renderRotatedQuad(p_342045_, p_342719_, f, f1, f2, p_343457_);
    }

    protected void renderRotatedQuad(VertexConsumer p_345131_, Quaternionf p_343948_, float p_344896_, float p_343625_, float p_342312_, float p_342822_) {
        float f = this.getQuadSize(p_342822_);
        float f1 = this.getU0();
        float f2 = this.getU1();
        float f3 = this.getV0();
        float f4 = this.getV1();
        int i = this.getLightColor(p_342822_);
        this.renderVertex(p_345131_, p_343948_, p_344896_, p_343625_, p_342312_, 1.0F, -1.0F, f, f2, f4, i);
        this.renderVertex(p_345131_, p_343948_, p_344896_, p_343625_, p_342312_, 1.0F, 1.0F, f, f2, f3, i);
        this.renderVertex(p_345131_, p_343948_, p_344896_, p_343625_, p_342312_, -1.0F, 1.0F, f, f1, f3, i);
        this.renderVertex(p_345131_, p_343948_, p_344896_, p_343625_, p_342312_, -1.0F, -1.0F, f, f1, f4, i);
    }

    private void renderVertex(
        VertexConsumer p_343555_,
        Quaternionf p_344882_,
        float p_343363_,
        float p_344803_,
        float p_345370_,
        float p_343670_,
        float p_345101_,
        float p_342842_,
        float p_342598_,
        float p_344326_,
        int p_345275_
    ) {
        Vector3f vector3f = new Vector3f(p_343670_, p_345101_, 0.0F).rotate(p_344882_).mul(p_342842_).add(p_343363_, p_344803_, p_345370_);
        p_343555_.addVertex(vector3f.x(), vector3f.y(), vector3f.z())
            .setUv(p_342598_, p_344326_)
            .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
            .setLight(p_345275_);
    }

    public float getQuadSize(float p_107681_) {
        return this.quadSize;
    }

    @Override
    public Particle scale(float p_107683_) {
        this.quadSize *= p_107683_;
        return super.scale(p_107683_);
    }

    protected abstract float getU0();

    protected abstract float getU1();

    protected abstract float getV0();

    protected abstract float getV1();

    @OnlyIn(Dist.CLIENT)
    public interface FacingCameraMode {
        SingleQuadParticle.FacingCameraMode LOOKAT_XYZ = (p_312026_, p_311956_, p_310043_) -> p_312026_.set(p_311956_.rotation());
        SingleQuadParticle.FacingCameraMode LOOKAT_Y = (p_310770_, p_309904_, p_311153_) -> p_310770_.set(
                0.0F, p_309904_.rotation().y, 0.0F, p_309904_.rotation().w
            );

        void setRotation(Quaternionf p_309893_, Camera p_309691_, float p_312801_);
    }
}