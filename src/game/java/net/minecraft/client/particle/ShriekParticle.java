package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class ShriekParticle extends TextureSheetParticle {
    private static final float MAGICAL_X_ROT = 1.0472F;
    private int delay;

    ShriekParticle(ClientLevel p_233976_, double p_233977_, double p_233978_, double p_233979_, int p_233980_) {
        super(p_233976_, p_233977_, p_233978_, p_233979_, 0.0, 0.0, 0.0);
        this.quadSize = 0.85F;
        this.delay = p_233980_;
        this.lifetime = 30;
        this.gravity = 0.0F;
        this.xd = 0.0;
        this.yd = 0.1;
        this.zd = 0.0;
    }

    @Override
    public float getQuadSize(float p_234003_) {
        return this.quadSize * Mth.clamp(((float)this.age + p_234003_) / (float)this.lifetime * 0.75F, 0.0F, 1.0F);
    }

    @Override
    public void render(VertexConsumer p_233985_, Camera p_233986_, float p_233987_) {
        if (this.delay <= 0) {
            this.alpha = 1.0F - Mth.clamp(((float)this.age + p_233987_) / (float)this.lifetime, 0.0F, 1.0F);
            Quaternionf quaternionf = new Quaternionf();
            quaternionf.rotationX(-1.0472F);
            this.renderRotatedQuad(p_233985_, p_233986_, quaternionf, p_233987_);
            quaternionf.rotationYXZ((float) -Math.PI, 1.0472F, 0.0F);
            this.renderRotatedQuad(p_233985_, p_233986_, quaternionf, p_233987_);
        }
    }

    @Override
    public int getLightColor(float p_233983_) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (this.delay > 0) {
            this.delay--;
        } else {
            super.tick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<ShriekParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_234008_) {
            this.sprite = p_234008_;
        }

        public Particle createParticle(
            ShriekParticleOption p_234019_,
            ClientLevel p_234020_,
            double p_234021_,
            double p_234022_,
            double p_234023_,
            double p_234024_,
            double p_234025_,
            double p_234026_
        ) {
            ShriekParticle shriekparticle = new ShriekParticle(p_234020_, p_234021_, p_234022_, p_234023_, p_234019_.getDelay());
            shriekparticle.pickSprite(this.sprite);
            shriekparticle.setAlpha(1.0F);
            return shriekparticle;
        }
    }
}