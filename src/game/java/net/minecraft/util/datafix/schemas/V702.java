package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V702 extends Schema {
    public V702(int p_18007_, Schema p_18008_) {
        super(p_18007_, p_18008_);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema p_18016_) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(p_18016_);
        p_18016_.register(
            map,
            "ZombieVillager",
            p_326703_ -> DSL.optionalFields("Offers", DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in(p_18016_))), V100.equipment(p_18016_))
        );
        p_18016_.register(map, "Husk", () -> V100.equipment(p_18016_));
        return map;
    }
}