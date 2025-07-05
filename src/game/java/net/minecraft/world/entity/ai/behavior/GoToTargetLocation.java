package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GoToTargetLocation {
    private static BlockPos getNearbyPos(Mob p_217251_, BlockPos p_217252_) {
        RandomSource randomsource = p_217251_.level().random;
        return p_217252_.offset(getRandomOffset(randomsource), 0, getRandomOffset(randomsource));
    }

    private static int getRandomOffset(RandomSource p_217247_) {
        return p_217247_.nextInt(3) - 1;
    }

    public static <E extends Mob> OneShot<E> create(MemoryModuleType<BlockPos> p_259938_, int p_259740_, float p_259957_) {
        return BehaviorBuilder.create(
            p_259997_ -> p_259997_.group(
                        p_259997_.present(p_259938_),
                        p_259997_.absent(MemoryModuleType.ATTACK_TARGET),
                        p_259997_.absent(MemoryModuleType.WALK_TARGET),
                        p_259997_.registered(MemoryModuleType.LOOK_TARGET)
                    )
                    .apply(p_259997_, (p_259831_, p_259115_, p_259521_, p_259223_) -> (p_374989_, p_374990_, p_374991_) -> {
                            BlockPos blockpos = p_259997_.get(p_259831_);
                            boolean flag = blockpos.closerThan(p_374990_.blockPosition(), (double)p_259740_);
                            if (!flag) {
                                BehaviorUtils.setWalkAndLookTargetMemories(p_374990_, getNearbyPos(p_374990_, blockpos), p_259957_, p_259740_);
                            }

                            return true;
                        })
        );
    }
}