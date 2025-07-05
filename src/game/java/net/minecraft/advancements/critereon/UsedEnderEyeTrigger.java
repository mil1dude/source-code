package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger extends SimpleCriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
    @Override
    public Codec<UsedEnderEyeTrigger.TriggerInstance> codec() {
        return UsedEnderEyeTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer p_73936_, BlockPos p_73937_) {
        double d0 = p_73936_.getX() - (double)p_73937_.getX();
        double d1 = p_73936_.getZ() - (double)p_73937_.getZ();
        double d2 = d0 * d0 + d1 * d1;
        this.trigger(p_73936_, p_73934_ -> p_73934_.matches(d2));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Doubles distance)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<UsedEnderEyeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_325257_ -> p_325257_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UsedEnderEyeTrigger.TriggerInstance::player),
                        MinMaxBounds.Doubles.CODEC
                            .optionalFieldOf("distance", MinMaxBounds.Doubles.ANY)
                            .forGetter(UsedEnderEyeTrigger.TriggerInstance::distance)
                    )
                    .apply(p_325257_, UsedEnderEyeTrigger.TriggerInstance::new)
        );

        public boolean matches(double p_73952_) {
            return this.distance.matchesSqr(p_73952_);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }
}