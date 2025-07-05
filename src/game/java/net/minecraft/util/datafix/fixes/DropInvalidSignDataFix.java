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

public class DropInvalidSignDataFix extends NamedEntityFix {
    private static final String[] FIELDS_TO_DROP = new String[]{
        "Text1", "Text2", "Text3", "Text4", "FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4", "Color", "GlowingText"
    };

    public DropInvalidSignDataFix(Schema p_297458_, String p_300331_, String p_300869_) {
        super(p_297458_, false, p_300331_, References.BLOCK_ENTITY, p_300869_);
    }

    private static <T> Dynamic<T> fix(Dynamic<T> p_297398_) {
        p_297398_ = p_297398_.update("front_text", DropInvalidSignDataFix::fixText);
        p_297398_ = p_297398_.update("back_text", DropInvalidSignDataFix::fixText);

        for (String s : FIELDS_TO_DROP) {
            p_297398_ = p_297398_.remove(s);
        }

        return p_297398_;
    }

    private static <T> Dynamic<T> fixText(Dynamic<T> p_299128_) {
        boolean flag = p_299128_.get("_filtered_correct").asBoolean(false);
        if (flag) {
            return p_299128_.remove("_filtered_correct");
        } else {
            Optional<Stream<Dynamic<T>>> optional = p_299128_.get("filtered_messages").asStreamOpt().result();
            if (optional.isEmpty()) {
                return p_299128_;
            } else {
                Dynamic<T> dynamic = ComponentDataFixUtils.createEmptyComponent(p_299128_.getOps());
                List<Dynamic<T>> list = p_299128_.get("messages").asStreamOpt().result().orElse(Stream.of()).toList();
                List<Dynamic<T>> list1 = Streams.mapWithIndex(optional.get(), (p_298117_, p_298041_) -> {
                    Dynamic<T> dynamic1 = p_298041_ < (long)list.size() ? list.get((int)p_298041_) : dynamic;
                    return p_298117_.equals(dynamic) ? dynamic1 : p_298117_;
                }).toList();
                return list1.stream().allMatch(p_300495_ -> p_300495_.equals(dynamic))
                    ? p_299128_.remove("filtered_messages")
                    : p_299128_.set("filtered_messages", p_299128_.createList(list1.stream()));
            }
        }
    }

    @Override
    protected Typed<?> fix(Typed<?> p_297432_) {
        return p_297432_.update(DSL.remainderFinder(), DropInvalidSignDataFix::fix);
    }
}