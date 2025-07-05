package net.minecraft.world.entity.monster.piglin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class RememberIfHoglinWasKilled {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            p_260168_ -> p_260168_.group(p_260168_.present(MemoryModuleType.ATTACK_TARGET), p_260168_.registered(MemoryModuleType.HUNTED_RECENTLY))
                    .apply(p_260168_, (p_259214_, p_260031_) -> (p_375157_, p_375158_, p_375159_) -> {
                            LivingEntity livingentity = p_260168_.get(p_259214_);
                            if (livingentity.getType() == EntityType.HOGLIN && livingentity.isDeadOrDying()) {
                                p_260031_.setWithExpiry(true, (long)PiglinAi.TIME_BETWEEN_HUNTS.sample(p_375158_.level().random));
                            }

                            return true;
                        })
        );
    }
}