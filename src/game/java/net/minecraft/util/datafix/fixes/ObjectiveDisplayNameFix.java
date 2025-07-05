package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class ObjectiveDisplayNameFix extends DataFix {
    public ObjectiveDisplayNameFix(Schema p_16521_, boolean p_16522_) {
        super(p_16521_, p_16522_);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.OBJECTIVE);
        return this.fixTypeEverywhereTyped(
            "ObjectiveDisplayNameFix",
            type,
            p_181039_ -> p_181039_.update(DSL.remainderFinder(), p_308994_ -> p_308994_.update("DisplayName", ComponentDataFixUtils::wrapLiteralStringAsComponent))
        );
    }
}