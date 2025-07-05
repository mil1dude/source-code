package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingLeavesParticle extends TextureSheetParticle {
    private static final float ACCELERATION_SCALE = 0.0025F;
    private static final int INITIAL_LIFETIME = 300;
    private static final int CURVE_ENDPOINT_TIME = 300;
    private float rotSpeed;
    private final float particleRandom;
    private final float spinAcceleration;
    private final float windBig;
    private boolean swirl;
    private boolean flowAway;
    private double xaFlowScale;
    private double zaFlowScale;
    private double swirlPeriod;

    protected FallingLeavesParticle(
        ClientLevel p_377646_,
        double p_377442_,
        double p_376050_,
        double p_377918_,
        SpriteSet p_376948_,
        float p_378651_,
        float p_376838_,
        boolean p_378490_,
        boolean p_376930_,
        float p_376718_,
        float p_378174_
    ) {
        super(p_377646_, p_377442_, p_376050_, p_377918_);
        this.setSprite(p_376948_.get(this.random.nextInt(12), 12));
        this.rotSpeed = (float)Math.toRadians(this.random.nextBoolean() ? -30.0 : 30.0);
        this.particleRandom = this.random.nextFloat();
        this.spinAcceleration = (float)Math.toRadians(this.random.nextBoolean() ? -5.0 : 5.0);
        this.windBig = p_376838_;
        this.swirl = p_378490_;
        this.flowAway = p_376930_;
        this.lifetime = 300;
        this.gravity = p_378651_ * 1.2F * 0.0025F;
        float f = p_376718_ * (this.random.nextBoolean() ? 0.05F : 0.075F);
        this.quadSize = f;
        this.setSize(f, f);
        this.friction = 1.0F;
        this.yd = (double)(-p_378174_);
        this.xaFlowScale = Math.cos(Math.toRadians((double)(this.particleRandom * 60.0F))) * (double)this.windBig;
        this.zaFlowScale = Math.sin(Math.toRadians((double)(this.particleRandom * 60.0F))) * (double)this.windBig;
        this.swirlPeriod = Math.toRadians((double)(1000.0F + this.particleRandom * 3000.0F));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        }

        if (!this.removed) {
            float f = (float)(300 - this.lifetime);
            float f1 = Math.min(f / 300.0F, 1.0F);
            double d0 = 0.0;
            double d1 = 0.0;
            if (this.flowAway) {
                d0 += this.xaFlowScale * Math.pow((double)f1, 1.25);
                d1 += this.zaFlowScale * Math.pow((double)f1, 1.25);
            }

            if (this.swirl) {
                d0 += (double)f1 * Math.cos((double)f1 * this.swirlPeriod) * (double)this.windBig;
                d1 += (double)f1 * Math.sin((double)f1 * this.swirlPeriod) * (double)this.windBig;
            }

            this.xd += d0 * 0.0025F;
            this.zd += d1 * 0.0025F;
            this.yd = this.yd - (double)this.gravity;
            this.rotSpeed = this.rotSpeed + this.spinAcceleration / 20.0F;
            this.oRoll = this.roll;
            this.roll = this.roll + this.rotSpeed / 20.0F;
            this.move(this.xd, this.yd, this.zd);
            if (this.onGround || this.lifetime < 299 && (this.xd == 0.0 || this.zd == 0.0)) {
                this.remove();
            }

            if (!this.removed) {
                this.xd = this.xd * (double)this.friction;
                this.yd = this.yd * (double)this.friction;
                this.zd = this.zd * (double)this.friction;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CherryProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public CherryProvider(SpriteSet p_376778_) {
            this.sprites = p_376778_;
        }

        public Particle createParticle(
            SimpleParticleType p_377921_,
            ClientLevel p_376453_,
            double p_375674_,
            double p_377368_,
            double p_376211_,
            double p_377051_,
            double p_377679_,
            double p_375799_
        ) {
            return new FallingLeavesParticle(p_376453_, p_375674_, p_377368_, p_376211_, this.sprites, 0.25F, 2.0F, false, true, 1.0F, 0.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class PaleOakProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public PaleOakProvider(SpriteSet p_378488_) {
            this.sprites = p_378488_;
        }

        public Particle createParticle(
            SimpleParticleType p_376667_,
            ClientLevel p_376868_,
            double p_378558_,
            double p_377831_,
            double p_378235_,
            double p_375996_,
            double p_378236_,
            double p_377227_
        ) {
            return new FallingLeavesParticle(p_376868_, p_378558_, p_377831_, p_378235_, this.sprites, 0.07F, 10.0F, true, false, 2.0F, 0.021F);
        }
    }
}