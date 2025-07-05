package net.minecraft.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Field;
import net.minecraft.CharPredicate;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;

public class ParserUtils {
    private static final Field JSON_READER_POS = Util.make(() -> {
        try {
            Field field = JsonReader.class.getDeclaredField("pos");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", nosuchfieldexception);
        }
    });
    private static final Field JSON_READER_LINESTART = Util.make(() -> {
        try {
            Field field = JsonReader.class.getDeclaredField("lineStart");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", nosuchfieldexception);
        }
    });

    private static int getPos(JsonReader p_311647_) {
        try {
            return JSON_READER_POS.getInt(p_311647_) - JSON_READER_LINESTART.getInt(p_311647_);
        } catch (IllegalAccessException illegalaccessexception) {
            throw new IllegalStateException("Couldn't read position of JsonReader", illegalaccessexception);
        }
    }

    public static <T> T parseJson(HolderLookup.Provider p_330013_, StringReader p_311860_, Codec<T> p_311403_) {
        JsonReader jsonreader = new JsonReader(new java.io.StringReader(p_311860_.getRemaining()));
        jsonreader.setLenient(false);

        Object object;
        try {
            JsonElement jsonelement = Streams.parse(jsonreader);
            object = p_311403_.parse(p_330013_.createSerializationContext(JsonOps.INSTANCE), jsonelement).getOrThrow(JsonParseException::new);
        } catch (StackOverflowError stackoverflowerror) {
            throw new JsonParseException(stackoverflowerror);
        } finally {
            p_311860_.setCursor(p_311860_.getCursor() + getPos(jsonreader));
        }

        return (T)object;
    }

    public static String readWhile(StringReader p_333885_, CharPredicate p_328669_) {
        int i = p_333885_.getCursor();

        while (p_333885_.canRead() && p_328669_.test(p_333885_.peek())) {
            p_333885_.skip();
        }

        return p_333885_.getString().substring(i, p_333885_.getCursor());
    }
}