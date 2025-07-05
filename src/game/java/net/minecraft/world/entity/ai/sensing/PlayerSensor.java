package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

public class PlayerSensor extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
    }

    @Override
    protected void doTick(ServerLevel p_26740_, LivingEntity p_26741_) {
        List<Player> list = p_26740_.players()
            .stream()
            .filter(EntitySelector.NO_SPECTATORS)
            .filter(p_359124_ -> p_26741_.closerThan(p_359124_, this.getFollowDistance(p_26741_)))
            .sorted(Comparator.comparingDouble(p_26741_::distanceToSqr))
            .collect(Collectors.toList());
        Brain<?> brain = p_26741_.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_PLAYERS, list);
        List<Player> list1 = list.stream().filter(p_359122_ -> isEntityTargetable(p_26740_, p_26741_, p_359122_)).collect(Collectors.toList());
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, list1.isEmpty() ? null : list1.get(0));
        Optional<Player> optional = list1.stream().filter(p_359119_ -> isEntityAttackable(p_26740_, p_26741_, p_359119_)).findFirst();
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, optional);
    }

    protected double getFollowDistance(LivingEntity p_369051_) {
        return p_369051_.getAttributeValue(Attributes.FOLLOW_RANGE);
    }
}