package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TemptingSensor extends Sensor<PathfinderMob> {
    private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().ignoreLineOfSight();
    private final Predicate<ItemStack> temptations;

    public TemptingSensor(Predicate<ItemStack> p_328517_) {
        this.temptations = p_328517_;
    }

    protected void doTick(ServerLevel p_148331_, PathfinderMob p_148332_) {
        Brain<?> brain = p_148332_.getBrain();
        TargetingConditions targetingconditions = TEMPT_TARGETING.copy().range((double)((float)p_148332_.getAttributeValue(Attributes.TEMPT_RANGE)));
        List<Player> list = p_148331_.players()
            .stream()
            .filter(EntitySelector.NO_SPECTATORS)
            .filter(p_359128_ -> targetingconditions.test(p_148331_, p_148332_, p_359128_))
            .filter(this::playerHoldingTemptation)
            .filter(p_359130_ -> !p_148332_.hasPassenger(p_359130_))
            .sorted(Comparator.comparingDouble(p_148332_::distanceToSqr))
            .collect(Collectors.toList());
        if (!list.isEmpty()) {
            Player player = list.get(0);
            brain.setMemory(MemoryModuleType.TEMPTING_PLAYER, player);
        } else {
            brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        }
    }

    private boolean playerHoldingTemptation(Player p_148337_) {
        return this.isTemptation(p_148337_.getMainHandItem()) || this.isTemptation(p_148337_.getOffhandItem());
    }

    private boolean isTemptation(ItemStack p_148339_) {
        return this.temptations.test(p_148339_);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
    }
}