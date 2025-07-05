package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class OptionsMenuBlurrinessFix extends DataFix {
    public OptionsMenuBlurrinessFix(Schema p_342701_) {
        super(p_342701_, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "OptionsMenuBlurrinessFix",
            this.getInputSchema().getType(References.OPTIONS),
            p_342873_ -> p_342873_.update(
                    DSL.remainderFinder(),
                    p_343322_ -> p_343322_.update("menuBackgroundBlurriness", p_344729_ -> p_344729_.createInt(this.convertToIntRange(p_344729_.asString("0.5"))))
                )
        );
    }

    private int convertToIntRange(String p_343273_) {
        try {
            return Math.round(Float.parseFloat(p_343273_) * 10.0F);
        } catch (NumberFormatException numberformatexception) {
            return 5;
        }
    }
}