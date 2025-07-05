package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ReactToBell {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            p_259349_ -> p_259349_.group(p_259349_.present(MemoryModuleType.HEARD_BELL_TIME)).apply(p_259349_, p_259472_ -> (p_375032_, p_375033_, p_375034_) -> {
                        Raid raid = p_375032_.getRaidAt(p_375033_.blockPosition());
                        if (raid == null) {
                            p_375033_.getBrain().setActiveActivityIfPossible(Activity.HIDE);
                        }

                        return true;
                    })
        );
    }
}