package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class StringTag implements Tag {
    private static final int SELF_SIZE_IN_BYTES = 36;
    public static final TagType<StringTag> TYPE = new TagType.VariableSize<StringTag>() {
        public StringTag load(DataInput p_129315_, NbtAccounter p_129317_) throws IOException {
            return StringTag.valueOf(readAccounted(p_129315_, p_129317_));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput p_197570_, StreamTagVisitor p_197571_, NbtAccounter p_301725_) throws IOException {
            return p_197571_.visit(readAccounted(p_197570_, p_301725_));
        }

        private static String readAccounted(DataInput p_301750_, NbtAccounter p_301732_) throws IOException {
            p_301732_.accountBytes(36L);
            String s = p_301750_.readUTF();
            p_301732_.accountBytes(2L, (long)s.length());
            return s;
        }

        @Override
        public void skip(DataInput p_197568_, NbtAccounter p_301752_) throws IOException {
            StringTag.skipString(p_197568_);
        }

        @Override
        public String getName() {
            return "STRING";
        }

        @Override
        public String getPrettyName() {
            return "TAG_String";
        }

        @Override
        public boolean isValue() {
            return true;
        }
    };
    private static final StringTag EMPTY = new StringTag("");
    private static final char DOUBLE_QUOTE = '"';
    private static final char SINGLE_QUOTE = '\'';
    private static final char ESCAPE = '\\';
    private static final char NOT_SET = '\u0000';
    private final String data;

    public static void skipString(DataInput p_197564_) throws IOException {
        p_197564_.skipBytes(p_197564_.readUnsignedShort());
    }

    private StringTag(String p_129293_) {
        Objects.requireNonNull(p_129293_, "Null string not allowed");
        this.data = p_129293_;
    }

    public static StringTag valueOf(String p_129298_) {
        return p_129298_.isEmpty() ? EMPTY : new StringTag(p_129298_);
    }

    @Override
    public void write(DataOutput p_129296_) throws IOException {
        p_129296_.writeUTF(this.data);
    }

    @Override
    public int sizeInBytes() {
        return 36 + 2 * this.data.length();
    }

    @Override
    public byte getId() {
        return 8;
    }

    @Override
    public TagType<StringTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return Tag.super.getAsString();
    }

    public StringTag copy() {
        return this;
    }

    @Override
    public boolean equals(Object p_129308_) {
        return this == p_129308_ ? true : p_129308_ instanceof StringTag && Objects.equals(this.data, ((StringTag)p_129308_).data);
    }

    @Override
    public int hashCode() {
        return this.data.hashCode();
    }

    @Override
    public String getAsString() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor p_178154_) {
        p_178154_.visitString(this);
    }

    public static String quoteAndEscape(String p_129304_) {
        StringBuilder stringbuilder = new StringBuilder(" ");
        char c0 = 0;

        for (int i = 0; i < p_129304_.length(); i++) {
            char c1 = p_129304_.charAt(i);
            if (c1 == '\\') {
                stringbuilder.append('\\');
            } else if (c1 == '"' || c1 == '\'') {
                if (c0 == 0) {
                    c0 = (char)(c1 == '"' ? 39 : 34);
                }

                if (c0 == c1) {
                    stringbuilder.append('\\');
                }
            }

            stringbuilder.append(c1);
        }

        if (c0 == 0) {
            c0 = '"';
        }

        stringbuilder.setCharAt(0, c0);
        stringbuilder.append(c0);
        return stringbuilder.toString();
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor p_197566_) {
        return p_197566_.visit(this.data);
    }
}