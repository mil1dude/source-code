package net.minecraft.world.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownExperienceBottle extends ThrowableItemProjectile {
    public ThrownExperienceBottle(EntityType<? extends ThrownExperienceBottle> p_37510_, Level p_37511_) {
        super(p_37510_, p_37511_);
    }

    public ThrownExperienceBottle(Level p_37513_, LivingEntity p_362218_, ItemStack p_363138_) {
        super(EntityType.EXPERIENCE_BOTTLE, p_362218_, p_37513_, p_363138_);
    }

    public ThrownExperienceBottle(Level p_37518_, double p_366636_, double p_361047_, double p_366792_, ItemStack p_368998_) {
        super(EntityType.EXPERIENCE_BOTTLE, p_366636_, p_361047_, p_366792_, p_37518_, p_368998_);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EXPERIENCE_BOTTLE;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.07;
    }

    @Override
    protected void onHit(HitResult p_37521_) {
        super.onHit(p_37521_);
        if (this.level() instanceof ServerLevel) {
            this.level().levelEvent(2002, this.blockPosition(), -13083194);
            int i = 3 + this.level().random.nextInt(5) + this.level().random.nextInt(5);
            ExperienceOrb.award((ServerLevel)this.level(), this.position(), i);
            this.discard();
        }
    }
}