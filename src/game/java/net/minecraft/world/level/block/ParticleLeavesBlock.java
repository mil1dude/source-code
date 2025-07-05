package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ParticleLeavesBlock extends LeavesBlock {
    public static final MapCodec<ParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_375713_ -> p_375713_.group(
                    ExtraCodecs.POSITIVE_INT.fieldOf("chance").forGetter(p_375878_ -> p_375878_.chance),
                    ParticleTypes.CODEC.fieldOf("particle").forGetter(p_376627_ -> p_376627_.particle),
                    propertiesCodec()
                )
                .apply(p_375713_, ParticleLeavesBlock::new)
    );
    private final ParticleOptions particle;
    private final int chance;

    @Override
    public MapCodec<ParticleLeavesBlock> codec() {
        return CODEC;
    }

    public ParticleLeavesBlock(int p_377019_, ParticleOptions p_378342_, BlockBehaviour.Properties p_377172_) {
        super(p_377172_);
        this.chance = p_377019_;
        this.particle = p_378342_;
    }

    @Override
    public void animateTick(BlockState p_376517_, Level p_377736_, BlockPos p_376886_, RandomSource p_376818_) {
        super.animateTick(p_376517_, p_377736_, p_376886_, p_376818_);
        if (p_376818_.nextInt(this.chance) == 0) {
            BlockPos blockpos = p_376886_.below();
            BlockState blockstate = p_377736_.getBlockState(blockpos);
            if (!isFaceFull(blockstate.getCollisionShape(p_377736_, blockpos), Direction.UP)) {
                ParticleUtils.spawnParticleBelow(p_377736_, p_376886_, p_376818_, this.particle);
            }
        }
    }
}