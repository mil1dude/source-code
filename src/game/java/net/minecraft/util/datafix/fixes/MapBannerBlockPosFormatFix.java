package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class MapBannerBlockPosFormatFix extends DataFix {
    public MapBannerBlockPosFormatFix(Schema p_333145_) {
        super(p_333145_, false);
    }

    private static <T> Dynamic<T> fixMapSavedData(Dynamic<T> p_328953_) {
        return p_328953_.update(
            "banners", p_331256_ -> p_331256_.createList(p_331256_.asStream().map(p_328913_ -> p_328913_.update("Pos", ExtraDataFixUtils::fixBlockPos)))
        );
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "MapBannerBlockPosFormatFix",
            this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA),
            p_331309_ -> p_331309_.update(DSL.remainderFinder(), p_334636_ -> p_334636_.update("data", MapBannerBlockPosFormatFix::fixMapSavedData))
        );
    }
}