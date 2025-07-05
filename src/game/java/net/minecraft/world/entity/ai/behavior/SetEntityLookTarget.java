package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetEntityLookTarget {
    public static BehaviorControl<LivingEntity> create(MobCategory p_259154_, float p_260240_) {
        return create(p_359032_ -> p_259154_.equals(p_359032_.getType().getCategory()), p_260240_);
    }

    public static OneShot<LivingEntity> create(EntityType<?> p_260318_, float p_259522_) {
        return create(p_359034_ -> p_260318_.equals(p_359034_.getType()), p_259522_);
    }

    public static OneShot<LivingEntity> create(float p_259830_) {
        return create(p_23913_ -> true, p_259830_);
    }

    public static OneShot<LivingEntity> create(Predicate<LivingEntity> p_260088_, float p_259747_) {
        float f = p_259747_ * p_259747_;
        return BehaviorBuilder.create(
            p_258663_ -> p_258663_.group(p_258663_.absent(MemoryModuleType.LOOK_TARGET), p_258663_.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES))
                    .apply(
                        p_258663_,
                        (p_258656_, p_258657_) -> (p_258650_, p_258651_, p_258652_) -> {
                                Optional<LivingEntity> optional = p_258663_.<NearestVisibleLivingEntities>get(p_258657_)
                                    .findClosest(p_260088_.and(p_359030_ -> p_359030_.distanceToSqr(p_258651_) <= (double)f && !p_258651_.hasPassenger(p_359030_)));
                                if (optional.isEmpty()) {
                                    return false;
                                } else {
                                    p_258656_.set(new EntityTracker(optional.get(), true));
                                    return true;
                                }
                            }
                    )
        );
    }
}