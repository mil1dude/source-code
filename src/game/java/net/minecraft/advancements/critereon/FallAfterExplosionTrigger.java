package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class FallAfterExplosionTrigger extends SimpleCriterionTrigger<FallAfterExplosionTrigger.TriggerInstance> {
    @Override
    public Codec<FallAfterExplosionTrigger.TriggerInstance> codec() {
        return FallAfterExplosionTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_335354_, Vec3 p_333990_, @Nullable Entity p_335867_) {
        Vec3 vec3 = p_335354_.position();
        LootContext lootcontext = p_335867_ != null ? EntityPredicate.createContext(p_335354_, p_335867_) : null;
        this.trigger(p_335354_, p_328967_ -> p_328967_.matches(p_335354_.serverLevel(), p_333990_, vec3, lootcontext));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<LocationPredicate> startPosition,
        Optional<DistancePredicate> distance,
        Optional<ContextAwarePredicate> cause
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<FallAfterExplosionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_334472_ -> p_334472_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(FallAfterExplosionTrigger.TriggerInstance::player),
                        LocationPredicate.CODEC.optionalFieldOf("start_position").forGetter(FallAfterExplosionTrigger.TriggerInstance::startPosition),
                        DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(FallAfterExplosionTrigger.TriggerInstance::distance),
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("cause").forGetter(FallAfterExplosionTrigger.TriggerInstance::cause)
                    )
                    .apply(p_334472_, FallAfterExplosionTrigger.TriggerInstance::new)
        );

        public static Criterion<FallAfterExplosionTrigger.TriggerInstance> fallAfterExplosion(DistancePredicate p_331300_, EntityPredicate.Builder p_329821_) {
            return CriteriaTriggers.FALL_AFTER_EXPLOSION
                .createCriterion(
                    new FallAfterExplosionTrigger.TriggerInstance(
                        Optional.empty(), Optional.empty(), Optional.of(p_331300_), Optional.of(EntityPredicate.wrap(p_329821_))
                    )
                );
        }

        @Override
        public void validate(CriterionValidator p_336137_) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(p_336137_);
            p_336137_.validateEntity(this.cause(), ".cause");
        }

        public boolean matches(ServerLevel p_329103_, Vec3 p_328213_, Vec3 p_332081_, @Nullable LootContext p_327871_) {
            if (this.startPosition.isPresent() && !this.startPosition.get().matches(p_329103_, p_328213_.x, p_328213_.y, p_328213_.z)) {
                return false;
            } else {
                return this.distance.isPresent()
                        && !this.distance
                            .get()
                            .matches(p_328213_.x, p_328213_.y, p_328213_.z, p_332081_.x, p_332081_.y, p_332081_.z)
                    ? false
                    : !this.cause.isPresent() || p_327871_ != null && this.cause.get().matches(p_327871_);
            }
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}