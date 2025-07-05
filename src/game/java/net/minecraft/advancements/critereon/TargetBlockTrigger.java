package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
    @Override
    public Codec<TargetBlockTrigger.TriggerInstance> codec() {
        return TargetBlockTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_70212_, Entity p_70213_, Vec3 p_70214_, int p_70215_) {
        LootContext lootcontext = EntityPredicate.createContext(p_70212_, p_70213_);
        this.trigger(p_70212_, p_70224_ -> p_70224_.matches(lootcontext, p_70214_, p_70215_));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints signalStrength, Optional<ContextAwarePredicate> projectile)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TargetBlockTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325255_ -> p_325255_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TargetBlockTrigger.TriggerInstance::player),
                        MinMaxBounds.Ints.CODEC
                            .optionalFieldOf("signal_strength", MinMaxBounds.Ints.ANY)
                            .forGetter(TargetBlockTrigger.TriggerInstance::signalStrength),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("projectile").forGetter(TargetBlockTrigger.TriggerInstance::projectile)
                    )
                    .apply(p_325255_, TargetBlockTrigger.TriggerInstance::new)
        );

        public static Criterion<TargetBlockTrigger.TriggerInstance> targetHit(MinMaxBounds.Ints p_286700_, Optional<ContextAwarePredicate> p_299065_) {
            return CriteriaTriggers.TARGET_BLOCK_HIT.createCriterion(new TargetBlockTrigger.TriggerInstance(Optional.empty(), p_286700_, p_299065_));
        }

        public boolean matches(LootContext p_70242_, Vec3 p_70243_, int p_70244_) {
            return !this.signalStrength.matches(p_70244_) ? false : !this.projectile.isPresent() || this.projectile.get().matches(p_70242_);
        }

        @Override
        public void validate(CriterionValidator p_312635_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_312635_);
            p_312635_.validateEntity(this.projectile, ".projectile");
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}