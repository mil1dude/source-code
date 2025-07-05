package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class FixWolfHealth extends NamedEntityFix {
    private static final String WOLF_ID = "minecraft:wolf";
    private static final String WOLF_HEALTH = "minecraft:generic.max_health";

    public FixWolfHealth(Schema p_332178_) {
        super(p_332178_, false, "FixWolfHealth", References.ENTITY, "minecraft:wolf");
    }

    @Override
    protected Typed<?> fix(Typed<?> p_332371_) {
        return p_332371_.update(
            DSL.remainderFinder(),
            p_332522_ -> {
                MutableBoolean mutableboolean = new MutableBoolean(false);
                p_332522_ = p_332522_.update(
                    "Attributes",
                    p_335906_ -> p_335906_.createList(
                            p_335906_.asStream()
                                .map(
                                    p_335561_ -> "minecraft:generic.max_health".equals(NamespacedSchema.ensureNamespaced(p_335561_.get("Name").asString("")))
                                            ? p_335561_.update("Base", p_334444_ -> {
                                                if (p_334444_.asDouble(0.0) == 20.0) {
                                                    mutableboolean.setTrue();
                                                    return p_334444_.createDouble(40.0);
                                                } else {
                                                    return p_334444_;
                                                }
                                            })
                                            : p_335561_
                                )
                        )
                );
                if (mutableboolean.isTrue()) {
                    p_332522_ = p_332522_.update("Health", p_335921_ -> p_335921_.createFloat(p_335921_.asFloat(0.0F) * 2.0F));
                }

                return p_332522_;
            }
        );
    }
}