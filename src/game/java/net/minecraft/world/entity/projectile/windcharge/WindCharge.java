package net.minecraft.world.entity.projectile.windcharge;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.Vec3;

public class WindCharge extends AbstractWindCharge {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(
        true, false, Optional.of(1.22F), BuiltInRegistries.BLOCK.get(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );
    private static final float RADIUS = 1.2F;
    private static final float MIN_CAMERA_DISTANCE_SQUARED = Mth.square(3.5F);
    private int noDeflectTicks = 5;

    public WindCharge(EntityType<? extends AbstractWindCharge> p_330526_, Level p_330063_) {
        super(p_330526_, p_330063_);
    }

    public WindCharge(Player p_336321_, Level p_330515_, double p_330095_, double p_333760_, double p_334828_) {
        super(EntityType.WIND_CHARGE, p_330515_, p_336321_, p_330095_, p_333760_, p_334828_);
    }

    public WindCharge(Level p_333074_, double p_329691_, double p_335041_, double p_329004_, Vec3 p_342087_) {
        super(EntityType.WIND_CHARGE, p_329691_, p_335041_, p_329004_, p_342087_, p_333074_);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.noDeflectTicks > 0) {
            this.noDeflectTicks--;
        }
    }

    @Override
    public boolean deflect(ProjectileDeflection p_342982_, @Nullable Entity p_343086_, @Nullable Entity p_342755_, boolean p_342935_) {
        return this.noDeflectTicks > 0 ? false : super.deflect(p_342982_, p_343086_, p_342755_, p_342935_);
    }

    @Override
    protected void explode(Vec3 p_342202_) {
        this.level()
            .explode(
                this,
                null,
                EXPLOSION_DAMAGE_CALCULATOR,
                p_342202_.x(),
                p_342202_.y(),
                p_342202_.z(),
                1.2F,
                false,
                Level.ExplosionInteraction.TRIGGER,
                ParticleTypes.GUST_EMITTER_SMALL,
                ParticleTypes.GUST_EMITTER_LARGE,
                SoundEvents.WIND_CHARGE_BURST
            );
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_365272_) {
        return this.tickCount < 2 && p_365272_ < (double)MIN_CAMERA_DISTANCE_SQUARED ? false : super.shouldRenderAtSqrDistance(p_365272_);
    }
}