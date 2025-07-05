package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record EntityFlagsPredicate(
    Optional<Boolean> isOnGround,
    Optional<Boolean> isOnFire,
    Optional<Boolean> isCrouching,
    Optional<Boolean> isSprinting,
    Optional<Boolean> isSwimming,
    Optional<Boolean> isFlying,
    Optional<Boolean> isBaby
) {
    public static final Codec<EntityFlagsPredicate> CODEC = RecordCodecBuilder.create(
        p_340755_ -> p_340755_.group(
                    Codec.BOOL.optionalFieldOf("is_on_ground").forGetter(EntityFlagsPredicate::isOnGround),
                    Codec.BOOL.optionalFieldOf("is_on_fire").forGetter(EntityFlagsPredicate::isOnFire),
                    Codec.BOOL.optionalFieldOf("is_sneaking").forGetter(EntityFlagsPredicate::isCrouching),
                    Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(EntityFlagsPredicate::isSprinting),
                    Codec.BOOL.optionalFieldOf("is_swimming").forGetter(EntityFlagsPredicate::isSwimming),
                    Codec.BOOL.optionalFieldOf("is_flying").forGetter(EntityFlagsPredicate::isFlying),
                    Codec.BOOL.optionalFieldOf("is_baby").forGetter(EntityFlagsPredicate::isBaby)
                )
                .apply(p_340755_, EntityFlagsPredicate::new)
    );

    public boolean matches(Entity p_33697_) {
        if (this.isOnGround.isPresent() && p_33697_.onGround() != this.isOnGround.get()) {
            return false;
        } else if (this.isOnFire.isPresent() && p_33697_.isOnFire() != this.isOnFire.get()) {
            return false;
        } else if (this.isCrouching.isPresent() && p_33697_.isCrouching() != this.isCrouching.get()) {
            return false;
        } else if (this.isSprinting.isPresent() && p_33697_.isSprinting() != this.isSprinting.get()) {
            return false;
        } else if (this.isSwimming.isPresent() && p_33697_.isSwimming() != this.isSwimming.get()) {
            return false;
        } else {
            if (this.isFlying.isPresent()) {
                boolean flag1;
                label53: {
                    if (p_33697_ instanceof LivingEntity livingentity
                        && (livingentity.isFallFlying() || livingentity instanceof Player player && player.getAbilities().flying)) {
                        flag1 = true;
                        break label53;
                    }

                    flag1 = false;
                }

                boolean flag = flag1;
                if (flag != this.isFlying.get()) {
                    return false;
                }
            }

            if (this.isBaby.isPresent() && p_33697_ instanceof LivingEntity livingentity1 && livingentity1.isBaby() != this.isBaby.get()) {
                return false;
            }

            return true;
        }
    }

    public static class Builder {
        private Optional<Boolean> isOnGround = Optional.empty();
        private Optional<Boolean> isOnFire = Optional.empty();
        private Optional<Boolean> isCrouching = Optional.empty();
        private Optional<Boolean> isSprinting = Optional.empty();
        private Optional<Boolean> isSwimming = Optional.empty();
        private Optional<Boolean> isFlying = Optional.empty();
        private Optional<Boolean> isBaby = Optional.empty();

        public static EntityFlagsPredicate.Builder flags() {
            return new EntityFlagsPredicate.Builder();
        }

        public EntityFlagsPredicate.Builder setOnGround(Boolean p_344024_) {
            this.isOnGround = Optional.of(p_344024_);
            return this;
        }

        public EntityFlagsPredicate.Builder setOnFire(Boolean p_33715_) {
            this.isOnFire = Optional.of(p_33715_);
            return this;
        }

        public EntityFlagsPredicate.Builder setCrouching(Boolean p_150058_) {
            this.isCrouching = Optional.of(p_150058_);
            return this;
        }

        public EntityFlagsPredicate.Builder setSprinting(Boolean p_150060_) {
            this.isSprinting = Optional.of(p_150060_);
            return this;
        }

        public EntityFlagsPredicate.Builder setSwimming(Boolean p_150062_) {
            this.isSwimming = Optional.of(p_150062_);
            return this;
        }

        public EntityFlagsPredicate.Builder setIsFlying(Boolean p_345165_) {
            this.isFlying = Optional.of(p_345165_);
            return this;
        }

        public EntityFlagsPredicate.Builder setIsBaby(Boolean p_33718_) {
            this.isBaby = Optional.of(p_33718_);
            return this;
        }

        public EntityFlagsPredicate build() {
            return new EntityFlagsPredicate(this.isOnGround, this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isFlying, this.isBaby);
        }
    }
}