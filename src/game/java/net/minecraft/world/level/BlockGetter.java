package net.minecraft.world.level;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface BlockGetter extends LevelHeightAccessor {
    int MAX_BLOCK_ITERATIONS_ALONG_TRAVEL = 16;

    @Nullable
    BlockEntity getBlockEntity(BlockPos p_45570_);

    default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos p_151367_, BlockEntityType<T> p_151368_) {
        BlockEntity blockentity = this.getBlockEntity(p_151367_);
        return blockentity != null && blockentity.getType() == p_151368_ ? Optional.of((T)blockentity) : Optional.empty();
    }

    BlockState getBlockState(BlockPos p_45571_);

    FluidState getFluidState(BlockPos p_45569_);

    default int getLightEmission(BlockPos p_45572_) {
        return this.getBlockState(p_45572_).getLightEmission();
    }

    default Stream<BlockState> getBlockStates(AABB p_45557_) {
        return BlockPos.betweenClosedStream(p_45557_).map(this::getBlockState);
    }

    default BlockHitResult isBlockInLine(ClipBlockStateContext p_151354_) {
        return traverseBlocks(
            p_151354_.getFrom(),
            p_151354_.getTo(),
            p_151354_,
            (p_275154_, p_275155_) -> {
                BlockState blockstate = this.getBlockState(p_275155_);
                Vec3 vec3 = p_275154_.getFrom().subtract(p_275154_.getTo());
                return p_275154_.isTargetBlock().test(blockstate)
                    ? new BlockHitResult(
                        p_275154_.getTo(),
                        Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z),
                        BlockPos.containing(p_275154_.getTo()),
                        false
                    )
                    : null;
            },
            p_275156_ -> {
                Vec3 vec3 = p_275156_.getFrom().subtract(p_275156_.getTo());
                return BlockHitResult.miss(
                    p_275156_.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(p_275156_.getTo())
                );
            }
        );
    }

    default BlockHitResult clip(ClipContext p_45548_) {
        return traverseBlocks(
            p_45548_.getFrom(),
            p_45548_.getTo(),
            p_45548_,
            (p_151359_, p_151360_) -> {
                BlockState blockstate = this.getBlockState(p_151360_);
                FluidState fluidstate = this.getFluidState(p_151360_);
                Vec3 vec3 = p_151359_.getFrom();
                Vec3 vec31 = p_151359_.getTo();
                VoxelShape voxelshape = p_151359_.getBlockShape(blockstate, this, p_151360_);
                BlockHitResult blockhitresult = this.clipWithInteractionOverride(vec3, vec31, p_151360_, voxelshape, blockstate);
                VoxelShape voxelshape1 = p_151359_.getFluidShape(fluidstate, this, p_151360_);
                BlockHitResult blockhitresult1 = voxelshape1.clip(vec3, vec31, p_151360_);
                double d0 = blockhitresult == null ? Double.MAX_VALUE : p_151359_.getFrom().distanceToSqr(blockhitresult.getLocation());
                double d1 = blockhitresult1 == null ? Double.MAX_VALUE : p_151359_.getFrom().distanceToSqr(blockhitresult1.getLocation());
                return d0 <= d1 ? blockhitresult : blockhitresult1;
            },
            p_275153_ -> {
                Vec3 vec3 = p_275153_.getFrom().subtract(p_275153_.getTo());
                return BlockHitResult.miss(
                    p_275153_.getTo(), Direction.getApproximateNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(p_275153_.getTo())
                );
            }
        );
    }

    @Nullable
    default BlockHitResult clipWithInteractionOverride(Vec3 p_45559_, Vec3 p_45560_, BlockPos p_45561_, VoxelShape p_45562_, BlockState p_45563_) {
        BlockHitResult blockhitresult = p_45562_.clip(p_45559_, p_45560_, p_45561_);
        if (blockhitresult != null) {
            BlockHitResult blockhitresult1 = p_45563_.getInteractionShape(this, p_45561_).clip(p_45559_, p_45560_, p_45561_);
            if (blockhitresult1 != null && blockhitresult1.getLocation().subtract(p_45559_).lengthSqr() < blockhitresult.getLocation().subtract(p_45559_).lengthSqr()) {
                return blockhitresult.withDirection(blockhitresult1.getDirection());
            }
        }

        return blockhitresult;
    }

    default double getBlockFloorHeight(VoxelShape p_45565_, Supplier<VoxelShape> p_45566_) {
        if (!p_45565_.isEmpty()) {
            return p_45565_.max(Direction.Axis.Y);
        } else {
            double d0 = p_45566_.get().max(Direction.Axis.Y);
            return d0 >= 1.0 ? d0 - 1.0 : Double.NEGATIVE_INFINITY;
        }
    }

    default double getBlockFloorHeight(BlockPos p_45574_) {
        return this.getBlockFloorHeight(this.getBlockState(p_45574_).getCollisionShape(this, p_45574_), () -> {
            BlockPos blockpos = p_45574_.below();
            return this.getBlockState(blockpos).getCollisionShape(this, blockpos);
        });
    }

    static <T, C> T traverseBlocks(Vec3 p_151362_, Vec3 p_151363_, C p_151364_, BiFunction<C, BlockPos, T> p_151365_, Function<C, T> p_151366_) {
        if (p_151362_.equals(p_151363_)) {
            return p_151366_.apply(p_151364_);
        } else {
            double d0 = Mth.lerp(-1.0E-7, p_151363_.x, p_151362_.x);
            double d1 = Mth.lerp(-1.0E-7, p_151363_.y, p_151362_.y);
            double d2 = Mth.lerp(-1.0E-7, p_151363_.z, p_151362_.z);
            double d3 = Mth.lerp(-1.0E-7, p_151362_.x, p_151363_.x);
            double d4 = Mth.lerp(-1.0E-7, p_151362_.y, p_151363_.y);
            double d5 = Mth.lerp(-1.0E-7, p_151362_.z, p_151363_.z);
            int i = Mth.floor(d3);
            int j = Mth.floor(d4);
            int k = Mth.floor(d5);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(i, j, k);
            T t = p_151365_.apply(p_151364_, blockpos$mutableblockpos);
            if (t != null) {
                return t;
            } else {
                double d6 = d0 - d3;
                double d7 = d1 - d4;
                double d8 = d2 - d5;
                int l = Mth.sign(d6);
                int i1 = Mth.sign(d7);
                int j1 = Mth.sign(d8);
                double d9 = l == 0 ? Double.MAX_VALUE : (double)l / d6;
                double d10 = i1 == 0 ? Double.MAX_VALUE : (double)i1 / d7;
                double d11 = j1 == 0 ? Double.MAX_VALUE : (double)j1 / d8;
                double d12 = d9 * (l > 0 ? 1.0 - Mth.frac(d3) : Mth.frac(d3));
                double d13 = d10 * (i1 > 0 ? 1.0 - Mth.frac(d4) : Mth.frac(d4));
                double d14 = d11 * (j1 > 0 ? 1.0 - Mth.frac(d5) : Mth.frac(d5));

                while (d12 <= 1.0 || d13 <= 1.0 || d14 <= 1.0) {
                    if (d12 < d13) {
                        if (d12 < d14) {
                            i += l;
                            d12 += d9;
                        } else {
                            k += j1;
                            d14 += d11;
                        }
                    } else if (d13 < d14) {
                        j += i1;
                        d13 += d10;
                    } else {
                        k += j1;
                        d14 += d11;
                    }

                    T t1 = p_151365_.apply(p_151364_, blockpos$mutableblockpos.set(i, j, k));
                    if (t1 != null) {
                        return t1;
                    }
                }

                return p_151366_.apply(p_151364_);
            }
        }
    }

    static Iterable<BlockPos> boxTraverseBlocks(Vec3 p_365175_, Vec3 p_365388_, AABB p_364385_) {
        Vec3 vec3 = p_365388_.subtract(p_365175_);
        Iterable<BlockPos> iterable = BlockPos.betweenClosed(p_364385_);
        if (vec3.lengthSqr() < (double)Mth.square(0.99999F)) {
            return iterable;
        } else {
            Set<BlockPos> set = new ObjectLinkedOpenHashSet<>();
            Vec3 vec31 = p_364385_.getMinPosition();
            Vec3 vec32 = vec31.subtract(vec3);
            addCollisionsAlongTravel(set, vec32, vec31, p_364385_);

            for (BlockPos blockpos : iterable) {
                set.add(blockpos.immutable());
            }

            return set;
        }
    }

    private static void addCollisionsAlongTravel(Set<BlockPos> p_369132_, Vec3 p_362149_, Vec3 p_368644_, AABB p_364725_) {
        Vec3 vec3 = p_368644_.subtract(p_362149_);
        int i = Mth.floor(p_362149_.x);
        int j = Mth.floor(p_362149_.y);
        int k = Mth.floor(p_362149_.z);
        int l = Mth.sign(vec3.x);
        int i1 = Mth.sign(vec3.y);
        int j1 = Mth.sign(vec3.z);
        double d0 = l == 0 ? Double.MAX_VALUE : (double)l / vec3.x;
        double d1 = i1 == 0 ? Double.MAX_VALUE : (double)i1 / vec3.y;
        double d2 = j1 == 0 ? Double.MAX_VALUE : (double)j1 / vec3.z;
        double d3 = d0 * (l > 0 ? 1.0 - Mth.frac(p_362149_.x) : Mth.frac(p_362149_.x));
        double d4 = d1 * (i1 > 0 ? 1.0 - Mth.frac(p_362149_.y) : Mth.frac(p_362149_.y));
        double d5 = d2 * (j1 > 0 ? 1.0 - Mth.frac(p_362149_.z) : Mth.frac(p_362149_.z));
        int k1 = 0;

        while (d3 <= 1.0 || d4 <= 1.0 || d5 <= 1.0) {
            if (d3 < d4) {
                if (d3 < d5) {
                    i += l;
                    d3 += d0;
                } else {
                    k += j1;
                    d5 += d2;
                }
            } else if (d4 < d5) {
                j += i1;
                d4 += d1;
            } else {
                k += j1;
                d5 += d2;
            }

            if (k1++ > 16) {
                break;
            }

            Optional<Vec3> optional = AABB.clip((double)i, (double)j, (double)k, (double)(i + 1), (double)(j + 1), (double)(k + 1), p_362149_, p_368644_);
            if (!optional.isEmpty()) {
                Vec3 vec31 = optional.get();
                double d6 = Mth.clamp(vec31.x, (double)i + 1.0E-5F, (double)i + 1.0 - 1.0E-5F);
                double d7 = Mth.clamp(vec31.y, (double)j + 1.0E-5F, (double)j + 1.0 - 1.0E-5F);
                double d8 = Mth.clamp(vec31.z, (double)k + 1.0E-5F, (double)k + 1.0 - 1.0E-5F);
                int l1 = Mth.floor(d6 + p_364725_.getXsize());
                int i2 = Mth.floor(d7 + p_364725_.getYsize());
                int j2 = Mth.floor(d8 + p_364725_.getZsize());

                for (int k2 = i; k2 <= l1; k2++) {
                    for (int l2 = j; l2 <= i2; l2++) {
                        for (int i3 = k; i3 <= j2; i3++) {
                            p_369132_.add(new BlockPos(k2, l2, i3));
                        }
                    }
                }
            }
        }
    }
}