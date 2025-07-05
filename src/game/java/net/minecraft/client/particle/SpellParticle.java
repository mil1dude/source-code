package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpellParticle extends TextureSheetParticle {
    private static final RandomSource RANDOM = RandomSource.create();
    private final SpriteSet sprites;
    private float originalAlpha = 1.0F;

    SpellParticle(
        ClientLevel p_107762_, double p_107763_, double p_107764_, double p_107765_, double p_107766_, double p_107767_, double p_107768_, SpriteSet p_107769_
    ) {
        super(p_107762_, p_107763_, p_107764_, p_107765_, 0.5 - RANDOM.nextDouble(), p_107767_, 0.5 - RANDOM.nextDouble());
        this.friction = 0.96F;
        this.gravity = -0.1F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = p_107769_;
        this.yd *= 0.2F;
        if (p_107766_ == 0.0 && p_107768_ == 0.0) {
            this.xd *= 0.1F;
            this.zd *= 0.1F;
        }

        this.quadSize *= 0.75F;
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.setSpriteFromAge(p_107769_);
        if (this.isCloseToScopingPlayer()) {
            this.setAlpha(0.0F);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.isCloseToScopingPlayer()) {
            this.alpha = 0.0F;
        } else {
            this.alpha = Mth.lerp(0.05F, this.alpha, this.originalAlpha);
        }
    }

    @Override
    protected void setAlpha(float p_332254_) {
        super.setAlpha(p_332254_);
        this.originalAlpha = p_332254_;
    }

    private boolean isCloseToScopingPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localplayer = minecraft.player;
        return localplayer != null
            && localplayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0
            && minecraft.options.getCameraType().isFirstPerson()
            && localplayer.isScoping();
    }

    @OnlyIn(Dist.CLIENT)
    public static class InstantProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public InstantProvider(SpriteSet p_107805_) {
            this.sprite = p_107805_;
        }

        public Particle createParticle(
            SimpleParticleType p_107816_,
            ClientLevel p_107817_,
            double p_107818_,
            double p_107819_,
            double p_107820_,
            double p_107821_,
            double p_107822_,
            double p_107823_
        ) {
            return new SpellParticle(p_107817_, p_107818_, p_107819_, p_107820_, p_107821_, p_107822_, p_107823_, this.sprite);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class MobEffectProvider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprite;

        public MobEffectProvider(SpriteSet p_328237_) {
            this.sprite = p_328237_;
        }

        public Particle createParticle(
            ColorParticleOption p_329447_,
            ClientLevel p_333235_,
            double p_327722_,
            double p_329690_,
            double p_335059_,
            double p_332176_,
            double p_334375_,
            double p_330165_
        ) {
            Particle particle = new SpellParticle(p_333235_, p_327722_, p_329690_, p_335059_, p_332176_, p_334375_, p_330165_, this.sprite);
            particle.setColor(p_329447_.getRed(), p_329447_.getGreen(), p_329447_.getBlue());
            particle.setAlpha(p_329447_.getAlpha());
            return particle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet p_107847_) {
            this.sprite = p_107847_;
        }

        public Particle createParticle(
            SimpleParticleType p_107858_,
            ClientLevel p_107859_,
            double p_107860_,
            double p_107861_,
            double p_107862_,
            double p_107863_,
            double p_107864_,
            double p_107865_
        ) {
            return new SpellParticle(p_107859_, p_107860_, p_107861_, p_107862_, p_107863_, p_107864_, p_107865_, this.sprite);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WitchProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WitchProvider(SpriteSet p_107868_) {
            this.sprite = p_107868_;
        }

        public Particle createParticle(
            SimpleParticleType p_107879_,
            ClientLevel p_107880_,
            double p_107881_,
            double p_107882_,
            double p_107883_,
            double p_107884_,
            double p_107885_,
            double p_107886_
        ) {
            SpellParticle spellparticle = new SpellParticle(p_107880_, p_107881_, p_107882_, p_107883_, p_107884_, p_107885_, p_107886_, this.sprite);
            float f = p_107880_.random.nextFloat() * 0.5F + 0.35F;
            spellparticle.setColor(1.0F * f, 0.0F * f, 1.0F * f);
            return spellparticle;
        }
    }
}