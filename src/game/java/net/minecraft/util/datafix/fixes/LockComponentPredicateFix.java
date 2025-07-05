package net.minecraft.util.datafix.fixes;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import javax.annotation.Nullable;

public class LockComponentPredicateFix extends DataComponentRemainderFix {
    public static final Escaper ESCAPER = Escapers.builder().addEscape('"', "\\\"").addEscape('\\', "\\\\").build();

    public LockComponentPredicateFix(Schema p_370065_) {
        super(p_370065_, "LockComponentPredicateFix", "minecraft:lock");
    }

    @Nullable
    @Override
    protected <T> Dynamic<T> fixComponent(Dynamic<T> p_360989_) {
        return fixLock(p_360989_);
    }

    @Nullable
    public static <T> Dynamic<T> fixLock(Dynamic<T> p_369566_) {
        Optional<String> optional = p_369566_.asString().result();
        if (optional.isEmpty()) {
            return null;
        } else if (optional.get().isEmpty()) {
            return null;
        } else {
            Dynamic<T> dynamic = p_369566_.createString("\"" + ESCAPER.escape(optional.get()) + "\"");
            Dynamic<T> dynamic1 = p_369566_.emptyMap().set("minecraft:custom_name", dynamic);
            return p_369566_.emptyMap().set("components", dynamic1);
        }
    }
}