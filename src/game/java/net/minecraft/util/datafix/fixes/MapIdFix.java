package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class MapIdFix extends DataFix {
    public MapIdFix(Schema p_16396_, boolean p_16397_) {
        super(p_16396_, p_16397_);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "Map id fix",
            this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA),
            p_296632_ -> p_296632_.update(DSL.remainderFinder(), p_145512_ -> p_145512_.createMap(ImmutableMap.of(p_145512_.createString("data"), p_145512_)))
        );
    }
}