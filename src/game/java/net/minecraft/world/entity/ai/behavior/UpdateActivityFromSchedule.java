package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;

public class UpdateActivityFromSchedule {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(p_259429_ -> p_259429_.point((p_375047_, p_375048_, p_375049_) -> {
                p_375048_.getBrain().updateActivityFromSchedule(p_375047_.getDayTime(), p_375047_.getGameTime());
                return true;
            }));
    }
}