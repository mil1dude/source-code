package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SpectralArrow extends AbstractArrow {
    private int duration = 200;

    public SpectralArrow(EntityType<? extends SpectralArrow> p_37411_, Level p_37412_) {
        super(p_37411_, p_37412_);
    }

    public SpectralArrow(Level p_37414_, LivingEntity p_311904_, ItemStack p_309799_, @Nullable ItemStack p_342763_) {
        super(EntityType.SPECTRAL_ARROW, p_311904_, p_37414_, p_309799_, p_342763_);
    }

    public SpectralArrow(Level p_37419_, double p_309976_, double p_310894_, double p_309922_, ItemStack p_311719_, @Nullable ItemStack p_343959_) {
        super(EntityType.SPECTRAL_ARROW, p_309976_, p_310894_, p_309922_, p_37419_, p_311719_, p_343959_);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && !this.isInGround()) {
            this.level().addParticle(ParticleTypes.INSTANT_EFFECT, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity p_37422_) {
        super.doPostHurtEffects(p_37422_);
        MobEffectInstance mobeffectinstance = new MobEffectInstance(MobEffects.GLOWING, this.duration, 0);
        p_37422_.addEffect(mobeffectinstance, this.getEffectSource());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_37424_) {
        super.readAdditionalSaveData(p_37424_);
        if (p_37424_.contains("Duration")) {
            this.duration = p_37424_.getInt("Duration");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_37426_) {
        super.addAdditionalSaveData(p_37426_);
        p_37426_.putInt("Duration", this.duration);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.SPECTRAL_ARROW);
    }
}