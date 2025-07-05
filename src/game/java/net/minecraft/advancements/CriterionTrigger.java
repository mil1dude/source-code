package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.server.PlayerAdvancements;

public interface CriterionTrigger<T extends CriterionTriggerInstance> {
    void addPlayerListener(PlayerAdvancements p_13674_, CriterionTrigger.Listener<T> p_13675_);

    void removePlayerListener(PlayerAdvancements p_13676_, CriterionTrigger.Listener<T> p_13677_);

    void removePlayerListeners(PlayerAdvancements p_13673_);

    Codec<T> codec();

    default Criterion<T> createCriterion(T p_299598_) {
        return new Criterion<>(this, p_299598_);
    }

    public static record Listener<T extends CriterionTriggerInstance>(T trigger, AdvancementHolder advancement, String criterion) {
        public void run(PlayerAdvancements p_13687_) {
            p_13687_.award(this.advancement, this.criterion);
        }
    }
}