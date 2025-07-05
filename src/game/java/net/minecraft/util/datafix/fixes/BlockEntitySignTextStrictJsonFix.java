package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class BlockEntitySignTextStrictJsonFix extends NamedEntityFix {
    public BlockEntitySignTextStrictJsonFix(Schema p_14864_, boolean p_14865_) {
        super(p_14864_, p_14865_, "BlockEntitySignTextStrictJsonFix", References.BLOCK_ENTITY, "Sign");
    }

    private Dynamic<?> updateLine(Dynamic<?> p_14871_, String p_14872_) {
        return p_14871_.update(p_14872_, ComponentDataFixUtils::rewriteFromLenient);
    }

    @Override
    protected Typed<?> fix(Typed<?> p_14867_) {
        return p_14867_.update(DSL.remainderFinder(), p_14869_ -> {
            p_14869_ = this.updateLine(p_14869_, "Text1");
            p_14869_ = this.updateLine(p_14869_, "Text2");
            p_14869_ = this.updateLine(p_14869_, "Text3");
            return this.updateLine(p_14869_, "Text4");
        });
    }
}