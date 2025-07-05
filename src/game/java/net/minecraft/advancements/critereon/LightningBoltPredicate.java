package net.minecraft.advancements.critereon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public record LightningBoltPredicate(MinMaxBounds.Ints blocksSetOnFire, Optional<EntityPredicate> entityStruck) implements EntitySubPredicate {
    public static final MapCodec<LightningBoltPredicate> CODEC = RecordCodecBuilder.mapCodec(
        p_325227_ -> p_325227_.group(
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("blocks_set_on_fire", MinMaxBounds.Ints.ANY).forGetter(LightningBoltPredicate::blocksSetOnFire),
                    EntityPredicate.CODEC.optionalFieldOf("entity_struck").forGetter(LightningBoltPredicate::entityStruck)
                )
                .apply(p_325227_, LightningBoltPredicate::new)
    );

    public static LightningBoltPredicate blockSetOnFire(MinMaxBounds.Ints p_297323_) {
        return new LightningBoltPredicate(p_297323_, Optional.empty());
    }

    @Override
    public MapCodec<LightningBoltPredicate> codec() {
        return EntitySubPredicates.LIGHTNING;
    }

    @Override
    public boolean matches(Entity p_300332_, ServerLevel p_297594_, @Nullable Vec3 p_298602_) {
        return !(p_300332_ instanceof LightningBolt lightningbolt)
            ? false
            : this.blocksSetOnFire.matches(lightningbolt.getBlocksSetOnFire())
                && (this.entityStruck.isEmpty() || lightningbolt.getHitEntities().anyMatch(p_299409_ -> this.entityStruck.get().matches(p_297594_, p_298602_, p_299409_)));
    }
}