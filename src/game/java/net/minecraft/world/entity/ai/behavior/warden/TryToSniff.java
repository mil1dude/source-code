package net.minecraft.world.entity.ai.behavior.warden;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class TryToSniff {
    private static final IntProvider SNIFF_COOLDOWN = UniformInt.of(100, 200);

    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(
            p_259979_ -> p_259979_.group(
                        p_259979_.registered(MemoryModuleType.IS_SNIFFING),
                        p_259979_.registered(MemoryModuleType.WALK_TARGET),
                        p_259979_.absent(MemoryModuleType.SNIFF_COOLDOWN),
                        p_259979_.present(MemoryModuleType.NEAREST_ATTACKABLE),
                        p_259979_.absent(MemoryModuleType.DISTURBANCE_LOCATION)
                    )
                    .apply(p_259979_, (p_260219_, p_260252_, p_260090_, p_259577_, p_260020_) -> (p_359083_, p_359084_, p_359085_) -> {
                            p_260219_.set(Unit.INSTANCE);
                            p_260090_.setWithExpiry(Unit.INSTANCE, (long)SNIFF_COOLDOWN.sample(p_359083_.getRandom()));
                            p_260252_.erase();
                            p_359084_.setPose(Pose.SNIFFING);
                            return true;
                        })
        );
    }
}