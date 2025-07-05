package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public abstract class NamedEntityWriteReadFix extends DataFix {
    private final String name;
    private final String entityName;
    private final TypeReference type;

    public NamedEntityWriteReadFix(Schema p_310297_, boolean p_312818_, String p_313129_, TypeReference p_311108_, String p_313092_) {
        super(p_310297_, p_312818_);
        this.name = p_313129_;
        this.type = p_311108_;
        this.entityName = p_313092_;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(this.type);
        Type<?> type1 = this.getInputSchema().getChoiceType(this.type, this.entityName);
        Type<?> type2 = this.getOutputSchema().getType(this.type);
        Type<?> type3 = this.getOutputSchema().getChoiceType(this.type, this.entityName);
        OpticFinder<?> opticfinder = DSL.namedChoice(this.entityName, type1);
        Type<?> type4 = ExtraDataFixUtils.patchSubType(type1, type, type2);
        return this.fix(type, type2, opticfinder, type3, type4);
    }

    private <S, T, A, B> TypeRewriteRule fix(Type<S> p_334263_, Type<T> p_329342_, OpticFinder<A> p_329193_, Type<B> p_333979_, Type<?> p_327956_) {
        return this.fixTypeEverywhere(this.name, p_334263_, p_329342_, p_326626_ -> p_326621_ -> {
                Typed<S> typed = new Typed<>(p_334263_, p_326626_, p_326621_);
                return (T)typed.update(p_329193_, p_333979_, p_326631_ -> {
                    Typed<A> typed1 = new Typed<>((Type<A>)p_327956_, p_326626_, p_326631_);
                    return Util.writeAndReadTypedOrThrow(typed1, p_333979_, this::fix).getValue();
                }).getValue();
            });
    }

    protected abstract <T> Dynamic<T> fix(Dynamic<T> p_310304_);
}