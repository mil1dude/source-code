package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ResetRaidStatus {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(p_259870_ -> p_259870_.point((p_375035_, p_375036_, p_375037_) -> {
                if (p_375035_.random.nextInt(20) != 0) {
                    return false;
                } else {
                    Brain<?> brain = p_375036_.getBrain();
                    Raid raid = p_375035_.getRaidAt(p_375036_.blockPosition());
                    if (raid == null || raid.isStopped() || raid.isLoss()) {
                        brain.setDefaultActivity(Activity.IDLE);
                        brain.updateActivityFromSchedule(p_375035_.getDayTime(), p_375035_.getGameTime());
                    }

                    return true;
                }
            }));
    }
}