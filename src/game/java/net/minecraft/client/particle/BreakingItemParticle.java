package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreakingItemParticle extends TextureSheetParticle {
    private final float uo;
    private final float vo;

    BreakingItemParticle(
        ClientLevel p_105646_,
        double p_105647_,
        double p_105648_,
        double p_105649_,
        double p_105650_,
        double p_105651_,
        double p_105652_,
        ItemStackRenderState p_376788_
    ) {
        this(p_105646_, p_105647_, p_105648_, p_105649_, p_376788_);
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        this.xd += p_105650_;
        this.yd += p_105651_;
        this.zd += p_105652_;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    protected BreakingItemParticle(ClientLevel p_105665_, double p_105666_, double p_105667_, double p_105668_, ItemStackRenderState p_375605_) {
        super(p_105665_, p_105666_, p_105667_, p_105668_, 0.0, 0.0, 0.0);
        TextureAtlasSprite textureatlassprite = p_375605_.pickParticleIcon(this.random);
        if (textureatlassprite != null) {
            this.setSprite(textureatlassprite);
        } else {
            this.setSprite(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation()));
        }

        this.gravity = 1.0F;
        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0F) / 4.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static class CobwebProvider extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType p_329960_,
            ClientLevel p_334942_,
            double p_332141_,
            double p_335808_,
            double p_331451_,
            double p_330404_,
            double p_335788_,
            double p_329792_
        ) {
            return new BreakingItemParticle(p_334942_, p_332141_, p_335808_, p_331451_, this.calculateState(new ItemStack(Items.COBWEB), p_334942_));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class ItemParticleProvider<T extends ParticleOptions> implements ParticleProvider<T> {
        private final ItemStackRenderState scratchRenderState = new ItemStackRenderState();

        protected ItemStackRenderState calculateState(ItemStack p_376099_, ClientLevel p_377180_) {
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(this.scratchRenderState, p_376099_, ItemDisplayContext.GROUND, false, p_377180_, null, 0);
            return this.scratchRenderState;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider extends BreakingItemParticle.ItemParticleProvider<ItemParticleOption> {
        public Particle createParticle(
            ItemParticleOption p_105677_,
            ClientLevel p_105678_,
            double p_105679_,
            double p_105680_,
            double p_105681_,
            double p_105682_,
            double p_105683_,
            double p_105684_
        ) {
            return new BreakingItemParticle(
                p_105678_, p_105679_, p_105680_, p_105681_, p_105682_, p_105683_, p_105684_, this.calculateState(p_105677_.getItem(), p_105678_)
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SlimeProvider extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType p_105705_,
            ClientLevel p_105706_,
            double p_105707_,
            double p_105708_,
            double p_105709_,
            double p_105710_,
            double p_105711_,
            double p_105712_
        ) {
            return new BreakingItemParticle(p_105706_, p_105707_, p_105708_, p_105709_, this.calculateState(new ItemStack(Items.SLIME_BALL), p_105706_));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SnowballProvider extends BreakingItemParticle.ItemParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType p_105724_,
            ClientLevel p_105725_,
            double p_105726_,
            double p_105727_,
            double p_105728_,
            double p_105729_,
            double p_105730_,
            double p_105731_
        ) {
            return new BreakingItemParticle(p_105725_, p_105726_, p_105727_, p_105728_, this.calculateState(new ItemStack(Items.SNOWBALL), p_105725_));
        }
    }
}