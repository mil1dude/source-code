package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class OminousBannerRarityFix extends DataFix {
    public OminousBannerRarityFix(Schema p_362925_) {
        super(p_362925_, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type<?> type1 = this.getInputSchema().getType(References.ITEM_STACK);
        TaggedChoiceType<?> taggedchoicetype = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> opticfinder1 = type.findField("components");
        OpticFinder<?> opticfinder2 = type1.findField("components");
        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("Ominous Banner block entity common rarity to uncommon rarity fix", type, p_360880_ -> {
            Object object = p_360880_.get(taggedchoicetype.finder()).getFirst();
            return object.equals("minecraft:banner") ? this.fix(p_360880_, opticfinder1) : p_360880_;
        }), this.fixTypeEverywhereTyped("Ominous Banner item stack common rarity to uncommon rarity fix", type1, p_367011_ -> {
            String s = p_367011_.getOptional(opticfinder).map(Pair::getSecond).orElse("");
            return s.equals("minecraft:white_banner") ? this.fix(p_367011_, opticfinder2) : p_367011_;
        }));
    }

    private Typed<?> fix(Typed<?> p_362105_, OpticFinder<?> p_363836_) {
        return p_362105_.updateTyped(
            p_363836_,
            p_368506_ -> p_368506_.update(
                    DSL.remainderFinder(),
                    p_364341_ -> {
                        boolean flag = p_364341_.get("minecraft:item_name")
                            .asString()
                            .result()
                            .flatMap(ComponentDataFixUtils::extractTranslationString)
                            .filter(p_368287_ -> p_368287_.equals("block.minecraft.ominous_banner"))
                            .isPresent();
                        return flag
                            ? p_364341_.set("minecraft:rarity", p_364341_.createString("uncommon"))
                                .set("minecraft:item_name", ComponentDataFixUtils.createTranslatableComponent(p_364341_.getOps(), "block.minecraft.ominous_banner"))
                            : p_364341_;
                    }
                )
        );
    }
}