package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class ItemCustomNameToComponentFix extends DataFix {
    public ItemCustomNameToComponentFix(Schema p_15927_, boolean p_15928_) {
        super(p_15927_, p_15928_);
    }

    private Dynamic<?> fixTag(Dynamic<?> p_15935_) {
        Optional<? extends Dynamic<?>> optional = p_15935_.get("display").result();
        if (optional.isPresent()) {
            Dynamic<?> dynamic = (Dynamic<?>)optional.get();
            Optional<String> optional1 = dynamic.get("Name").asString().result();
            if (optional1.isPresent()) {
                dynamic = dynamic.set("Name", ComponentDataFixUtils.createPlainTextComponent(dynamic.getOps(), optional1.get()));
            }

            return p_15935_.set("display", dynamic);
        } else {
            return p_15935_;
        }
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> opticfinder = type.findField("tag");
        return this.fixTypeEverywhereTyped(
            "ItemCustomNameToComponentFix",
            type,
            p_15931_ -> p_15931_.updateTyped(opticfinder, p_145384_ -> p_145384_.update(DSL.remainderFinder(), this::fixTag))
        );
    }
}