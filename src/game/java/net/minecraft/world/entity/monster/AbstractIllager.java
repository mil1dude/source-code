package net.minecraft.world.entity.monster;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

public abstract class AbstractIllager extends Raider {
    protected AbstractIllager(EntityType<? extends AbstractIllager> p_32105_, Level p_32106_) {
        super(p_32105_, p_32106_);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    public AbstractIllager.IllagerArmPose getArmPose() {
        return AbstractIllager.IllagerArmPose.CROSSED;
    }

    @Override
    public boolean canAttack(LivingEntity p_186270_) {
        return p_186270_ instanceof AbstractVillager && p_186270_.isBaby() ? false : super.canAttack(p_186270_);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity p_366292_) {
        if (super.considersEntityAsAlly(p_366292_)) {
            return true;
        } else {
            return !p_366292_.getType().is(EntityTypeTags.ILLAGER_FRIENDS) ? false : this.getTeam() == null && p_366292_.getTeam() == null;
        }
    }

    public static enum IllagerArmPose {
        CROSSED,
        ATTACKING,
        SPELLCASTING,
        BOW_AND_ARROW,
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE,
        CELEBRATING,
        NEUTRAL;
    }

    protected class RaiderOpenDoorGoal extends OpenDoorGoal {
        public RaiderOpenDoorGoal(final Raider p_32128_) {
            super(p_32128_, false);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && AbstractIllager.this.hasActiveRaid();
        }
    }
}