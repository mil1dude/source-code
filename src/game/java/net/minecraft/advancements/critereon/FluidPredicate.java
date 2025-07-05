package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public record FluidPredicate(Optional<HolderSet<Fluid>> fluids, Optional<StatePropertiesPredicate> properties) {
    public static final Codec<FluidPredicate> CODEC = RecordCodecBuilder.create(
        p_325215_ -> p_325215_.group(
                    RegistryCodecs.homogeneousList(Registries.FLUID).optionalFieldOf("fluids").forGetter(FluidPredicate::fluids),
                    StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(FluidPredicate::properties)
                )
                .apply(p_325215_, FluidPredicate::new)
    );

    public boolean matches(ServerLevel p_41105_, BlockPos p_41106_) {
        if (!p_41105_.isLoaded(p_41106_)) {
            return false;
        } else {
            FluidState fluidstate = p_41105_.getFluidState(p_41106_);
            return this.fluids.isPresent() && !fluidstate.is(this.fluids.get())
                ? false
                : !this.properties.isPresent() || this.properties.get().matches(fluidstate);
        }
    }

    public static class Builder {
        private Optional<HolderSet<Fluid>> fluids = Optional.empty();
        private Optional<StatePropertiesPredicate> properties = Optional.empty();

        private Builder() {
        }

        public static FluidPredicate.Builder fluid() {
            return new FluidPredicate.Builder();
        }

        public FluidPredicate.Builder of(Fluid p_151172_) {
            this.fluids = Optional.of(HolderSet.direct(p_151172_.builtInRegistryHolder()));
            return this;
        }

        public FluidPredicate.Builder of(HolderSet<Fluid> p_330747_) {
            this.fluids = Optional.of(p_330747_);
            return this;
        }

        public FluidPredicate.Builder setProperties(StatePropertiesPredicate p_151170_) {
            this.properties = Optional.of(p_151170_);
            return this;
        }

        public FluidPredicate build() {
            return new FluidPredicate(this.fluids, this.properties);
        }
    }
}