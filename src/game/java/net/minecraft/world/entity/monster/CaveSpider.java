package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class CaveSpider extends Spider {
    public CaveSpider(EntityType<? extends CaveSpider> p_32254_, Level p_32255_) {
        super(p_32254_, p_32255_);
    }

    public static AttributeSupplier.Builder createCaveSpider() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, 12.0);
    }

    @Override
    public boolean doHurtTarget(ServerLevel p_368649_, Entity p_32257_) {
        if (super.doHurtTarget(p_368649_, p_32257_)) {
            if (p_32257_ instanceof LivingEntity) {
                int i = 0;
                if (this.level().getDifficulty() == Difficulty.NORMAL) {
                    i = 7;
                } else if (this.level().getDifficulty() == Difficulty.HARD) {
                    i = 15;
                }

                if (i > 0) {
                    ((LivingEntity)p_32257_).addEffect(new MobEffectInstance(MobEffects.POISON, i * 20, 0), this);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_32259_, DifficultyInstance p_32260_, EntitySpawnReason p_363097_, @Nullable SpawnGroupData p_32262_) {
        return p_32262_;
    }

    @Override
    public Vec3 getVehicleAttachmentPoint(Entity p_329499_) {
        return p_329499_.getBbWidth() <= this.getBbWidth() ? new Vec3(0.0, 0.21875 * (double)this.getScale(), 0.0) : super.getVehicleAttachmentPoint(p_329499_);
    }
}