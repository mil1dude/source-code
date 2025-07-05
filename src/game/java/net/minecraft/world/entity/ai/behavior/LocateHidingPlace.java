package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public class LocateHidingPlace {
    public static OneShot<LivingEntity> create(int p_259202_, float p_259881_, int p_259982_) {
        return BehaviorBuilder.create(
            p_258505_ -> p_258505_.group(
                        p_258505_.absent(MemoryModuleType.WALK_TARGET),
                        p_258505_.registered(MemoryModuleType.HOME),
                        p_258505_.registered(MemoryModuleType.HIDING_PLACE),
                        p_258505_.registered(MemoryModuleType.PATH),
                        p_258505_.registered(MemoryModuleType.LOOK_TARGET),
                        p_258505_.registered(MemoryModuleType.BREED_TARGET),
                        p_258505_.registered(MemoryModuleType.INTERACTION_TARGET)
                    )
                    .apply(
                        p_258505_,
                        (p_258484_, p_258485_, p_258486_, p_258487_, p_258488_, p_258489_, p_258490_) -> (p_375011_, p_375012_, p_375013_) -> {
                                p_375011_.getPoiManager()
                                    .find(
                                        p_217258_ -> p_217258_.is(PoiTypes.HOME),
                                        p_23425_ -> true,
                                        p_375012_.blockPosition(),
                                        p_259982_ + 1,
                                        PoiManager.Occupancy.ANY
                                    )
                                    .filter(p_374999_ -> p_374999_.closerToCenterThan(p_375012_.position(), (double)p_259982_))
                                    .or(
                                        () -> p_375011_.getPoiManager()
                                                .getRandom(
                                                    p_217256_ -> p_217256_.is(PoiTypes.HOME),
                                                    p_23421_ -> true,
                                                    PoiManager.Occupancy.ANY,
                                                    p_375012_.blockPosition(),
                                                    p_259202_,
                                                    p_375012_.getRandom()
                                                )
                                    )
                                    .or(() -> p_258505_.<GlobalPos>tryGet(p_258485_).map(GlobalPos::pos))
                                    .ifPresent(p_375024_ -> {
                                        p_258487_.erase();
                                        p_258488_.erase();
                                        p_258489_.erase();
                                        p_258490_.erase();
                                        p_258486_.set(GlobalPos.of(p_375011_.dimension(), p_375024_));
                                        if (!p_375024_.closerToCenterThan(p_375012_.position(), (double)p_259982_)) {
                                            p_258484_.set(new WalkTarget(p_375024_, p_259881_, p_259982_));
                                        }
                                    });
                                return true;
                            }
                    )
        );
    }
}