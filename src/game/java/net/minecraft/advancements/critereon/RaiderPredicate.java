package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.Vec3;

public record RaiderPredicate(boolean hasRaid, boolean isCaptain) implements EntitySubPredicate {
    public static final MapCodec<RaiderPredicate> CODEC = RecordCodecBuilder.mapCodec(
        p_334461_ -> p_334461_.group(
                    Codec.BOOL.optionalFieldOf("has_raid", Boolean.valueOf(false)).forGetter(RaiderPredicate::hasRaid),
                    Codec.BOOL.optionalFieldOf("is_captain", Boolean.valueOf(false)).forGetter(RaiderPredicate::isCaptain)
                )
                .apply(p_334461_, RaiderPredicate::new)
    );
    public static final RaiderPredicate CAPTAIN_WITHOUT_RAID = new RaiderPredicate(false, true);

    @Override
    public MapCodec<RaiderPredicate> codec() {
        return EntitySubPredicates.RAIDER;
    }

    @Override
    public boolean matches(Entity p_333043_, ServerLevel p_332324_, @Nullable Vec3 p_334148_) {
        return !(p_333043_ instanceof Raider raider) ? false : raider.hasRaid() == this.hasRaid && raider.isCaptain() == this.isCaptain;
    }
}