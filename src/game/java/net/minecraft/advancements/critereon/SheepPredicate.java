package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

public record SheepPredicate(Optional<Boolean> sheared, Optional<DyeColor> color) implements EntitySubPredicate {
    public static final MapCodec<SheepPredicate> CODEC = RecordCodecBuilder.mapCodec(
        p_362292_ -> p_362292_.group(
                    Codec.BOOL.optionalFieldOf("sheared").forGetter(SheepPredicate::sheared),
                    DyeColor.CODEC.optionalFieldOf("color").forGetter(SheepPredicate::color)
                )
                .apply(p_362292_, SheepPredicate::new)
    );

    @Override
    public MapCodec<SheepPredicate> codec() {
        return EntitySubPredicates.SHEEP;
    }

    @Override
    public boolean matches(Entity p_365747_, ServerLevel p_366681_, @Nullable Vec3 p_363108_) {
        if (p_365747_ instanceof Sheep sheep) {
            return this.sheared.isPresent() && sheep.isSheared() != this.sheared.get()
                ? false
                : !this.color.isPresent() || sheep.getColor() == this.color.get();
        } else {
            return false;
        }
    }

    public static SheepPredicate hasWool(DyeColor p_361420_) {
        return new SheepPredicate(Optional.of(false), Optional.of(p_361420_));
    }
}