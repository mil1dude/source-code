package net.minecraft.world.entity.monster.piglin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.Items;

public class StopHoldingItemIfNoLongerAdmiring {
    public static BehaviorControl<Piglin> create() {
        return BehaviorBuilder.create(
            p_259197_ -> p_259197_.group(p_259197_.absent(MemoryModuleType.ADMIRING_ITEM)).apply(p_259197_, p_259512_ -> (p_359309_, p_359310_, p_359311_) -> {
                        if (!p_359310_.getOffhandItem().isEmpty() && !p_359310_.getOffhandItem().is(Items.SHIELD)) {
                            PiglinAi.stopHoldingOffHandItem(p_359309_, p_359310_, true);
                            return true;
                        } else {
                            return false;
                        }
                    })
        );
    }
}