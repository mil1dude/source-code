package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlockEntityCustomNameToComponentFix extends DataFix {
    public BlockEntityCustomNameToComponentFix(Schema p_14817_, boolean p_14818_) {
        super(p_14817_, p_14818_);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<String> opticfinder = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
        return this.fixTypeEverywhereTyped(
            "BlockEntityCustomNameToComponentFix",
            this.getInputSchema().getType(References.BLOCK_ENTITY),
            p_14821_ -> p_14821_.update(
                    DSL.remainderFinder(),
                    p_145133_ -> {
                        Optional<String> optional = p_14821_.getOptional(opticfinder);
                        return optional.isPresent() && Objects.equals(optional.get(), "minecraft:command_block")
                            ? p_145133_
                            : EntityCustomNameToComponentFix.fixTagCustomName(p_145133_);
                    }
                )
        );
    }
}