package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public record DamagePredicate(
    MinMaxBounds.Doubles dealtDamage,
    MinMaxBounds.Doubles takenDamage,
    Optional<EntityPredicate> sourceEntity,
    Optional<Boolean> blocked,
    Optional<DamageSourcePredicate> type
) {
    public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(
        p_325199_ -> p_325199_.group(
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("dealt", MinMaxBounds.Doubles.ANY).forGetter(DamagePredicate::dealtDamage),
                    MinMaxBounds.Doubles.CODEC.optionalFieldOf("taken", MinMaxBounds.Doubles.ANY).forGetter(DamagePredicate::takenDamage),
                    EntityPredicate.CODEC.optionalFieldOf("source_entity").forGetter(DamagePredicate::sourceEntity),
                    Codec.BOOL.optionalFieldOf("blocked").forGetter(DamagePredicate::blocked),
                    DamageSourcePredicate.CODEC.optionalFieldOf("type").forGetter(DamagePredicate::type)
                )
                .apply(p_325199_, DamagePredicate::new)
    );

    public boolean matches(ServerPlayer p_24918_, DamageSource p_24919_, float p_24920_, float p_24921_, boolean p_24922_) {
        if (!this.dealtDamage.matches((double)p_24920_)) {
            return false;
        } else if (!this.takenDamage.matches((double)p_24921_)) {
            return false;
        } else if (this.sourceEntity.isPresent() && !this.sourceEntity.get().matches(p_24918_, p_24919_.getEntity())) {
            return false;
        } else {
            return this.blocked.isPresent() && this.blocked.get() != p_24922_
                ? false
                : !this.type.isPresent() || this.type.get().matches(p_24918_, p_24919_);
        }
    }

    public static class Builder {
        private MinMaxBounds.Doubles dealtDamage = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles takenDamage = MinMaxBounds.Doubles.ANY;
        private Optional<EntityPredicate> sourceEntity = Optional.empty();
        private Optional<Boolean> blocked = Optional.empty();
        private Optional<DamageSourcePredicate> type = Optional.empty();

        public static DamagePredicate.Builder damageInstance() {
            return new DamagePredicate.Builder();
        }

        public DamagePredicate.Builder dealtDamage(MinMaxBounds.Doubles p_148146_) {
            this.dealtDamage = p_148146_;
            return this;
        }

        public DamagePredicate.Builder takenDamage(MinMaxBounds.Doubles p_148148_) {
            this.takenDamage = p_148148_;
            return this;
        }

        public DamagePredicate.Builder sourceEntity(EntityPredicate p_148144_) {
            this.sourceEntity = Optional.of(p_148144_);
            return this;
        }

        public DamagePredicate.Builder blocked(Boolean p_24935_) {
            this.blocked = Optional.of(p_24935_);
            return this;
        }

        public DamagePredicate.Builder type(DamageSourcePredicate p_148142_) {
            this.type = Optional.of(p_148142_);
            return this;
        }

        public DamagePredicate.Builder type(DamageSourcePredicate.Builder p_24933_) {
            this.type = Optional.of(p_24933_.build());
            return this;
        }

        public DamagePredicate build() {
            return new DamagePredicate(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
        }
    }
}