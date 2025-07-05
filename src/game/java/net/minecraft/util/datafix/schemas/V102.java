package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V102 extends Schema {
    public V102(int p_17356_, Schema p_17357_) {
        super(p_17356_, p_17357_);
    }

    @Override
    public void registerTypes(Schema p_17361_, Map<String, Supplier<TypeTemplate>> p_17362_, Map<String, Supplier<TypeTemplate>> p_17363_) {
        super.registerTypes(p_17361_, p_17362_, p_17363_);
        p_17361_.registerType(
            true,
            References.ITEM_STACK,
            () -> DSL.hook(
                    DSL.optionalFields(
                        "id",
                        References.ITEM_NAME.in(p_17361_),
                        "tag",
                        DSL.optionalFields(
                            Pair.of("EntityTag", References.ENTITY_TREE.in(p_17361_)),
                            Pair.of("BlockEntityTag", References.BLOCK_ENTITY.in(p_17361_)),
                            Pair.of("CanDestroy", DSL.list(References.BLOCK_NAME.in(p_17361_))),
                            Pair.of("CanPlaceOn", DSL.list(References.BLOCK_NAME.in(p_17361_))),
                            Pair.of("Items", DSL.list(References.ITEM_STACK.in(p_17361_))),
                            Pair.of("ChargedProjectiles", DSL.list(References.ITEM_STACK.in(p_17361_)))
                        )
                    ),
                    V99.ADD_NAMES,
                    HookFunction.IDENTITY
                )
        );
    }
}