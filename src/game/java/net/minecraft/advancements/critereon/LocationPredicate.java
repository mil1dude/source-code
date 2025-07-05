package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;

public record LocationPredicate(
    Optional<LocationPredicate.PositionPredicate> position,
    Optional<HolderSet<Biome>> biomes,
    Optional<HolderSet<Structure>> structures,
    Optional<ResourceKey<Level>> dimension,
    Optional<Boolean> smokey,
    Optional<LightPredicate> light,
    Optional<BlockPredicate> block,
    Optional<FluidPredicate> fluid,
    Optional<Boolean> canSeeSky
) {
    public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create(
        p_296137_ -> p_296137_.group(
                    LocationPredicate.PositionPredicate.CODEC.optionalFieldOf("position").forGetter(LocationPredicate::position),
                    RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes").forGetter(LocationPredicate::biomes),
                    RegistryCodecs.homogeneousList(Registries.STRUCTURE).optionalFieldOf("structures").forGetter(LocationPredicate::structures),
                    ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(LocationPredicate::dimension),
                    Codec.BOOL.optionalFieldOf("smokey").forGetter(LocationPredicate::smokey),
                    LightPredicate.CODEC.optionalFieldOf("light").forGetter(LocationPredicate::light),
                    BlockPredicate.CODEC.optionalFieldOf("block").forGetter(LocationPredicate::block),
                    FluidPredicate.CODEC.optionalFieldOf("fluid").forGetter(LocationPredicate::fluid),
                    Codec.BOOL.optionalFieldOf("can_see_sky").forGetter(LocationPredicate::canSeeSky)
                )
                .apply(p_296137_, LocationPredicate::new)
    );

    public boolean matches(ServerLevel p_52618_, double p_52619_, double p_52620_, double p_52621_) {
        if (this.position.isPresent() && !this.position.get().matches(p_52619_, p_52620_, p_52621_)) {
            return false;
        } else if (this.dimension.isPresent() && this.dimension.get() != p_52618_.dimension()) {
            return false;
        } else {
            BlockPos blockpos = BlockPos.containing(p_52619_, p_52620_, p_52621_);
            boolean flag = p_52618_.isLoaded(blockpos);
            if (!this.biomes.isPresent() || flag && this.biomes.get().contains(p_52618_.getBiome(blockpos))) {
                if (!this.structures.isPresent() || flag && p_52618_.structureManager().getStructureWithPieceAt(blockpos, this.structures.get()).isValid()) {
                    if (!this.smokey.isPresent() || flag && this.smokey.get() == CampfireBlock.isSmokeyPos(p_52618_, blockpos)) {
                        if (this.light.isPresent() && !this.light.get().matches(p_52618_, blockpos)) {
                            return false;
                        } else if (this.block.isPresent() && !this.block.get().matches(p_52618_, blockpos)) {
                            return false;
                        } else {
                            return this.fluid.isPresent() && !this.fluid.get().matches(p_52618_, blockpos)
                                ? false
                                : !this.canSeeSky.isPresent() || this.canSeeSky.get() == p_52618_.canSeeSky(blockpos);
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public static class Builder {
        private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
        private Optional<HolderSet<Biome>> biomes = Optional.empty();
        private Optional<HolderSet<Structure>> structures = Optional.empty();
        private Optional<ResourceKey<Level>> dimension = Optional.empty();
        private Optional<Boolean> smokey = Optional.empty();
        private Optional<LightPredicate> light = Optional.empty();
        private Optional<BlockPredicate> block = Optional.empty();
        private Optional<FluidPredicate> fluid = Optional.empty();
        private Optional<Boolean> canSeeSky = Optional.empty();

        public static LocationPredicate.Builder location() {
            return new LocationPredicate.Builder();
        }

        public static LocationPredicate.Builder inBiome(Holder<Biome> p_334208_) {
            return location().setBiomes(HolderSet.direct(p_334208_));
        }

        public static LocationPredicate.Builder inDimension(ResourceKey<Level> p_300753_) {
            return location().setDimension(p_300753_);
        }

        public static LocationPredicate.Builder inStructure(Holder<Structure> p_333866_) {
            return location().setStructures(HolderSet.direct(p_333866_));
        }

        public static LocationPredicate.Builder atYLocation(MinMaxBounds.Doubles p_297662_) {
            return location().setY(p_297662_);
        }

        public LocationPredicate.Builder setX(MinMaxBounds.Doubles p_153971_) {
            this.x = p_153971_;
            return this;
        }

        public LocationPredicate.Builder setY(MinMaxBounds.Doubles p_153975_) {
            this.y = p_153975_;
            return this;
        }

        public LocationPredicate.Builder setZ(MinMaxBounds.Doubles p_153979_) {
            this.z = p_153979_;
            return this;
        }

        public LocationPredicate.Builder setBiomes(HolderSet<Biome> p_330531_) {
            this.biomes = Optional.of(p_330531_);
            return this;
        }

        public LocationPredicate.Builder setStructures(HolderSet<Structure> p_330147_) {
            this.structures = Optional.of(p_330147_);
            return this;
        }

        public LocationPredicate.Builder setDimension(ResourceKey<Level> p_153977_) {
            this.dimension = Optional.of(p_153977_);
            return this;
        }

        public LocationPredicate.Builder setLight(LightPredicate.Builder p_298990_) {
            this.light = Optional.of(p_298990_.build());
            return this;
        }

        public LocationPredicate.Builder setBlock(BlockPredicate.Builder p_298525_) {
            this.block = Optional.of(p_298525_.build());
            return this;
        }

        public LocationPredicate.Builder setFluid(FluidPredicate.Builder p_298614_) {
            this.fluid = Optional.of(p_298614_.build());
            return this;
        }

        public LocationPredicate.Builder setSmokey(boolean p_299005_) {
            this.smokey = Optional.of(p_299005_);
            return this;
        }

        public LocationPredicate.Builder setCanSeeSky(boolean p_342130_) {
            this.canSeeSky = Optional.of(p_342130_);
            return this;
        }

        public LocationPredicate build() {
            Optional<LocationPredicate.PositionPredicate> optional = LocationPredicate.PositionPredicate.of(this.x, this.y, this.z);
            return new LocationPredicate(
                optional, this.biomes, this.structures, this.dimension, this.smokey, this.light, this.block, this.fluid, this.canSeeSky
            );
        }
    }

    static record PositionPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z) {
        public static final Codec<LocationPredicate.PositionPredicate> CODEC = RecordCodecBuilder.create(
            p_325229_ -> p_325229_.group(
                        MinMaxBounds.Doubles.CODEC
                            .optionalFieldOf("x", MinMaxBounds.Doubles.ANY)
                            .forGetter(LocationPredicate.PositionPredicate::x),
                        MinMaxBounds.Doubles.CODEC
                            .optionalFieldOf("y", MinMaxBounds.Doubles.ANY)
                            .forGetter(LocationPredicate.PositionPredicate::y),
                        MinMaxBounds.Doubles.CODEC
                            .optionalFieldOf("z", MinMaxBounds.Doubles.ANY)
                            .forGetter(LocationPredicate.PositionPredicate::z)
                    )
                    .apply(p_325229_, LocationPredicate.PositionPredicate::new)
        );

        static Optional<LocationPredicate.PositionPredicate> of(
            MinMaxBounds.Doubles p_300563_, MinMaxBounds.Doubles p_301250_, MinMaxBounds.Doubles p_299764_
        ) {
            return p_300563_.isAny() && p_301250_.isAny() && p_299764_.isAny()
                ? Optional.empty()
                : Optional.of(new LocationPredicate.PositionPredicate(p_300563_, p_301250_, p_299764_));
        }

        public boolean matches(double p_299909_, double p_298621_, double p_299854_) {
            return this.x.matches(p_299909_) && this.y.matches(p_298621_) && this.z.matches(p_299854_);
        }
    }
}