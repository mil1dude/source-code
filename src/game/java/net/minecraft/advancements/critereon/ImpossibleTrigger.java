package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;

public class ImpossibleTrigger implements CriterionTrigger<ImpossibleTrigger.TriggerInstance> {
    @Override
    public void addPlayerListener(PlayerAdvancements p_41565_, CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> p_41566_) {
    }

    @Override
    public void removePlayerListener(PlayerAdvancements p_41572_, CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> p_41573_) {
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements p_41563_) {
    }

    @Override
    public Codec<ImpossibleTrigger.TriggerInstance> codec() {
        return ImpossibleTrigger.TriggerInstance.CODEC;
    }

    public static record TriggerInstance() implements CriterionTriggerInstance {
        public static final Codec<ImpossibleTrigger.TriggerInstance> CODEC = Codec.unit(new ImpossibleTrigger.TriggerInstance());

        @Override
        public void validate(CriterionValidator p_312764_) {
        }
    }
}