package net.minecraft.world.level.block;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class TorchBlock extends BaseTorchBlock {
    protected static final MapCodec<SimpleParticleType> PARTICLE_OPTIONS_FIELD = BuiltInRegistries.PARTICLE_TYPE
        .byNameCodec()
        .comapFlatMap(
            p_311133_ -> p_311133_ instanceof SimpleParticleType simpleparticletype
                    ? DataResult.success(simpleparticletype)
                    : DataResult.error(() -> "Not a SimpleParticleType: " + p_311133_),
            p_311047_ -> (ParticleType<?>)p_311047_
        )
        .fieldOf("particle_options");
    public static final MapCodec<TorchBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_360457_ -> p_360457_.group(PARTICLE_OPTIONS_FIELD.forGetter(p_309438_ -> p_309438_.flameParticle), propertiesCodec()).apply(p_360457_, TorchBlock::new)
    );
    protected final SimpleParticleType flameParticle;

    @Override
    public MapCodec<? extends TorchBlock> codec() {
        return CODEC;
    }

    protected TorchBlock(SimpleParticleType p_310235_, BlockBehaviour.Properties p_57491_) {
        super(p_57491_);
        this.flameParticle = p_310235_;
    }

    @Override
    public void animateTick(BlockState p_222593_, Level p_222594_, BlockPos p_222595_, RandomSource p_222596_) {
        double d0 = (double)p_222595_.getX() + 0.5;
        double d1 = (double)p_222595_.getY() + 0.7;
        double d2 = (double)p_222595_.getZ() + 0.5;
        p_222594_.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
        p_222594_.addParticle(this.flameParticle, d0, d1, d2, 0.0, 0.0, 0.0);
    }
}