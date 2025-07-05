package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class RandomSequenceSettingsFix extends DataFix {
    public RandomSequenceSettingsFix(Schema p_299509_) {
        super(p_299509_, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "RandomSequenceSettingsFix",
            this.getInputSchema().getType(References.SAVED_DATA_RANDOM_SEQUENCES),
            p_298336_ -> p_298336_.update(
                    DSL.remainderFinder(), p_297894_ -> p_297894_.update("data", p_299932_ -> p_299932_.emptyMap().set("sequences", p_299932_))
                )
        );
    }
}