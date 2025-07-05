package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class BlockEntitySignDoubleSidedEditableTextFix extends NamedEntityFix {
    public static final String FILTERED_CORRECT = "_filtered_correct";
    private static final String DEFAULT_COLOR = "black";

    public BlockEntitySignDoubleSidedEditableTextFix(Schema p_277789_, String p_278061_, String p_277403_) {
        super(p_277789_, false, p_278061_, References.BLOCK_ENTITY, p_277403_);
    }

    private static <T> Dynamic<T> fixTag(Dynamic<T> p_278110_) {
        return p_278110_.set("front_text", fixFrontTextTag(p_278110_)).set("back_text", createDefaultText(p_278110_)).set("is_waxed", p_278110_.createBoolean(false));
    }

    private static <T> Dynamic<T> fixFrontTextTag(Dynamic<T> p_300654_) {
        Dynamic<T> dynamic = ComponentDataFixUtils.createEmptyComponent(p_300654_.getOps());
        List<Dynamic<T>> list = getLines(p_300654_, "Text").map(p_297945_ -> p_297945_.orElse(dynamic)).toList();
        Dynamic<T> dynamic1 = p_300654_.emptyMap()
            .set("messages", p_300654_.createList(list.stream()))
            .set("color", p_300654_.get("Color").result().orElse(p_300654_.createString("black")))
            .set("has_glowing_text", p_300654_.get("GlowingText").result().orElse(p_300654_.createBoolean(false)))
            .set("_filtered_correct", p_300654_.createBoolean(true));
        List<Optional<Dynamic<T>>> list1 = getLines(p_300654_, "FilteredText").toList();
        if (list1.stream().anyMatch(Optional::isPresent)) {
            dynamic1 = dynamic1.set("filtered_messages", p_300654_.createList(Streams.mapWithIndex(list1.stream(), (p_299542_, p_300269_) -> {
                Dynamic<T> dynamic2 = list.get((int)p_300269_);
                return p_299542_.orElse(dynamic2);
            })));
        }

        return dynamic1;
    }

    private static <T> Stream<Optional<Dynamic<T>>> getLines(Dynamic<T> p_298173_, String p_299789_) {
        return Stream.of(
            p_298173_.get(p_299789_ + "1").result(),
            p_298173_.get(p_299789_ + "2").result(),
            p_298173_.get(p_299789_ + "3").result(),
            p_298173_.get(p_299789_ + "4").result()
        );
    }

    private static <T> Dynamic<T> createDefaultText(Dynamic<T> p_299439_) {
        return p_299439_.emptyMap()
            .set("messages", createEmptyLines(p_299439_))
            .set("color", p_299439_.createString("black"))
            .set("has_glowing_text", p_299439_.createBoolean(false));
    }

    private static <T> Dynamic<T> createEmptyLines(Dynamic<T> p_299579_) {
        Dynamic<T> dynamic = ComponentDataFixUtils.createEmptyComponent(p_299579_.getOps());
        return p_299579_.createList(Stream.of(dynamic, dynamic, dynamic, dynamic));
    }

    @Override
    protected Typed<?> fix(Typed<?> p_277962_) {
        return p_277962_.update(DSL.remainderFinder(), BlockEntitySignDoubleSidedEditableTextFix::fixTag);
    }
}