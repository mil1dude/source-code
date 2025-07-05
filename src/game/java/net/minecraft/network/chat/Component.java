package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;

public interface Component extends Message, FormattedText {
    Style getStyle();

    ComponentContents getContents();

    @Override
    default String getString() {
        return FormattedText.super.getString();
    }

    default String getString(int p_130669_) {
        StringBuilder stringbuilder = new StringBuilder();
        this.visit(p_130673_ -> {
            int i = p_130669_ - stringbuilder.length();
            if (i <= 0) {
                return STOP_ITERATION;
            } else {
                stringbuilder.append(p_130673_.length() <= i ? p_130673_ : p_130673_.substring(0, i));
                return Optional.empty();
            }
        });
        return stringbuilder.toString();
    }

    List<Component> getSiblings();

    @Nullable
    default String tryCollapseToString() {
        if (this.getContents() instanceof PlainTextContents plaintextcontents && this.getSiblings().isEmpty() && this.getStyle().isEmpty()) {
            return plaintextcontents.text();
        }

        return null;
    }

    default MutableComponent plainCopy() {
        return MutableComponent.create(this.getContents());
    }

    default MutableComponent copy() {
        return new MutableComponent(this.getContents(), new ArrayList<>(this.getSiblings()), this.getStyle());
    }

    FormattedCharSequence getVisualOrderText();

    @Override
    default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> p_130679_, Style p_130680_) {
        Style style = this.getStyle().applyTo(p_130680_);
        Optional<T> optional = this.getContents().visit(p_130679_, style);
        if (optional.isPresent()) {
            return optional;
        } else {
            for (Component component : this.getSiblings()) {
                Optional<T> optional1 = component.visit(p_130679_, style);
                if (optional1.isPresent()) {
                    return optional1;
                }
            }

            return Optional.empty();
        }
    }

    @Override
    default <T> Optional<T> visit(FormattedText.ContentConsumer<T> p_130677_) {
        Optional<T> optional = this.getContents().visit(p_130677_);
        if (optional.isPresent()) {
            return optional;
        } else {
            for (Component component : this.getSiblings()) {
                Optional<T> optional1 = component.visit(p_130677_);
                if (optional1.isPresent()) {
                    return optional1;
                }
            }

            return Optional.empty();
        }
    }

    default List<Component> toFlatList() {
        return this.toFlatList(Style.EMPTY);
    }

    default List<Component> toFlatList(Style p_178406_) {
        List<Component> list = Lists.newArrayList();
        this.visit((p_178403_, p_178404_) -> {
            if (!p_178404_.isEmpty()) {
                list.add(literal(p_178404_).withStyle(p_178403_));
            }

            return Optional.empty();
        }, p_178406_);
        return list;
    }

    default boolean contains(Component p_240571_) {
        if (this.equals(p_240571_)) {
            return true;
        } else {
            List<Component> list = this.toFlatList();
            List<Component> list1 = p_240571_.toFlatList(this.getStyle());
            return Collections.indexOfSubList(list, list1) != -1;
        }
    }

    static Component nullToEmpty(@Nullable String p_130675_) {
        return (Component)(p_130675_ != null ? literal(p_130675_) : CommonComponents.EMPTY);
    }

    static MutableComponent literal(String p_237114_) {
        return MutableComponent.create(PlainTextContents.create(p_237114_));
    }

    static MutableComponent translatable(String p_237116_) {
        return MutableComponent.create(new TranslatableContents(p_237116_, null, TranslatableContents.NO_ARGS));
    }

    static MutableComponent translatable(String p_237111_, Object... p_237112_) {
        return MutableComponent.create(new TranslatableContents(p_237111_, null, p_237112_));
    }

    static MutableComponent translatableEscape(String p_312579_, Object... p_312922_) {
        for (int i = 0; i < p_312922_.length; i++) {
            Object object = p_312922_[i];
            if (!TranslatableContents.isAllowedPrimitiveArgument(object) && !(object instanceof Component)) {
                p_312922_[i] = String.valueOf(object);
            }
        }

        return translatable(p_312579_, p_312922_);
    }

    static MutableComponent translatableWithFallback(String p_265747_, @Nullable String p_265287_) {
        return MutableComponent.create(new TranslatableContents(p_265747_, p_265287_, TranslatableContents.NO_ARGS));
    }

    static MutableComponent translatableWithFallback(String p_265449_, @Nullable String p_265281_, Object... p_265785_) {
        return MutableComponent.create(new TranslatableContents(p_265449_, p_265281_, p_265785_));
    }

    static MutableComponent empty() {
        return MutableComponent.create(PlainTextContents.EMPTY);
    }

    static MutableComponent keybind(String p_237118_) {
        return MutableComponent.create(new KeybindContents(p_237118_));
    }

    static MutableComponent nbt(String p_237106_, boolean p_237107_, Optional<Component> p_237108_, DataSource p_237109_) {
        return MutableComponent.create(new NbtContents(p_237106_, p_237107_, p_237108_, p_237109_));
    }

    static MutableComponent score(SelectorPattern p_367861_, String p_361558_) {
        return MutableComponent.create(new ScoreContents(Either.left(p_367861_), p_361558_));
    }

    static MutableComponent score(String p_237100_, String p_237101_) {
        return MutableComponent.create(new ScoreContents(Either.right(p_237100_), p_237101_));
    }

    static MutableComponent selector(SelectorPattern p_366885_, Optional<Component> p_237104_) {
        return MutableComponent.create(new SelectorContents(p_366885_, p_237104_));
    }

    static Component translationArg(Date p_313239_) {
        return literal(p_313239_.toString());
    }

    static Component translationArg(Message p_312086_) {
        return (Component)(p_312086_ instanceof Component component ? component : literal(p_312086_.getString()));
    }

    static Component translationArg(UUID p_311149_) {
        return literal(p_311149_.toString());
    }

    static Component translationArg(ResourceLocation p_311439_) {
        return literal(p_311439_.toString());
    }

    static Component translationArg(ChunkPos p_312850_) {
        return literal(p_312850_.toString());
    }

    static Component translationArg(URI p_344435_) {
        return literal(p_344435_.toString());
    }

    public static class Serializer {
        private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

        private Serializer() {
        }

        static MutableComponent deserialize(JsonElement p_130720_, HolderLookup.Provider p_334184_) {
            return (MutableComponent)ComponentSerialization.CODEC
                .parse(p_334184_.createSerializationContext(JsonOps.INSTANCE), p_130720_)
                .getOrThrow(JsonParseException::new);
        }

        static JsonElement serialize(Component p_130706_, HolderLookup.Provider p_332074_) {
            return ComponentSerialization.CODEC.encodeStart(p_332074_.createSerializationContext(JsonOps.INSTANCE), p_130706_).getOrThrow(JsonParseException::new);
        }

        public static String toJson(Component p_130704_, HolderLookup.Provider p_334954_) {
            return GSON.toJson(serialize(p_130704_, p_334954_));
        }

        @Nullable
        public static MutableComponent fromJson(String p_332445_, HolderLookup.Provider p_334661_) {
            JsonElement jsonelement = JsonParser.parseString(p_332445_);
            return jsonelement == null ? null : deserialize(jsonelement, p_334661_);
        }

        @Nullable
        public static MutableComponent fromJson(@Nullable JsonElement p_330936_, HolderLookup.Provider p_331821_) {
            return p_330936_ == null ? null : deserialize(p_330936_, p_331821_);
        }

        @Nullable
        public static MutableComponent fromJsonLenient(String p_130715_, HolderLookup.Provider p_335522_) {
            JsonReader jsonreader = new JsonReader(new StringReader(p_130715_));
            jsonreader.setLenient(true);
            JsonElement jsonelement = JsonParser.parseReader(jsonreader);
            return jsonelement == null ? null : deserialize(jsonelement, p_335522_);
        }
    }

    public static class SerializerAdapter implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
        private final HolderLookup.Provider registries;

        public SerializerAdapter(HolderLookup.Provider p_330707_) {
            this.registries = p_330707_;
        }

        public MutableComponent deserialize(JsonElement p_311708_, Type p_310257_, JsonDeserializationContext p_310325_) throws JsonParseException {
            return Component.Serializer.deserialize(p_311708_, this.registries);
        }

        public JsonElement serialize(Component p_309493_, Type p_310679_, JsonSerializationContext p_312693_) {
            return Component.Serializer.serialize(p_309493_, this.registries);
        }
    }
}