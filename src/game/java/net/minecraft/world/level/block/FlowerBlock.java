package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerBlock extends BushBlock implements SuspiciousEffectHolder {
    protected static final MapCodec<SuspiciousStewEffects> EFFECTS_FIELD = SuspiciousStewEffects.CODEC.fieldOf("suspicious_stew_effects");
    public static final MapCodec<FlowerBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_360432_ -> p_360432_.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply(p_360432_, FlowerBlock::new)
    );
    protected static final float AABB_OFFSET = 3.0F;
    protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);
    private final SuspiciousStewEffects suspiciousStewEffects;

    @Override
    public MapCodec<? extends FlowerBlock> codec() {
        return CODEC;
    }

    public FlowerBlock(Holder<MobEffect> p_334860_, float p_331000_, BlockBehaviour.Properties p_309749_) {
        this(makeEffectList(p_334860_, p_331000_), p_309749_);
    }

    public FlowerBlock(SuspiciousStewEffects p_330616_, BlockBehaviour.Properties p_53514_) {
        super(p_53514_);
        this.suspiciousStewEffects = p_330616_;
    }

    protected static SuspiciousStewEffects makeEffectList(Holder<MobEffect> p_335138_, float p_330663_) {
        return new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(p_335138_, Mth.floor(p_330663_ * 20.0F))));
    }

    @Override
    protected VoxelShape getShape(BlockState p_53517_, BlockGetter p_53518_, BlockPos p_53519_, CollisionContext p_53520_) {
        Vec3 vec3 = p_53517_.getOffset(p_53519_);
        return SHAPE.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public SuspiciousStewEffects getSuspiciousEffects() {
        return this.suspiciousStewEffects;
    }

    @Nullable
    public MobEffectInstance getBeeInteractionEffect() {
        return null;
    }
}